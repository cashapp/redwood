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

package app.cash.treehouse.schema.generator

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import kotlin.reflect.KType

/**
 * Returns a code block that decodes a JsonElement expression.
 *
 * ```
 * Json.encodeToJsonElement(String.serializer(), expression)
 * ```
 */
internal fun TypeName.jsonEncode(format: String, vararg args: Any?): CodeBlock {
  return CodeBlock.of(
    "%M(%L, %L)",
    encodeToJsonElement,
    serializer,
    CodeBlock.of(format, *args)
  )
}

internal fun KType.jsonEncode(format: String, vararg args: Any?) =
  asTypeName().jsonEncode(format, *args)

/**
 * Returns a code block that encodes an expression as a JsonElement.
 *
 * ```
 * Json.decodeFromJsonElement(String.serializer(), diff.value)
 * ```
 */
internal fun TypeName.jsonDecode(format: String, vararg args: Any?): CodeBlock {
  return CodeBlock.of(
    "%M(%L, %L)",
    decodeFromJsonElement,
    serializer,
    CodeBlock.of(format, *args)
  )
}

internal fun KType.jsonDecode(format: String, vararg args: Any?) =
  asTypeName().jsonDecode(format, *args)

/**
 * Returns a code block for the JSON serializer of this type.
 *
 * ```
 * String.serializer().nullable
 * ```
 */
internal val TypeName.serializer: CodeBlock
  get() {
    val result = CodeBlock.builder()
    result.add(
      "%T.%M()",
      copy(nullable = false),
      MemberName("kotlinx.serialization.builtins", "serializer")
    )
    if (isNullable) {
      result.add(".%M", MemberName("kotlinx.serialization.builtins", "nullable"))
    }
    return result.build()
  }
