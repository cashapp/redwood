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
package app.cash.redwood.treehouse

import kotlinx.coroutines.CoroutineScope

internal interface FrameClock {
  /** Run this clock until [scope] is canceled. */
  fun start(
    scope: CoroutineScope,
    dispatchers: TreehouseDispatchers,
  )

  /**
   * Request a call to [AppLifecycle.sendFrame]. It is an error to call [requestFrame] again before
   * that call is made.
   *
   * It is an error to call this before [start].
   */
  fun requestFrame(appLifecycle: AppLifecycle)
}
