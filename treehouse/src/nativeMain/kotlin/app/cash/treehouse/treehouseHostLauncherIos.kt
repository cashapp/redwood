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

import app.cash.zipline.loader.ZiplineHttpClient
import app.cash.zipline.loader.ZiplineLoader
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.modules.SerializersModule
import okio.FileSystem
import okio.Path.Companion.toPath
import platform.Foundation.NSDate
import platform.Foundation.NSLog
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.timeIntervalSince1970
import platform.UIKit.UIView

fun TreehouseHostLauncher(
  appLauncher: TreehouseAppLauncher<UIView>,
  cacheRootDir: String = NSTemporaryDirectory(),
  viewBinderAdapter: ViewBinder.Adapter,
  httpClient: ZiplineHttpClient,
  manifestUrlFlow: Flow<String>,
  serializersModule: SerializersModule,
  freshCodePolicy: FreshCodePolicy,
): TreehouseHostLauncher<UIView> {
  val cacheDirectory = cacheRootDir.toPath() / "zipline"

  val dispatchers = IosTreehouseDispatchers()

  val eventListener = TreehouseEventListener { _, message, throwable ->
    if (throwable != null) {
      NSLog("Zipline: $message ${throwable.stackTraceToString()}")
    } else {
      NSLog("Zipline: $message")
    }
  }

  val ziplineLoader = ZiplineLoader(
    dispatcher = dispatchers.zipline,
    httpClient = httpClient,
    nowEpochMs = { NSDate().timeIntervalSince1970().toLong() * 1000 },
    eventListener = eventListener,
    serializersModule = serializersModule,
  ).withCache(
    fileSystem = FileSystem.SYSTEM,
    directory = cacheDirectory,
    maxSizeInBytes = 50L * 1024L * 1024L,
  ).withEmbedded(
    embeddedDir = "/".toPath(),
    embeddedFileSystem = FileSystem.SYSTEM,
  )

  return TreehouseHostLauncher(
    dispatchers = dispatchers,
    viewBinderAdapter = viewBinderAdapter,
    launcher = appLauncher,
    manifestUrlFlow = manifestUrlFlow,
    ziplineLoader = ziplineLoader,
    freshCodePolicy = freshCodePolicy,
  )
}
