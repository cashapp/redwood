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
package app.cash.redwood.lint

import app.cash.redwood.tooling.lint.ApiMerger
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.help
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.path
import java.nio.file.FileSystem
import kotlin.io.path.readText
import kotlin.io.path.writeText

internal class ApiMergeCommand(
  fileSystem: FileSystem,
) : CliktCommand(name = "api-merge") {
  private val inputs by argument("FILE")
    .help("One or more API definition XML documents")
    .path(fileSystem = fileSystem)
    .multiple(required = true)

  private val output by option("-o", "--out", metavar = "FILE")
    .help("Output file for the merged API definition XML document")
    .path(fileSystem = fileSystem)
    .required()

  override fun run() {
    val merger = ApiMerger()
    for (input in inputs) {
      merger += input.readText()
    }
    output.writeText(merger.merge())
  }
}
