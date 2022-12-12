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
package app.cash.redwood.generator

import app.cash.redwood.generator.GenerateCommand.Type.Compose
import app.cash.redwood.generator.GenerateCommand.Type.ComposeProtocol
import app.cash.redwood.generator.GenerateCommand.Type.LayoutModifiers
import app.cash.redwood.generator.GenerateCommand.Type.Widget
import app.cash.redwood.generator.GenerateCommand.Type.WidgetProtocol
import app.cash.redwood.tooling.schema.parseSchema
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.help
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.switch
import com.github.ajalt.clikt.parameters.types.path
import java.io.File
import java.net.URLClassLoader

internal class GenerateCommand : CliktCommand(name = "generate") {
  enum class Type {
    Compose,
    ComposeProtocol,
    LayoutModifiers,
    Widget,
    WidgetProtocol,
  }

  private val type by option()
    .switch(
      "--compose" to Compose,
      "--compose-protocol" to ComposeProtocol,
      "--layout-modifiers" to LayoutModifiers,
      "--widget" to Widget,
      "--widget-protocol" to WidgetProtocol,
    )
    .help("Type of code to generate")
    .required()

  private val out by option().path().required()
    .help("Directory into which generated files are written")

  private val classpath by option("-cp", "--class-path")
    .convert { it.split(File.pathSeparator).map(::File) }
    .required()

  private val schemaTypeName by argument("schema")
    .help("Fully-qualified class name for the @Schema-annotated interface")

  override fun run() {
    val classLoader = URLClassLoader(classpath.map { it.toURI().toURL() }.toTypedArray())
    val schemaType = classLoader.loadClass(schemaTypeName).kotlin
    val schema = parseSchema(schemaType)

    when (type) {
      Compose -> {
        for (dependency in schema.allSchemas) {
          generateLayoutModifierImpls(dependency)?.writeTo(out)
          for (scope in dependency.scopes) {
            generateScope(dependency, scope).writeTo(out)
          }
          for (widget in dependency.widgets) {
            generateComposable(dependency, widget, host = schema).writeTo(out)
          }
        }
      }
      ComposeProtocol -> {
        for (dependency in schema.allSchemas) {
          generateDiffProducingWidgetFactory(dependency, host = schema).writeTo(out)
          generateDiffProducingLayoutModifiers(dependency, host = schema).writeTo(out)
          for (widget in dependency.widgets) {
            generateDiffProducingWidget(dependency, widget, host = schema).writeTo(out)
          }
        }
      }
      LayoutModifiers -> {
        for (layoutModifier in schema.layoutModifiers) {
          generateLayoutModifierInterface(schema, layoutModifier).writeTo(out)
        }
      }
      Widget -> {
        generateWidgetFactory(schema).writeTo(out)
        for (widget in schema.widgets) {
          generateWidget(schema, widget).writeTo(out)
        }
      }
      WidgetProtocol -> {
        for (dependency in schema.allSchemas) {
          generateDiffConsumingWidgetFactory(dependency, host = schema).writeTo(out)
          generateDiffConsumingLayoutModifiers(dependency, host = schema).writeTo(out)
          for (widget in dependency.widgets) {
            generateDiffConsumingWidget(dependency, widget, host = schema).writeTo(out)
          }
        }
      }
    }
  }
}
