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
package app.cash.redwood.treehouse

import app.cash.redwood.treehouse.LayoutTester.Constraint
import app.cash.redwood.treehouse.LayoutTester.Subject
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class TreehouseUIViewLayoutTest {
  /** Confirm that the layout with the treehouse view is the same as the layout without it. */
  @Test
  fun layoutIsPassThrough() {
    for (horizontal in Constraint.entries) {
      for (vertical in Constraint.entries) {
        layoutIsPassThrough(Subject.Reference, horizontal, vertical)
        layoutIsPassThrough(Subject.TreehouseView, horizontal, vertical)
      }
    }
  }

  private fun layoutIsPassThrough(subject: Subject, horizontal: Constraint, vertical: Constraint) {
    val tester = LayoutTester(subject, horizontal, vertical)
    assertThat(tester.subjectFrame())
      .isEqualTo(
        Rectangle(
          x = horizontal.initialX,
          y = vertical.initialY,
          width = horizontal.initialWidth,
          height = vertical.initialHeight,
        ),
      )

    tester.shrinkSubject()
    assertThat(tester.subjectFrame())
      .isEqualTo(
        Rectangle(
          x = horizontal.shrunkX,
          y = vertical.shrunkY,
          width = horizontal.shrunkWidth,
          height = vertical.shrunkHeight,
        ),
      )

    tester.growSubject()
    assertThat(tester.subjectFrame())
      .isEqualTo(
        Rectangle(
          x = horizontal.grownX,
          y = vertical.grownY,
          width = horizontal.grownWidth,
          height = vertical.grownHeight,
        ),
      )
  }
}
