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
@file:JvmName("Main")

package app.cash.redwood.schema.generator

import app.cash.exhaustive.Exhaustive
import app.cash.redwood.schema.generator.RedwoodGenerator.Type.Compose
import app.cash.redwood.schema.generator.RedwoodGenerator.Type.ComposeProtocol
import app.cash.redwood.schema.generator.RedwoodGenerator.Type.Test
import app.cash.redwood.schema.generator.RedwoodGenerator.Type.Widget
import app.cash.redwood.schema.generator.RedwoodGenerator.Type.WidgetProtocol
import app.cash.redwood.schema.parser.parseSchema
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.arguments.help
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.switch
import com.github.ajalt.clikt.parameters.types.path

public fun main(vararg args: String) {
  RedwoodGenerator().main(args)
}

private class RedwoodGenerator : CliktCommand() {
  enum class Type {
    Compose,
    ComposeProtocol,
    Test,
    Widget,
    WidgetProtocol,
  }

  private val type by option()
    .switch(
      "--compose" to Compose,
      "--compose-protocol" to ComposeProtocol,
      "--test" to Test,
      "--widget" to Widget,
      "--widget-protocol" to WidgetProtocol,
    )
    .help("Type of code to generate")
    .required()

  private val out by option().path().required()
    .help("Directory into which generated files are written")

  private val schemaType by argument("schema")
    .help("Fully-qualified class name for the @Schema-annotated interface")
    .convert {
      // Replace with https://youtrack.jetbrains.com/issue/KT-10440 once it ships.
      try {
        Class.forName(it).kotlin
      } catch (e: ClassNotFoundException) {
        throw CliktError("Unable to load class: $it", e)
      }
    }

  override fun run() {
    val schema = parseSchema(schemaType)
    @Exhaustive when (type) {
      Compose -> {
        for (widget in schema.widgets) {
          generateComposable(schema, widget).writeTo(out)
        }
      }
      ComposeProtocol -> {
        generateDiffProducingWidgetFactory(schema).writeTo(out)
        for (widget in schema.widgets) {
          generateDiffProducingWidget(schema, widget).writeTo(out)
        }
      }
      Test -> {
        generateSchemaWidgetFactory(schema).writeTo(out)
        for (widget in schema.widgets) {
          generateSchemaWidget(schema, widget).writeTo(out)
        }
      }
      Widget -> {
        generateWidgetFactory(schema).writeTo(out)
        for (widget in schema.widgets) {
          generateWidget(schema, widget).writeTo(out)
        }
      }
      WidgetProtocol -> {
        generateDiffConsumingWidgetFactory(schema).writeTo(out)
        for (widget in schema.widgets) {
          generateDiffConsumingWidget(schema, widget).writeTo(out)
        }
      }
    }
  }
}
