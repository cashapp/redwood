package app.cash.treehouse.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composition
import androidx.compose.runtime.MonotonicFrameClock
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.withFrameMillis
import app.cash.treehouse.protocol.Event
import app.cash.treehouse.protocol.WidgetDiff
import app.cash.treehouse.protocol.PropertyDiff
import app.cash.treehouse.protocol.Diff
import app.cash.treehouse.protocol.Diff.Companion.RootId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

interface TreehouseComposition {
  fun sendEvent(event: Event)
  fun setContent(content: @Composable TreehouseScope.() -> Unit)
  fun cancel()
}

fun interface DiffSink {
  fun apply(diff: Diff)
}

/**
 * @param scope A [CoroutineScope] whose [coroutineContext][kotlin.coroutines.CoroutineContext]
 * must have a [MonotonicFrameClock] key which is being ticked.
 */
fun TreehouseComposition(
  scope: CoroutineScope,
  diff: DiffSink,
): TreehouseComposition {
  val clock = requireNotNull(scope.coroutineContext[MonotonicFrameClock]) {
    "Composition scope's CoroutineContext must have a MonotonicFrameClock key"
  }
  val server = RealTreehouseComposition(scope, clock, diff)
  server.launch()
  return server
}

private class RealTreehouseComposition(
  private val scope: CoroutineScope,
  private val clock: MonotonicFrameClock,
  private val diffSink: DiffSink,
) : TreehouseComposition {
  private var nodeDiffs = mutableListOf<WidgetDiff>()
  private var propertyDiffs = mutableListOf<PropertyDiff>()

  private val treehouseScope = RealTreehouseScope()
  inner class RealTreehouseScope : TreehouseScope {
    // TODO atomics if compose becomes multithreaded?
    private var nextId = RootId + 1
    override fun nextId() = nextId++

    override fun appendDiff(diff: WidgetDiff) {
      nodeDiffs.add(diff)
    }

    override fun appendDiff(diff: PropertyDiff) {
      propertyDiffs.add(diff)
    }
  }

  private val applier = ProtocolApplier(treehouseScope)
  private val recomposer = Recomposer(scope.coroutineContext)
  private val composition = Composition(applier, recomposer)

  private lateinit var job: Job

  fun launch() {
    // Set up a trigger to apply changes on the next frame if a global write was observed.
    // TODO where should this live?
    var applyScheduled = false
    Snapshot.registerGlobalWriteObserver {
      if (!applyScheduled) {
        applyScheduled = true
        scope.launch {
          applyScheduled = false
          Snapshot.sendApplyNotifications()
        }
      }
    }

    job = scope.launch {
      coroutineScope {
        launch {
          recomposer.runRecomposeAndApplyChanges()
        }
        launch {
          while (true) {
            clock.withFrameMillis {
              val existingNodeDiffs = nodeDiffs
              val existingPropertyDiffs = propertyDiffs
              if (existingPropertyDiffs.isNotEmpty() || existingNodeDiffs.isNotEmpty()) {
                nodeDiffs = mutableListOf()
                propertyDiffs = mutableListOf()

                diffSink.apply(Diff(
                  widgetDiffs = existingNodeDiffs,
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

