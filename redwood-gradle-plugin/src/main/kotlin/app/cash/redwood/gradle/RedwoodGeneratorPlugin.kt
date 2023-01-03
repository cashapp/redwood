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
    internal val dependencyArtifactId: String,
  ) {
    Compose("--compose", "redwood-compose"),
    ComposeProtocol("--compose-protocol", "redwood-protocol-compose"),
    LayoutModifiers("--layout-modifiers", "redwood-runtime"),
    Widget("--widget", "redwood-widget"),
    WidgetProtocol("--widget-protocol", "redwood-protocol-widget"),
  }

  override fun apply(project: Project) {
    if (strategy == Compose) {
      project.plugins.apply(RedwoodPlugin::class.java)
    }
    if (strategy == ComposeProtocol || strategy == WidgetProtocol) {
      project.plugins.apply("org.jetbrains.kotlin.plugin.serialization")
    }

    val extension = project.extensions.create(
      "redwoodSchema",
      RedwoodGeneratorExtension::class.java,
    )

    val cliConfiguration = project.configurations.create("redwoodCli").apply {
      isCanBeConsumed = false
      isVisible = false
    }
    project.dependencies.add(cliConfiguration.name, project.redwoodDependency("redwood-cli"))

    val schemaConfiguration = project.configurations.create("redwoodSchema").apply {
      isCanBeConsumed = false
      isVisible = false
    }
    val generate = project.tasks.register("redwoodGenerate", RedwoodGeneratorTask::class.java) {
      it.group = BUILD_GROUP
      it.description = "Generate Redwood sources"

      it.toolClasspath.from(cliConfiguration)
      it.outputDir.set(project.layout.buildDirectory.dir("generated/redwood"))
      it.generatorFlag.set(strategy.generatorFlag)
      it.schemaType.set(extension.type)
      it.classpath.from(schemaConfiguration)
    }

    project.afterEvaluate {
      check(project.plugins.hasPlugin("org.jetbrains.kotlin.multiplatform")) {
        "Redwood schema plugin requires the Kotlin multiplatform plugin to be applied."
      }

      val schemaProject = extension.source.get()
      project.dependencies.add(schemaConfiguration.name, schemaProject)

      val kotlin = project.extensions.getByType(KotlinMultiplatformExtension::class.java)
      kotlin.sourceSets.getByName("commonMain") { sourceSet ->
        sourceSet.kotlin.srcDir(generate.map { it.outputDir })
        sourceSet.dependencies {
          api(project.redwoodDependency(strategy.dependencyArtifactId))
        }
      }
    }
  }
}
