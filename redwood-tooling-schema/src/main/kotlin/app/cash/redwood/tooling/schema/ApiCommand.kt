/*
 * Copyright (C) 2023 Square, Inc.
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
package app.cash.redwood.tooling.schema

import app.cash.redwood.tooling.schema.ValidationMode.Check
import app.cash.redwood.tooling.schema.ValidationMode.Generate
import app.cash.redwood.tooling.schema.ValidationResult.Failure
import app.cash.redwood.tooling.schema.ValidationResult.Success
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.PrintMessage
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.help
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.split
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.path
import java.io.File
import java.net.URLClassLoader

internal class ApiCommand : CliktCommand(
  name = "api",
  help = "Write schema protocol API to XML, or validate schema compatibility with existing XML",
) {
  private val file by option("-f", "--file")
    .path()
    .required()
    .help("XML file")

  private val classpath by option("-cp", "--class-path")
    .file()
    .split(File.pathSeparator)
    .required()

  private val mode by option("-m", "--mode")
    .choice("check" to Check, "generate" to Generate)
    .default(Check)

  private val schemaTypeName by argument("schema")
    .help("Fully-qualified class name for the @Schema-annotated interface")

  private val fixCommand by option("--fix-with")
    .help("The command to generate an updated file (reported in failure messages)")
    .default("generate")

  override fun run() {
    val classLoader = URLClassLoader(classpath.map { it.toURI().toURL() }.toTypedArray())
    val schemaType = classLoader.loadClass(schemaTypeName).kotlin
    val schema = ProtocolSchemaSet.parse(schemaType).schema
    val currentApi = ApiSchema(schema)

    when (val result = currentApi.validateAgainst(file, mode, fixCommand)) {
      is Failure -> {
        throw PrintMessage(result.message, true)
      }
      Success -> {
        // Nothing to do!
      }
    }
  }
}
