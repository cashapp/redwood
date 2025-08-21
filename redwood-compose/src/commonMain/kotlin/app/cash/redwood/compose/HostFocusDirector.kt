/*
 * Copyright (C) 2025 Square, Inc.
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
package app.cash.redwood.compose

import app.cash.redwood.ui.core.api.FocusDirector
import app.cash.redwood.ui.core.api.FocusRequester
import app.cash.redwood.ui.core.modifier.FocusRequester as FocusRequesterElement
import app.cash.redwood.widget.RedwoodView
import app.cash.redwood.widget.Widget

/**
 * When an [FocusRequester] is activated, this finds the corresponding element and then gets
 * [RedwoodView] to do the work.
 */
internal class HostFocusDirector<W : Any>(
  private val redwoodView: RedwoodView<W>,
) : FocusDirector {
  override fun hideSoftwareKeyboard() {
  }

  override fun newFocusRequester(): FocusRequester = HostFocusRequester()

  private inner class HostFocusRequester : FocusRequester {
    override fun requestFocus() {
      val widget = redwoodView.children.findFocusRequesterRecursive(this) ?: return
      redwoodView.requestFocus(widget)
    }
  }
}

/** Returns the widget with [focusRequester] as one of its modifiers. */
internal fun <W : Any> Widget.Children<W>.findFocusRequesterRecursive(
  focusRequester: FocusRequester,
): Widget<W>? {
  return widgets.depthFirst().firstNotNullOfOrNull { widget ->
    if (widget.focusRequester == focusRequester) {
      widget
    } else {
      null
    }
  }
}

/** Digs through this widget's modifier elements to find its focus requester. */
private val Widget<*>.focusRequester: FocusRequester?
  get() {
    var result: FocusRequester? = null
    modifier.forEachUnscoped { element ->
      if (result != null) return@forEachUnscoped // Already found it.
      val matchingElement = (element as? FocusRequesterElement) ?: return@forEachUnscoped
      result = matchingElement.value
    }
    return result
  }

internal fun <W : Any> List<Widget<W>>.depthFirst(): Sequence<Widget<W>> {
  return sequence { yieldDepthFirst(this@depthFirst) }
}

private suspend fun <W : Any> SequenceScope<Widget<W>>.yieldDepthFirst(widgets: List<Widget<W>>) {
  for (widget in widgets) {
    yield(widget)
    for (children in widget.allChildren) {
      yieldDepthFirst(children.widgets)
    }
  }
}
