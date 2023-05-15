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
import kotlinx.coroutines.launch

internal class IosDisplayLinkClock : FrameClock {
  private lateinit var scope: CoroutineScope
  private lateinit var dispatchers: TreehouseDispatchers
  private lateinit var displayLinkTarget: DisplayLinkTarget

  /** Non-null if we're expecting a call to [tickClock]. */
  private var appLifecycle: AppLifecycle? = null

  override fun start(scope: CoroutineScope, dispatchers: TreehouseDispatchers) {
    this.scope = scope
    this.dispatchers = dispatchers
    this.displayLinkTarget = DisplayLinkTarget {
      unsubscribe()
      scope.launch(dispatchers.zipline) {
        appLifecycle?.sendFrame(0L)
        appLifecycle = null
      }
    }
  }

  override fun requestFrame(appLifecycle: AppLifecycle) {
    this.appLifecycle = appLifecycle
    this.displayLinkTarget.subscribe()
  }
}
