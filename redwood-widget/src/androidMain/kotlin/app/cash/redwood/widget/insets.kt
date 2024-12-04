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

import android.view.View
import androidx.core.view.OnApplyWindowInsetsListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsCompat

internal fun View.setOnWindowInsetsChangeListener(listener: (WindowInsetsCompat?) -> Unit) {
  val callback = WindowInsetsCallback(listener)
  ViewCompat.setOnApplyWindowInsetsListener(this, callback)

  if (isAttachedToWindow) requestApplyInsets()
  addOnAttachStateChangeListener(callback)

  ViewCompat.setWindowInsetsAnimationCallback(this, callback)
}

private class WindowInsetsCallback(
  private val listener: (WindowInsetsCompat?) -> Unit,
) : WindowInsetsAnimationCompat.Callback(DISPATCH_MODE_STOP),
  OnApplyWindowInsetsListener,
  View.OnAttachStateChangeListener {

  override fun onProgress(
    insets: WindowInsetsCompat,
    runningAnimations: List<WindowInsetsAnimationCompat>,
  ): WindowInsetsCompat {
    listener(insets)
    return WindowInsetsCompat.CONSUMED
  }

  override fun onApplyWindowInsets(
    view: View,
    insets: WindowInsetsCompat,
  ): WindowInsetsCompat {
    listener(insets)
    return WindowInsetsCompat.CONSUMED
  }

  override fun onViewAttachedToWindow(view: View) {
    view.requestApplyInsets()
    listener(view.rootWindowInsetsCompat)
  }

  override fun onViewDetachedFromWindow(view: View) {}
}
