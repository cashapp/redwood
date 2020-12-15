@file:JvmName("Main")

package app.cash.treehouse.schema.generator

import app.cash.exhaustive.Exhaustive
import app.cash.treehouse.schema.generator.TreehouseGenerator.Type.Client
import app.cash.treehouse.schema.generator.TreehouseGenerator.Type.Server
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
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
    Client, Server
  }

  private val type by option()
    .switch("--client" to Client, "--server" to Server)
    .help("Type of code to generate")
    .required()

  private val out by option().path().required()
    .help("Directory into which generated files are written")

  private val schemaFqcn by argument("schema")
    .help("Fully-qualified class name for the @Schema-annotated interface")

  override fun run() {
    val schema = parseSchema(schemaFqcn)
    @Exhaustive when (type) {
      Client -> {
        generateClientNodeFactory(schema).writeTo(out)
        for (node in schema.nodes) {
          generateClientNode(schema, node).writeTo(out)
        }
      }
      Server -> {
        for (node in schema.nodes) {
          generateServerNode(schema, node).writeTo(out)
        }
      }
    }
  }
}
