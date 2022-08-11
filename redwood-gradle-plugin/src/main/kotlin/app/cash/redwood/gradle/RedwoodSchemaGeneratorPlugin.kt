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

import app.cash.redwood.gradle.RedwoodSchemaGeneratorPlugin.Strategy.Compose
import app.cash.redwood.gradle.RedwoodSchemaGeneratorPlugin.Strategy.ComposeProtocol
import app.cash.redwood.gradle.RedwoodSchemaGeneratorPlugin.Strategy.LayoutModifiers
import app.cash.redwood.gradle.RedwoodSchemaGeneratorPlugin.Strategy.Widget
import app.cash.redwood.gradle.RedwoodSchemaGeneratorPlugin.Strategy.WidgetProtocol
import java.io.File
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.JavaExec
import org.gradle.language.base.plugins.LifecycleBasePlugin.BUILD_GROUP
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

@Suppress("unused") // Invoked reflectively by Gradle.
public class RedwoodSchemaComposePlugin : RedwoodSchemaGeneratorPlugin(Compose)

@Suppress("unused") // Invoked reflectively by Gradle.
public class RedwoodSchemaComposeProtocolPlugin : RedwoodSchemaGeneratorPlugin(ComposeProtocol)

@Suppress("unused") // Invoked reflectively by Gradle.
public class RedwoodSchemaLayoutModifiersPlugin : RedwoodSchemaGeneratorPlugin(LayoutModifiers)

@Suppress("unused") // Invoked reflectively by Gradle.
public class RedwoodSchemaWidgetPlugin : RedwoodSchemaGeneratorPlugin(Widget)

@Suppress("unused") // Invoked reflectively by Gradle.
public class RedwoodSchemaWidgetProtocolPlugin : RedwoodSchemaGeneratorPlugin(WidgetProtocol)

public abstract class RedwoodSchemaGeneratorPlugin(
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
    var applied = false

    if (strategy == Compose) {
      project.plugins.apply(RedwoodPlugin::class.java)
    }

    val extension = project.extensions.create(
      RedwoodSchemaExtension::class.java,
      "redwoodSchema",
      RedwoodSchemaExtensionImpl::class.java,
    )

    val configuration = project.configurations.create("redwoodSchema")
    project.dependencies.add(
      configuration.name,
      "app.cash.redwood:redwood-cli:$redwoodVersion",
    )

    val generatedDir = File(project.buildDir, "generated/redwood")
    val generate = project.tasks.register("redwoodGenerate", JavaExec::class.java) { exec ->
      exec.group = BUILD_GROUP
      exec.description = "Generate Redwood sources"

      exec.outputs.dir(generatedDir)

      exec.classpath(configuration)
      exec.mainClass.set("app.cash.redwood.cli.Main")

      @Suppress("ObjectLiteralToLambda") // Gradle wants an anonymous class and not a lambda.
      val deleteGeneratedDir = object : Action<Task> {
        override fun execute(task: Task) {
          generatedDir.deleteRecursively()
        }
      }
      exec.doFirst(deleteGeneratedDir)
    }

    project.plugins.withId("org.jetbrains.kotlin.multiplatform") {
      applied = true
    }

    project.afterEvaluate {
      check(applied) {
        "Redwood schema plugin requires the Kotlin multiplatform plugin to be applied."
      }

      val schemaType = requireNotNull(extension.type) {
        "Redwood schema type name must be specified!"
      }
      val schemaProject = requireNotNull(extension.source) {
        "Redwood schema project must be specified!"
      }

      project.dependencies.add(configuration.name, schemaProject)

      generate.configure {
        it.args = listOf(
          strategy.generatorFlag,
          "--out",
          generatedDir.toString(),
          schemaType,
        )
      }

      val kotlin = project.extensions.getByType(KotlinMultiplatformExtension::class.java)

      kotlin.sourceSets.getByName("commonMain") { sourceSet ->
        sourceSet.kotlin.srcDir(generatedDir)
        sourceSet.dependencies {
          api(strategy.dependencyCoordinate)
        }
      }

      kotlin.targets.all { target ->
        target.compilations.all { compilation ->
          compilation.compileKotlinTaskProvider.configure {
            it.dependsOn(generate.get())
          }
        }
      }
    }
  }
}
