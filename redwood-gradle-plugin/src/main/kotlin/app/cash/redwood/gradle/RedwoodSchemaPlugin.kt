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
import org.gradle.jvm.tasks.Jar
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation.Companion.MAIN_COMPILATION_NAME

public class RedwoodSchemaPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    val extension = project.extensions.create(
      "redwoodSchema",
      RedwoodSchemaExtension::class.java,
    )

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
    val cliConfiguration = project.configurations.create("redwoodCli").apply {
      isCanBeConsumed = false
      isVisible = false
    }
    project.dependencies.add(cliConfiguration.name, project.redwoodDependency("redwood-cli"))

    val kotlin = project.extensions.getByType(KotlinJvmProjectExtension::class.java)
    val compilation = kotlin.target.compilations.getByName(MAIN_COMPILATION_NAME)
    val classpath = project.configurations.getByName(compilation.compileDependencyConfigurationName)

    val generate = project.tasks.register("redwoodGenerate", RedwoodSchemaTask::class.java) {
      it.group = LifecycleBasePlugin.BUILD_GROUP
      it.description = "Generate parsed schema JSON"

      it.toolClasspath.from(cliConfiguration)
      it.outputDir.set(project.layout.buildDirectory.dir("generated/redwood"))
      it.schemaType.set(extension.type)
      it.classpath.from(classpath, compilation.output.classesDirs)
    }

    project.tasks.named("jar", Jar::class.java).configure {
      it.from(generate)
    }

    compilation.defaultSourceSet.dependencies {
      api(project.redwoodDependency("redwood-schema"))
    }
  }
}
