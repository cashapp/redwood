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

import app.cash.redwood.protocol.PropertyTag
import app.cash.redwood.protocol.widget.ProtocolMismatchHandler
import app.cash.zipline.Call
import app.cash.zipline.CallResult
import app.cash.zipline.EventListener as ZiplineEventListener
import app.cash.zipline.Zipline
import app.cash.zipline.ZiplineService

internal class EventPublisher(
  private val listener: EventListener,
) {
  private var nameToApplication = mapOf<String, TreehouseApp<*>>()

  fun appCreated(app: TreehouseApp<*>) {
    nameToApplication = nameToApplication + (app.spec.name to app)
    listener.appCreated(app)
  }

  fun appCanceled(app: TreehouseApp<*>) {
    nameToApplication = nameToApplication - app.spec.name
    listener.appCanceled(app)
  }

  val ziplineEventListener = object : ZiplineEventListener() {
    private var ziplineToApplication = mapOf<Zipline, TreehouseApp<*>>()

    override fun applicationLoadStart(applicationName: String, manifestUrl: String?): Any? {
      val app = nameToApplication[applicationName]!!
      return listener.codeLoadStart(app, manifestUrl)
    }

    override fun applicationLoadSuccess(
      applicationName: String,
      manifestUrl: String?,
      zipline: Zipline,
      startValue: Any?,
    ) {
      val app = nameToApplication[applicationName]!!
      ziplineToApplication = ziplineToApplication + (zipline to app)
      listener.codeLoadSuccess(app, manifestUrl, zipline, startValue)
    }

    override fun applicationLoadSkipped(
      applicationName: String,
      manifestUrl: String,
      startValue: Any?,
    ) {
      val app = nameToApplication[applicationName]!!
      listener.codeLoadSkipped(app, manifestUrl, startValue)
    }

    override fun applicationLoadFailed(
      applicationName: String,
      manifestUrl: String?,
      exception: Exception,
      startValue: Any?,
    ) {
      val app = nameToApplication[applicationName]!!
      listener.codeLoadFailed(app, manifestUrl, exception, startValue)
    }

    override fun bindService(zipline: Zipline, name: String, service: ZiplineService) {
      val app = ziplineToApplication[zipline]!!
      listener.bindService(app, name, service)
    }

    override fun callStart(zipline: Zipline, call: Call): Any? {
      val app = ziplineToApplication[zipline]!!
      return listener.callStart(app, call)
    }

    override fun callEnd(zipline: Zipline, call: Call, result: CallResult, startValue: Any?) {
      val app = ziplineToApplication[zipline]!!
      listener.callEnd(app, call, result, startValue)
    }

    override fun downloadStart(applicationName: String, url: String): Any? {
      val app = nameToApplication[applicationName]!!
      return listener.downloadStart(app, url)
    }

    override fun downloadEnd(applicationName: String, url: String, startValue: Any?) {
      val app = nameToApplication[applicationName]!!
      listener.downloadSuccess(app, url, startValue)
    }

    override fun downloadFailed(
      applicationName: String,
      url: String,
      exception: Exception,
      startValue: Any?,
    ) {
      val app = nameToApplication[applicationName]!!
      listener.downloadFailed(app, url, exception, startValue)
    }

    override fun manifestParseFailed(applicationName: String, url: String?, exception: Exception) {
      val app = nameToApplication[applicationName]!!
      listener.manifestParseFailed(app, url, exception)
    }

    override fun takeService(zipline: Zipline, name: String, service: ZiplineService) {
      val app = ziplineToApplication[zipline]!!
      listener.takeService(app, name, service)
    }

    override fun serviceLeaked(zipline: Zipline, name: String) {
      val app = ziplineToApplication[zipline] ?: return // Ignore leaks after Zipline.close.
      listener.serviceLeaked(app, name)
    }

    override fun ziplineClosed(zipline: Zipline) {
      val app = ziplineToApplication[zipline]!!
      ziplineToApplication = ziplineToApplication - zipline
      listener.codeUnloaded(app, zipline)
    }
  }

  fun protocolMismatchHandler(app: TreehouseApp<*>) = object : ProtocolMismatchHandler {
    override fun onUnknownWidget(kind: Int) {
      listener.onUnknownWidget(app, kind)
    }

    override fun onUnknownLayoutModifier(tag: Int) {
      listener.onUnknownLayoutModifier(app, tag)
    }

    override fun onUnknownChildren(kind: Int, tag: UInt) {
      listener.onUnknownChildren(app, kind, tag)
    }

    override fun onUnknownProperty(kind: Int, tag: PropertyTag) {
      listener.onUnknownProperty(app, kind, tag)
    }
  }
}
