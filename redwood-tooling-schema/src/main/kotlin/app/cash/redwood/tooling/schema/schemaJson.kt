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

import app.cash.redwood.tooling.schema.ProtocolWidget.ProtocolChildren
import app.cash.redwood.tooling.schema.ProtocolWidget.ProtocolEvent
import app.cash.redwood.tooling.schema.ProtocolWidget.ProtocolProperty
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
    type = type,
    scopes = scopes,
    widgets = widgets.map { widget ->
      WidgetJson(
        type = widget.type,
        traits = widget.traits.map { trait ->
          when (trait) {
            is ProtocolProperty -> {
              PropertyJson(
                tag = trait.tag,
                name = trait.name,
                type = trait.type,
                defaultExpression = trait.defaultExpression,
              )
            }
            is ProtocolEvent -> {
              EventJson(
                tag = trait.tag,
                name = trait.name,
                parameterType = trait.parameterType,
                defaultExpression = trait.defaultExpression,
              )
            }
            is ProtocolChildren -> {
              ChildrenJson(
                tag = trait.tag,
                name = trait.name,
                scope = trait.scope,
              )
            }
          }
        },
      )
    },
    layoutModifiers = layoutModifiers.map { layoutModifier ->
      LayoutModifierJson(
        tag = layoutModifier.tag,
        type = layoutModifier.type,
        scopes = layoutModifier.scopes,
        properties = layoutModifier.properties.map {
          LayoutModifierPropertyJson(
            name = it.name,
            type = it.type,
            defaultExpression = it.defaultExpression,
          )
        },
      )
    },
    dependencies = dependencies.map { it.type },
  )
  val json = json.encodeToString(SchemaJsonV1.serializer(), schemaJson)

  return EmbeddedSchema(
    path = type.names[0].replace('.', '/') + "/" + type.names.drop(1).joinToString(".") + ".json",
    json = json,
  )
}

private val json = Json {
  prettyPrint = true
  prettyPrintIndent = "\t"
  classDiscriminator = "kind"
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
