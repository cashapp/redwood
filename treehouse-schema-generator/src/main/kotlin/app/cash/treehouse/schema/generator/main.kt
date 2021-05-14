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

package app.cash.treehouse.schema.generator

import app.cash.exhaustive.Exhaustive
import app.cash.treehouse.schema.generator.TreehouseGenerator.Type.Compose
import app.cash.treehouse.schema.generator.TreehouseGenerator.Type.Test
import app.cash.treehouse.schema.generator.TreehouseGenerator.Type.Widget
import app.cash.treehouse.schema.parser.parseSchema
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
  TreehouseGenerator().main(args)
}

private class TreehouseGenerator : CliktCommand() {
  enum class Type {
    Compose,
    Test,
    Widget,
  }

  private val type by option()
    .switch(
      "--compose" to Compose,
      "--test" to Test,
      "--widget" to Widget,
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
      Widget -> {
        generateWidgetFactory(schema).writeTo(out)
        for (widget in schema.widgets) {
          generateWidget(schema, widget).writeTo(out)
        }
      }
      Compose -> {
        for (widget in schema.widgets) {
          generateComposeNode(schema, widget).writeTo(out)
        }
      }
      Test -> {
        generateSchemaWidgetFactory(schema).writeTo(out)
        for (widget in schema.widgets) {
          generateSchemaWidget(schema, widget).writeTo(out)
        }
      }
    }
  }
}
