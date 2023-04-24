/*
 * Copyright (C) 2022 Square, Inc.
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

import app.cash.redwood.tooling.schema.Deprecation.Level
import app.cash.redwood.tooling.schema.LayoutModifier.Property
import app.cash.redwood.tooling.schema.ProtocolWidget.ProtocolChildren
import app.cash.redwood.tooling.schema.ProtocolWidget.ProtocolEvent
import app.cash.redwood.tooling.schema.ProtocolWidget.ProtocolProperty
import app.cash.redwood.tooling.schema.ProtocolWidget.ProtocolTrait
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.EncodeDefault.Mode.ALWAYS
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

internal data class ParsedProtocolSchemaSet(
  override val schema: ProtocolSchema,
  override val dependencies: Map<FqType, ProtocolSchema>,
) : ProtocolSchemaSet

@Serializable
internal data class ParsedProtocolSchema(
  /** The format version of this JSON. */
  @OptIn(ExperimentalSerializationApi::class) // Easier than second constructor setting fixed value.
  @EncodeDefault(ALWAYS)
  val version: Int = 1,
  override val type: FqType,
  override val scopes: List<FqType> = emptyList(),
  override val widgets: List<ParsedProtocolWidget> = emptyList(),
  override val layoutModifiers: List<ParsedProtocolLayoutModifier> = emptyList(),
  @SerialName("dependencies")
  override val taggedDependencies: Map<Int, FqType> = emptyMap(),
) : ProtocolSchema {
  init {
    require(version == 1) {
      "Only version 1 is supported"
    }
  }

  override fun toEmbeddedSchema(): EmbeddedSchema {
    return EmbeddedSchema(
      path = toEmbeddedPath(type),
      json = jsonFormat.encodeToString(serializer(), this),
    )
  }

  companion object {
    fun toEmbeddedPath(type: FqType): String {
      return type.names[0].replace('.', '/') + "/" + type.names.drop(1).joinToString(".") + ".json"
    }

    fun parseEmbeddedJson(json: String, tagOffset: Int = 0): ProtocolSchema {
      val schema = jsonFormat.decodeFromString(serializer(), json)
      if (tagOffset == 0) {
        return schema
      }
      // The schema JSON was generated locally from at its source with its normal tag numbers.
      // If we are consuming it as a tagged dependency, offset the widget and modifier tag numbers
      // to simplify usage downstream.
      return schema.copy(
        widgets = schema.widgets.map { widget ->
          widget.copy(tag = tagOffset + widget.tag)
        },
        layoutModifiers = schema.layoutModifiers.map { layoutModifier ->
          layoutModifier.copy(tag = tagOffset + layoutModifier.tag)
        },
      )
    }

    @OptIn(ExperimentalSerializationApi::class) // For custom indent, which is only a nice-to-have.
    private val jsonFormat = Json {
      prettyPrint = true
      prettyPrintIndent = "\t"
      classDiscriminator = "kind"
      serializersModule = SerializersModule {
        polymorphic(ProtocolTrait::class) {
          subclass(ParsedProtocolChildren::class)
          subclass(ParsedProtocolEvent::class)
          subclass(ParsedProtocolProperty::class)
        }
      }
    }
  }
}

@Serializable
internal data class ParsedDeprecation(
  override val level: Level,
  override val message: String,
) : Deprecation

@Serializable
internal data class ParsedProtocolWidget(
  override val tag: Int,
  override val type: FqType,
  override val deprecation: ParsedDeprecation? = null,
  override val traits: List<ProtocolTrait> = emptyList(),
) : ProtocolWidget

@Serializable
@SerialName("property")
internal data class ParsedProtocolProperty(
  override val tag: Int,
  override val name: String,
  override val type: FqType,
  override val defaultExpression: String? = null,
  override val deprecation: ParsedDeprecation? = null,
) : ProtocolProperty

@Serializable
@SerialName("event")
internal data class ParsedProtocolEvent(
  override val tag: Int,
  override val name: String,
  override val parameterType: FqType?,
  override val isNullable: Boolean,
  override val defaultExpression: String? = null,
  override val deprecation: ParsedDeprecation? = null,
) : ProtocolEvent

@Serializable
@SerialName("children")
internal data class ParsedProtocolChildren(
  override val tag: Int,
  override val name: String,
  override val scope: FqType? = null,
  override val defaultExpression: String? = null,
  override val deprecation: ParsedDeprecation? = null,
) : ProtocolChildren

@Serializable
internal data class ParsedProtocolLayoutModifier(
  override val tag: Int,
  override val scopes: List<FqType>,
  override val type: FqType,
  override val deprecation: ParsedDeprecation? = null,
  override val properties: List<ParsedProtocolLayoutModifierProperty> = emptyList(),
) : ProtocolLayoutModifier

@Serializable
internal data class ParsedProtocolLayoutModifierProperty(
  override val name: String,
  override val type: FqType,
  override val isSerializable: Boolean,
  override val defaultExpression: String? = null,
  override val deprecation: ParsedDeprecation? = null,
) : Property
