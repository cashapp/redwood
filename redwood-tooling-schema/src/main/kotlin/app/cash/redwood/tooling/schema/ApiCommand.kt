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
import com.github.ajalt.clikt.core.PrintMessage
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.path

internal class ApiCommand :
  AbstractSchemaCommand(
    name = "api",
    help = "Write schema protocol API to XML, or validate schema compatibility with existing XML",
  ) {
  private val file by option("-f", "--file")
    .path()
    .required()
    .help("XML file")

  private val mode by option("-m", "--mode")
    .choice("check" to Check, "generate" to Generate)
    .default(Check)

  private val fixCommand by option("--fix-with")
    .help("The command to generate an updated file (reported in failure messages)")
    .default("generate")

  override fun run(schema: ProtocolSchema) {
    val currentApi = ApiSchema(schema)

    when (val result = currentApi.validateAgainst(file, mode, fixCommand)) {
      is Failure -> {
        throw PrintMessage(result.message, statusCode = 127, printError = true)
      }

      Success -> {
        // Nothing to do!
      }
    }
  }
}
