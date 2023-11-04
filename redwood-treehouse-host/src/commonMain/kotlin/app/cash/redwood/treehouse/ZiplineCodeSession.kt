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
import kotlinx.serialization.json.Json

internal class ZiplineCodeSession<A : AppService>(
  dispatchers: TreehouseDispatchers,
  eventPublisher: EventPublisher,
  appScope: CoroutineScope,
  appService: A,
  private val frameClockFactory: FrameClock.Factory,
  val zipline: Zipline,
) : CodeSession<A>(
  dispatchers = dispatchers,
  eventPublisher = eventPublisher,
  appScope = appScope,
  appService = appService,
) {
  private val ziplineScope = ZiplineScope()

  override val json: Json
    get() = zipline.json

  override fun ziplineStart() {
    val appLifecycle = appService.withScope(ziplineScope).appLifecycle

    val host = RealAppLifecycleHost(
      appLifecycle = appLifecycle,
      frameClock = frameClockFactory.create(scope, dispatchers),
      eventPublisher = eventPublisher,
      codeSession = this,
    )

    appLifecycle.start(host)
  }

  override fun ziplineStop() {
    ziplineScope.close()
    zipline.close()
  }

  override fun newServiceScope(): ServiceScope<A> {
    val ziplineScope = ZiplineScope()

    return object : ServiceScope<A> {
      override fun apply(appService: A): A {
        return appService.withScope(ziplineScope)
      }

      override fun close() {
        ziplineScope.close()
      }
    }
  }
}
