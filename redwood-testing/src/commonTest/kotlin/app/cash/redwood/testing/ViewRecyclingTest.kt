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
@file:Suppress(
  "CANNOT_OVERRIDE_INVISIBLE_MEMBER",
  "INVISIBLE_MEMBER",
  "INVISIBLE_REFERENCE",
)

package app.cash.redwood.testing

import androidx.compose.runtime.mutableStateOf
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isNotSameInstanceAs
import assertk.assertions.isSameInstanceAs
import com.example.redwood.testing.compose.Text
import com.example.redwood.testing.widget.TextValue
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class ViewRecyclingTest {
  /** Confirm views are recycled in the simplest case. */
  @Test
  fun widgetsAreRecycled() = runTest {
    viewRecyclingTest {
      val version = mutableStateOf(1)
      setContent {
        when (version.value) {
          1 -> Text("one")
          2 -> Text("two")
          3 -> Text("three")
        }
      }

      // get the mutable view objects behind the snapshot
      assertThat(awaitSnapshot()).containsExactly(TextValue(text = "one"))
      val textWidgetNodeV1 = widgets.single()

      version.value = 2
      assertThat(awaitSnapshot()).containsExactly(TextValue(text = "two"))
      // When a widget is removed from a layout, it has to go to the pool before it goes back into
      // the layout. (TODO: explain this idea better)
      assertThat(widgets.single()).isNotSameInstanceAs(textWidgetNodeV1)

      version.value = 3
      // get the mutable view objects behind the snapshot
      assertThat(awaitSnapshot()).containsExactly(TextValue(text = "three"))
      // TODO: implement view recycling to get this to pass.
       assertThat(widgets.single()).isSameInstanceAs(textWidgetNodeV1)
    }
  }
}
