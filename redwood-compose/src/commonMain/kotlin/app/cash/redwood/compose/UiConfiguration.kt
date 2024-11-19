/*
 * Copyright (C) 2022 Square, Inc.
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
package app.cash.redwood.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.compositionLocalWithComputedDefaultOf
import app.cash.redwood.ui.Margin
import app.cash.redwood.ui.UiConfiguration
import app.cash.redwood.ui.dp

/**
 * Provide various configurations of the UI.
 * This value will be bound automatically.
 * Custom values should only be provided into a composition for testing purposes!
 *
 * @see UiConfiguration.Companion.current
 */
public val LocalUiConfiguration: ProvidableCompositionLocal<UiConfiguration> =
  compositionLocalOf {
    throw AssertionError("UiConfiguration was not provided!")
  }

/**
 * Expose various configuration properties of the host.
 *
 * @see UiConfiguration
 */
@Suppress("unused") // Emulating a CompositionLocal.
public val UiConfiguration.Companion.current: UiConfiguration
  @Composable
  @ReadOnlyComposable
  get() = LocalUiConfiguration.current

/**
 * The insets of the viewport that the composition is not safe for content because it is obscured
 * by the system.
 *
 * To avoid double-consuming insets, consume them using [ConsumeInsets].
 */
public val LocalViewInsets: ProvidableCompositionLocal<Margin> =
  compositionLocalWithComputedDefaultOf {
    LocalUiConfiguration.currentValue.viewInsets
  }

/**
 * Consume [LocalViewInsets] for the execution of [block]. This will consume all available insets
 * unless [maximumValue] is set.
 *
 * The parameter to [block] is the actual insets that were consumed.
 *
 * Note that this function **does not** apply to the code preceding or following the caller, so it
 * is possible for insets to be consumed multiple times. In layouts like `Box`, this may be the
 * desired behavior.
 */
@Composable
public fun ConsumeInsets(
  maximumValue: Margin? = null,
  block: @Composable (Margin) -> Unit,
) {
  val previous = LocalViewInsets.current

  val consumed: Margin
  val updated: Margin
  if (maximumValue != null) {
    consumed = previous.coerceAtMost(maximumValue)
    updated = previous - consumed
  } else {
    consumed = previous
    updated = Margin.Zero
  }

  CompositionLocalProvider(LocalViewInsets provides updated) {
    block(consumed)
  }
}

private fun Margin.coerceAtMost(maximumValue: Margin): Margin {
  return Margin(
    start = start.value.coerceAtMost(maximumValue.start.value).dp,
    end = end.value.coerceAtMost(maximumValue.end.value).dp,
    top = top.value.coerceAtMost(maximumValue.top.value).dp,
    bottom = bottom.value.coerceAtMost(maximumValue.bottom.value).dp,
  )
}

private operator fun Margin.minus(other: Margin): Margin {
  return Margin(
    start = (start.value - other.start.value).dp,
    end = (end.value - other.end.value).dp,
    top = (top.value - other.top.value).dp,
    bottom = (bottom.value - other.bottom.value).dp,
  )
}
