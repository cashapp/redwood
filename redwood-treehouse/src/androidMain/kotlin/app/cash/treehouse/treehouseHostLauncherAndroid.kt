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

import android.content.Context
import android.util.Log
import app.cash.zipline.loader.ZiplineLoader
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.modules.SerializersModule
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toOkioPath
import okio.Path.Companion.toPath

public fun <T : Any> TreehouseHostLauncher(
  context: Context,
  appLauncher: TreehouseAppLauncher<T>,
  viewBinderAdapter: ViewBinder.Adapter,
  serializersModule: SerializersModule,
  manifestUrlFlow: Flow<HttpUrl>,
  okHttpClient: OkHttpClient,
  dispatchers: TreehouseDispatchers,
  freshCodePolicy: FreshCodePolicy,
): TreehouseHostLauncher<T> {
  val cacheDirectory: Path = context.cacheDir.toOkioPath() / "zipline"

  val eventListener = TreehouseEventListener { warning, message, throwable ->
    if (warning) {
      Log.w("Zipline", message, throwable)
    } else {
      Log.i("Zipline", message, throwable)
    }
  }

  val ziplineLoader = ZiplineLoader(
    context = context,
    dispatcher = dispatchers.zipline,
    httpClient = okHttpClient,
    eventListener = eventListener,
    nowEpochMs = { System.currentTimeMillis() },
    serializersModule = serializersModule,
  ).withCache(
    fileSystem = FileSystem.SYSTEM,
    directory = cacheDirectory,
    maxSizeInBytes = 50L * 1024L * 1024L,
  ).withEmbedded(
    embeddedDir = "/".toPath(),
    embeddedFileSystem = FileSystem.RESOURCES,
  )

  return TreehouseHostLauncher(
    dispatchers = dispatchers,
    viewBinderAdapter = viewBinderAdapter,
    launcher = appLauncher,
    manifestUrlFlow = manifestUrlFlow.map { it.toString() },
    ziplineLoader = ziplineLoader,
    freshCodePolicy = freshCodePolicy,
  )
}
