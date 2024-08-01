/*
 * Copyright (C) 2024 Square, Inc.
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

import java.io.File
import kotlin.reflect.KClass

/**
 * Parse the [ProtocolSchemaSet] of [type] using the following properties:
 * * `java.home` - Path to the executing JVM (automatically set).
 * * `redwood.internal.sources` - Path-separated list of source files.
 * * `redwood.internal.classpath` - Path-separated list of classpath dependencies.
 */
fun parseTestSchema(type: KClass<*>): ProtocolSchemaSet {
  val jdkHome = System.getProperty("java.home").let(::File)
  val sources = System.getProperty("redwood.internal.sources")
    .split(File.pathSeparator)
    .map(::File)
    .filter(File::exists) // Entries that don't exist produce warnings.
  val classpath = System.getProperty("redwood.internal.classpath")
    .split(File.pathSeparator)
    .map(::File)
    .filter(File::exists) // Entries that don't exist produce warnings.
  return parseProtocolSchema(jdkHome, sources, classpath, type.toFqType())
}
