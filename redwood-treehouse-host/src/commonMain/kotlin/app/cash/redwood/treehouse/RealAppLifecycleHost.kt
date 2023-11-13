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

import app.cash.redwood.protocol.EventTag
import app.cash.redwood.protocol.Id
import app.cash.redwood.protocol.WidgetTag

internal class RealAppLifecycleHost(
  private val appLifecycle: AppLifecycle,
  private val frameClock: FrameClock,
  private val eventPublisher: EventPublisher,
  private val codeSession: CodeSession<*>,
) : AppLifecycle.Host {
  override fun requestFrame() {
    frameClock.requestFrame(appLifecycle)
  }

  override fun onUnknownEvent(
    widgetTag: WidgetTag,
    tag: EventTag,
  ) {
    eventPublisher.onUnknownEvent(widgetTag, tag)
  }

  override fun onUnknownEventNode(
    id: Id,
    tag: EventTag,
  ) {
    eventPublisher.onUnknownEventNode(id, tag)
  }

  override fun handleUncaughtException(exception: Throwable) {
    codeSession.handleUncaughtException(exception)
  }
}
