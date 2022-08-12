/*
 * Copyright (C) 2022 Square, Inc.
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

import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import java.io.File
import java.util.Locale.ROOT
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.attributes.Usage
import org.gradle.api.attributes.Usage.JAVA_RUNTIME
import org.gradle.api.attributes.Usage.USAGE_ATTRIBUTE
import org.gradle.api.tasks.TaskProvider
import org.gradle.language.base.plugins.LifecycleBasePlugin.CHECK_TASK_NAME
import org.gradle.language.base.plugins.LifecycleBasePlugin.VERIFICATION_GROUP
import org.jetbrains.kotlin.gradle.dsl.KotlinJsProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinSingleTargetExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation.Companion.MAIN_COMPILATION_NAME
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType.androidJvm
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType.common
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget

private const val baseTaskName = "redwoodLint"

@Suppress("unused") // Invoked reflectively by Gradle.
public class RedwoodLintPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    project.afterEvaluate {
      val androidPlugin = if (project.plugins.hasPlugin("com.android.application")) {
        AndroidPlugin.Application
      } else if (project.plugins.hasPlugin("com.android.library")) {
        AndroidPlugin.Library
      } else {
        null
      }

      val task = if (project.plugins.hasPlugin("org.jetbrains.kotlin.multiplatform")) {
        val rootTask = project.tasks.register(baseTaskName) {
          it.group = VERIFICATION_GROUP
          it.description = taskDescription("all Kotlin targets")
        }
        if (androidPlugin != null) {
          configureKotlinMultiplatformTargets(project, rootTask, skipAndroid = true)
          configureKotlinAndroidVariants(project, rootTask, androidPlugin, prefix = true)
        } else {
          configureKotlinMultiplatformTargets(project, rootTask)
        }
        rootTask
      } else if (project.plugins.hasPlugin("org.jetbrains.kotlin.js")) {
        configureKotlinJsProject(project)
      } else if (project.plugins.hasPlugin("org.jetbrains.kotlin.jvm")) {
        configureKotlinJvmProject(project)
      } else if (project.plugins.hasPlugin("org.jetbrains.kotlin.android")) {
        checkNotNull(androidPlugin) {
          "Kotlin Android plugin requires either Android application or library plugin"
        }
        val rootTask = project.tasks.register(baseTaskName) {
          it.group = VERIFICATION_GROUP
          it.description = taskDescription("all Kotlin targets")
        }
        configureKotlinAndroidVariants(project, rootTask, androidPlugin, prefix = false)
        rootTask
      } else {
        val name = if (project === project.rootProject) {
          "root project"
        } else {
          "project ${project.path}"
        }
        throw IllegalStateException(
          "'app.cash.redwood.lint' requires a compatible Kotlin plugin to be applied ($name)",
        )
      }

      project.tasks.named(CHECK_TASK_NAME).configure {
        it.dependsOn(task)
      }
    }
  }
}

private enum class AndroidPlugin {
  Application,
  Library,
}

private fun configureKotlinAndroidVariants(
  project: Project,
  rootTask: TaskProvider<Task>,
  android: AndroidPlugin,
  prefix: Boolean,
) {
  val extensions = project.extensions
  val variants = when (android) {
    AndroidPlugin.Application -> extensions.getByType(AppExtension::class.java).applicationVariants
    AndroidPlugin.Library -> extensions.getByType(LibraryExtension::class.java).libraryVariants
  }
  variants.configureEach { variant ->
    val taskName = buildString {
      append(baseTaskName)
      if (prefix) {
        append("Android")
      }
      append(variant.name.capitalize(ROOT))
    }
    val task = project.createRedwoodLintTask(
      taskName,
      "Kotlin Android ${variant.name} variant",
      sourceDirs = { variant.sourceSets.flatMap { it.kotlinDirectories } },
      classpath = { variant.compileConfiguration },
    )
    rootTask.configure {
      it.dependsOn(task)
    }
  }
}

private fun configureKotlinMultiplatformTargets(
  project: Project,
  rootTask: TaskProvider<Task>,
  skipAndroid: Boolean = false,
) {
  val kotlin = project.extensions.getByType(KotlinMultiplatformExtension::class.java)
  kotlin.targets.configureEach { target ->
    if (target.platformType == common) {
      return@configureEach // All code ends up in platform targets.
    }
    if (target.platformType == androidJvm) {
      if (skipAndroid) return@configureEach
      throw AssertionError("Found Android Kotlin target but no Android plugin was detected")
    }

    val task = createKotlinTargetRedwoodLintTask(
      project,
      target,
      taskName = baseTaskName + target.name.capitalize(ROOT),
    )
    rootTask.configure {
      it.dependsOn(task)
    }
  }
}

private fun configureKotlinJsProject(
  project: Project,
): TaskProvider<out Task> {
  val kotlin = project.extensions.getByType(KotlinJsProjectExtension::class.java)
  // Cast to supertype avoids deprecation on subtype of related mutable DSL API.
  val target = (kotlin as KotlinSingleTargetExtension).target
  return createKotlinTargetRedwoodLintTask(project, target, baseTaskName)
}

private fun configureKotlinJvmProject(
  project: Project,
): TaskProvider<out Task> {
  val kotlin = project.extensions.getByType(KotlinJvmProjectExtension::class.java)
  return createKotlinTargetRedwoodLintTask(project, kotlin.target, baseTaskName)
}

private fun createKotlinTargetRedwoodLintTask(
  project: Project,
  target: KotlinTarget,
  taskName: String,
): TaskProvider<out Task> {
  val compilation = target.compilations.getByName(MAIN_COMPILATION_NAME)
  return project.createRedwoodLintTask(
    taskName,
    "Kotlin ${target.name} target",
    sourceDirs = {
      compilation.allKotlinSourceSets.flatMap { it.kotlin.sourceDirectories.files }
    },
    classpath = {
      project.configurations.getByName(compilation.compileDependencyConfigurationName)
    },
  )
}

private fun Project.createRedwoodLintTask(
  name: String,
  descriptionTarget: String? = null,
  sourceDirs: () -> Collection<File>,
  classpath: () -> Configuration,
): TaskProvider<out Task> {
  val configuration = configurations.maybeCreate("redwoodLint")
  dependencies.add(
    configuration.name,
    "app.cash.redwood:redwood-lint:$redwoodVersion",
  )

  return tasks.register(name, RedwoodLintTask::class.java) { task ->
    task.group = VERIFICATION_GROUP
    task.description = taskDescription(descriptionTarget)

    task.toolClasspath.setFrom(configuration.incoming.artifacts.artifactFiles)
    task.projectDirectory.set(project.projectDir)
    task.sourceDirectories.set(sourceDirs())
    task.dependencies.setFrom(
      classpath().incoming.artifactView {
        it.attributes {
          it.attribute(USAGE_ATTRIBUTE, objects.named(Usage::class.java, JAVA_RUNTIME))
        }
      }.artifacts.artifactFiles,
    )
  }
}

private fun taskDescription(target: String? = null) = buildString {
  append("Run Redwood's Compose lint checks")
  if (target != null) {
    append(" on ")
    append(target)
  }
}
