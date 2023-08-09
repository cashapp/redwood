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

import dev.drewhamilton.poko.Poko
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
@Poko
public class Event(
  /** Identifier for the widget from which this event originated. */
  public val id: Id,
  /** Identifies which event occurred on the widget with [id]. */
  public val tag: EventTag,
  public val args: List<JsonElement> = emptyList(),
)

@Serializable
public sealed interface Change {
  /** Identifier for the widget which is the subject of this change. */
  public val id: Id
}

@Serializable
@SerialName("create")
@Poko
public class Create(
  override val id: Id,
  public val tag: WidgetTag,
) : Change

public sealed interface ValueChange : Change

@Serializable
@SerialName("property")
@Poko
public class PropertyChange(
  override val id: Id,
  /** Identifies which property changed on the widget with [id]. */
  public val tag: PropertyTag,
  public val value: JsonElement = JsonNull,
) : ValueChange

@Serializable
@SerialName("modifier")
@Poko
public class ModifierChange(
  override val id: Id,
  public val elements: List<ModifierElement> = emptyList(),
) : ValueChange

@Serializable(with = ModifierElementSerializer::class)
@Poko
public class ModifierElement(
  public val tag: ModifierTag,
  public val value: JsonElement = DefaultValue,
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
  @Poko
  public class Add(
    override val id: Id,
    override val tag: ChildrenTag,
    public val childId: Id,
    public val index: Int,
  ) : ChildrenChange

  @Serializable
  @SerialName("move")
  @Poko
  public class Move(
    override val id: Id,
    override val tag: ChildrenTag,
    public val fromIndex: Int,
    public val toIndex: Int,
    public val count: Int,
  ) : ChildrenChange

  @Serializable
  @SerialName("remove")
  @Poko
  public class Remove(
    override val id: Id,
    override val tag: ChildrenTag,
    public val index: Int,
    public val count: Int,
    public val removedIds: List<Id>,
  ) : ChildrenChange {
    init {
      require(count == removedIds.size) {
        "Count $count != Removed ID list size ${removedIds.size}"
      }
    }
  }
}
