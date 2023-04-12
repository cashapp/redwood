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

import app.cash.zipline.Zipline
import app.cash.zipline.ZiplineScope
import app.cash.zipline.withScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/** The host state for a single code load. We get a new session each time we get new code. */
internal class ZiplineSession<A : AppService>(
  val app: TreehouseApp<A>,
  val appScope: CoroutineScope,
  val sessionScope: CoroutineScope,
  val appService: A,
  val zipline: Zipline,
  val isInitialLaunch: Boolean,
) {
  private val ziplineScope = ZiplineScope()

  fun startFrameClock() {
    sessionScope.launch(app.dispatchers.zipline) {
      tickFrameClock(
        dispatchers = app.dispatchers,
        clockService = appService.withScope(ziplineScope).frameClockService,
      )
    }
  }

  fun cancel() {
    appScope.launch(app.dispatchers.zipline) {
      sessionScope.cancel()
      ziplineScope.close()
      zipline.close()
    }
  }
}
