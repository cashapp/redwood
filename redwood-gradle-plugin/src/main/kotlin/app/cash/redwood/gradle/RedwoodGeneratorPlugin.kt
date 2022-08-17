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

import app.cash.redwood.gradle.RedwoodGeneratorPlugin.Strategy.Compose
import app.cash.redwood.gradle.RedwoodGeneratorPlugin.Strategy.ComposeProtocol
import app.cash.redwood.gradle.RedwoodGeneratorPlugin.Strategy.LayoutModifiers
import app.cash.redwood.gradle.RedwoodGeneratorPlugin.Strategy.Widget
import app.cash.redwood.gradle.RedwoodGeneratorPlugin.Strategy.WidgetProtocol
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.language.base.plugins.LifecycleBasePlugin.BUILD_GROUP
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

@Suppress("unused") // Invoked reflectively by Gradle.
public class RedwoodComposeGeneratorPlugin : RedwoodGeneratorPlugin(Compose)

@Suppress("unused") // Invoked reflectively by Gradle.
public class RedwoodComposeProtocolGeneratorPlugin : RedwoodGeneratorPlugin(ComposeProtocol)

@Suppress("unused") // Invoked reflectively by Gradle.
public class RedwoodLayoutModifiersGeneratorPlugin : RedwoodGeneratorPlugin(LayoutModifiers)

@Suppress("unused") // Invoked reflectively by Gradle.
public class RedwoodWidgetGeneratorPlugin : RedwoodGeneratorPlugin(Widget)

@Suppress("unused") // Invoked reflectively by Gradle.
public class RedwoodWidgetProtocolGeneratorPlugin : RedwoodGeneratorPlugin(WidgetProtocol)

public abstract class RedwoodGeneratorPlugin(
  private val strategy: Strategy,
) : Plugin<Project> {
  public enum class Strategy(
    internal val generatorFlag: String,
    internal val dependencyCoordinate: String,
  ) {
    // TODO This should only rely on redwood-compose and not redwood-protocol-compose.
    Compose("--compose", "app.cash.redwood:redwood-protocol-compose:$redwoodVersion"),
    ComposeProtocol("--compose-protocol", "app.cash.redwood:redwood-protocol-compose:$redwoodVersion"),
    LayoutModifiers("--layout-modifiers", "app.cash.redwood:redwood-runtime:$redwoodVersion"),
    Widget("--widget", "app.cash.redwood:redwood-widget:$redwoodVersion"),
    WidgetProtocol("--widget-protocol", "app.cash.redwood:redwood-protocol-widget:$redwoodVersion"),
  }

  override fun apply(project: Project) {
    if (strategy == Compose) {
      project.plugins.apply(RedwoodPlugin::class.java)
    }

    val extension = project.extensions.create(
      "redwoodSchema",
      RedwoodGeneratorExtension::class.java,
    )

    val configuration = project.configurations.create("redwoodSchema")
    project.dependencies.add(
      configuration.name,
      "app.cash.redwood:redwood-cli:$redwoodVersion",
    )

    val generate = project.tasks.register("redwoodGenerate", RedwoodGeneratorTask::class.java) {
      it.group = BUILD_GROUP
      it.description = "Generate Redwood sources"

      it.toolClasspath.from(configuration)
      it.outputDir.set(project.layout.buildDirectory.dir("generated/redwood"))
      it.generatorFlag.set(strategy.generatorFlag)
      it.schemaType.set(extension.type)
    }

    project.afterEvaluate {
      check(project.plugins.hasPlugin("org.jetbrains.kotlin.multiplatform")) {
        "Redwood schema plugin requires the Kotlin multiplatform plugin to be applied."
      }

      val schemaProject = extension.source.get()
      project.dependencies.add(configuration.name, schemaProject)

      val kotlin = project.extensions.getByType(KotlinMultiplatformExtension::class.java)
      kotlin.sourceSets.getByName("commonMain") { sourceSet ->
        sourceSet.kotlin.srcDir(generate.map { it.outputDir })
        sourceSet.dependencies {
          api(strategy.dependencyCoordinate)
        }
      }
    }
  }
}
