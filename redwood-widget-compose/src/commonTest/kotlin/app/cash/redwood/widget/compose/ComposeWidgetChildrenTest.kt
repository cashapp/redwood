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
package app.cash.redwood.widget.compose

import androidx.compose.runtime.AbstractApplier
import androidx.compose.runtime.BroadcastFrameClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composition
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.compositionLocalOf
import app.cash.redwood.widget.testing.AbstractWidgetChildrenTest
import kotlinx.coroutines.Job

class ComposeWidgetChildrenTest : AbstractWidgetChildrenTest<@Composable () -> Unit>() {
  override val children = ComposeWidgetChildren()

  override fun widget(name: String): @Composable () -> Unit {
    // There's no way to peek inside a composable function to identify it. Instead we
    // side-effect into a mutable list composition local in order to observe them.
    return { LocalNames.current += name }
  }

  override fun names(): List<String> {
    val clock = BroadcastFrameClock()
    val job = Job()
    val composeContext = clock + job

    val recomposer = Recomposer(composeContext)
    val composition = Composition(ThrowingApplier(), recomposer)

    // The initial call to setContent will recompose synchronously allowing us to
    // record the names and then immediately cancel the composition.
    val names = mutableListOf<String>()
    composition.setContent {
      CompositionLocalProvider(LocalNames provides names) {
        children.Render()
      }
    }

    job.cancel()
    composition.dispose()

    return names
  }
}

private val LocalNames = compositionLocalOf<MutableList<String>> {
  throw AssertionError()
}

private class ThrowingApplier : AbstractApplier<Unit>(Unit) {
  override fun insertBottomUp(index: Int, instance: Unit) = throw AssertionError()
  override fun insertTopDown(index: Int, instance: Unit) = throw AssertionError()
  override fun move(from: Int, to: Int, count: Int) = throw AssertionError()
  override fun remove(index: Int, count: Int) = throw AssertionError()
  override fun onClear() {}
}
