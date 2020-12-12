package app.cash.treehouse.server

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.compositionFor
import androidx.compose.runtime.dispatch.DefaultMonotonicFrameClock
import androidx.compose.runtime.dispatch.MonotonicFrameClock
import androidx.compose.runtime.dispatch.withFrameMillis
import app.cash.treehouse.protocol.Event
import app.cash.treehouse.protocol.NodeDiff
import app.cash.treehouse.protocol.PropertyDiff
import app.cash.treehouse.protocol.TreeDiff
import app.cash.treehouse.protocol.TreeDiff.Companion.RootId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

interface TreehouseServer {
  fun sendEvent(event: Event)
  fun setContent(content: @Composable TreehouseScope.() -> Unit)
  fun cancel()
}

fun interface DiffSink {
  fun apply(diff: TreeDiff)
}

fun TreehouseServer(
  scope: CoroutineScope,
  diff: DiffSink,
): TreehouseServer {
  val server = RealTreehouseServer(scope, diff)
  server.launch()
  return server
}

private class RealTreehouseServer(
  private val scope: CoroutineScope,
  private val diffSink: DiffSink,
) : TreehouseServer {
  private var nodeDiffs = mutableListOf<NodeDiff>()
  private var propertyDiffs = mutableListOf<PropertyDiff>()

  private val treehouseScope = RealTreehouseScope()
  inner class RealTreehouseScope : TreehouseScope {
    // TODO atomics if compose becomes multithreaded?
    private var nextId = RootId + 1
    override fun nextId() = nextId++

    override fun appendDiff(diff: NodeDiff) {
      nodeDiffs.add(diff)
    }

    override fun appendDiff(diff: PropertyDiff) {
      propertyDiffs.add(diff)
    }
  }

  private val applier = ProtocolApplier(Node(RootId, -1), treehouseScope)
  private val recomposer = Recomposer(scope.coroutineContext)
  private val composition = compositionFor(Any(), applier, recomposer)

  private lateinit var job: Job

  fun launch() {
    job = scope.launch {
      coroutineScope {
        launch {
          recomposer.runRecomposeAndApplyChanges()
        }
        launch {
          val clock = coroutineContext[MonotonicFrameClock] ?: DefaultMonotonicFrameClock
          while (true) {
            clock.withFrameMillis {
              val existingNodeDiffs = nodeDiffs
              val existingPropertyDiffs = propertyDiffs
              if (existingPropertyDiffs.isNotEmpty() || existingNodeDiffs.isNotEmpty()) {
                nodeDiffs = mutableListOf()
                propertyDiffs = mutableListOf()

                diffSink.apply(TreeDiff(
                  nodeDiffs = existingNodeDiffs,
                  propertyDiffs = existingPropertyDiffs,
                ))
              }
            }
          }
        }
      }
    }
  }

  override fun sendEvent(event: Event) {
    val node = applier.nodes[event.nodeId]
    if (node == null) {
      // TODO how to handle race where an incoming event targets this removed node?
      throw IllegalArgumentException("Unknown node ${event.nodeId} for event with tag ${event.tag}")
    }
    node.sendEvent(event)
  }

  override fun setContent(content: @Composable TreehouseScope.() -> Unit) {
    composition.setContent {
      treehouseScope.content()
    }
  }

  override fun cancel() {
    job.cancel()
  }
}

