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
import app.cash.redwood.layout.compose.Box
import app.cash.redwood.layout.widget.BoxValue
import app.cash.redwood.layout.widget.MutableBox
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
          1 -> {
            Box {
              Text("one")
            }
          }
          2 -> {
            Box {
              Text("two")
            }
          }
          3 -> {
            Box {
              Text("three")
            }
          }
        }
      }

      // get the mutable view objects behind the snapshot
      assertThat(awaitSnapshot()).containsExactly(
        BoxValue(children = listOf(TextValue(text = "one")))
      )
      val snapshot1Box = widgets.single() as MutableBox
      val snapshot1BoxText = snapshot1Box.children.single()

      version.value = 2
      assertThat(awaitSnapshot()).containsExactly(
        BoxValue(children = listOf(TextValue(text = "two")))
      )
      val snapshot2Box = widgets.single() as MutableBox
      val snapshot2BoxText = snapshot2Box.children.single()
      assertThat(snapshot2Box).isNotSameInstanceAs(snapshot1Box)
      assertThat(snapshot2BoxText).isNotSameInstanceAs(snapshot1Box)

      version.value = 3
      // get the mutable view objects behind the snapshot
      assertThat(awaitSnapshot()).containsExactly(
        BoxValue(children = listOf(TextValue(text = "three")))
      )
      val snapshot3Box = widgets.single() as MutableBox
      val snapshot3BoxText = snapshot3Box.children.single()
      assertThat(snapshot3Box).isSameInstanceAs(snapshot1Box)
      assertThat(snapshot3BoxText).isSameInstanceAs(snapshot1BoxText)
    }
  }
}
