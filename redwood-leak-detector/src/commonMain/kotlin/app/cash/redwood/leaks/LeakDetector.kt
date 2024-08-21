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

import app.cash.redwood.leaks.LeakDetector.Callback
import kotlin.time.Duration
import kotlin.time.TimeSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch

/** Watch references and detect when they leak. */
@RedwoodLeakApi
public interface LeakDetector : AutoCloseable {
  /**
   * Add [reference] as a watched instance that is expected to be garbage collected soon.
   *
   * This function is safe to call from any thread.
   *
   * @param note Information about why [reference] is being watched.
   */
  public fun watchReference(reference: Any, note: String)

  /**
   * Suspend until all current watched references are either collected or detected as a leak, and
   * throw an exception for any new calls to [watchReference].
   */
  public suspend fun awaitClose()

  /**
   * Immediately stop all leak detection of current watched references, and throw an exception for
   * any new calls to [watchReference].
   */
  override fun close()

  public companion object {
    /**
     * Create a time-based [LeakDetector] which reports leaks if watched references
     * have not been garbage collected before the [leakThreshold].
     *
     * This function may return a [no-op detector][none] if the platform does not support weak references.
     */
    public fun timeBasedIn(
      scope: CoroutineScope,
      timeSource: TimeSource,
      leakThreshold: Duration,
      callback: Callback,
    ): LeakDetector {
      if (hasWeakReference()) {
        return TimeBasedLeakDetector(scope, detectGc(), timeSource, leakThreshold, callback)
      }
      return none()
    }

    /** A [LeakDetector] that does not watch references for leaks. */
    public fun none(): LeakDetector = NoOpLeakDetector()
  }

  public fun interface Callback {
    public fun onReferenceLeaked(reference: Any, note: String)
  }
}

internal class TimeBasedLeakDetector(
  private val scope: CoroutineScope,
  internal val gc: Gc,
  private val timeSource: TimeSource,
  private val leakThreshold: Duration,
  private val callback: Callback,
) : LeakDetector {
  private var closed = false

  private val gcJob = Job()
  private val gcNotifications = flow {
    val checkPeriod = leakThreshold / 2
    while (true) {
      delay(checkPeriod)
      gc.collect()
      emit(Unit)
    }
  }.shareIn(
    scope = CoroutineScope(scope.coroutineContext + gcJob),
    started = SharingStarted.WhileSubscribed(),
  )

  private val watchJob = Job()

  override fun watchReference(reference: Any, note: String) {
    check(!closed) { "closed" }
    internalWatch(WeakReference(reference), note)
  }

  private fun internalWatch(weakReference: WeakReference<Any>, reason: String) {
    scope.launch(watchJob, start = UNDISPATCHED) {
      val watchedAt = timeSource.markNow()
      gcNotifications.collect {
        val reference = weakReference.get() ?: cancel()
        if (watchedAt.elapsedNow() >= leakThreshold) {
          callback.onReferenceLeaked(reference, reason)
          cancel()
        }
      }
    }
  }

  override suspend fun awaitClose() {
    closed = true
    // Wait for all active watches to complete.
    watchJob.children.forEach { it.join() }
    watchJob.cancel()
    gcJob.cancel()
  }

  override fun close() {
    closed = true
    watchJob.cancel()
    gcJob.cancel()
  }
}

private class NoOpLeakDetector : LeakDetector {
  private var closed = false

  override fun watchReference(reference: Any, note: String) {
    check(!closed) { "closed" }
  }

  override suspend fun awaitClose() {
    closed = true
  }

  override fun close() {
    closed = true
  }
}
