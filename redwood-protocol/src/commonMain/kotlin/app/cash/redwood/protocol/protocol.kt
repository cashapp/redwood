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

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.serializer

@Serializable
public data class Event(
  /** Identifier for the widget from which this event originated. */
  val id: Id,
  /** Identifies which event occurred on the widget with [id]. */
  val tag: EventTag,
  val value: JsonElement = JsonNull,
)

@Serializable
public data class Diff(
  val childrenDiffs: List<ChildrenDiff> = emptyList(),
  val layoutModifiers: List<LayoutModifiers> = emptyList(),
  val propertyDiffs: List<PropertyDiff> = emptyList(),
)

@Serializable
public data class PropertyDiff(
  /** Identifier for the widget whose property has changed. */
  val id: Id,
  /** Identifies which property changed on the widget with [id]. */
  val tag: PropertyTag,
  val value: JsonElement = JsonNull,
)

@Serializable
public data class LayoutModifiers(
  /** Identifier for the widget whose layout modifier has changed. */
  val id: Id,
  val elements: List<LayoutModifierElement> = emptyList(),
)

@Serializable(with = LayoutModifierElementSerializer::class)
public data class LayoutModifierElement(
  val tag: LayoutModifierTag,
  val value: JsonElement = DefaultValue,
) {
  internal companion object {
    val DefaultValue get() = JsonNull
  }
}

private object LayoutModifierElementSerializer : KSerializer<LayoutModifierElement> {
  private val delegate = serializer<Array<JsonElement>>()

  @OptIn(ExperimentalSerializationApi::class)
  override val descriptor = SerialDescriptor("LayoutModifierElement", delegate.descriptor)

  override fun deserialize(decoder: Decoder): LayoutModifierElement {
    val decoded = decoder.decodeSerializableValue(delegate)
    check(decoded.size in 1..2) {
      "LayoutModifierElement array may only have 1 or 2 values. Found: ${decoded.size}"
    }
    val tag = LayoutModifierTag(decoded[0].jsonPrimitive.content.toInt())
    val value = decoded.getOrElse(1) { LayoutModifierElement.DefaultValue }
    return LayoutModifierElement(tag, value)
  }

  override fun serialize(encoder: Encoder, value: LayoutModifierElement) {
    encoder.beginStructure(delegate.descriptor).apply {
      encodeIntElement(Int.serializer().descriptor, 0, value.tag.value)
      if (value.value != LayoutModifierElement.DefaultValue) {
        encodeSerializableElement(
          JsonElement.serializer().descriptor,
          1,
          JsonElement.serializer(),
          value.value,
        )
      }
      endStructure(delegate.descriptor)
    }
  }
}

@Serializable
public sealed class ChildrenDiff {
  /** Identifier for the widget whose children have changed. */
  public abstract val id: Id

  /** Identifies which group of children changed on the widget with [id]. */
  public abstract val tag: ChildrenTag

  @Serializable
  @SerialName("insert")
  public data class Insert(
    override val id: Id,
    override val tag: ChildrenTag,
    val childId: Id,
    val widgetTag: WidgetTag,
    val index: Int,
  ) : ChildrenDiff()

  @Serializable
  @SerialName("move")
  public data class Move(
    override val id: Id,
    override val tag: ChildrenTag,
    val fromIndex: Int,
    val toIndex: Int,
    val count: Int,
  ) : ChildrenDiff()

  @Serializable
  @SerialName("remove")
  public data class Remove(
    override val id: Id,
    override val tag: ChildrenTag,
    val index: Int,
    val count: Int,
  ) : ChildrenDiff()
}
