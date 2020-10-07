package example

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams
import android.widget.LinearLayout.VERTICAL
import app.cash.treehouse.Treehouse
import app.cash.treehouse.protocol.NodeDiff
import app.cash.treehouse.protocol.PropertyDiff
import app.cash.treehouse.protocol.TreeDiff

class MainActivity : Activity() {
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

    treehouse.apply(TreeDiff(
      nodeDiffs = listOf(
        NodeDiff.Insert(id = 0L, childId = 1L, type = 2 /* button */, index = 0),
        NodeDiff.Insert(id = 0L, childId = 2L, type = 1 /* text */, index = 1),
        NodeDiff.Insert(id = 0L, childId = 3L, type = 2 /* button */, index = 2),
      ),
      propertyDiffs = listOf(
        PropertyDiff(id = 1L, tag = 1 /* value */, value = "-1"),
        PropertyDiff(id = 1L, tag = 2 /* clickable */, value = true),
        PropertyDiff(id = 2L, tag = 1 /* value */, value = "0"),
        PropertyDiff(id = 3L, tag = 1 /* value */, value = "+1"),
        PropertyDiff(id = 3L, tag = 2 /* clickable */, value = true),
      ),
    ))

    val handler = Handler(Looper.getMainLooper())
    handler.postDelayed({
      // treehouse.events... for +1

      treehouse.apply(TreeDiff(
        propertyDiffs = listOf(
          PropertyDiff(id = 2L, tag = 1 /* value */, value = "1"),
          PropertyDiff(id = 2L, tag = 2 /* color */, value = "#aaffaa"),
        )
      ))
    }, 1_000)

    handler.postDelayed({
      // treehouse.events... for -1

      treehouse.apply(TreeDiff(
        propertyDiffs = listOf(
          PropertyDiff(id = 2L, tag = 1 /* value */, value = "0"),
          PropertyDiff(id = 2L, tag = 2 /* color */, value = "#ffaaaa"),
        )
      ))
    }, 2_000)
  }
}
