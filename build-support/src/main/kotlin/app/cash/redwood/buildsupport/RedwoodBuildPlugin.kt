/*
 * Copyright (C) 2023 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package app.cash.redwood.buildsupport

import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.SonatypeHost
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension

@Suppress("unused") // Invoked reflectively by Gradle.
class RedwoodBuildPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    target.extensions.add(
      RedwoodBuildExtension::class.java,
      "redwoodBuild",
      RedwoodBuildExtensionImpl(target),
    )
  }
}

private class RedwoodBuildExtensionImpl(private val project: Project) : RedwoodBuildExtension {
  override fun composeCompiler() {
    project.plugins.apply(ComposePlugin::class.java)
  }

  override fun publishing() {
    project.plugins.apply("com.vanniktech.maven.publish")
    project.plugins.apply("org.jetbrains.dokka")

    val publishing = project.extensions.getByName("publishing") as PublishingExtension
    publishing.apply {
      repositories {
        it.maven {
          it.name = "LocalMaven"
          it.url = project.rootProject.buildDir.resolve("localMaven").toURI()
        }

        // Want to push to an internal repository for testing?
        // Set the following properties in ~/.gradle/gradle.properties.
        //
        // internalUrl=YOUR_INTERNAL_URL
        // internalUsername=YOUR_USERNAME
        // internalPassword=YOUR_PASSWORD
        //
        // Then run the following command to publish a new internal release:
        //
        // ./gradlew publishAllPublicationsToInternalRepository -DRELEASE_SIGNING_ENABLED=false
        val internalUrl = project.providers.gradleProperty("internalUrl")
        if (internalUrl.isPresent) {
          it.maven {
            it.name = "internal"
            it.setUrl(internalUrl)
            it.credentials {
              it.username = project.providers.gradleProperty("internalUsername").get()
              it.password = project.providers.gradleProperty("internalPassword").get()
            }
          }
        }
      }
    }

    val mavenPublishing = project.extensions.getByName("mavenPublishing") as MavenPublishBaseExtension
    mavenPublishing.apply {
      publishToMavenCentral(SonatypeHost.DEFAULT)
      if (project.providers.systemProperty("RELEASE_SIGNING_ENABLED").getOrElse("true").toBoolean()) {
        signAllPublications()
      }
    }

    // DokkaTaskPartial configures subprojects for multimodule docs
    // All options: https://kotlinlang.org/docs/dokka-gradle.html#configuration-options
    project.tasks.withType(org.jetbrains.dokka.gradle.DokkaTaskPartial::class.java) { task ->
      task.dokkaSourceSets.configureEach {
        it.suppressGeneratedFiles.set(false) // document generated code
      }
    }
  }
}
