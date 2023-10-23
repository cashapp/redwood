/*
 * Copyright (C) 2023 Square, Inc.
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
package app.cash.redwood.widget

import app.cash.redwood.ui.Cancellable
import app.cash.redwood.ui.OnBackPressedCallback
import app.cash.redwood.ui.OnBackPressedDispatcher
import app.cash.redwood.ui.Size
import app.cash.redwood.ui.UiConfiguration
import app.cash.redwood.ui.dp
import app.cash.redwood.widget.Widget.Children
import kotlinx.browser.window
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.dom.clear
import org.w3c.dom.HTMLElement
import org.w3c.dom.MediaQueryList

public fun HTMLElement.asRedwoodView(): RedwoodView<HTMLElement> {
  checkNotNull(parentNode) {
    "Element $this must be attached to Document to be used as RedwoodView"
  }
  return RedwoodHTMLElementView(this)
}

private class RedwoodHTMLElementView(
  private val element: HTMLElement,
) : RedwoodView<HTMLElement> {
  private val _children = HTMLElementChildren(element)
  override val children: Children<HTMLElement> get() = _children

  override val onBackPressedDispatcher: OnBackPressedDispatcher = object : OnBackPressedDispatcher {
    override fun addCallback(onBackPressedCallback: OnBackPressedCallback): Cancellable {
      // TODO Delegate `onBackPressedCallback` to browser
      return object : Cancellable {
        override fun cancel() = Unit
      }
    }
  }

  override val uiConfiguration = MutableStateFlow(
    UiConfiguration(
      darkMode = window.matchMedia("(prefers-color-scheme: dark)").matches,
      viewportSize = Size(width = element.offsetWidth.dp, height = element.offsetHeight.dp),
    ),
  )
  override val savedStateRegistry: SavedStateRegistry?
    get() = null

  init {
    window.matchMedia("(prefers-color-scheme: dark)").addEventListener("change", { event ->
      uiConfiguration.update { old ->
        UiConfiguration(
          darkMode = event.unsafeCast<MediaQueryList>().matches,
          safeAreaInsets = old.safeAreaInsets,
          viewportSize = old.viewportSize,
          density = old.density,
        )
      }
    })

    // TODO Watch density change
    //  https://developer.mozilla.org/en-US/docs/Web/API/Window/devicePixelRatio#javascript_2

    // TODO Watch size change
    //   https://developer.mozilla.org/en-US/docs/Web/API/ResizeObserver
  }

  override fun reset() {
    _children.remove(0, _children.widgets.size)

    // Ensure any out-of-band nodes are also removed.
    element.clear()
  }
}
