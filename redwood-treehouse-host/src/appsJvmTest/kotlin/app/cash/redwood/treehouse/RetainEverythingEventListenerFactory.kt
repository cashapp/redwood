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

import app.cash.zipline.Zipline
import app.cash.zipline.ZiplineManifest

/**
 * An event listener (and factory) that keeps a reference to everything it sees, for defensive leak
 * testing.
 */
class RetainEverythingEventListenerFactory(
  private val eventLog: EventLog,
) : EventListener(), EventListener.Factory {
  var app: TreehouseApp<*>? = null
  var manifestUrl: String? = null
  var zipline: Zipline? = null
  var ziplineManifest: ZiplineManifest? = null

  override fun create(app: TreehouseApp<*>, manifestUrl: String?): EventListener {
    this.app = app
    this.manifestUrl = manifestUrl
    return this
  }

  override fun codeLoadSuccess(manifest: ZiplineManifest, zipline: Zipline, startValue: Any?) {
    this.zipline = zipline
    this.ziplineManifest = manifest
  }

  override fun codeUnloaded() {
    eventLog += "codeUnloaded"
  }
}
