package app.cash.treehouse

import app.cash.treehouse.protocol.NodeDiff
import app.cash.treehouse.protocol.PropertyDiff
import app.cash.treehouse.protocol.TreeDiff
import app.cash.treehouse.protocol.TreeDiff.Companion.RootId
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.flow.consumeAsFlow

class Treehouse<N : TreeNode>(
  private val bridge: TreeBridge<N>,
) {
  private val nodes = mutableMapOf(RootId to bridge.root)

  private val eventChannel = Channel<Event>(UNLIMITED)
  private val eventSink = EventSink(eventChannel::offer)
  val events get() = eventChannel.consumeAsFlow()

  fun apply(diffs: List<TreeDiff>) {
    for (diff in diffs) {
      val node = nodes[diff.id]
      checkNotNull(node) { "Unknown node ${diff.id}" }

      @Suppress("UNREACHABLE_CODE", "UNUSED_VARIABLE")
      val exhaustive: Unit = when (diff) {
        is PropertyDiff -> {
          node.apply(diff)
        }
        is NodeDiff -> {
          @Suppress("UNREACHABLE_CODE", "UNUSED_VARIABLE")
          val exhaustive: Unit = when (diff) {
            is NodeDiff.Insert -> {
              nodes[diff.childId] = bridge.insert(node, diff, eventSink)
            }
            is NodeDiff.Move -> {
              bridge.move(node, diff)
            }
            is NodeDiff.Remove -> {
              bridge.remove(node, diff)
            }
            NodeDiff.Clear -> {
              bridge.clear()
            }
          }
        }
      }
    }
  }
}
