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

import app.cash.zipline.EventListener as ZiplineEventListener
import app.cash.redwood.protocol.widget.ProtocolMismatchHandler
import app.cash.zipline.Call
import app.cash.zipline.CallResult
import app.cash.zipline.ZiplineService

internal class EventPublisher(
  private val listener: EventListener,
) {
  private var applications = mapOf<String, TreehouseApp<*>>()

  fun appCreated(app: TreehouseApp<*>) {
    applications = applications + (app.spec.name to app)
    listener.appCreated(app)
  }

  fun appCanceled(app: TreehouseApp<*>) {
    applications = applications - app.spec.name
    listener.appCanceled(app)
  }

  val ziplineEventListener = object : ZiplineEventListener() {
    override fun applicationLoadStart(applicationName: String, manifestUrl: String?): Any? {
      val app = applications[applicationName]!!
      return listener.codeLoadStart(app, manifestUrl)
    }

    override fun applicationLoadEnd(applicationName: String, manifestUrl: String?, startValue: Any?) {
      val app = applications[applicationName]!!
      listener.codeLoadSuccess(app, manifestUrl, startValue)
    }

    override fun applicationLoadFailed(
      applicationName: String,
      manifestUrl: String?,
      exception: Exception,
      startValue: Any?,
    ) {
      val app = applications[applicationName]!!
      listener.codeLoadFailed(app, manifestUrl, exception, startValue)
    }

    override fun bindService(name: String, service: ZiplineService) {
      listener.bindService(name, service)
    }

    override fun callStart(call: Call): Any? {
      return listener.callStart(call)
    }

    override fun callEnd(call: Call, result: CallResult, startValue: Any?) {
      listener.callEnd(call, result, startValue)
    }

    override fun downloadStart(applicationName: String, url: String): Any? {
      val app = applications[applicationName]!!
      return listener.downloadStart(app, url)
    }

    override fun downloadEnd(applicationName: String, url: String, startValue: Any?) {
      val app = applications[applicationName]!!
      listener.downloadSuccess(app, url, startValue)
    }

    override fun downloadFailed(
      applicationName: String,
      url: String,
      exception: Exception,
      startValue: Any?,
    ) {
      val app = applications[applicationName]!!
      listener.downloadFailed(app, url, exception, startValue)
    }

    override fun manifestParseFailed(applicationName: String, url: String?, exception: Exception) {
      val app = applications[applicationName]!!
      listener.manifestParseFailed(app, url, exception)
    }

    override fun takeService(name: String, service: ZiplineService) {
      listener.takeService(name, service)
    }

    override fun serviceLeaked(name: String) {
      listener.serviceLeaked(name)
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

    override fun onUnknownProperty(kind: Int, tag: UInt) {
      listener.onUnknownProperty(app, kind, tag)
    }
  }
}
