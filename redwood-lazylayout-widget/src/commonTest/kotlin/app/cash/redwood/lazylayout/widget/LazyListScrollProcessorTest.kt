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

import app.cash.redwood.lazylayout.api.ScrollItemIndex
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import kotlin.test.Test

class LazyListScrollProcessorTest {
  private val processor = FakeScrollProcessor()

  @Test
  fun programmaticScrollDeferredUntilOnEndChanges() {
    processor.size = 30

    // Don't apply the scroll immediately; it should be held until onEndChanges(). Otherwise we'll
    // scroll while we're changing the list's content.
    processor.scrollItemIndex(ScrollItemIndex(0, 10))
    assertThat(processor.takeEvents()).isEmpty()

    processor.onEndChanges()
    assertThat(processor.takeEvents()).containsExactly("programmaticScroll(10)")
  }

  @Test
  fun programmaticScrollDeferredUntilItsWithinContentSize() {
    // Don't apply the scroll because we don't have enough rows!
    processor.scrollItemIndex(ScrollItemIndex(0, 10))
    processor.onEndChanges()
    assertThat(processor.takeEvents()).isEmpty()

    // Once we have enough rows we can apply the scroll.
    processor.size = 30
    processor.onEndChanges()
    assertThat(processor.takeEvents()).containsExactly("programmaticScroll(10)")
  }

  @Test
  fun programmaticScrollDiscardedAfterUserScroll() {
    processor.size = 30

    // Do a user scroll.
    processor.onUserScroll(5, 14)
    assertThat(processor.takeEvents()).containsExactly("userScroll(5, 14)")

    // Don't apply the programmatic scroll. That fights the user.
    processor.scrollItemIndex(ScrollItemIndex(0, 10))
    processor.onEndChanges()
    assertThat(processor.takeEvents()).isEmpty()
  }

  @Test
  fun programmaticScrollOnlyTriggeredOnce() {
    processor.size = 30

    processor.scrollItemIndex(ScrollItemIndex(0, 10))
    processor.onEndChanges()
    assertThat(processor.takeEvents()).containsExactly("programmaticScroll(10)")

    // Confirm onEndIndex() only applies its change once.
    processor.onEndChanges()
    assertThat(processor.takeEvents()).isEmpty()
  }

  @Test
  fun userScrollDeduplicated() {
    processor.size = 30

    // Do a user scroll.
    processor.onUserScroll(5, 14)
    assertThat(processor.takeEvents()).containsExactly("userScroll(5, 14)")

    // Another scroll with no change in visibility triggers no updates.
    processor.onUserScroll(5, 14)
    assertThat(processor.takeEvents()).isEmpty()

    // But a proper scroll will trigger updates.
    processor.onUserScroll(6, 15)
    assertThat(processor.takeEvents()).containsExactly("userScroll(6, 15)")
  }
}
