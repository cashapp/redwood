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

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.help
import com.github.ajalt.clikt.parameters.options.defaultLazy
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.split
import com.github.ajalt.clikt.parameters.types.file
import java.io.File
import java.net.URLClassLoader

internal abstract class AbstractSchemaCommand(
  name: String,
  help: String,
) : CliktCommand(name = name, help = help) {
  private val fir by option("--use-fir")
    .flag()
    .help("Use new FIR-based parser")

  private val jdkHome by option("--jdk-home")
    .file()
    .defaultLazy { System.getProperty("java.home").let(::File) }
    .help("Path to the JDK installation (defaults to this JDK)")

  private val sources by option("-s", "--source")
    .file()
    .multiple()
    .help("Kotlin source files and folders")

  private val classpath by option("-cp", "--class-path")
    .file()
    .split(File.pathSeparator)
    .required()

  private val schemaTypeName by argument("schema")
    .help("Fully-qualified class name for the @Schema-annotated interface")

  protected val schema: ProtocolSchema by lazy {
    if (fir) {
      parseProtocolSchema(jdkHome, sources, classpath, FqType.bestGuess(schemaTypeName)).schema
    } else {
      val classLoader = URLClassLoader(classpath.map { it.toURI().toURL() }.toTypedArray())
      val schemaType = classLoader.loadClass(schemaTypeName).kotlin
      ProtocolSchemaSet.parse(schemaType).schema
    }
  }
}
