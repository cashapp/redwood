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
package app.cash.redwood.protocol

import assertk.assertAll
import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isGreaterThan
import assertk.assertions.isInstanceOf
import assertk.assertions.isLessThan
import assertk.assertions.message
import kotlin.test.Test

class RedwoodVersionTest {
  @Test fun parsing() {
    // Too few numbers.
    assertInvalidVersion("1")
    assertInvalidVersion("1-beta")
    assertInvalidVersion("1.1")
    assertInvalidVersion("1.1-beta")

    // Trailing dots.
    assertInvalidVersion("1.")
    assertInvalidVersion("1.1.")
    assertInvalidVersion("1.1.1.")

    // Non-digit numbers.
    assertInvalidVersion("a.1.1")
    assertInvalidVersion("1.a.1")
    assertInvalidVersion("1.1.a")

    // Too many numbers.
    assertInvalidVersion("1.0.0.0")
    assertInvalidVersion("1.0.0.0-beta")

    // Leading zeros.
    assertInvalidVersion("01.1.1")
    assertInvalidVersion("1.01.1")
    assertInvalidVersion("1.1.01")

    // Trailing label separator.
    assertInvalidVersion("1-")
    assertInvalidVersion("1.0-")
    assertInvalidVersion("1.0.0-")
    assertInvalidVersion("1.0.0.0-")

    // Invalid label characters.
    assertInvalidVersion("1.0.0-h∑y")
  }

  private fun assertInvalidVersion(version: String) {
    assertFailure { RedwoodVersion(version) }
      .isInstanceOf<IllegalArgumentException>()
      .message()
      .isEqualTo("Invalid version format: $version")
  }

  @Test fun ordering() {
    // Examples from docs.
    assertVersionOrdering("2.1.0", "1.2.3")
    assertVersionOrdering("1.3.1", "1.2.0")
    assertVersionOrdering("1.1.4", "1.1.2")
    assertVersionOrdering("1.0.0", "1.0.0-beta")
    assertVersionOrdering("1.0.0-SNAPSHOT", "1.0.0-beta")
    assertVersionOrdering("1.0.0-beta", "1.0.0-alpha")
    assertVersionOrdering("1.0.0-beta2", "1.0.0-beta")

    // Multi-digit examples.
    assertVersionOrdering("20.0.0", "1.0.0")
    assertVersionOrdering("20.0.0", "10.0.0")
    assertVersionOrdering("1.20.0", "1.1.0")
    assertVersionOrdering("1.20.0", "1.10.0")
    assertVersionOrdering("1.1.20", "1.1.0")
    assertVersionOrdering("1.1.20", "1.1.10")

    // Label sorting.
    assertVersionOrdering("1.0.0-z", "1.0.0-a")
    assertVersionOrdering("1.0.0-az", "1.0.0-a")
    assertVersionOrdering("1.0.0-beta2", "1.0.0-beta10")
    assertVersionOrdering("1.0.0-beta10", "1.0.0-beta02")
    assertVersionOrdering("1.0.0-beta20", "1.0.0-beta10")
  }

  private fun assertVersionOrdering(newerVersion: String, olderVersion: String) {
    val newer = RedwoodVersion(newerVersion)
    val older = RedwoodVersion(olderVersion)
    assertAll {
      assertThat(newer).isGreaterThan(older)
      assertThat(older).isLessThan(newer)
    }
  }
}
