/*
 * Copyright (C) 2024 Square, Inc.
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
package app.cash.redwood.treehouse

import app.cash.redwood.Modifier
import app.cash.redwood.testing.WidgetValue
import app.cash.redwood.widget.MutableListChildren
import app.cash.redwood.widget.RedwoodView
import app.cash.redwood.widget.Widget

class FakeRoot(
  private val eventLog: EventLog,
) : RedwoodView.Root<WidgetValue> {
  private val childrenDelegate = MutableListChildren<WidgetValue>()

  /** Keep views (but not widgets) after [Widget.Children.detach]. */
  private var viewsAfterDetach: List<WidgetValue>? = null

  val views: List<WidgetValue>
    get() = viewsAfterDetach ?: childrenDelegate.widgets.map { it.value }

  override val children = object : Widget.Children<WidgetValue> by childrenDelegate {
    override fun detach() {
      viewsAfterDetach = childrenDelegate.widgets.map { it.value }
      childrenDelegate.detach()
    }
  }

  override val value: WidgetValue
    get() = error("unexpected call")
  override var modifier: Modifier = Modifier

  override fun contentState(loadCount: Int, attached: Boolean, uncaughtException: Throwable?) {
    // Canonicalize "java.lang.Exception(boom!)" to "kotlin.Exception(boom!)".
    val exceptionString = uncaughtException?.toString()?.replace("java.lang.", "kotlin.")

    // TODO(jwilson): this is a backwards-compatibility shim. Emit a simpler event.
    eventLog += when {
      loadCount == 0 && !attached -> "codeListener.onInitialCodeLoading()"
      attached -> "codeListener.onCodeLoaded($loadCount)"
      else -> "codeListener.onCodeDetached($exceptionString)"
    }
  }

  override fun restart(restart: (() -> Unit)?) {
  }
}
