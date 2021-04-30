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
package app.cash.treehouse.gradle

import app.cash.treehouse.gradle.TreehouseSchemaGeneratorPlugin.Strategy.Compose
import app.cash.treehouse.gradle.TreehouseSchemaGeneratorPlugin.Strategy.Test
import app.cash.treehouse.gradle.TreehouseSchemaGeneratorPlugin.Strategy.Widget
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.attributes.Usage
import org.gradle.api.attributes.Usage.JAVA_RUNTIME
import org.gradle.api.attributes.Usage.USAGE_ATTRIBUTE
import org.gradle.api.tasks.JavaExec
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import java.io.File

@Suppress("unused") // Invoked reflectively by Gradle.
public class TreehouseSchemaComposePlugin : TreehouseSchemaGeneratorPlugin(Compose)

@Suppress("unused") // Invoked reflectively by Gradle.
public class TreehouseSchemaTestPlugin : TreehouseSchemaGeneratorPlugin(Test)

@Suppress("unused") // Invoked reflectively by Gradle.
public class TreehouseSchemaWidgetPlugin : TreehouseSchemaGeneratorPlugin(Widget)

public abstract class TreehouseSchemaGeneratorPlugin(
  private val strategy: Strategy,
) : Plugin<Project> {
  public enum class Strategy(
    internal val generatorFlag: String,
    internal val dependencyCoordinate: String,
  ) {
    Compose("--compose", "app.cash.treehouse:treehouse-compose:$treehouseVersion"),
    Test("--test", "app.cash.treehouse:treehouse-widget:$treehouseVersion"),
    Widget("--widget", "app.cash.treehouse:treehouse-widget:$treehouseVersion"),
  }

  override fun apply(project: Project) {
    var applied = false

    if (strategy == Compose) {
      project.plugins.apply(TreehousePlugin::class.java)
    }

    val extension = project.extensions.create(
      TreehouseSchemaExtension::class.java,
      "treehouseSchema",
      TreehouseSchemaExtensionImpl::class.java,
    )

    val configuration = project.configurations.create("treehouseSchema") {
      // Ensure we get JVM artifacts from any multiplatform dependencies for use with JavaExec.
      val runtimeUsage = project.objects.named(Usage::class.java, JAVA_RUNTIME)
      it.attributes.attribute(USAGE_ATTRIBUTE, runtimeUsage)
    }
    project.dependencies.add(
      configuration.name,
      "app.cash.treehouse:treehouse-schema-generator:$treehouseVersion",
    )

    val generatedDir = File(project.buildDir, "generated/treehouse")
    val generate = project.tasks.register("treehouseGenerate", JavaExec::class.java) { exec ->
      exec.outputs.dir(generatedDir)

      exec.classpath(configuration)
      exec.main = "app.cash.treehouse.schema.generator.Main"

      exec.doFirst {
        generatedDir.deleteRecursively()
      }
    }

    project.plugins.withId("org.jetbrains.kotlin.multiplatform") {
      applied = true
    }

    project.afterEvaluate {
      check(applied) {
        "Treehouse schema plugin requires the Kotlin multiplatform plugin to be applied."
      }

      val schemaType = requireNotNull(extension.type) {
        "Treehouse schema type name must be specified!"
      }
      val schemaProject = requireNotNull(extension.source) {
        "Treehouse schema project must be specified!"
      }

      project.dependencies.add(configuration.name, schemaProject)

      generate.configure {
        it.args = listOf(
          strategy.generatorFlag,
          "--out", generatedDir.toString(),
          schemaType
        )
      }

      val kotlin = project.extensions.getByType(KotlinMultiplatformExtension::class.java)

      kotlin.sourceSets.getByName("commonMain") { sourceSet ->
        sourceSet.kotlin.srcDir(generatedDir)
        sourceSet.dependencies {
          api(strategy.dependencyCoordinate)

          if (strategy == Test) {
            api(schemaProject)
          }
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
