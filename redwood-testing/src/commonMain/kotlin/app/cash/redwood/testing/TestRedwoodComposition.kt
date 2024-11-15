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
package app.cash.redwood.testing

import androidx.compose.runtime.BroadcastFrameClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.SaveableStateRegistry
import app.cash.redwood.compose.RedwoodComposition
import app.cash.redwood.compose.current
import app.cash.redwood.ui.Cancellable
import app.cash.redwood.ui.OnBackPressedCallback
import app.cash.redwood.ui.OnBackPressedDispatcher
import app.cash.redwood.ui.UiConfiguration
import app.cash.redwood.widget.Widget
import app.cash.redwood.widget.WidgetSystem
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.plus
import kotlinx.coroutines.withTimeout

/**
 * Performs Redwood composition strictly for testing.
 */
@Suppress("FunctionName") // ktlint bug
public fun <W : Any, S> TestRedwoodComposition(
  scope: CoroutineScope,
  widgetSystem: WidgetSystem<W>,
  container: Widget.Children<W>,
  onBackPressedDispatcher: OnBackPressedDispatcher = NoOpOnBackPressedDispatcher,
  savedState: TestSavedState? = null,
  initialUiConfiguration: UiConfiguration = UiConfiguration(),
  createSnapshot: () -> S,
): TestRedwoodComposition<S> {
  return RealTestRedwoodComposition(
    scope,
    widgetSystem,
    container,
    onBackPressedDispatcher,
    savedState,
    initialUiConfiguration,
    createSnapshot,
  )
}

public interface TestRedwoodComposition<S> : RedwoodComposition {
  /**
   * Returns a snapshot, waiting if necessary for changes to occur since the previous snapshot.
   *
   * @throws TimeoutCancellationException if no new snapshot is produced before [timeout].
   */
  public suspend fun awaitSnapshot(timeout: Duration = 1.seconds): S

  /**
   * The mutable [UiConfiguration] instance bound to [UiConfiguration.current][current]
   * inside the composition.
   */
  public val uiConfigurations: MutableStateFlow<UiConfiguration>

  public fun saveState(): TestSavedState
}

/** An opaque type representing the saved state of a test composition. */
public sealed class TestSavedState

private class MapBasedTestSavedState(
  val values: Map<String, List<Any?>>,
) : TestSavedState()

/** Performs Redwood composition strictly for testing. */
private class RealTestRedwoodComposition<W : Any, S>(
  scope: CoroutineScope,
  widgetSystem: WidgetSystem<W>,
  container: Widget.Children<W>,
  onBackPressedDispatcher: OnBackPressedDispatcher,
  savedState: TestSavedState?,
  initialUiConfiguration: UiConfiguration,
  private val createSnapshot: () -> S,
) : TestRedwoodComposition<S> {
  /** Emits frames manually in [awaitSnapshot]. */
  private val clock = BroadcastFrameClock()
  private var timeNanos = 0L
  private val frameDelay = 1.seconds / 60
  private var contentSet = false
  private var hasChanges = false

  override val uiConfigurations = MutableStateFlow(initialUiConfiguration)

  private val savedStateRegistry = SaveableStateRegistry(
    restoredValues = savedState?.let {
      when (it) {
        is MapBasedTestSavedState -> it.values
      }
    },
    canBeSaved = { true },
  )

  override fun saveState() = MapBasedTestSavedState(savedStateRegistry.performSave())

  private val composition = RedwoodComposition(
    scope = scope + clock,
    container = container,
    onBackPressedDispatcher = onBackPressedDispatcher,
    saveableStateRegistry = savedStateRegistry,
    uiConfigurations = uiConfigurations,
    widgetSystem = widgetSystem,
    onEndChanges = { hasChanges = true },
  )

  override fun setContent(content: @Composable () -> Unit) {
    contentSet = true
    composition.setContent(content)
  }

  override suspend fun awaitSnapshot(timeout: Duration): S {
    check(contentSet) { "setContent must be called first!" }

    // Await changes, sending at least one frame while we wait.
    withTimeout(timeout) {
      while (true) {
        clock.sendFrame(timeNanos)
        if (hasChanges) break

        timeNanos += frameDelay.inWholeNanoseconds
        delay(frameDelay)
      }
    }

    hasChanges = false
    return createSnapshot()
  }

  override fun cancel() {
    composition.cancel()
  }
}

public object NoOpOnBackPressedDispatcher : OnBackPressedDispatcher {
  override fun addCallback(onBackPressedCallback: OnBackPressedCallback): Cancellable {
    return object : Cancellable {
      override fun cancel() = Unit
    }
  }
}
