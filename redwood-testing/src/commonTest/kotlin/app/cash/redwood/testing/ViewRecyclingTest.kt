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

import app.cash.redwood.Modifier
import app.cash.redwood.layout.compose.Box
import app.cash.redwood.layout.compose.Column
import app.cash.redwood.layout.widget.BoxValue
import app.cash.redwood.layout.widget.ColumnValue
import app.cash.redwood.layout.widget.MutableBox
import app.cash.redwood.layout.widget.MutableColumn
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEqualTo
import assertk.assertions.isNotSameInstanceAs
import assertk.assertions.isSameInstanceAs
import com.example.redwood.testing.compose.Button
import com.example.redwood.testing.compose.Split
import com.example.redwood.testing.compose.Text
import com.example.redwood.testing.compose.reuse
import com.example.redwood.testing.widget.SplitValue
import com.example.redwood.testing.widget.TextValue
import kotlin.test.Ignore
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class ViewRecyclingTest {
  private val reuse = Modifier.reuse()

  /** Confirm views are recycled in the simplest case. */
  @Test
  fun happyPath() = runTest {
    viewRecyclingTest {
      // Set the initial content. New widgets are created.
      setContent {
        Box(modifier = reuse) {
          Text("one")
        }
      }
      assertThat(awaitSnapshot()).containsExactly(
        BoxValue(
          modifier = reuse,
          children = listOf(TextValue(text = "one")),
        ),
      )
      val snapshot1Box = widgets.single() as MutableBox
      val snapshot1BoxText = snapshot1Box.children.single()

      // Update the content. The old widgets are pooled and new widgets are created.
      setContent {
        Box(modifier = reuse) {
          Text("two")
        }
      }
      assertThat(awaitSnapshot()).containsExactly(
        BoxValue(
          modifier = reuse,
          children = listOf(TextValue(text = "two")),
        ),
      )
      val snapshot2Box = widgets.single() as MutableBox
      val snapshot2BoxText = snapshot2Box.children.single()
      assertThat(snapshot2Box).isNotSameInstanceAs(snapshot1Box)
      assertThat(snapshot2BoxText).isNotSameInstanceAs(snapshot1Box)

      // Update the content again. The pooled widgets are used.
      setContent {
        Box(modifier = reuse) {
          Text("three")
        }
      }
      assertThat(awaitSnapshot()).containsExactly(
        BoxValue(
          modifier = reuse,
          children = listOf(TextValue(text = "three")),
        ),
      )
      val snapshot3Box = widgets.single() as MutableBox
      val snapshot3BoxText = snapshot3Box.children.single()
      assertThat(snapshot3Box).isSameInstanceAs(snapshot1Box)
      assertThat(snapshot3BoxText).isSameInstanceAs(snapshot1BoxText)
    }
  }

  @Test
  fun reuseWithMultipleChildren() = runTest {
    assertReuse(
      step1 = {
        Box(modifier = reuse) {
          Text("a")
          Text("b")
        }
      },
      step2 = {
        Box(modifier = reuse) {
          Text("c")
          Text("d")
        }
      },
      step2Value = BoxValue(
        modifier = reuse,
        children = listOf(
          TextValue(text = "c"),
          TextValue(text = "d"),
        ),
      ),
    )
  }

  @Test
  fun reuseWithNestedChildren() = runTest {
    assertReuse(
      step1 = {
        Box(modifier = reuse) {
          Column {
            Text("a")
          }
          Column {
            Text("b")
          }
        }
      },
      step2 = {
        Box(modifier = reuse) {
          Column {
            Text("c")
          }
          Column {
            Text("d")
          }
        }
      },
      step2Value = BoxValue(
        modifier = reuse,
        children = listOf(
          ColumnValue(
            children = listOf(
              TextValue(text = "c"),
            ),
          ),
          ColumnValue(
            children = listOf(
              TextValue(text = "d"),
            ),
          ),
        ),
      ),
    )
  }

  @Test
  fun reuseWithDeeplyNestedChildren() = runTest {
    assertReuse(
      step1 = {
        Box(modifier = reuse) {
          Text("a")
          Column {
            Column {
              Text("b")
              Column {
                Text("c")
              }
            }
            Text("d")
          }
        }
      },
      step2 = {
        Box(modifier = reuse) {
          Text("e")
          Column {
            Column {
              Text("f")
              Column {
                Text("g")
              }
            }
            Text("h")
          }
        }
      },
      step2Value = BoxValue(
        modifier = reuse,
        children = listOf(
          TextValue(text = "e"),
          ColumnValue(
            children = listOf(
              ColumnValue(
                children = listOf(
                  TextValue(text = "f"),
                  ColumnValue(
                    children = listOf(
                      TextValue(text = "g"),
                    ),
                  ),
                ),
              ),
              TextValue(text = "h"),
            ),
          ),
        ),
      ),
    )
  }

  @Test
  @Ignore // We don't recycle the Box when its parent is removed.
  fun reuseNotAtRoot() = runTest {
    val (step1Widgets, step2Widgets) = assertReuse(
      step1 = {
        Column {
          Box(modifier = reuse) {
            Text("a")
          }
        }
      },
      step2 = {
        Column {
          Box(modifier = reuse) {
            Text("b")
          }
        }
      },
      assertFullSubtreesEqual = false,
      step2Value = ColumnValue(
        children = listOf(
          BoxValue(
            modifier = reuse,
            children = listOf(
              TextValue(text = "b"),
            ),
          ),
        ),
      ),
    )

    val step1Column = step1Widgets.single() as MutableColumn
    val step2Column = step2Widgets.single() as MutableColumn
    assertThat(step2Column).isNotEqualTo(step1Column)
    assertThat(step2Column.children).isEqualTo(step1Column.children)
  }

  @Test
  fun reuseMultipleChildrenWithDifferentTags() = runTest {
    assertReuse(
      step1 = {
        Split(
          modifier = reuse,
          left = {
            Text("a")
            Text("b")
            Text("c")
          },
          right = {
            Text("d")
            Text("e")
          },
        )
      },
      step2 = {
        Split(
          modifier = reuse,
          left = {
            Text("f")
            Text("g")
            Text("h")
          },
          right = {
            Text("i")
            Text("j")
          },
        )
      },
      step2Value = SplitValue(
        modifier = reuse,
        left = listOf(
          TextValue(text = "f"),
          TextValue(text = "g"),
          TextValue(text = "h"),
        ),
        right = listOf(
          TextValue(text = "i"),
          TextValue(text = "j"),
        ),
      ),
    )
  }

  @Test
  fun noReuseWhenNewNodeHasMoreChildren() = runTest {
    assertNoReuse(
      step1 = {
        Box(modifier = reuse) {
          Text("a")
        }
      },
      step2 = {
        Box(modifier = reuse) {
          Text("a")
          Text("b")
        }
      },
    )
  }

  @Test
  fun noReuseWhenNewNodeHasFewerChildren() = runTest {
    assertNoReuse(
      step1 = {
        Box(modifier = reuse) {
          Text("a")
          Text("b")
        }
      },
      step2 = {
        Box(modifier = reuse) {
          Text("a")
        }
      },
    )
  }

  @Test
  fun noReuseWhenNodeHasDifferentType() = runTest {
    assertNoReuse(
      step1 = {
        Split(
          modifier = reuse,
          left = {
            Text("a")
          },
          right = {
          },
        )
      },
      step2 = {
        Box(modifier = reuse) {
          Text("a")
        }
      },
    )
  }

  @Test
  fun noReuseWhenChildNodeHasDifferentType() = runTest {
    assertNoReuse(
      step1 = {
        Box(modifier = reuse) {
          Text("a")
        }
      },
      step2 = {
        Box(modifier = reuse) {
          Button("a", onClick = null)
        }
      },
    )
  }

  @Test
  fun noReuseWhenChildHasDifferentChildrenTag() = runTest {
    assertNoReuse(
      step1 = {
        Split(
          modifier = reuse,
          left = {
            Text("a")
          },
          right = {
          },
        )
      },
      step2 = {
        Split(
          modifier = reuse,
          left = {
          },
          right = {
            Text("a")
          },
        )
      },
    )
  }

  @Test
  fun noReuseWhenReuseIdNotSet() = runTest {
    assertNoReuse(
      step1 = {
        Box {
          Text("a")
        }
      },
      step2 = {
        Box {
          Text("a")
        }
      },
    )
  }

  @Test
  fun noReuseWhenReusedNodeIsAddedAndThenRemovedInSameUpdate() = runTest {
    assertNoReuse(
      step1 = {
        Box(modifier = reuse) {
          Text("a")
        }
      },
      beforeStep2CompositionOnly = {
        Box(modifier = reuse) {
          Text("a")
        }
      },
      step2 = {
      },
    )
  }

  @Test
  fun noReuseWhenChildIsAddedAndThenRemovedInSameUpdate() = runTest {
    assertNoReuse(
      step1 = {
        Box(modifier = reuse) {
          Text("a")
        }
      },
      beforeStep2CompositionOnly = {
        Box(modifier = reuse) {
          Text("a")
        }
      },
      step2 = {
        Box(modifier = reuse) {
        }
      },
    )
  }

  @Test
  fun noReuseWhenChildAddsAreOutOfOrder() = runTest {
  }
}
