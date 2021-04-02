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
data class TreeDiff(
  val nodeDiffs: List<NodeDiff> = emptyList(),
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
sealed class NodeDiff {
  abstract val id: Long
  abstract val childrenIndex: Int

  @Serializable
  object Clear : NodeDiff() {
    override val id get() = TreeDiff.RootId
    override val childrenIndex get() = throw UnsupportedOperationException()
  }

  @Serializable
  data class Insert(
    override val id: Long,
    override val childrenIndex: Int,
    val childId: Long,
    val kind: Int,
    val index: Int,
  ) : NodeDiff()

  @Serializable
  data class Move(
    override val id: Long,
    override val childrenIndex: Int,
    val fromIndex: Int,
    val toIndex: Int,
    val count: Int,
  ) : NodeDiff()

  @Serializable
  data class Remove(
    override val id: Long,
    override val childrenIndex: Int,
    val index: Int,
    val count: Int,
  ) : NodeDiff()
}
