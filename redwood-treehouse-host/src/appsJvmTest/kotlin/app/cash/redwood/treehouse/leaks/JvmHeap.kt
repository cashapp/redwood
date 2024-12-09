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
package app.cash.redwood.treehouse.leaks

import androidx.collection.IntObjectMap
import androidx.collection.ScatterSet
import app.cash.redwood.treehouse.EventLog
import com.example.redwood.testapp.treehouse.HostApi
import java.lang.ref.WeakReference
import java.lang.reflect.Field
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule

/**
 * Inspect the current process heap using reflection.
 *
 * This is not a general-purpose heap API. It is specifically targeted to finding reference cycles
 * and ignores types that do not participate in these.
 *
 * This attempts to avoid traversing into implementation details of platform types and library
 * types that are typically reachable in the tests that use it. Keeping the list of known types
 * up-to-date is simple and manual and potentially toilsome.
 */
internal object JvmHeap : Heap {
  /** Attempt to avoid computing each type's declared fields on every single instance. */
  private val classToFields = mutableMapOf<Class<*>, List<Field>>()

  override fun references(instance: Any): List<Edge> {
    val javaClass = instance::class.java
    val javaPackageName = javaClass.`package`?.name ?: "?"

    return when {
      // Collection-like types reference their contents. Note that this doesn't consider Redwood's
      // own collection implementations like Widget.Children, as these may have additional fields
      // that we must include.
      javaClass.isArray -> {
        when (instance) {
          is Array<*> -> references(instance.toList())
          else -> listOf() // Primitive array.
        }
      }
      instance is Collection<*> && javaPackageName.isDescendant("java", "kotlin", "androidx") -> {
        instance.mapIndexed { index, value -> Edge("[$index]", value) }
      }
      instance is Map<*, *> && javaPackageName.isDescendant("java", "kotlin", "androidx") -> {
        references(instance.entries)
      }
      instance is Map.Entry<*, *> && javaPackageName.isDescendant("java", "kotlin", "androidx") -> {
        listOf(
          Edge("key", instance.key),
          Edge("value", instance.value),
        )
      }
      instance is IntObjectMap<*> -> {
        references(buildList { instance.forEachValue(::add) })
      }
      instance is ScatterSet<*> -> {
        references(instance.asSet())
      }
      instance is StateFlow<*> -> listOf(
        Edge("value", instance.value),
      )

      // Don't traverse further on types that are unlikely to contain application-scoped data.
      // We want to avoid loading the entire application heap into memory!
      instance is Class<*> -> listOf()
      instance is CoroutineDispatcher -> listOf()
      instance is Enum<*> -> listOf()
      instance is EventLog -> listOf()
      instance is HostApi -> listOf()
      instance is Int -> listOf()
      instance is Job -> listOf()
      instance is Json -> listOf()
      instance is KSerializer<*> -> listOf()
      instance is SerializersModule -> listOf()
      instance is String -> listOf()
      instance is WeakReference<*> -> listOf()

      // Explore everything else by reflecting on its fields.
      javaPackageName.isDescendant(
        "app.cash",
        "com.example",
        "kotlin",
        "kotlinx.coroutines",
        "okio",
      ) -> {
        fields(javaClass).map { field -> Edge(field.name, field.get(instance)) }
      }

      else -> error("unexpected class needs to be added to JvmHeap.kt: $javaClass")
    }
  }

  /**
   * Returns true if this package is a descendant of any prefix. For example, the descendants of
   * 'kotlin' are 'kotlin.collections' and 'kotlin' itself, but not 'kotlinx.coroutines'.
   */
  private fun String.isDescendant(vararg prefixes: String) =
    prefixes.any { prefix ->
      startsWith(prefix) && (length == prefix.length || this[prefix.length] == '.')
    }

  private fun fields(type: Class<*>): List<Field> {
    return classToFields.getOrPut(type) {
      buildList {
        for (supertype in type.supertypes) {
          for (field in supertype.declaredFields) {
            if (field.type.isPrimitive) continue // Ignore primitive fields.
            try {
              field.isAccessible = true
            } catch (e: Exception) {
              throw Exception("failed to set $type.${field.name} accessible", e)
            }
            add(field)
          }
        }
      }
    }
  }

  private val Class<*>.supertypes: Sequence<Class<*>>
    get() = generateSequence(this) { it.superclass }
}
