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
package app.cash.redwood.treehouse

import app.cash.redwood.protocol.EventTag
import app.cash.redwood.protocol.WidgetTag
import app.cash.zipline.Zipline
import app.cash.zipline.ZiplineManifest

class FakeEventListener(
  private val eventLog: EventLog,
  private val app: TreehouseApp<*>,
) : EventListener() {
  class Factory(
    private val eventLog: EventLog,
  ) : EventListener.Factory {
    override fun create(app: TreehouseApp<*>, manifestUrl: String?) =
      FakeEventListener(eventLog, app)
  }

  override fun codeLoadStart(): Any? {
    eventLog += "${app.name}.codeLoadStart()"
    return null
  }

  override fun codeLoadSuccess(manifest: ZiplineManifest, zipline: Zipline, startValue: Any?) {
    eventLog += "${app.name}.codeLoadSuccess()"
  }

  override fun codeLoadFailed(exception: Exception, startValue: Any?) {
    eventLog += "${app.name}.codeLoadFailed()"
  }

  override fun codeUnloaded() {
    eventLog += "${app.name}.codeUnloaded()"
  }

  override fun unknownEvent(widgetTag: WidgetTag, tag: EventTag) {
    eventLog += "${app.name}.unknownEvent($widgetTag, $tag)"
  }

  override fun uncaughtException(exception: Throwable) {
    eventLog += "${app.name}.uncaughtException($exception)"
  }
}
