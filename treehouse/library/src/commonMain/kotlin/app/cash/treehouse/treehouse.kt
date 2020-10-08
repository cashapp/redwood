package app.cash.treehouse

import app.cash.treehouse.protocol.NodeDiff
import app.cash.treehouse.protocol.TreeDiff
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.flow.consumeAsFlow

class Treehouse<N : TreeNode>(
  private val bridge: TreeBridge<N>,
) {
  private val nodes = mutableMapOf(0L to bridge.root)

  private val eventChannel = Channel<Event>(UNLIMITED)
  private val eventSink = EventSink(eventChannel::offer)
  val events get() = eventChannel.consumeAsFlow()

  fun apply(diff: TreeDiff) {
    for (nodeDiff in diff.nodeDiffs) {
      val container = nodes.getValue(nodeDiff.id)

      @Suppress("UNREACHABLE_CODE")
      val exhaustive = when (nodeDiff) {
        is NodeDiff.Insert -> {
          nodes[nodeDiff.childId] = bridge.insert(container, nodeDiff, eventSink)
        }
        is NodeDiff.Move -> {
          bridge.move(container, nodeDiff)
        }
        is NodeDiff.Remove -> {
          bridge.remove(container, nodeDiff)
        }
      }
    }

    for (propertyDiff in diff.propertyDiffs) {
      val mutator = nodes[propertyDiff.id]
      checkNotNull(mutator) { "Unknown node ${propertyDiff.id}" }
      mutator.apply(propertyDiff)
    }
  }
}
