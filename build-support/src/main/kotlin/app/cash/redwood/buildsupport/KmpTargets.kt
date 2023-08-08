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
@file:JvmName("KmpTargets")

package app.cash.redwood.buildsupport

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

@JvmOverloads
fun addAllTargets(project: Project, skipJs: Boolean = false) {
  project.plugins.withId("org.jetbrains.kotlin.multiplatform") {
    project.extensions.getByType(KotlinMultiplatformExtension::class.java).apply {
      if (project.plugins.hasPlugin("com.android.library")) {
        androidTarget {
          publishLibraryVariants("release")
        }
      }

      if (!skipJs) {
        js {
          browser()
        }
      }

      iosArm64()
      iosX64()
      iosSimulatorArm64()

      jvm()

      macosArm64()
      macosX64()

      val commonMain = sourceSets.getByName("commonMain")
      val commonTest = sourceSets.getByName("commonTest")

      val nativeMain = sourceSets.create("nativeMain").apply {
        dependsOn(commonMain)
      }
      val nativeTest = sourceSets.create("nativeTest").apply {
        dependsOn(commonTest)
      }

      val iosMain = sourceSets.create("iosMain").apply {
        dependsOn(nativeMain)
      }
      val iosTest = sourceSets.create("iosTest").apply {
        dependsOn(nativeTest)
      }

      val macosMain = sourceSets.create("macosMain").apply {
        dependsOn(nativeMain)
      }
      val macosTest = sourceSets.create("macosTest").apply {
        dependsOn(nativeTest)
      }

      targets.all { target ->
        // Some Kotlin targets do not have this property, but native ones always will.
        if (target.platformType.name == "native") {
          if (target.name.startsWith("ios")) {
            target.compilations.getByName("main").defaultSourceSet.dependsOn(iosMain)
            target.compilations.getByName("test").defaultSourceSet.dependsOn(iosTest)
          } else if (target.name.startsWith("macos")) {
            target.compilations.getByName("main").defaultSourceSet.dependsOn(macosMain)
            target.compilations.getByName("test").defaultSourceSet.dependsOn(macosTest)
          } else {
            throw AssertionError("Unknown target ${target.name}")
          }
        }
      }
    }
  }
}
