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

import app.cash.zipline.loader.ManifestVerifier
import app.cash.zipline.loader.ZiplineCache
import app.cash.zipline.loader.ZiplineHttpClient
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import platform.Foundation.NSLog
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSThread

public fun TreehouseAppFactory(
  httpClient: ZiplineHttpClient,
  manifestVerifier: ManifestVerifier,
  eventListener: EventListener = EventListener.NONE,
  embeddedDir: Path = "/".toPath(),
  embeddedFileSystem: FileSystem = FileSystem.SYSTEM,
  cacheName: String = "zipline",
  cacheMaxSizeInBytes: Long = 50L * 1024L * 1024L,
): TreehouseApp.Factory = TreehouseApp.Factory(
  platform = IosTreehousePlatform(),
  dispatchers = IosTreehouseDispatchers(),
  eventListener = eventListener,
  httpClient = httpClient,
  manifestVerifier = manifestVerifier,
  embeddedDir = embeddedDir,
  embeddedFileSystem = embeddedFileSystem,
  cacheName = cacheName,
  cacheMaxSizeInBytes = cacheMaxSizeInBytes,
)

internal class IosTreehousePlatform : TreehousePlatform {
  override fun logInfo(message: String, throwable: Throwable?) {
    if (throwable != null) {
      NSLog("Treehouse: $message ${throwable.stackTraceToString()}")
    } else {
      NSLog("Treehouse: $message")
    }
  }

  override fun logWarning(message: String, throwable: Throwable?) {
    if (throwable != null) {
      NSLog("Treehouse: $message ${throwable.stackTraceToString()}")
    } else {
      NSLog("Treehouse: $message")
    }
  }

  override fun newCache(name: String, maxSizeInBytes: Long) = ZiplineCache(
    fileSystem = FileSystem.SYSTEM,
    directory = NSTemporaryDirectory().toPath() / name,
    maxSizeInBytes = maxSizeInBytes,
  )
}

// TODO(jwilson): we're currently doing everything on the UI thread on iOS.
internal class IosTreehouseDispatchers : TreehouseDispatchers {
  override val ui: CoroutineDispatcher get() = Dispatchers.Main
  override val zipline: CoroutineDispatcher get() = Dispatchers.Main

  override fun checkUi() {
    check(NSThread.isMainThread)
  }

  override fun checkZipline() {
    checkUi()
  }
}
