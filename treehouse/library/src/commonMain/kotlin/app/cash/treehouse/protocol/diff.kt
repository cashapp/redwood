package app.cash.treehouse.protocol

data class TreeDiff(
  val nodeDiffs: List<NodeDiff> = emptyList(),
  val propertyDiffs: List<PropertyDiff> = emptyList()
)

data class PropertyDiff(
  val id: Long,
  val tag: Int,
  val value: Any?, // TODO
)

sealed class NodeDiff {
  abstract val id: Long

  data class Insert(
    override val id: Long,
    val childId: Long,
    val type: Int,
    val index: Int,
  ) : NodeDiff()

  data class Move(
    override val id: Long,
    val fromIndex: Int,
    val toIndex: Int,
    val count: Int,
  ) : NodeDiff()

  data class Remove(
    override val id: Long,
    val index: Int,
    val count: Int,
  ) : NodeDiff()
}
