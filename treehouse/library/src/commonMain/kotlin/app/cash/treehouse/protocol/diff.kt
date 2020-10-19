package app.cash.treehouse.protocol

sealed class TreeDiff {
  abstract val id: Long
}

data class PropertyDiff(
  override val id: Long,
  val tag: Int,
  val value: Any?, // TODO
) : TreeDiff()

sealed class NodeDiff : TreeDiff() {
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
