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
package app.cash.redwood.leaks

import kotlin.time.Duration
import kotlin.time.TimeMark
import kotlin.time.TimeSource

/** Watch references and detect when they leak. */
@RedwoodLeakApi
public interface LeakDetector {
  /**
   * Add [reference] as a watched instance that is expected to be garbage collected soon.
   *
   * This function is safe to call from any thread.
   */
  public fun watchReference(reference: Any, name: String)

  /**
   * Trigger garbage collection and determine if any watched references have leaked per this
   * instance's leak policy.
   *
   * This function is safe to call from any thread.
   */
  public suspend fun checkLeaks()

  public object None : LeakDetector {
    override fun watchReference(reference: Any, name: String) {}
    override suspend fun checkLeaks() {}
  }

  public companion object {
    /**
     * Create a time-based [LeakDetector] which reports leaks to [listener] if watched references
     * have not been garbage collected before the [leakThreshold].
     *
     * This function may return [None] if the platform does not support weak references, in which
     * case [listener] will never be invoked.
     */
    public fun timeBased(
      listener: LeakListener,
      timeSource: TimeSource,
      leakThreshold: Duration,
    ): LeakDetector {
      if (hasWeakReference()) {
        return TimeBasedLeakDetector(listener, timeSource, leakThreshold)
      }
      return None
    }
  }
}

@RedwoodLeakApi
public interface LeakListener {
  public fun onReferenceCollected(name: String)
  public fun onReferenceLeaked(name: String, alive: Duration)
}

internal class TimeBasedLeakDetector(
  private val listener: LeakListener,
  private val timeSource: TimeSource,
  private val leakThreshold: Duration,
) : LeakDetector {
  internal val gc = detectGc()
  private val watchedReferences = concurrentMutableListOf<WatchedReference>()

  override fun watchReference(reference: Any, name: String) {
    watchedReferences += WatchedReference(
      name = name,
      weakReference = WeakReference(reference),
      watchedAt = timeSource.markNow(),
    )
  }

  override suspend fun checkLeaks() {
    gc.collect()

    watchedReferences.removeIf { watchedReference ->
      if (watchedReference.weakReference.get() == null) {
        listener.onReferenceCollected(watchedReference.name)
        return@removeIf true
      }

      val alive = watchedReference.watchedAt.elapsedNow()
      if (alive >= leakThreshold) {
        listener.onReferenceLeaked(watchedReference.name, alive)
        return@removeIf true
      }

      false
    }
  }

  private class WatchedReference(
    val name: String,
    val weakReference: WeakReference<Any>,
    val watchedAt: TimeMark,
  )
}
