@file:JvmName("Main")

package app.cash.treehouse.schema.generator

import app.cash.exhaustive.Exhaustive
import app.cash.treehouse.schema.generator.TreehouseGenerator.Type.Compose
import app.cash.treehouse.schema.generator.TreehouseGenerator.Type.Display
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
    Display, Compose
  }

  private val type by option()
    .switch("--display" to Display, "--compose" to Compose)
    .help("Type of code to generate")
    .required()

  private val out by option().path().required()
    .help("Directory into which generated files are written")

  private val schemaType by argument("schema")
    .help("Fully-qualified class name for the @Schema-annotated interface")
    .convert { Class.forName(it) as Class<*> }

  override fun run() {
    val schema = parseSchema(schemaType)
    @Exhaustive when (type) {
      Display -> {
        generateDisplayNodeFactory(schema).writeTo(out)
        for (node in schema.nodes) {
          generateDisplayNode(schema, node).writeTo(out)
        }
      }
      Compose -> {
        for (node in schema.nodes) {
          generateComposeNode(schema, node).writeTo(out)
        }
      }
    }
  }
}
