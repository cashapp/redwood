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
package app.cash.redwood.layout

import app.cash.redwood.layout.api.Constraint
import app.cash.redwood.layout.widget.Box
import app.cash.redwood.ui.Dp
import app.cash.redwood.ui.dp
import kotlin.test.Test

abstract class AbstractBoxTest<T : Any> {

  abstract fun Box(block: Box<T>.() -> Unit = {}): Box<T>

  abstract fun Color(block: Color<T>.() -> Unit = {}): Color<T>

  fun Color(color: Int, width: Dp, height: Dp) = Color {
    color(color)
    width(width)
    height(height)
  }

  abstract fun verifySnapshot(value: T)

  @Test
  fun testDefaults() {
    val widget = Box()
    verifySnapshot(widget.value)
  }

  @Test
  fun testWrap() {
    val widget = Box {
      width(Constraint.Wrap)
      height(Constraint.Wrap)
    }
    verifySnapshot(widget.value)
  }

  @Test
  fun testFill() {
    val widget = Box {
      width(Constraint.Fill)
      height(Constraint.Fill)
    }
    verifySnapshot(widget.value)
  }

  @Test
  fun testWrapWithChildren() {
    val widget = Box {
      width(Constraint.Wrap)
      height(Constraint.Wrap)
      children.insert(0, Color(Red, 300.dp, 300.dp))
      children.insert(1, Color(Green, 200.dp, 200.dp))
      children.insert(2, Color(Blue, 100.dp, 100.dp))
    }
    verifySnapshot(widget.value)
  }

  @Test
  fun testFillWithChildren() {
    val widget = Box {
      width(Constraint.Fill)
      height(Constraint.Fill)
      children.insert(0, Color(Red, 300.dp, 300.dp))
      children.insert(1, Color(Green, 200.dp, 200.dp))
      children.insert(2, Color(Blue, 100.dp, 100.dp))
    }
    verifySnapshot(widget.value)
  }
}
