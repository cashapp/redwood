package example.android

import android.app.Activity
import android.os.Bundle
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams
import android.widget.LinearLayout.VERTICAL
import app.cash.treehouse.Treehouse
import example.android.sunspot.AndroidContainerMutator
import example.android.sunspot.AndroidSunspotNodeFactory
import example.android.sunspot.AndroidSunspotViewGroup
import example.shared.launchCounterIn
import example.sunspot.SunspotBridge
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

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

    treehouse.launchCounterIn(scope)
  }

  override fun onDestroy() {
    scope.cancel()
    super.onDestroy()
  }
}
