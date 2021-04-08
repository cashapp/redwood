@file:JvmName("Main")

package app.cash.treehouse.schema.generator

import app.cash.exhaustive.Exhaustive
import app.cash.treehouse.schema.generator.TreehouseGenerator.Type.Compose
import app.cash.treehouse.schema.generator.TreehouseGenerator.Type.Widget
import app.cash.treehouse.schema.parser.parseSchema
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.arguments.help
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.switch
import com.github.ajalt.clikt.parameters.types.path

fun main(vararg args: String) {
  TreehouseGenerator().main(args)
}

private class TreehouseGenerator : CliktCommand() {
  enum class Type {
    Compose,
    Widget,
  }

  private val type by option()
    .switch("--compose" to Compose, "--widget" to Widget)
    .help("Type of code to generate")
    .required()

  private val out by option().path().required()
    .help("Directory into which generated files are written")

  private val schemaType by argument("schema")
    .help("Fully-qualified class name for the @Schema-annotated interface")
    .convert {
      // Replace with https://youtrack.jetbrains.com/issue/KT-10440 once it ships.
      Class.forName(it).kotlin
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
    }
  }
}
