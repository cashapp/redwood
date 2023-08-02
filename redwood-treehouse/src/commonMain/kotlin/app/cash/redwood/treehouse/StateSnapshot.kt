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
package app.cash.redwood.treehouse

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import kotlin.jvm.JvmInline
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull

@Serializable
public class StateSnapshot(
  public val content: Map<String, List<JsonElement?>>,
) {
  public fun toValuesMap(): Map<String, List<Any?>>? {
    return content.mapValues { entry ->
      entry.value.map { mutableStateOf(it.fromJsonElement()) }
    }
  }

  public fun canBeSaved(): Boolean {
    return try {
      // Test if the content is serializable
      toValuesMap()
      true
    } catch (t: IllegalStateException) {
      false
    }
  }

  @JvmInline
  @Serializable
  public value class Id(public val value: String?)
}

/**
 * Supported types:
 * String, Boolean, Int, List (of supported primitive types), Map (of supported primitive types)
 */
public fun Map<String, List<Any?>>.toStateSnapshot(): StateSnapshot? {
  val map = mutableMapOf<String, List<JsonElement?>>()
  this.forEach { entry ->
    val list = entry.value.map { element ->
      when (element) {
        is MutableState<*> -> {
          element.value.toJsonElement()
        }

        else -> error("unexpected type: $this")
      }
    }
    map[entry.key] = list
  }
  return StateSnapshot(map)
}

private fun Any?.toJsonElement(): JsonElement {
  return when (this) {
    is String -> JsonPrimitive(this)
    is List<*> -> JsonArray(map { it.toJsonElement() })
    is JsonElement -> this
    else -> error("unexpected type: $this")
    // TODO: add support to Map<*, *>
  }
}

private fun JsonElement?.fromJsonElement(): Any {
  return when (this) {
    is JsonPrimitive -> {
      if (this.isString) {
        return content
      }
      return booleanOrNull ?: doubleOrNull ?: error("unexpected type: $this")
      // TODO add other primitive types (double, float, long) when needed
    }

    is JsonArray -> listOf({ this.forEach { it.toJsonElement() } })
    // TODO: map, numbers
    // is Map<*, *> -> JsonElement
    else -> error("unexpected type: $this")
  }
}
