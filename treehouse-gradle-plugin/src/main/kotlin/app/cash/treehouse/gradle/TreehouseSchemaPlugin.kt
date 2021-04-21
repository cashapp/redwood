/*
 * Copyright (C) 2021 Square, Inc.
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
package app.cash.treehouse.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget

public class TreehouseSchemaPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    var applied = false

    project.plugins.withId("org.jetbrains.kotlin.jvm") {
      applied = true

      val kotlin = project.extensions.getByType(KotlinJvmProjectExtension::class.java)
      kotlin.target.applySchemaAnnotationDependency()
    }
    project.plugins.withId("org.jetbrains.kotlin.multiplatform") {
      applied = true

      val kotlin = project.extensions.getByType(KotlinMultiplatformExtension::class.java)
      kotlin.targets.all { it.applySchemaAnnotationDependency() }

      project.afterEvaluate {
        check(kotlin.targets.any { it.platformType == KotlinPlatformType.jvm }) {
          "Treehouse schema plugin requires a jvm() target when used with Kotlin multiplatform"
        }
      }
    }

    project.afterEvaluate {
      check(applied) {
        "Treehouse schema plugin requires the Kotlin JVM or multiplatform plugin to be applied."
      }
    }
  }

  private fun KotlinTarget.applySchemaAnnotationDependency() {
    compilations.getByName("main") { compilation ->
      compilation.dependencies {
        api("app.cash.treehouse:treehouse-schema-annotations:$treehouseVersion")
      }
    }
  }
}
