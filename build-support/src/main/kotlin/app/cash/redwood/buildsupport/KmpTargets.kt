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

      // This will happen by default, but we explicitly invoke it so that projects can add custom
      // source sets that depend on the ones produced as a result of these defaults.
      applyDefaultHierarchyTemplate()
    }
  }
}
