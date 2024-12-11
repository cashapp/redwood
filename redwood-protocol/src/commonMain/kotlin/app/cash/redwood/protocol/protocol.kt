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

/*
Note: The types in this file are written in a careful way such that their serializable properties
are scalars which can be serialized directly rather than using value classes are boxed within the
generated kotlinx.serialization code.
*/

@Serializable
@Poko
public class Event private constructor(
  @SerialName("id")
  private val _id: Int,
  @SerialName("tag")
  private val _tag: Int,
  public val args: List<JsonElement> = emptyList(),
) {
  /** Identifier for the widget from which this event originated. */
  public val id: Id get() = Id(_id)

  /** Identifies which event occurred on the widget with [id]. */
  public val tag: EventTag get() = EventTag(_tag)

  public companion object {
    public operator fun invoke(
      id: Id,
      tag: EventTag,
      args: List<JsonElement> = emptyList(),
    ): Event = Event(id.value, tag.value, args)
  }
}

@Serializable
public sealed interface Change {
  /** Identifier for the widget which is the subject of this change. */
  public val id: Id
}

@Serializable
@SerialName("create")
@Poko
public class Create private constructor(
  @SerialName("id")
  private val _id: Int,
  @SerialName("tag")
  private val _tag: Int,
) : Change {
  override val id: Id get() = Id(_id)
  public val tag: WidgetTag get() = WidgetTag(_tag)

  public companion object {
    public operator fun invoke(
      id: Id,
      tag: WidgetTag,
    ): Create = Create(id.value, tag.value)
  }
}

public sealed interface ValueChange : Change

@Serializable
@SerialName("property")
@Poko
public class PropertyChange private constructor(
  @SerialName("id")
  private val _id: Int,
  @SerialName("widget")
  private val _widgetTag: Int = -1,
  @SerialName("tag")
  private val _tag: Int,
  public val value: JsonElement = JsonNull,
) : ValueChange {
  override val id: Id get() = Id(_id)

  /** Identifies the widget on which the property changed. */
  public val widgetTag: WidgetTag get() = WidgetTag(_widgetTag)

  /** Identifies which property changed on the widget. */
  public val propertyTag: PropertyTag get() = PropertyTag(_tag)

  public companion object {
    public operator fun invoke(
      id: Id,
      /** Identifies the widget on which the property changed. */
      widgetTag: WidgetTag,
      /** Identifies which property changed on the widget. */
      propertyTag: PropertyTag,
      value: JsonElement = JsonNull,
    ): PropertyChange = PropertyChange(id.value, widgetTag.value, propertyTag.value, value)
  }
}

@Serializable
@SerialName("modifier")
@Poko
public class ModifierChange private constructor(
  @SerialName("id")
  private val _id: Int,
  public val elements: List<ModifierElement> = emptyList(),
) : ValueChange {
  override val id: Id get() = Id(_id)

  public companion object {
    public operator fun invoke(
      id: Id,
      elements: List<ModifierElement> = emptyList(),
    ): ModifierChange = ModifierChange(id.value, elements)
  }
}

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
  public class Add private constructor(
    @SerialName("id")
    private val _id: Int,
    @SerialName("tag")
    private val _tag: Int,
    @SerialName("childId")
    private val _childId: Int,
    public val index: Int,
  ) : ChildrenChange {
    override val id: Id get() = Id(_id)
    override val tag: ChildrenTag get() = ChildrenTag(_tag)
    public val childId: Id get() = Id(_childId)

    public companion object {
      public operator fun invoke(
        id: Id,
        tag: ChildrenTag,
        childId: Id,
        index: Int,
      ): Add = Add(id.value, tag.value, childId.value, index)
    }
  }

  @Serializable
  @SerialName("move")
  @Poko
  public class Move private constructor(
    @SerialName("id")
    private val _id: Int,
    @SerialName("tag")
    private val _tag: Int,
    public val fromIndex: Int,
    public val toIndex: Int,
    public val count: Int,
  ) : ChildrenChange {
    override val id: Id get() = Id(_id)
    override val tag: ChildrenTag get() = ChildrenTag(_tag)

    public companion object {
      public operator fun invoke(
        id: Id,
        tag: ChildrenTag,
        fromIndex: Int,
        toIndex: Int,
        count: Int,
      ): Move = Move(id.value, tag.value, fromIndex, toIndex, count)
    }
  }

  @Serializable
  @SerialName("remove")
  @Poko
  public class Remove private constructor(
    @SerialName("id")
    private val _id: Int,
    @SerialName("tag")
    private val _tag: Int,
    public val index: Int,
    @Deprecated("Always 1 after 0.17.0. Will be >1 with earlier guests.")
    public val count: Int = 1,
    /**
     * When true, the associated nodes should only be detached from the tree with the expectation
     * that a future [Add] will re-attach them. Otherwise, nodes should be detached and fully
     * removed for garbage collection.
     *
     * Note: This property is only populated for hosts running 0.17.0 or newer.
     */
    public val detach: Boolean = false,
  ) : ChildrenChange {
    override val id: Id get() = Id(_id)
    override val tag: ChildrenTag get() = ChildrenTag(_tag)

    public companion object {
      public operator fun invoke(
        id: Id,
        tag: ChildrenTag,
        index: Int,
        detach: Boolean,
      ): Remove = Remove(id.value, tag.value, index, 1, detach)

      @Deprecated("Count is only supported on hosts older than 0.17.0.")
      public operator fun invoke(
        id: Id,
        tag: ChildrenTag,
        index: Int,
        count: Int,
      ): Remove = Remove(id.value, tag.value, index, count, false)
    }
  }
}
