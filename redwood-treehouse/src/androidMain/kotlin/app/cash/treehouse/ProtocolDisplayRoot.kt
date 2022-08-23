/*
 * Copyright (C) 2022 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package app.cash.treehouse

import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import app.cash.redwood.LayoutModifier
import app.cash.redwood.protocol.EventSink
import app.cash.redwood.protocol.PropertyDiff
import app.cash.redwood.protocol.widget.DiffConsumingWidget
import app.cash.redwood.widget.Widget
import kotlinx.serialization.json.JsonArray

internal class ProtocolDisplayRoot(
  override val value: TreehouseWidgetView<*>,
) : DiffConsumingWidget<View> {
  private val children = object : Widget.Children<View> {
    override fun insert(index: Int, widget: View) {
      value.addView(widget, index, FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT))
    }

    override fun move(fromIndex: Int, toIndex: Int, count: Int) {
      error("not implemented")
    }

    override fun remove(index: Int, count: Int) {
      for (i in 0 until count) {
        value.removeViewAt(index)
      }
    }

    override fun clear() {
      for (i in value.childCount - 1 downTo 0) {
        value.removeViewAt(i)
      }
    }
  }

  override var layoutModifiers: LayoutModifier = LayoutModifier

  override fun updateLayoutModifier(value: JsonArray) {
  }

  override fun apply(diff: PropertyDiff, eventSink: EventSink) {
    error("unexpected update on view root: $diff")
  }

  override fun children(tag: Int) = children
}
