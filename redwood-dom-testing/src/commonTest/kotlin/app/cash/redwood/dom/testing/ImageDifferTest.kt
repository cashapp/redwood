/*
 * Copyright (C) 2025 Square, Inc.
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
package app.cash.redwood.dom.testing

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlinx.browser.document
import kotlinx.coroutines.test.runTest
import org.w3c.dom.Element
import org.w3c.files.Blob

internal class ImageDifferTest {
  @Test
  fun exactMatch() = runTest {
    val yellowRect = document.createElement("div").apply {
      setAttribute(
        "style",
        """
        |background-color: #ffff00;
        |width: 100px;
        |height: 100px;
        """.trimMargin(),
      )
    }.toBlob()

    val diffResult = ImageDiffer().compare(yellowRect, yellowRect)
    assertThat(diffResult.isDifferent).isEqualTo(false)
  }

  @Test
  fun tenPercentRGBDiff() = runTest {
    val yellowRect = document.createElement("div").apply {
      setAttribute(
        "style",
        """
        |background-color: #ffff00;
        |width: 100px;
        |height: 100px;
        """.trimMargin(),
      )
    }.toBlob()
    val yellowRect10PercentBlue = document.createElement("div").apply {
      setAttribute(
        "style",
        """
        |background-color: #ffff00;
        |width: 100px;
        |height: 100px;
        """.trimMargin(),
      )
      appendChild(
        document.createElement("div").apply {
          setAttribute(
            "style",
            """
            |background-color: #0000ff;
            |width: 10px;
            |height: 100px;
            """.trimMargin(),
          )
        },
      )
    }.toBlob()

    val diffResult = ImageDiffer().compare(yellowRect, yellowRect10PercentBlue)
    assertThat(diffResult.isDifferent).isEqualTo(true)
    assertThat(diffResult.percentDifference).isEqualTo(10f)
  }

  @Test
  fun twentyPercentAlphaDiff() = runTest {
    val yellowRect = document.createElement("div").apply {
      setAttribute(
        "style",
        """
        |background-color: #ffff00;
        |width: 100px;
        |height: 100px;
        """.trimMargin(),
      )
    }.toBlob()
    val transparentYellowRect = document.createElement("div").apply {
      setAttribute(
        "style",
        """
        |background-color: #ffff00;
        |opacity: 0.8;
        |width: 100px;
        |height: 100px;
        """.trimMargin(),
      )
    }.toBlob()

    val diffResult = ImageDiffer().compare(yellowRect, transparentYellowRect)
    assertThat(diffResult.isDifferent).isEqualTo(true)
    assertThat(diffResult.percentDifference).isEqualTo(20f)
  }

  /**
   * Compare a 100x200 transparent image against a 200x100 transparent image. We consider any pixel
   * that isn't in the bounds of the other image to be different, so these two rectangles differ by
   * 75%.
   */
  @Test
  fun fullAlphaSizeMismatch() = runTest {
    val transparent200x100 = document.createElement("div").apply {
      setAttribute(
        "style",
        """
        |background-color: #00000000;
        |width: 200px;
        |height: 100px;
        """.trimMargin(),
      )
    }.toBlob()
    val transparent100x200 = document.createElement("div").apply {
      setAttribute(
        "style",
        """
        |background-color: #00000000;
        |width: 100px;
        |height: 200px;
        """.trimMargin(),
      )
    }.toBlob()

    val diffResult = ImageDiffer().compare(transparent200x100, transparent100x200)
    assertThat(diffResult.isDifferent).isEqualTo(true)
    assertThat(diffResult.percentDifference).isEqualTo(75f)
  }

  internal suspend fun Element.toBlob(): Blob = DomSnapshotter().snapshot(this, Frame.None, false).images.first()!!
}
