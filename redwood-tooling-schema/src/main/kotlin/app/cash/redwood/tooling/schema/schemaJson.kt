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
@file:OptIn(ExperimentalSerializationApi::class) // Tooling use only.

package app.cash.redwood.tooling.schema

import app.cash.redwood.tooling.schema.FqType.Variance.In
import app.cash.redwood.tooling.schema.FqType.Variance.Invariant
import app.cash.redwood.tooling.schema.FqType.Variance.Out
import app.cash.redwood.tooling.schema.ProtocolWidget.ProtocolChildren
import app.cash.redwood.tooling.schema.ProtocolWidget.ProtocolEvent
import app.cash.redwood.tooling.schema.ProtocolWidget.ProtocolProperty
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection
import kotlin.reflect.KVariance.IN
import kotlin.reflect.KVariance.INVARIANT
import kotlin.reflect.KVariance.OUT
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.EncodeDefault.Mode.ALWAYS
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

public data class EmbeddedSchema(
  val path: String,
  val json: String,
)

/**
 * Convert this schema to JSON which can be embedded inside the schema artifact.
 * This JSON will be read when the schema is used as a dependency.
 */
public fun ProtocolSchema.toEmbeddedSchema(): EmbeddedSchema {
  val schemaJson = SchemaJsonV1(
    type = FqType(listOf(`package`, name)),
    scopes = scopes.map { it.toFqType() },
    widgets = widgets.map { widget ->
      WidgetJson(
        type = widget.type.toFqType(),
        traits = widget.traits.map { trait ->
          when (trait) {
            is ProtocolProperty -> {
              PropertyJson(
                tag = trait.tag,
                name = trait.name,
                type = trait.type.toFqType(),
                defaultExpression = trait.defaultExpression,
              )
            }
            is ProtocolEvent -> {
              EventJson(
                tag = trait.tag,
                name = trait.name,
                parameterType = trait.parameterType?.toFqType(),
                defaultExpression = trait.defaultExpression,
              )
            }
            is ProtocolChildren -> {
              ChildrenJson(
                tag = trait.tag,
                name = trait.name,
                scope = trait.scope?.toFqType(),
              )
            }
          }
        },
      )
    },
    layoutModifiers = layoutModifiers.map { layoutModifier ->
      LayoutModifierJson(
        tag = layoutModifier.tag,
        type = layoutModifier.type.toFqType(),
        scopes = layoutModifier.scopes.map { it.toFqType() },
        properties = layoutModifier.properties.map {
          LayoutModifierPropertyJson(
            name = it.name,
            type = it.type.toFqType(),
            defaultExpression = it.defaultExpression,
          )
        },
      )
    },
    dependencies = dependencies.map {
      FqType(listOf(it.`package`, it.name))
    },
  )
  val json = json.encodeToString(SchemaJsonV1.serializer(), schemaJson)

  return EmbeddedSchema(
    path = "${`package`.replace('.', '/')}/$name.json",
    json = json,
  )
}

private val json = Json {
  prettyPrint = true
  prettyPrintIndent = "\t"
  classDiscriminator = "kind"
}

@Serializable
private data class FqType(
  /**
   * The package name followed by one or more simple names. The package may be empty.
   * A wildcard is represented by an empty package and a single "*" simple name.
   */
  val names: List<String>,
  val variance: Variance = Invariant,
  val parameterTypes: List<FqType> = emptyList(),
  val nullable: Boolean = false,
) {
  enum class Variance {
    Invariant,

    @SerialName("in")
    In,

    @SerialName("out")
    Out,
  }

  override fun toString(): String = buildString {
    when (variance) {
      Invariant -> Unit // Nothing to do.
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
}

private fun KClass<*>.toFqType(): FqType {
  var qualifiedName = qualifiedName ?: throw AssertionError(this)
  val simpleNames = ArrayDeque<String>()

  var target: Class<*>? = java
  while (target != null) {
    target = target.enclosingClass

    val dot = qualifiedName.lastIndexOf('.')
    if (dot == -1) {
      if (target != null) throw AssertionError(this) // More enclosing classes than dots.
      simpleNames.addFirst(qualifiedName)
      qualifiedName = ""
    } else {
      simpleNames.addFirst(qualifiedName.substring(dot + 1))
      qualifiedName = qualifiedName.substring(0, dot)
    }
  }

  simpleNames.addFirst(qualifiedName)

  return FqType(simpleNames)
}

private fun KType.toFqType(): FqType {
  val kClass = classifier as? KClass<*>
    ?: throw IllegalArgumentException("$this classifier must be a class")

  return kClass.toFqType().copy(
    parameterTypes = arguments.map { it.toFqType() },
    nullable = isMarkedNullable,
  )
}

private fun KTypeProjection.toFqType(): FqType {
  val json = type?.toFqType() ?: FqType(names = listOf("*"))
  return when (variance) {
    null, INVARIANT -> json
    IN -> json.copy(variance = In)
    OUT -> json.copy(variance = Out)
  }
}

@Serializable
private data class SchemaJsonV1(
  /** The format version of this JSON. */
  @EncodeDefault(ALWAYS)
  val version: Int = 1,
  val type: FqType,
  val scopes: List<FqType> = emptyList(),
  val widgets: List<WidgetJson> = emptyList(),
  val layoutModifiers: List<LayoutModifierJson> = emptyList(),
  val dependencies: List<FqType> = emptyList(),
) {
  init {
    require(version == 1) {
      "Only version 1 is supported"
    }
  }
}

@Serializable
private data class WidgetJson(
  val type: FqType,
  val traits: List<TraitJson> = emptyList(),
)

@Serializable
private sealed interface TraitJson {
  val tag: Int?
  val name: String
  val defaultExpression: String?
}

@Serializable
@SerialName("property")
private data class PropertyJson(
  override val tag: Int? = null,
  override val name: String,
  val type: FqType,
  override val defaultExpression: String? = null,
) : TraitJson

@Serializable
@SerialName("event")
private data class EventJson(
  override val tag: Int? = null,
  override val name: String,
  val parameterType: FqType? = null,
  override val defaultExpression: String? = null,
) : TraitJson

@Serializable
@SerialName("children")
private data class ChildrenJson(
  override val tag: Int? = null,
  override val name: String,
  val scope: FqType? = null,
  override val defaultExpression: String? = null,
) : TraitJson

@Serializable
private data class LayoutModifierJson(
  val tag: Int? = null,
  val type: FqType,
  val scopes: List<FqType>,
  val properties: List<LayoutModifierPropertyJson> = emptyList(),
)

@Serializable
private data class LayoutModifierPropertyJson(
  val name: String,
  val type: FqType,
  val defaultExpression: String? = null,
)
