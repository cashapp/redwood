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
package app.cash.redwood.snapshot.testing

import android.view.View
import app.cash.paparazzi.Paparazzi

class ViewSnapshotter(
  private val paparazzi: Paparazzi,
  private val view: View,
) : Snapshotter {
  override fun snapshot(name: String?, scrolling: Boolean) {
    paparazzi.snapshot(view = view, name = name)

    if (scrolling) {
      var scrollCount = 0
      while (view.canScrollVertically(1)) {
        view.scrollBy(0, view.height)
        scrollCount++

        check(scrollCount < 15) {
          "This view has been scrolled 15 times! Bad input?"
        }

        paparazzi.snapshot(view = view, name = "${name.orEmpty()}_$scrollCount")
      }
      view.scrollTo(0, 0)
    }
  }
}
