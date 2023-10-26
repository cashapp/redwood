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

import app.cash.redwood.protocol.ChildrenTag
import app.cash.redwood.protocol.EventTag
import app.cash.redwood.protocol.Id
import app.cash.redwood.protocol.ModifierTag
import app.cash.redwood.protocol.PropertyTag
import app.cash.redwood.protocol.WidgetTag
import app.cash.redwood.protocol.widget.ProtocolMismatchHandler
import app.cash.zipline.Call
import app.cash.zipline.CallResult
import app.cash.zipline.EventListener as ZiplineEventListener
import app.cash.zipline.Zipline
import app.cash.zipline.ZiplineManifest
import app.cash.zipline.ZiplineService

internal class RealEventPublisher(
  private val listener: EventListener,
  private val app: TreehouseApp<*>,
) : EventPublisher {
  override val ziplineEventListener = object : ZiplineEventListener() {
    override fun applicationLoadStart(
      applicationName: String,
      manifestUrl: String?,
    ): Any? {
      return listener.codeLoadStart(app, manifestUrl)
    }

    override fun applicationLoadSuccess(
      applicationName: String,
      manifestUrl: String?,
      manifest: ZiplineManifest,
      zipline: Zipline,
      startValue: Any?,
    ) {
      listener.codeLoadSuccess(app, manifestUrl, manifest, zipline, startValue)
    }

    override fun applicationLoadSkipped(
      applicationName: String,
      manifestUrl: String,
      startValue: Any?,
    ) {
      listener.codeLoadSkipped(app, manifestUrl, startValue)
    }

    override fun applicationLoadFailed(
      applicationName: String,
      manifestUrl: String?,
      exception: Exception,
      startValue: Any?,
    ) {
      listener.codeLoadFailed(app, manifestUrl, exception, startValue)
    }

    override fun bindService(
      zipline: Zipline,
      name: String,
      service: ZiplineService,
    ) {
      listener.bindService(app, name, service)
    }

    override fun callStart(
      zipline: Zipline,
      call: Call,
    ): Any? {
      return listener.callStart(app, call)
    }

    override fun callEnd(zipline: Zipline, call: Call, result: CallResult, startValue: Any?) {
      listener.callEnd(app, call, result, startValue)
    }

    override fun downloadStart(applicationName: String, url: String): Any? {
      return listener.downloadStart(app, url)
    }

    override fun downloadEnd(applicationName: String, url: String, startValue: Any?) {
      listener.downloadSuccess(app, url, startValue)
    }

    override fun downloadFailed(
      applicationName: String,
      url: String,
      exception: Exception,
      startValue: Any?,
    ) {
      listener.downloadFailed(app, url, exception, startValue)
    }

    override fun manifestVerified(
      applicationName: String,
      manifestUrl: String?,
      manifest: ZiplineManifest,
      verifiedKey: String,
    ) {
      listener.manifestVerified(app, manifestUrl, manifest, verifiedKey)
    }

    override fun moduleLoadStart(zipline: Zipline, moduleId: String): Any? {
      return listener.moduleLoadStart(app, zipline, moduleId)
    }

    override fun moduleLoadEnd(zipline: Zipline, moduleId: String, startValue: Any?) {
      listener.moduleLoadEnd(app, zipline, moduleId, startValue)
    }

    override fun initializerStart(zipline: Zipline, applicationName: String): Any? {
      return listener.initializerStart(app, zipline, applicationName)
    }

    override fun initializerEnd(zipline: Zipline, applicationName: String, startValue: Any?) {
      listener.initializerEnd(app, zipline, applicationName, startValue)
    }

    override fun mainFunctionStart(zipline: Zipline, applicationName: String): Any? {
      return listener.mainFunctionStart(app, zipline, applicationName)
    }

    override fun mainFunctionEnd(zipline: Zipline, applicationName: String, startValue: Any?) {
      listener.mainFunctionEnd(app, zipline, applicationName, startValue)
    }

    override fun manifestParseFailed(applicationName: String, url: String?, exception: Exception) {
      listener.manifestParseFailed(app, url, exception)
    }

    override fun takeService(zipline: Zipline, name: String, service: ZiplineService) {
      listener.takeService(app, name, service)
    }

    override fun serviceLeaked(zipline: Zipline, name: String) {
      listener.serviceLeaked(app, name)
    }

    override fun ziplineClosed(zipline: Zipline) {
      listener.codeUnloaded(app, zipline)
    }
  }

  override val widgetProtocolMismatchHandler = object : ProtocolMismatchHandler {
    override fun onUnknownWidget(tag: WidgetTag) {
      listener.onUnknownWidget(app, tag)
    }

    override fun onUnknownModifier(tag: ModifierTag) {
      listener.onUnknownModifier(app, tag)
    }

    override fun onUnknownChildren(widgetTag: WidgetTag, tag: ChildrenTag) {
      listener.onUnknownChildren(app, widgetTag, tag)
    }

    override fun onUnknownProperty(widgetTag: WidgetTag, tag: PropertyTag) {
      listener.onUnknownProperty(app, widgetTag, tag)
    }
  }

  override fun appStart() {
    listener.appStart(app)
  }

  override fun appCanceled() {
    listener.appCanceled(app)
  }

  override fun onUnknownEvent(widgetTag: WidgetTag, tag: EventTag) {
    listener.onUnknownEvent(app, widgetTag, tag)
  }

  override fun onUnknownEventNode(id: Id, tag: EventTag) {
    listener.onUnknownEventNode(app, id, tag)
  }
}
