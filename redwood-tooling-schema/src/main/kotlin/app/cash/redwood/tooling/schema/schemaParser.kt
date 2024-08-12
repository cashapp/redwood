/*
 * Copyright (C) 2021 Square, Inc.
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

import java.io.InputStream

private const val MAX_SCHEMA_TAG = 2_000
internal const val MAX_MEMBER_TAG = 1_000_000

internal fun loadProtocolSchemaSet(
  type: FqType,
  classLoader: ClassLoader,
): ProtocolSchemaSet {
  val schema = loadProtocolSchema(type, classLoader)
  return loadProtocolSchemaDependencies(schema, classLoader)
}

internal fun loadProtocolSchemaDependencies(
  schema: ProtocolSchema,
  classLoader: ClassLoader,
): ParsedProtocolSchemaSet {
  val dependencies = schema.taggedDependencies.entries
    .associate { (tag, type) ->
      require(tag != 0) { "Dependency $type tag must be non-zero" }
      type to loadProtocolSchema(type, classLoader, tag)
    }
  return ParsedProtocolSchemaSet(schema, dependencies)
}

internal fun loadProtocolSchema(
  type: FqType,
  classLoader: ClassLoader,
  tag: Int = 0,
): ProtocolSchema {
  require(tag in 0..MAX_SCHEMA_TAG) {
    "$type tag must be 0 for the root or in range (0, $MAX_SCHEMA_TAG] as a dependency: $tag"
  }
  val tagOffset = tag * MAX_MEMBER_TAG

  val path = ParsedProtocolSchema.toEmbeddedPath(type)
  val schema = classLoader
    .getResourceAsStream(path)
    ?.use(InputStream::readBytes)
    ?.decodeToString()
    ?.let { json -> ParsedProtocolSchema.parseEmbeddedJson(json, tagOffset) }
    ?: throw IllegalArgumentException("Unable to locate JSON for $type at $path")

  require(tag == 0 || schema.dependencies.isEmpty()) {
    "Schema dependency $type also has its own dependencies. " +
      "For now, only a single level of dependencies is supported."
  }

  return schema
}

/** Returns true if [memberType] is a known special modifier tag and name. */
internal fun isSpecialModifier(tag: Int, memberType: FqType): Boolean {
  return tag == -4_543_827 && memberType.names.last() == "Reuse"
}
