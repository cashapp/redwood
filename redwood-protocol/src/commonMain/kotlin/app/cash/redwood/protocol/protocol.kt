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

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull

public fun interface EventSink {
  public fun sendEvent(event: Event)
}

public fun interface DiffSink {
  public fun sendDiff(diff: Diff)
}

@Serializable
public data class Event(
  /** Identifier for the widget from which this event originated. */
  val id: ULong,
  /** Identifies which event occurred on the widget with [id]. */
  val tag: UInt,
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
  val id: ULong,
  /** Identifies which property changed on the widget with [id]. */
  val tag: UInt,
  val value: JsonElement = JsonNull,
)

@Serializable
public data class LayoutModifiers(
  /** Identifier for the widget whose layout modifier has changed. */
  val id: ULong,
  /**
   * Array of layout modifiers. Each element of this array is itself a two-element array of the
   * layout modifier tag and then serialized value.
   */
  val elements: JsonArray,
)

@Serializable
public sealed class ChildrenDiff {
  /** Identifier for the widget whose children have changed. */
  public abstract val id: ULong

  /** Identifies which group of children changed on the widget with [id]. */
  public abstract val tag: UInt

  @Serializable
  @SerialName("clear")
  public object Clear : ChildrenDiff() {
    override val id: ULong get() = RootId
    override val tag: UInt get() = RootChildrenTag
  }

  @Serializable
  @SerialName("insert")
  public data class Insert(
    override val id: ULong,
    override val tag: UInt,
    val childId: ULong,
    val kind: Int,
    val index: Int,
  ) : ChildrenDiff()

  @Serializable
  @SerialName("move")
  public data class Move(
    override val id: ULong,
    override val tag: UInt,
    val fromIndex: Int,
    val toIndex: Int,
    val count: Int,
  ) : ChildrenDiff()

  @Serializable
  @SerialName("remove")
  public data class Remove(
    override val id: ULong,
    override val tag: UInt,
    val index: Int,
    val count: Int,
    val removedIds: List<ULong>,
  ) : ChildrenDiff() {
    init {
      require(count == removedIds.size) {
        "Count $count != Removed ID list size ${removedIds.size}"
      }
    }
  }

  public companion object {
    public const val RootId: ULong = 0UL
    public const val RootChildrenTag: UInt = 1U
  }
}
