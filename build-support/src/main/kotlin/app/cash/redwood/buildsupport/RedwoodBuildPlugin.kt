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

import com.diffplug.gradle.spotless.SpotlessExtension
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.SonatypeHost
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaApplication
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.jetbrains.kotlin.gradle.dsl.KotlinTopLevelExtension

private const val redwoodGroupId = "app.cash.redwood"

// HEY! If you change the major version update release.yaml doc folder.
private const val redwoodVersion = "0.6.0"

@Suppress("unused") // Invoked reflectively by Gradle.
class RedwoodBuildPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    target.group = redwoodGroupId
    target.version = redwoodVersion

    val libs = target.extensions.getByName("libs") as LibrariesForLibs

    target.extensions.add(
      RedwoodBuildExtension::class.java,
      "redwoodBuild",
      RedwoodBuildExtensionImpl(target),
    )

    target.plugins.apply("com.diffplug.spotless")
    val spotless = target.extensions.getByName("spotless") as SpotlessExtension
    val licenseHeaderFile = target.rootProject.file("gradle/license-header.txt")
    spotless.apply {
      // The nested build-support Gradle project contains Java sources. Use our root project to
      // target its sources rather than duplicating the Spotless setup in multiple places.
      if (target.path == ":") {
        java {
          it.target("build-support/settings/src/**/*.java")
          it.googleJavaFormat(libs.googleJavaFormat.get().version)
          it.licenseHeaderFile(licenseHeaderFile)
        }
      }

      kotlin {
        // The nested build-support Gradle project contains Kotlin sources. Use our root project to
        // target its sources rather than duplicating the Spotless setup in multiple places.
        if (target.path == ":") {
          it.target("build-support/src/**/*.kt")
        } else {
          it.target("src/**/*.kt")
          // Avoid 'build' folders within test fixture projects which may contain generated sources.
          it.targetExclude("src/test/fixture/**/build/**")
        }
        it.ktlint(libs.ktlint.get().version).editorConfigOverride(
          mapOf("ktlint_standard_filename" to "disabled"),
        )
        it.licenseHeaderFile(licenseHeaderFile)
      }
    }
  }
}

private class RedwoodBuildExtensionImpl(private val project: Project) : RedwoodBuildExtension {
  override fun composeCompiler() {
    project.plugins.apply(ComposePlugin::class.java)
  }

  override fun publishing() {
    project.plugins.apply("com.vanniktech.maven.publish")
    // project.plugins.apply("org.jetbrains.dokka")

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
      publishToMavenCentral(SonatypeHost.DEFAULT, automaticRelease = true)
      if (project.providers.systemProperty("RELEASE_SIGNING_ENABLED").getOrElse("true").toBoolean()) {
        signAllPublications()
      }

      coordinates(redwoodGroupId, project.name, redwoodVersion)

      pom { pom ->
        pom.name.set(project.name)
        pom.description.set("Multiplatform reactive UI using Kotlin and Jetpack Compose")
        pom.inceptionYear.set("2020")
        pom.url.set("https://github.com/cashapp/redwood/")

        pom.licenses {
          it.license { license ->
            license.name.set("Apache-2.0")
            license.url.set("https://www.apache.org/licenses/LICENSE-2.0")
            license.distribution.set("repo")
          }
        }

        pom.developers {
          it.developer { developer ->
            developer.id.set("cashapp")
            developer.name.set("CashApp")
            developer.url.set("https://github.com/cashapp")
          }
        }

        pom.scm { scm ->
          scm.url.set("https://github.com/cashapp/redwood/")
          scm.connection.set("scm:git:git://github.com/cashapp/redwood.git")
          scm.developerConnection.set("scm:git:ssh://git@github.com/cashapp/redwood.git")
        }
      }
    }

    // DokkaTaskPartial configures subprojects for multimodule docs
    // All options: https://kotlinlang.org/docs/dokka-gradle.html#configuration-options
    // project.tasks.withType(org.jetbrains.dokka.gradle.DokkaTaskPartial::class.java) { task ->
    //   task.dokkaSourceSets.configureEach {
    //     it.suppressGeneratedFiles.set(false) // document generated code
    //   }
    // }

    // Published modules should be explicit about their API visibility.
    var explicit = false
    val kotlinPluginHandler: (Plugin<Any>) -> Unit = {
      val kotlin = project.extensions.getByType(KotlinTopLevelExtension::class.java)
      kotlin.explicitApi()
      explicit = true
    }
    project.plugins.withId("org.jetbrains.kotlin.android", kotlinPluginHandler)
    project.plugins.withId("org.jetbrains.kotlin.js", kotlinPluginHandler)
    project.plugins.withId("org.jetbrains.kotlin.jvm", kotlinPluginHandler)
    project.plugins.withId("org.jetbrains.kotlin.multiplatform", kotlinPluginHandler)
    project.afterEvaluate {
      check(explicit) {
        """Project "${project.path}" has unknown Kotlin plugin which needs explicit API tracking"""
      }
    }
  }

  override fun application(name: String, mainClass: String) {
    project.plugins.apply("application")

    val application = project.extensions.getByName("application") as JavaApplication
    application.applicationName = name
    application.mainClass.set(mainClass)

    // A zip and tar are configured by default. We don't care about tar.
    project.tasks.named("distTar").configure { task ->
      task.enabled = false
    }

    // Build an exploded application directory by default for local testing.
    project.tasks.named("assemble").configure { task ->
      task.dependsOn(project.tasks.named("installDist"))
    }

    // If this module is also being published, attach the bundled application zip so that we get
    // free versioned hosting from Maven Central.
    project.plugins.withId("maven-publish") {
      val publishing = project.extensions.getByName("publishing") as PublishingExtension
      publishing.publications.withType(MavenPublication::class.java).configureEach { publication ->
        publication.artifact(project.tasks.named("distZip")) {
          // We must specify a classifier or else the pom's packaging value will change to
          // no longer reflect the primary build artifact.
          it.classifier = "cli"
        }
      }
    }
  }
}
