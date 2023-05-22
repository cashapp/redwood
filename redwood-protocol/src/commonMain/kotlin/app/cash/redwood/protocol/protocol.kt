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
package app.cash.redwood.protocol

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

@Serializable
public data class Event(
  /** Identifier for the widget from which this event originated. */
  val id: Id,
  /** Identifies which event occurred on the widget with [id]. */
  val tag: EventTag,
  val args: List<JsonElement> = emptyList(),
)

@Serializable
public sealed interface Change {
  /** Identifier for the widget which is the subject of this change. */
  public val id: Id
}

@Serializable
@SerialName("create")
public data class Create(
  override val id: Id,
  val tag: WidgetTag,
) : Change

public sealed interface ValueChange : Change

@Serializable
@SerialName("property")
public data class PropertyChange(
  override val id: Id,
  /** Identifies which property changed on the widget with [id]. */
  val tag: PropertyTag,
  val value: JsonElement = JsonNull,
) : ValueChange

@Serializable
@SerialName("modifier")
public data class ModifierChange(
  override val id: Id,
  val elements: List<ModifierElement> = emptyList(),
) : ValueChange

@Serializable(with = ModifierElementSerializer::class)
public data class ModifierElement(
  val tag: ModifierTag,
  val value: JsonElement = DefaultValue,
) {
  internal companion object {
    val DefaultValue get() = JsonNull
  }
}

private object ModifierElementSerializer : KSerializer<ModifierElement> {
  override val descriptor = buildClassSerialDescriptor("ModifierElement") {
    element<ModifierTag>("tag")
    element<JsonElement>("value")
  }

  override fun deserialize(decoder: Decoder): ModifierElement {
    check(decoder is JsonDecoder) { "Can be deserialized only by JSON" }
    val decoded = decoder.decodeJsonElement().jsonArray
    check(decoded.size in 1..2) {
      "ModifierElement array may only have 1 or 2 values. Found: ${decoded.size}"
    }
    val tag = ModifierTag(decoded[0].jsonPrimitive.content.toInt())
    val value = decoded.getOrElse(1) { ModifierElement.DefaultValue }
    return ModifierElement(tag, value)
  }

  override fun serialize(encoder: Encoder, value: ModifierElement) {
    check(encoder is JsonEncoder) { "Can be serialized only by JSON" }
    encoder.encodeJsonElement(
      buildJsonArray {
        add(JsonPrimitive(value.tag.value))
        if (value.value != ModifierElement.DefaultValue) {
          add(value.value)
        }
      },
    )
  }
}

@Serializable
public sealed interface ChildrenChange : Change {
  /** Identifies which group of children changed on the widget with [id]. */
  public val tag: ChildrenTag

  @Serializable
  @SerialName("add")
  public data class Add(
    override val id: Id,
    override val tag: ChildrenTag,
    val childId: Id,
    val index: Int,
  ) : ChildrenChange

  @Serializable
  @SerialName("move")
  public data class Move(
    override val id: Id,
    override val tag: ChildrenTag,
    val fromIndex: Int,
    val toIndex: Int,
    val count: Int,
  ) : ChildrenChange

  @Serializable
  @SerialName("remove")
  public data class Remove(
    override val id: Id,
    override val tag: ChildrenTag,
    val index: Int,
    val count: Int,
    val removedIds: List<Id>,
  ) : ChildrenChange {
    init {
      require(count == removedIds.size) {
        "Count $count != Removed ID list size ${removedIds.size}"
      }
    }
  }
}
