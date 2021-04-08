package app.cash.treehouse.protocol

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable

@Serializable
data class Event(
  val nodeId: Long,
  val tag: Int,
  @Polymorphic val value: Any?,
)

@Serializable
data class Diff(
  val widgetDiffs: List<WidgetDiff> = emptyList(),
  val propertyDiffs: List<PropertyDiff> = emptyList()
) {
  companion object {
    const val RootId = 0L
    const val RootChildrenIndex = 1
  }
}

@Serializable
data class PropertyDiff(
  val id: Long,
  val tag: Int,
  @Polymorphic val value: Any?,
)

@Serializable
sealed class WidgetDiff {
  abstract val id: Long
  abstract val childrenIndex: Int

  @Serializable
  object Clear : WidgetDiff() {
    override val id get() = Diff.RootId
    override val childrenIndex get() = throw UnsupportedOperationException()
  }

  @Serializable
  data class Insert(
    override val id: Long,
    override val childrenIndex: Int,
    val childId: Long,
    val kind: Int,
    val index: Int,
  ) : WidgetDiff()

  @Serializable
  data class Move(
    override val id: Long,
    override val childrenIndex: Int,
    val fromIndex: Int,
    val toIndex: Int,
    val count: Int,
  ) : WidgetDiff()

  @Serializable
  data class Remove(
    override val id: Long,
    override val childrenIndex: Int,
    val index: Int,
    val count: Int,
  ) : WidgetDiff()
}
