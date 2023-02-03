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
package app.cash.redwood.cli

import app.cash.redwood.tooling.codegen.CodegenType
import app.cash.redwood.tooling.codegen.ProtocolCodegenType
import app.cash.redwood.tooling.codegen.generate
import app.cash.redwood.tooling.schema.parseProtocolSchema
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
  private val type by option()
    .switch(
      "--compose" to CodegenType.Compose,
      "--compose-protocol" to ProtocolCodegenType.Compose,
      "--layout-modifiers" to CodegenType.LayoutModifiers,
      "--testing" to CodegenType.Testing,
      "--widget" to CodegenType.Widget,
      "--widget-protocol" to ProtocolCodegenType.Widget,
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

    when (val type = type) {
      is CodegenType -> {
        val schema = parseSchema(schemaType)
        schema.generate(type, out)
      }
      is ProtocolCodegenType -> {
        val schema = parseProtocolSchema(schemaType)
        schema.generate(type, out)
      }
      else -> throw AssertionError()
    }
  }
}
