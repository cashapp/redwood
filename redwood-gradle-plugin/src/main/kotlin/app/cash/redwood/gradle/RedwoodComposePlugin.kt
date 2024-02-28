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

import com.android.build.gradle.BaseExtension
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

private const val EXTENSION_NAME = "redwood"
private const val REDWOOD_COMPOSE_ARTIFACT_ID = "redwood-compose"

public class RedwoodComposePlugin : KotlinCompilerPluginSupportPlugin {
  private lateinit var extension: RedwoodComposeExtension

  override fun apply(target: Project) {
    super.apply(target)

    extension = RedwoodComposeExtensionImpl(target)
    target.extensions.add(
      RedwoodComposeExtension::class.java,
      EXTENSION_NAME,
      extension,
    )

    target.plugins.withId("org.jetbrains.compose") {
      throw IllegalStateException(
        """
        |The Redwood Gradle plugin cannot be applied to the same project as the JetBrains Compose Gradle plugin.
        |
        |Both plugins attempt to configure the Compose compiler plugin which is incompatible. To use Redwood
        |within a JetBrains Compose project you only need to add the runtime dependency:
        |
        |    kotlin {
        |      sourceSets {
        |        commonMain {
        |          dependencies {
        |            implementation("${target.redwoodDependency(REDWOOD_COMPOSE_ARTIFACT_ID)}")
        |          }
        |        }
        |      }
        |    }
        """.trimMargin(),
      )
    }
    target.plugins.withId("com.android.base") {
      val android = target.extensions.getByName("android") as BaseExtension
      target.afterEvaluate {
        check(android.buildFeatures.compose != true) {
          """
          |The Redwood Gradle plugin cannot be applied to an Android project which enables Compose.
          |
          |Both plugins attempt to configure the Compose compiler plugin which is incompatible. To use Redwood
          |within an Android Compose-based project you only need to add the runtime dependency:
          |
          |    dependencies {
          |      implementation("${target.redwoodDependency(REDWOOD_COMPOSE_ARTIFACT_ID)}")
          |    }
          """.trimMargin()
        }
      }
    }

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
        |Illegal format of '$EXTENSION_NAME.${RedwoodComposeExtension::kotlinCompilerPlugin.name}' property.
        |Expected format: either '<VERSION>' or '<GROUP_ID>:<ARTIFACT_ID>:<VERSION>'
        |Actual value: '$plugin'
        """.trimMargin(),
      )
    }
  }

  override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
    kotlinCompilation.dependencies {
      api(project.redwoodDependency(REDWOOD_COMPOSE_ARTIFACT_ID))
    }

    return kotlinCompilation.target.project.provider { emptyList() }
  }
}
