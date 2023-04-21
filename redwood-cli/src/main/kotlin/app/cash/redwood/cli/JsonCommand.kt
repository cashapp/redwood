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
package app.cash.redwood.cli

import app.cash.redwood.tooling.schema.parseProtocolSchema
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.help
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.path
import java.io.File
import java.net.URLClassLoader
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

internal class JsonCommand : CliktCommand(
  name = "json",
  help = "Parse schema members into a JSON representation",
) {
  private val out by option().path().required()
    .help("Directory into which JSON is written")

  private val classpath by option("-cp", "--class-path")
    .convert { it.split(File.pathSeparator).map(::File) }
    .required()

  private val schemaTypeName by argument("schema")
    .help("Fully-qualified class name for the @Schema-annotated interface")

  override fun run() {
    val classLoader = URLClassLoader(classpath.map { it.toURI().toURL() }.toTypedArray())
    val schemaType = classLoader.loadClass(schemaTypeName).kotlin
    val schema = parseProtocolSchema(schemaType).schema
    val embeddedSchema = schema.toEmbeddedSchema()
    val path = out.resolve(embeddedSchema.path)
    path.parent.createDirectories()
    path.writeText(embeddedSchema.json)
  }
}
