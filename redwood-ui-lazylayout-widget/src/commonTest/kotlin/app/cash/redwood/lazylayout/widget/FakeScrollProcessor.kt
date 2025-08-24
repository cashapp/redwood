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
package app.cash.redwood.lazylayout.widget

class FakeScrollProcessor : LazyListScrollProcessor() {
  private val events = mutableListOf<String>()

  /** How many rows are in the list. */
  var size = 0

  init {
    this.onViewportChanged { firstIndex, lastIndex ->
      events += "userScroll($firstIndex, $lastIndex)"
    }
  }

  override fun contentSize(): Int = size

  override fun programmaticScroll(firstIndex: Int, animated: Boolean) {
    require(firstIndex < size)
    events += "programmaticScroll(firstIndex = $firstIndex, animated = $animated)"
  }

  fun takeEvents(): List<String> {
    val result = events.toList()
    events.clear()
    return result
  }
}
