package app.cash.treehouse.protocol

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable

@Serializable
data class Event(
  /** Identifier for the widget from which this event originated. */
  val id: Long,
  /** Identifies which event occurred on the widget with [id]. */
  val tag: Int,
  @Polymorphic val value: Any?,
)

@Serializable
data class Diff(
  val childrenDiffs: List<ChildrenDiff> = emptyList(),
  val propertyDiffs: List<PropertyDiff> = emptyList()
)

@Serializable
data class PropertyDiff(
  /** Identifier for the widget whose property has changed. */
  val id: Long,
  /** Identifies which property changed on the widget with [id]. */
  val tag: Int,
  @Polymorphic val value: Any?,
)

@Serializable
sealed class ChildrenDiff {
  /** Identifier for the widget whose children have changed. */
  abstract val id: Long
  /** Identifies which group of children changed on the widget with [id]. */
  abstract val tag: Int

  @Serializable
  object Clear : ChildrenDiff() {
    override val id get() = RootId
    override val tag get() = RootChildrenTag
  }

  @Serializable
  data class Insert(
    override val id: Long,
    override val tag: Int,
    val childId: Long,
    val kind: Int,
    val index: Int,
  ) : ChildrenDiff()

  @Serializable
  data class Move(
    override val id: Long,
    override val tag: Int,
    val fromIndex: Int,
    val toIndex: Int,
    val count: Int,
  ) : ChildrenDiff()

  @Serializable
  data class Remove(
    override val id: Long,
    override val tag: Int,
    val index: Int,
    val count: Int,
  ) : ChildrenDiff()

  companion object {
    const val RootId = 0L
    const val RootChildrenTag = 1
  }
}
