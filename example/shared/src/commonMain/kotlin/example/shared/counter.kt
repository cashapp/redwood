package example.shared

import app.cash.treehouse.Event
import app.cash.treehouse.Treehouse
import app.cash.treehouse.protocol.NodeDiff
import app.cash.treehouse.protocol.PropertyDiff
import app.cash.treehouse.protocol.TreeDiff
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

fun Treehouse<*>.launchCounterIn(scope: CoroutineScope) {
  var count = 0

  apply(TreeDiff(
    nodeDiffs = listOf(
      NodeDiff.Insert(id = 0L, childId = 1L, type = 2 /* button */, index = 0),
      NodeDiff.Insert(id = 0L, childId = 2L, type = 1 /* text */, index = 1),
      NodeDiff.Insert(id = 0L, childId = 3L, type = 2 /* button */, index = 2),
    ),
    propertyDiffs = listOf(
      PropertyDiff(id = 1L, tag = 1 /* value */, value = "-1"),
      PropertyDiff(id = 1L, tag = 2 /* clickable */, value = true),
      PropertyDiff(id = 2L, tag = 1 /* value */, value = count.toString()),
      PropertyDiff(id = 3L, tag = 1 /* value */, value = "+1"),
      PropertyDiff(id = 3L, tag = 2 /* clickable */, value = true),
    ),
  ))

  scope.launch {
    events.collect { event ->
      when (event) {
        Event(1L /* -1 */, 1L /* clicked */) -> {
          apply(TreeDiff(
            propertyDiffs = listOf(
              PropertyDiff(id = 2L, tag = 1 /* value */, value = (--count).toString()),
              PropertyDiff(id = 2L, tag = 2 /* color */, value = "#ffaaaa"),
            )
          ))
        }
        Event(3L /* +1 */, 1L /* clicked */) -> {
          apply(TreeDiff(
            propertyDiffs = listOf(
              PropertyDiff(id = 2L, tag = 1 /* value */, value = (++count).toString()),
              PropertyDiff(id = 2L, tag = 2 /* color */, value = "#aaffaa"),
            )
          ))
        }
        else -> throw IllegalStateException("Unknown event $event")
      }
    }
  }
}
