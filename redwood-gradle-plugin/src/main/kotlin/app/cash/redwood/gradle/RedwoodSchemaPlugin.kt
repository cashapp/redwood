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
package app.cash.redwood.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.language.base.plugins.LifecycleBasePlugin.BUILD_GROUP
import org.gradle.language.base.plugins.LifecycleBasePlugin.CHECK_TASK_NAME
import org.gradle.language.base.plugins.LifecycleBasePlugin.VERIFICATION_GROUP
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation.Companion.MAIN_COMPILATION_NAME

public class RedwoodSchemaPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    val extension = project.extensions.create(
      "redwoodSchema",
      RedwoodSchemaExtension::class.java,
    ).apply {
      apiTracking.convention(true)
    }

    var applied = false
    project.plugins.withId("org.jetbrains.kotlin.jvm") {
      applied = true
      applyToProject(project, extension)
    }
    project.afterEvaluate {
      check(applied) {
        "Redwood schema plugin requires 'org.jetbrains.kotlin.jvm' plugin."
      }
    }
  }

  private fun applyToProject(project: Project, extension: RedwoodSchemaExtension) {
    val toolingConfiguration = project.configurations.create("redwoodToolingSchema").apply {
      isCanBeConsumed = false
      isVisible = false
    }
    project.dependencies.add(toolingConfiguration.name, project.redwoodDependency("redwood-tooling-schema"))

    val kotlin = project.extensions.getByType(KotlinJvmProjectExtension::class.java)
    val compilation = kotlin.target.compilations.getByName(MAIN_COMPILATION_NAME)
    val classpath = project.configurations.getByName(compilation.compileDependencyConfigurationName)
    val sources = compilation.defaultSourceSet.kotlin.sourceDirectories

    val generateJson = project.tasks.register("redwoodJsonGenerate", RedwoodSchemaJsonTask::class.java) {
      it.group = BUILD_GROUP
      it.description = "Generate parsed schema JSON"

      it.toolClasspath.from(project.files(toolingConfiguration))
      it.outputDir.set(project.redwoodGeneratedDir("schema-json"))
      it.schemaType.set(extension.type)
      it.sources.setFrom(sources)
      it.classpath.from(project.files(classpath))
    }
    compilation.defaultSourceSet.resources.srcDir(generateJson)

    // Wait for build script to run before checking if API tracking is still enabled.
    project.afterEvaluate {
      if (extension.apiTracking.get()) {
        val apiFile = project.layout.projectDirectory.file("redwood-api.xml")

        project.tasks.register(REDWOOD_API_GENERATE_TASK_NAME, RedwoodSchemaApiGenerateTask::class.java) {
          it.group = BUILD_GROUP
          it.description = "Write an updated API tracking file for the current schema"

          it.toolClasspath.from(project.files(toolingConfiguration))
          it.apiFile.set(apiFile)
          it.schemaType.set(extension.type)
          it.sources.setFrom(sources)
          it.classpath.from(project.files(classpath))
        }

        val apiCheck =
          project.tasks.register("redwoodApiCheck", RedwoodSchemaApiCheckTask::class.java) {
            it.group = VERIFICATION_GROUP
            it.description = "Validate the API tracking file against the latest schema"

            it.toolClasspath.from(project.files(toolingConfiguration))
            it.apiFile.set(apiFile)
            it.schemaType.set(extension.type)
            it.sources.setFrom(sources)
            it.classpath.from(project.files(classpath))

            // Dummy output required to skip task if no inputs have changed.
            it.dummyOutputFile.set(project.layout.buildDirectory.file("tmp/redwoodApiCheckDummy.txt"))
          }
        project.tasks.named(CHECK_TASK_NAME).configure {
          it.dependsOn(apiCheck)
        }
      }
    }

    compilation.defaultSourceSet.dependencies {
      api(project.redwoodDependency("redwood-schema"))
    }
  }
}

internal const val REDWOOD_API_GENERATE_TASK_NAME = "redwoodApiGenerate"
