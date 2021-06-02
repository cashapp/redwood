/*
 * Copyright (C) 2021 Square, Inc.
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

package androidx.compose.runtime

import androidx.compose.runtime.snapshots.SnapshotMutableState
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.yield
import kotlin.native.identityHashCode
import kotlin.system.getTimeNanos
import kotlin.time.ExperimentalTime

// TODO actual thread local lol
internal actual open class ThreadLocal<T> actual constructor(
  initialValue: () -> T
) {
  private var value: T = initialValue()

  actual fun get(): T = value

  actual fun set(value: T) {
    this.value = value
  }
}

actual class AtomicReference<V> actual constructor(value: V) {
  private val delegate = atomic(value)

  actual fun get(): V = delegate.value

  actual fun set(value: V) {
    delegate.value = value
  }

  actual fun getAndSet(value: V): V =
    delegate.getAndSet(value)

  actual fun compareAndSet(expect: V, newValue: V): Boolean =
    delegate.compareAndSet(expect, newValue)
}

internal actual fun identityHashCode(instance: Any?): Int =
  instance.identityHashCode()

actual annotation class TestOnly

actual inline fun <R> synchronized(lock: Any, block: () -> R): R =
  block()

actual val DefaultMonotonicFrameClock: MonotonicFrameClock = MonotonicClockImpl()

@OptIn(ExperimentalTime::class)
private class MonotonicClockImpl : MonotonicFrameClock {
  override suspend fun <R> withFrameNanos(
    onFrame: (Long) -> R
  ): R {
    yield()
    return onFrame(getTimeNanos())
  }
}

internal actual object Trace {
  actual fun beginSection(name: String): Any? {
    return null
  }

  actual fun endSection(token: Any?) {
  }
}

actual annotation class CheckResult actual constructor(actual val suggest: String)

internal actual fun <T> createSnapshotMutableState(
  value: T,
  policy: SnapshotMutationPolicy<T>
): SnapshotMutableState<T> =
  SnapshotMutableStateImpl(value, policy)

//fixme: not actually thread local
internal actual class SnapshotThreadLocal<T> actual constructor() {
  private var value: T? = null

  actual fun get(): T? = value
  actual fun set(value: T?) {
    this.value = value
  }
}
