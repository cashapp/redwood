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
package app.cash.redwood.buildsupport

import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType.js
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType.wasm
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

internal class ComposePlugin : KotlinCompilerPluginSupportPlugin {
  override fun isApplicable(kotlinCompilation: KotlinCompilation<*>) = true

  override fun getCompilerPluginId() = "app.cash.redwood.tools.compose"

  override fun getPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
    composeCompilerGroupId,
    composeCompilerArtifactId,
    composeCompilerVersion,
  )

  override fun applyToCompilation(
    kotlinCompilation: KotlinCompilation<*>,
  ): Provider<List<SubpluginOption>> {
    when (kotlinCompilation.platformType) {
      js, wasm -> {
        // The Compose compiler sometimes chooses to emit a duplicate signature rather than looking
        // for an existing one. This occurs on all targets, but JS and WASM (which currently uses
        // the JS compiler) have an explicit check for this. We disable this check which is deemed
        // safe as the first-party JB Compose plugin does the same thing.
        // https://github.com/JetBrains/compose-multiplatform/issues/3418#issuecomment-1971555314
        kotlinCompilation.compilerOptions.configure {
          freeCompilerArgs.add("-Xklib-enable-signature-clash-checks=false")
        }
      }

      else -> {}
    }

    return kotlinCompilation.target.project.provider { emptyList() }
  }
}
