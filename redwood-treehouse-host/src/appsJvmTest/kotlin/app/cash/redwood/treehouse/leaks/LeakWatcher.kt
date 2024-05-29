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
package app.cash.redwood.treehouse.leaks

import assertk.assertThat
import assertk.assertions.isNotNull
import java.lang.ref.PhantomReference
import java.lang.ref.ReferenceQueue

/**
 * Use this to confirm that potentially leaked objects are eligible for garbage collection when they
 * should be.
 *
 * Be careful to not retain a reference to the allocated object in the calling code. The runtime is
 * known to not collect objects that are referenced in the current stack frame, even if they are no
 * longer needed by subsequent code in that stack frame. We support this here by accepting a
 * function to allocate an instance, rather than accepting an instance directly.
 *
 * This started as a class in Zipline:
 * https://github.com/cashapp/zipline/blob/trunk/zipline/src/jniTest/kotlin/app/cash/zipline/LeakWatcher.kt
 */
class LeakWatcher<T>(
  allocate: () -> T,
) {
  /** Null after a call to [assertNotLeaked]. */
  private var reference: T? = allocate()

  /**
   * Asserts that the subject contains a transitive reference back to itself. This is problematic
   * when the subject could be a reference-counted Swift instance.
   */
  fun assertObjectInReferenceCycle() {
    val reference = this.reference ?: error("cannot call this after assertNotLeaked()")
    assertThat(JvmHeap.findCycle(reference)).isNotNull()
  }

  /**
   * Asserts that the subject is not strongly reachable from any garbage collection roots.
   *
   * This function works by requesting a garbage collection and confirming that the object is
   * collected in the process. An alternate, more robust implementation could do a heap dump and
   * report the shortest paths from GC roots if any exist.
   */
  fun assertNotLeaked() {
    if (reference == null) return // Idempotent.

    val shortestCycle = JvmHeap.findCycle(reference!!)
    if (shortestCycle != null) {
      throw AssertionError("object is in a retain cycle: $shortestCycle")
    }

    val referenceQueue = ReferenceQueue<T>()
    val phantomReference = PhantomReference(reference!!, referenceQueue)
    reference = null

    awaitGarbageCollection()

    if (referenceQueue.poll() != phantomReference) {
      throw AssertionError("object was not garbage collected")
    }
  }

  /**
   * See FinalizationTester for discussion on how to best trigger GC in tests.
   * https://android.googlesource.com/platform/libcore/+/master/support/src/test/java/libcore/
   * java/lang/ref/FinalizationTester.java
   */
  private fun awaitGarbageCollection() {
    Runtime.getRuntime().gc()
    Thread.sleep(100)
    System.runFinalization()
  }
}
