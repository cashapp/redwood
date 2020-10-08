package example

import android.app.Activity
import android.os.Bundle
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams
import android.widget.LinearLayout.VERTICAL
import app.cash.treehouse.Event
import app.cash.treehouse.Treehouse
import app.cash.treehouse.protocol.NodeDiff
import app.cash.treehouse.protocol.PropertyDiff
import app.cash.treehouse.protocol.TreeDiff
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainActivity : Activity() {
  private val scope = MainScope()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val root = LinearLayout(this).apply {
      orientation = VERTICAL
      layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
    }
    setContentView(root)

    val treehouse = Treehouse(
      bridge = SunspotBridge(
        root = AndroidSunspotViewGroup(root),
        factory = AndroidSunspotNodeFactory,
        mutator = AndroidContainerMutator,
      ),
    )

    var count = 0

    treehouse.apply(TreeDiff(
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
      treehouse.events.collect { event ->
        when (event) {
          Event(1L /* -1 */, 1L /* clicked */) -> {
            treehouse.apply(TreeDiff(
              propertyDiffs = listOf(
                PropertyDiff(id = 2L, tag = 1 /* value */, value = (--count).toString()),
                PropertyDiff(id = 2L, tag = 2 /* color */, value = "#ffaaaa"),
              )
            ))
          }
          Event(3L /* +1 */, 1L /* clicked */) -> {
            treehouse.apply(TreeDiff(
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

  override fun onDestroy() {
    scope.cancel()
    super.onDestroy()
  }
}
