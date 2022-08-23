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

import app.cash.redwood.LayoutModifier
import app.cash.redwood.protocol.EventSink
import app.cash.redwood.protocol.PropertyDiff
import app.cash.redwood.protocol.widget.DiffConsumingWidget
import app.cash.redwood.widget.UIViewChildren
import app.cash.zipline.loader.ManifestVerifier
import app.cash.zipline.loader.ZiplineCache
import app.cash.zipline.loader.ZiplineHttpClient
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.JsonArray
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import platform.Foundation.NSLog
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSThread
import platform.UIKit.UIView

public fun TreehouseLauncher(
  httpClient: ZiplineHttpClient,
  manifestVerifier: ManifestVerifier,
  embeddedDir: Path = "/".toPath(),
  embeddedFileSystem: FileSystem = FileSystem.SYSTEM,
  cacheName: String = "zipline",
  cacheMaxSizeInBytes: Long = 50L * 1024L * 1024L,
): TreehouseLauncher = TreehouseLauncher(
  platform = IosTreehousePlatform(),
  dispatchers = IosTreehouseDispatchers(),
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

// TODO(jwilson): we're currently doing everything on the main thread on iOS.
internal class IosTreehouseDispatchers : TreehouseDispatchers {
  override val main: CoroutineDispatcher = Dispatchers.Main
  override val zipline: CoroutineDispatcher = Dispatchers.Main

  override fun checkMain() {
    check(NSThread.isMainThread)
  }

  override fun checkZipline() {
    checkMain()
  }
}

internal class ProtocolDisplayRoot(
  override val value: UIView,
) : DiffConsumingWidget<UIView> {
  private val children = UIViewChildren(value)

  override var layoutModifiers: LayoutModifier = LayoutModifier

  override fun updateLayoutModifier(value: JsonArray) {
  }

  override fun apply(diff: PropertyDiff, eventSink: EventSink) {
    error("unexpected update on view root: $diff")
  }

  override fun children(tag: Int) = children
}
