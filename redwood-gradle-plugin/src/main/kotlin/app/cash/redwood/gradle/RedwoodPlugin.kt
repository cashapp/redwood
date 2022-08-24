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
package app.cash.redwood.gradle

import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType.androidJvm
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType.common
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType.js
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType.jvm
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType.native
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType.wasm
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

public class RedwoodPlugin : KotlinCompilerPluginSupportPlugin {
  override fun apply(target: Project) {
    super.apply(target)

    // Automatically run lint on usages of our Compose plugin.
    target.plugins.apply("app.cash.redwood.lint")
  }

  override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean = true

  override fun getCompilerPluginId(): String = "app.cash.redwood"

  override fun getPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
    "androidx.compose.compiler",
    "compiler",
    androidxComposeCompiler,
  )

  override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
    kotlinCompilation.dependencies {
      api("app.cash.redwood:redwood-compose:$redwoodVersion")
    }

    when (kotlinCompilation.platformType) {
      androidJvm, jvm -> {
        if ((kotlinCompilation.kotlinOptions as KotlinJvmOptions).useOldBackend) {
          throw IllegalStateException("Redwood only works with the default IR-based backend")
        }
      }
      js -> {
        // This enables a workaround for Compose lambda generation to function correctly in JS.
        kotlinCompilation.kotlinOptions.freeCompilerArgs +=
          listOf("-P", "plugin:androidx.compose.compiler.plugins.kotlin:generateDecoys=true")
      }
      native -> {
        // Kotlin/Native compiler reports its version like 1.4.21-344 whereas Kotlin/JVM and
        // Kotlin/JS say only 1.4.21. Compose checks this version and fails for Kotlin/Native.
        kotlinCompilation.kotlinOptions.freeCompilerArgs +=
          listOf("-P", "plugin:androidx.compose.compiler.plugins.kotlin:suppressKotlinVersionCompatibilityCheck=true")
      }
      common, wasm -> {
        // Nothing to do!
      }
    }

    return kotlinCompilation.target.project.provider { emptyList() }
  }
}
