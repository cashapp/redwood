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

import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.path
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

internal class JsonCommand : AbstractSchemaCommand("json") {
  override fun help(context: Context) =
    "Parse schema members into a JSON representation"

  private val out by option().path().required()
    .help("Directory into which JSON is written")

  override fun run(schema: ProtocolSchema) {
    val embeddedSchema = schema.toEmbeddedSchema()
    val path = out.resolve(embeddedSchema.path)
    path.parent.createDirectories()
    path.writeText(embeddedSchema.json)
  }
}
