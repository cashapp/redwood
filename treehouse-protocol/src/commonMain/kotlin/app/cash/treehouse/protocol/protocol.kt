package app.cash.treehouse.protocol

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable

@Serializable
data class Event(
  val nodeId: Long,
  val eventId: Long,
  @Polymorphic val value: Any?,
)

@Serializable
data class TreeDiff(
  val nodeDiffs: List<NodeDiff> = emptyList(),
  val propertyDiffs: List<PropertyDiff> = emptyList()
) {
  companion object {
    const val RootId = 0L
  }
}

@Serializable
data class PropertyDiff(
  val id: Long,
  val tag: Int,
  @Polymorphic val value: Any?,
)

@Serializable
sealed class NodeDiff {
  abstract val id: Long

  @Serializable
  object Clear : NodeDiff() {
    override val id get() = TreeDiff.RootId
  }

  @Serializable
  data class Insert(
    override val id: Long,
    val childId: Long,
    val kind: Int,
    val index: Int,
  ) : NodeDiff()

  @Serializable
  data class Move(
    override val id: Long,
    val fromIndex: Int,
    val toIndex: Int,
    val count: Int,
  ) : NodeDiff()

  @Serializable
  data class Remove(
    override val id: Long,
    val index: Int,
    val count: Int,
  ) : NodeDiff()
}
