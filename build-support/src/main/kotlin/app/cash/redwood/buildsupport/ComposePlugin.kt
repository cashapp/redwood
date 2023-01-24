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

import java.util.Locale
import org.gradle.api.Project
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Provider
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
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

@Suppress("unused") // Invoked reflectively by Gradle.
class ComposePlugin : KotlinCompilerPluginSupportPlugin {
  private lateinit var extension: ComposeExtension

  override fun apply(target: Project) {
    extension = target.extensions.create("redwoodBuildCompose", ComposeExtension::class.java)

    target.tasks.withType(KotlinCompile::class.java).configureEach {
      it.kotlinOptions.apply {
        freeCompilerArgs = freeCompilerArgs +
          listOf("-P", "plugin:androidx.compose.compiler.plugins.kotlin:suppressKotlinVersionCompatibilityCheck=1.8.10-407")
      }
    }
  }

  override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean {
    val name = kotlinCompilation.target.name +
      kotlinCompilation.name.replaceFirstChar { it.titlecase(Locale.ROOT) }

    val targetCompilations = extension.targetCompilations.get()
    return when {
      targetCompilations.isEmpty() -> true
      else -> name in targetCompilations
    }
  }

  override fun getCompilerPluginId() = "app.cash.redwood.tools.compose"

  override fun getPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
    "org.jetbrains.compose.compiler",
    "compiler",
    jbComposeCompiler,
  )

  override fun applyToCompilation(
    kotlinCompilation: KotlinCompilation<*>
  ): Provider<List<SubpluginOption>> {
    when (kotlinCompilation.platformType) {
      js -> {
        // This enables a workaround for Compose lambda generation to function correctly in JS.
        // Note: We cannot use SubpluginOption to do this because it targets the Compose plugin.
        kotlinCompilation.kotlinOptions.freeCompilerArgs +=
          listOf("-P", "plugin:androidx.compose.compiler.plugins.kotlin:generateDecoys=true")
      }
      common, androidJvm, jvm, native, wasm -> {
        // Nothing to do!
      }
    }

    return kotlinCompilation.target.project.provider { emptyList() }
  }
}

interface ComposeExtension {
  val targetCompilations: ListProperty<String>
}
