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
package app.cash.treehouse

import app.cash.zipline.EventListener
import app.cash.zipline.ZiplineService

internal class TreehouseEventListener(
  private val platform: TreehousePlatform,
) : EventListener() {
  override fun bindService(name: String, service: ZiplineService) {
    platform.logInfo("Binding service $name of type ${service::class}", null)
  }

  override fun takeService(name: String, service: ZiplineService) {
    platform.logInfo("Taking service $name of type ${service::class}", null)
  }

  override fun serviceLeaked(name: String) {
    platform.logWarning("Service $name wasn't closed!", null)
  }

  override fun applicationLoadStart(applicationName: String, manifestUrl: String?) {
    platform.logInfo("Loading application $applicationName from $manifestUrl", null)
  }

  override fun applicationLoadEnd(applicationName: String, manifestUrl: String?) {
    platform.logInfo("Loaded application $applicationName", null)
  }

  override fun applicationLoadFailed(
    applicationName: String,
    manifestUrl: String?,
    exception: Exception,
  ) {
    platform.logWarning("Loading application $applicationName failed", exception)
  }

  override fun downloadFailed(applicationName: String, url: String, exception: Exception) {
    platform.logInfo("Downloading code failed; will retry: $exception", null)
  }

  override fun manifestParseFailed(applicationName: String, url: String?, exception: Exception) {
    platform.logInfo("Failed to parse the Zipline Manifest; will retry: $exception", null)
  }
}
