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
package app.cash.redwood.treehouse

import kotlin.native.ObjCName
import kotlinx.coroutines.CoroutineDispatcher

/**
 * One of the trickiest things Treehouse needs to do is balance its two dispatchers:
 *
 *  * [ui] executes dispatched tasks on the platform's UI thread.
 *  * [zipline] executes dispatched tasks on the thread where downloaded code executes.
 *
 * This class makes it easier to specify invariants on which dispatcher is expected for which work.
 */
@ObjCName("TreehouseDispatchers", exact = true)
public interface TreehouseDispatchers {
  public val ui: CoroutineDispatcher

  public val zipline: CoroutineDispatcher

  /**
   * Confirm that this is being called on the UI thread.
   *
   * @throws IllegalStateException if invoked on non-UI thread.
   */
  public fun checkUi()

  /**
   * Confirm that this is being called on the zipline thread.
   *
   * @throws IllegalStateException if invoked on non-zipline thread.
   */
  public fun checkZipline()

  /**
   * Release the threads owned by this instance. On most platforms this will not release the UI
   * thread, as it is not owned by this instance.
   *
   * Most applications should not to call this; instead they should allow these dispatchers to
   * run until the process exits. This may be useful in tests.
   */
  public fun close()
}

/**
 * We configure an 8 MiB thread size because we've experimentally found that's sufficient for our
 * guest programs.
 */
internal val ZIPLINE_THREAD_STACK_SIZE = 8 * 1024 * 1024
