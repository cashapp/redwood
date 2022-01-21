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
import app.cash.redwood.gradle.RedwoodSchemaGeneratorPlugin.Strategy.Test
import app.cash.redwood.gradle.RedwoodSchemaGeneratorPlugin.Strategy.Widget
import app.cash.redwood.gradle.RedwoodSchemaGeneratorPlugin.Strategy.WidgetProtocol
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.attributes.Usage
import org.gradle.api.attributes.Usage.JAVA_RUNTIME
import org.gradle.api.attributes.Usage.USAGE_ATTRIBUTE
import org.gradle.api.tasks.JavaExec
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import java.io.File

@Suppress("unused") // Invoked reflectively by Gradle.
public class RedwoodSchemaComposePlugin : RedwoodSchemaGeneratorPlugin(Compose)

@Suppress("unused") // Invoked reflectively by Gradle.
public class RedwoodSchemaComposeProtocolPlugin : RedwoodSchemaGeneratorPlugin(ComposeProtocol)

@Suppress("unused") // Invoked reflectively by Gradle.
public class RedwoodSchemaTestPlugin : RedwoodSchemaGeneratorPlugin(Test)

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
    // TODO This should only rely on treehouse-compose and not treehouse-protocol-compose.
    Compose("--compose", "app.cash.treehouse:treehouse-protocol-compose:$redwoodVersion"),
    ComposeProtocol("--compose-protocol", "app.cash.treehouse:treehouse-protocol-compose:$redwoodVersion"),
    Test("--test", "app.cash.treehouse:treehouse-widget:$redwoodVersion"),
    Widget("--widget", "app.cash.treehouse:treehouse-widget:$redwoodVersion"),
    WidgetProtocol("--widget-protocol", "app.cash.treehouse:treehouse-protocol-widget:$redwoodVersion"),
  }

  override fun apply(project: Project) {
    var applied = false

    if (strategy == Compose) {
      project.plugins.apply(RedwoodPlugin::class.java)
    }

    val extension = project.extensions.create(
      RedwoodSchemaExtension::class.java,
      "treehouseSchema",
      RedwoodSchemaExtensionImpl::class.java,
    )

    val configuration = project.configurations.create("redwoodSchema") {
      // Ensure we get JVM artifacts from any multiplatform dependencies for use with JavaExec.
      val runtimeUsage = project.objects.named(Usage::class.java, JAVA_RUNTIME)
      it.attributes.attribute(USAGE_ATTRIBUTE, runtimeUsage)
      it.attributes.attribute(KotlinPlatformType.attribute, KotlinPlatformType.jvm)
    }
    project.dependencies.add(
      configuration.name,
      "app.cash.treehouse:redwood-schema-generator:$redwoodVersion",
    )

    val generatedDir = File(project.buildDir, "generated/redwood")
    val generate = project.tasks.register("redwoodGenerate", JavaExec::class.java) { exec ->
      exec.outputs.dir(generatedDir)

      exec.classpath(configuration)
      exec.main = "app.cash.redwood.schema.generator.Main"

      exec.doFirst {
        generatedDir.deleteRecursively()
      }
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
