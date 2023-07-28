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

private const val extensionName = "redwood"

public class RedwoodComposePlugin : KotlinCompilerPluginSupportPlugin {
  private lateinit var extension: RedwoodComposeExtension

  override fun apply(target: Project) {
    super.apply(target)

    extension = target.extensions.create(extensionName, RedwoodComposeExtension::class.java)

    // TODO Automatically run lint on usages of our Compose plugin once the check works.
    //  target.plugins.apply(RedwoodLintPlugin::class.java)
  }

  override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean = true

  override fun getCompilerPluginId(): String = "app.cash.redwood"

  override fun getPluginArtifact(): SubpluginArtifact {
    val plugin = extension.kotlinCompilerPlugin.get()
    val parts = plugin.split(":")
    return when (parts.size) {
      1 -> SubpluginArtifact("org.jetbrains.compose.compiler", "compiler", parts[0])
      3 -> SubpluginArtifact(parts[0], parts[1], parts[2])
      else -> error(
        """
        |Illegal format of '$extensionName.${RedwoodComposeExtension::kotlinCompilerPlugin.name}' property.
        |Expected format: either '<VERSION>' or '<GROUP_ID>:<ARTIFACT_ID>:<VERSION>'
        |Actual value: '$plugin'
        """.trimMargin(),
      )
    }
  }

  override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
    kotlinCompilation.dependencies {
      api(project.redwoodDependency("redwood-compose"))
    }

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
