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
import app.cash.redwood.ui.LayoutDirection
import app.cash.redwood.ui.Margin
import app.cash.redwood.ui.OnBackPressedCallback
import app.cash.redwood.ui.OnBackPressedDispatcher
import app.cash.redwood.ui.Size
import app.cash.redwood.ui.UiConfiguration
import app.cash.redwood.ui.dp
import app.cash.redwood.widget.Widget.Children
import kotlinx.browser.window
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.dom.clear
import org.w3c.dom.HTMLElement
import org.w3c.dom.MediaQueryList
import org.w3c.dom.events.Event

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

  private var pixelRatioQueryRemover: (() -> Unit)? = null

  override val onBackPressedDispatcher: OnBackPressedDispatcher = object : OnBackPressedDispatcher {
    override fun addCallback(onBackPressedCallback: OnBackPressedCallback): Cancellable {
      // TODO Delegate `onBackPressedCallback` to browser
      return object : Cancellable {
        override fun cancel() = Unit
      }
    }
  }

  private val _uiConfiguration: MutableStateFlow<UiConfiguration>
  override val uiConfiguration: StateFlow<UiConfiguration> get() = _uiConfiguration

  override var windowInsets: Margin
    get() = uiConfiguration.value.windowInsets
    set(value) {
      updateUiConfiguration { old ->
        UiConfiguration(
          darkMode = old.darkMode,
          safeAreaInsets = old.safeAreaInsets,
          windowInsets = value,
          viewportSize = old.viewportSize,
          density = old.density,
          layoutDirection = old.layoutDirection,
        )
      }
    }

  override val savedStateRegistry: SavedStateRegistry?
    get() = null

  init {
    val colorSchemeQuery = window.matchMedia("(prefers-color-scheme: dark)")

    _uiConfiguration = MutableStateFlow(
      UiConfiguration(
        darkMode = colorSchemeQuery.matches,
        windowInsets = Margin.Zero,
        viewportSize = Size(width = element.offsetWidth.dp, height = element.offsetHeight.dp),
        layoutDirection = when (element.dir) {
          "ltr" -> LayoutDirection.Ltr
          "rtl" -> LayoutDirection.Rtl
          "auto" -> LayoutDirection.Auto
          else -> LayoutDirection.Ltr
        },
      ),
    )

    colorSchemeQuery.addEventListener("change", { event ->
      updateUiConfiguration { old ->
        UiConfiguration(
          darkMode = event.unsafeCast<MediaQueryList>().matches,
          safeAreaInsets = old.safeAreaInsets,
          windowInsets = old.windowInsets,
          viewportSize = old.viewportSize,
          density = old.density,
          layoutDirection = old.layoutDirection,
        )
      }
    })

    observePixelRatioChange()

    // TODO Watch size change
    //   https://developer.mozilla.org/en-US/docs/Web/API/ResizeObserver
  }

  private fun observePixelRatioChange() {
    // From https://developer.mozilla.org/en-US/docs/Web/API/Window/devicePixelRatio#javascript_2.

    // Remove the listener based on the old pixel ratio, if it exists.
    pixelRatioQueryRemover?.invoke()

    // Create a media query based on the current pixel ratio value.
    val pixelRatioQuery = window.matchMedia("(resolution: ${window.devicePixelRatio}dppx)")

    val listener: (Event) -> Unit = { observePixelRatioChange() }
    pixelRatioQuery.addEventListener("change", listener)
    pixelRatioQueryRemover = {
      pixelRatioQuery.removeEventListener("change", listener)
    }

    updateUiConfiguration { old ->
      UiConfiguration(
        darkMode = old.darkMode,
        safeAreaInsets = old.safeAreaInsets,
        windowInsets = old.windowInsets,
        viewportSize = old.viewportSize,
        density = window.devicePixelRatio,
        layoutDirection = old.layoutDirection,
      )
    }
  }

  override fun reset() {
    _children.remove(0, _children.widgets.size)

    // Ensure any out-of-band nodes are also removed.
    element.clear()
  }

  private fun updateUiConfiguration(updater: (UiConfiguration) -> UiConfiguration) {
    // We skip MutableStateFlow.update because it uses verbose CAS loop and JS only has one thread.
    _uiConfiguration.value = updater(_uiConfiguration.value)
  }
}
