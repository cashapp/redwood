/*
 * Copyright (C) 2021 Square, Inc.
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
package app.cash.treehouse.widget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

abstract class AbstractWidgetChildrenTest<T : Any> {
  abstract val children: Widget.Children<T>
  abstract fun widget(name: String): T
  abstract fun names(): List<String>

  @Test fun insertAppend() {
    children.insert(0, widget("one"))
    children.insert(1, widget("two"))
    children.insert(2, widget("three"))
    assertEquals(listOf("one", "two", "three"), names())
  }

  @Test fun insertPrepend() {
    children.insert(0, widget("one"))
    children.insert(0, widget("two"))
    children.insert(0, widget("three"))
    assertEquals(listOf("three", "two", "one"), names())
  }

  @Test fun insertRandom() {
    children.insert(0, widget("one"))
    children.insert(0, widget("two"))
    children.insert(1, widget("three"))
    assertEquals(listOf("two", "three", "one"), names())
  }

  @Test fun insertIndexTooLarge() {
    assertFailsWith<IndexOutOfBoundsException> {
      children.insert(1, widget("one"))
    }
  }

  @Test fun insertIndexTooSmall() {
    assertFailsWith<IndexOutOfBoundsException> {
      children.insert(-1, widget("one"))
    }
  }

  @Test fun moveSingleForward() {
    children.insert(0, widget("one"))
    children.insert(1, widget("two"))
    children.insert(2, widget("three"))
    children.move(0, 3, 1)
    assertEquals(listOf("two", "three", "one"), names())
  }

  @Test fun moveSingleComposeApplierDocumentation() {
    // This test comes from Compose Applier's `move` documentation.
    children.insert(0, widget("A"))
    children.insert(1, widget("B"))
    children.insert(2, widget("C"))
    children.insert(3, widget("D"))
    children.insert(4, widget("E"))
    children.move(1, 3, 1)
    assertEquals(listOf("A", "C", "B", "D", "E"), names())
  }

  @Test fun moveMultipleForward() {
    children.insert(0, widget("one"))
    children.insert(1, widget("two"))
    children.insert(2, widget("three"))
    children.move(0, 3, 2)
    assertEquals(listOf("three", "one", "two"), names())
  }

  @Test fun moveSingleBackward() {
    children.insert(0, widget("one"))
    children.insert(1, widget("two"))
    children.insert(2, widget("three"))
    children.move(2, 0, 1)
    assertEquals(listOf("three", "one", "two"), names())
  }

  @Test fun moveMultipleBackward() {
    children.insert(0, widget("one"))
    children.insert(1, widget("two"))
    children.insert(2, widget("three"))
    children.move(1, 0, 2)
    assertEquals(listOf("two", "three", "one"), names())
  }

  @Test fun moveZero() {
    children.insert(0, widget("one"))
    children.insert(1, widget("two"))
    children.insert(2, widget("three"))
    children.move(0, 1, 0)
    assertEquals(listOf("one", "two", "three"), names())
    children.move(1, 0, 0)
    assertEquals(listOf("one", "two", "three"), names())
  }

  @Test fun moveFromIndexTooSmall() {
    children.insert(0, widget("one"))
    children.insert(1, widget("two"))
    children.insert(2, widget("three"))
    assertFailsWith<IndexOutOfBoundsException> {
      children.move(-1, 1, 1)
    }
  }

  @Test fun moveFromIndexTooLarge() {
    children.insert(0, widget("one"))
    children.insert(1, widget("two"))
    children.insert(2, widget("three"))
    assertFailsWith<IndexOutOfBoundsException> {
      children.move(4, 1, 1)
    }
  }

  @Test fun moveToIndexTooSmall() {
    children.insert(0, widget("one"))
    children.insert(1, widget("two"))
    children.insert(2, widget("three"))
    assertFailsWith<IndexOutOfBoundsException> {
      children.move(1, -1, 1)
    }
  }

  @Test fun moveToIndexTooLarge() {
    children.insert(0, widget("one"))
    children.insert(1, widget("two"))
    children.insert(2, widget("three"))
    assertFailsWith<IndexOutOfBoundsException> {
      children.move(1, 4, 1)
    }
  }

  @Test fun moveCountTooSmall() {
    children.insert(0, widget("one"))
    children.insert(1, widget("two"))
    children.insert(2, widget("three"))
    assertFailsWith<IndexOutOfBoundsException> {
      children.move(0, 1, -1)
    }
  }

  @Test fun moveCountTooLarge() {
    children.insert(0, widget("one"))
    children.insert(1, widget("two"))
    children.insert(2, widget("three"))
    assertFailsWith<IndexOutOfBoundsException> {
      children.move(0, 1, 4)
    }
  }

  @Test fun removeSingleStart() {
    children.insert(0, widget("one"))
    children.insert(1, widget("two"))
    children.insert(2, widget("three"))
    children.remove(0, 1)
    assertEquals(listOf("two", "three"), names())
  }

  @Test fun removeMultipleStart() {
    children.insert(0, widget("one"))
    children.insert(1, widget("two"))
    children.insert(2, widget("three"))
    children.remove(0, 2)
    assertEquals(listOf("three"), names())
  }

  @Test fun removeSingleEnd() {
    children.insert(0, widget("one"))
    children.insert(1, widget("two"))
    children.insert(2, widget("three"))
    children.remove(2, 1)
    assertEquals(listOf("one", "two"), names())
  }

  @Test fun removeMultipleEnd() {
    children.insert(0, widget("one"))
    children.insert(1, widget("two"))
    children.insert(2, widget("three"))
    children.remove(1, 2)
    assertEquals(listOf("one"), names())
  }

  @Test fun removeZero() {
    children.insert(0, widget("one"))
    children.insert(1, widget("two"))
    children.insert(2, widget("three"))
    children.remove(1, 0)
    assertEquals(listOf("one", "two", "three"), names())
  }

  @Test fun removeIndexTooSmall() {
    children.insert(0, widget("one"))
    children.insert(1, widget("two"))
    children.insert(2, widget("three"))
    assertFailsWith<IndexOutOfBoundsException> {
      children.remove(-1, 1)
    }
  }

  @Test fun removeIndexTooLarge() {
    children.insert(0, widget("one"))
    children.insert(1, widget("two"))
    children.insert(2, widget("three"))
    assertFailsWith<IndexOutOfBoundsException> {
      children.remove(3, 1)
    }
  }

  @Test fun removeCountTooSmall() {
    children.insert(0, widget("one"))
    children.insert(1, widget("two"))
    children.insert(2, widget("three"))
    assertFailsWith<IndexOutOfBoundsException> {
      children.remove(1, -1)
    }
  }

  @Test fun removeCountTooLarge() {
    children.insert(0, widget("one"))
    children.insert(1, widget("two"))
    children.insert(2, widget("three"))
    assertFailsWith<IndexOutOfBoundsException> {
      children.remove(1, 3)
    }
  }

  @Test fun clearWhenEmpty() {
    children.clear()
    assertEquals(listOf(), names())
  }

  @Test fun clearWhenNonEmpty() {
    children.insert(0, widget("one"))
    children.insert(1, widget("two"))
    children.clear()
    assertEquals(listOf(), names())
  }
}
