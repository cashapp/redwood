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

import app.cash.redwood.tooling.schema.FqType.Variance.In
import app.cash.redwood.tooling.schema.FqType.Variance.Invariant
import app.cash.redwood.tooling.schema.FqType.Variance.Out
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection
import kotlin.reflect.KVariance.IN
import kotlin.reflect.KVariance.INVARIANT
import kotlin.reflect.KVariance.OUT
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class FqType(
  /**
   * The package name followed by one or more simple names. The package may be empty.
   * A star projection is represented by an empty package and a single "*" simple name.
   */
  val names: List<String>,
  val variance: Variance = Invariant,
  val parameterTypes: List<FqType> = emptyList(),
  val nullable: Boolean = false,
) {
  init {
    require(names.size >= 2) {
      "At least two names are required: package and a simple name: $names"
    }
    if (names[1] == "*") {
      require(names[0] == "") {
        "Star projection must use empty package name: ${names[0]}"
      }
      require(names.size == 2) {
        "Star projection cannot have nested types: ${names.drop(1)}"
      }
      require(variance == Invariant) {
        "Star projection must be Invariant: $variance"
      }
      require(parameterTypes.isEmpty()) {
        "Star projection must not have parameter types: $parameterTypes"
      }
      require(!nullable) {
        "Star projection must not be nullable"
      }
    }
  }

  public enum class Variance {
    Invariant,

    @SerialName("in")
    In,

    @SerialName("out")
    Out,
  }

  override fun toString(): String = buildString {
    when (variance) {
      // Nothing to do.
      Invariant -> Unit

      In -> append("in ")

      Out -> append("out ")
    }
    if (names[0] != "") {
      append(names[0])
      append('.')
    }
    append(names[1])
    for (i in 2 until names.size) {
      append('.')
      append(names[i])
    }
    if (parameterTypes.isNotEmpty()) {
      append('<')
      parameterTypes.joinTo(this, separator = ", ")
      append('>')
    }
    if (nullable) {
      append('?')
    }
  }

  public companion object {
    public val Star: FqType = FqType(listOf("", "*"))

    public fun bestGuess(name: String): FqType {
      val names = mutableListOf<String>()

      // Add the package name, like "java.util.concurrent", or "" for no package.
      var p = 0
      while (p < name.length && Character.isLowerCase(name.codePointAt(p))) {
        p = name.indexOf('.', p) + 1
        require(p != 0) { "Couldn't guess: $name" }
      }
      names += if (p != 0) name.substring(0, p - 1) else ""

      // Add the class names, like "Map" and "Entry".
      for (part in name.substring(p).split('.')) {
        require(part.isNotEmpty() && Character.isUpperCase(part.codePointAt(0))) {
          "Couldn't guess: $name"
        }
        names += part
      }

      require(names.size >= 2) { "couldn't make a guess for $name" }
      return FqType(names)
    }
  }
}

internal fun KClass<*>.toFqType(): FqType {
  // We only parse public API declarations on which it should be impossible to use
  // function-local classes or anonymous classes.
  var qualifiedName = qualifiedName ?: throw AssertionError(this)

  // First, check for Kotlin types whose enclosing class name is a type that is mapped to a JVM
  // class. Thus, the class backing the nested Kotlin type does not have an enclosing class
  // (i.e., a parent) and the normal algorithm will fail.
  val names = when (qualifiedName) {
    "kotlin.Boolean.Companion" -> listOf("kotlin", "Boolean", "Companion")

    "kotlin.Byte.Companion" -> listOf("kotlin", "Byte", "Companion")

    "kotlin.Char.Companion" -> listOf("kotlin", "Char", "Companion")

    "kotlin.Double.Companion" -> listOf("kotlin", "Double", "Companion")

    "kotlin.Enum.Companion" -> listOf("kotlin", "Enum", "Companion")

    "kotlin.Float.Companion" -> listOf("kotlin", "Float", "Companion")

    "kotlin.Int.Companion" -> listOf("kotlin", "Int", "Companion")

    "kotlin.Long.Companion" -> listOf("kotlin", "Long", "Companion")

    "kotlin.Short.Companion" -> listOf("kotlin", "Short", "Companion")

    "kotlin.String.Companion" -> listOf("kotlin", "String", "Companion")

    else -> {
      val names = ArrayDeque<String>()
      var target: Class<*>? = java
      while (target != null) {
        target = target.enclosingClass

        val dot = qualifiedName.lastIndexOf('.')
        if (dot == -1) {
          if (target != null) throw AssertionError(this) // More enclosing classes than dots.
          names.addFirst(qualifiedName)
          qualifiedName = ""
        } else {
          names.addFirst(qualifiedName.substring(dot + 1))
          qualifiedName = qualifiedName.substring(0, dot)
        }
      }

      names.addFirst(qualifiedName)
      names.toList()
    }
  }

  return FqType(names)
}

internal fun KType.toFqType(): FqType {
  val kClass = classifier as? KClass<*>
    ?: throw IllegalArgumentException("$this classifier must be a class")

  return kClass.toFqType().copy(
    parameterTypes = arguments.map { it.toFqType() },
    nullable = isMarkedNullable,
  )
}

private fun KTypeProjection.toFqType(): FqType {
  val json = type?.toFqType() ?: FqType.Star
  return when (variance) {
    null, INVARIANT -> json
    IN -> json.copy(variance = In)
    OUT -> json.copy(variance = Out)
  }
}
