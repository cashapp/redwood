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
package app.cash.treehouse.protocol

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class Event(
  /** Identifier for the widget from which this event originated. */
  val id: Long,
  /** Identifies which event occurred on the widget with [id]. */
  val tag: Int,
  @Polymorphic val value: Any? = null,
)

@Serializable
public data class Diff(
  val childrenDiffs: List<ChildrenDiff> = emptyList(),
  val propertyDiffs: List<PropertyDiff> = emptyList()
)

@Serializable
public data class PropertyDiff(
  /** Identifier for the widget whose property has changed. */
  val id: Long,
  /** Identifies which property changed on the widget with [id]. */
  val tag: Int,
  @Polymorphic val value: Any? = null,
)

@Serializable
public sealed class ChildrenDiff {
  /** Identifier for the widget whose children have changed. */
  public abstract val id: Long
  /** Identifies which group of children changed on the widget with [id]. */
  public abstract val tag: Int

  @Serializable
  @SerialName("clear")
  public object Clear : ChildrenDiff() {
    override val id: Long get() = RootId
    override val tag: Int get() = RootChildrenTag
  }

  @Serializable
  @SerialName("insert")
  public data class Insert(
    override val id: Long,
    override val tag: Int,
    val childId: Long,
    val kind: Int,
    val index: Int,
  ) : ChildrenDiff()

  @Serializable
  @SerialName("move")
  public data class Move(
    override val id: Long,
    override val tag: Int,
    val fromIndex: Int,
    val toIndex: Int,
    val count: Int,
  ) : ChildrenDiff()

  @Serializable
  @SerialName("remove")
  public data class Remove(
    override val id: Long,
    override val tag: Int,
    val index: Int,
    val count: Int,
    val removedIds: List<Long>,
  ) : ChildrenDiff() {
    init {
      require(count == removedIds.size) {
        "Count $count != Removed ID list size ${removedIds.size}"
      }
    }
  }

  public companion object {
    public const val RootId: Long = 0L
    public const val RootChildrenTag: Int = 1
  }
}
