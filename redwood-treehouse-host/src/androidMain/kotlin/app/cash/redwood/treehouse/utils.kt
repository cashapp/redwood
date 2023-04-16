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
package app.cash.redwood.treehouse

import android.view.View
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import app.cash.redwood.layout.api.Margin

internal fun Insets.toMargin(density: Double) = Margin(
  left = left / density,
  right = right / density,
  top = top / density,
  bottom = bottom / density,
)

// Root insets will be null if the view is not attached.
internal val View.rootWindowInsetsCompat: WindowInsetsCompat?
  get() = ViewCompat.getRootWindowInsets(this)

internal val WindowInsetsCompat?.systemBars: Insets
  get() = this?.getInsets(WindowInsetsCompat.Type.systemBars()) ?: Insets.NONE
