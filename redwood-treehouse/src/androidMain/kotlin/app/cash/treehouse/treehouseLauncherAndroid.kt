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
import android.os.Looper
import android.util.Log
import android.view.View
import app.cash.redwood.LayoutModifier
import app.cash.redwood.protocol.EventSink
import app.cash.redwood.protocol.PropertyDiff
import app.cash.redwood.protocol.widget.DiffConsumingWidget
import app.cash.redwood.widget.ViewGroupChildren
import app.cash.zipline.loader.ManifestVerifier
import app.cash.zipline.loader.ZiplineLoader
import java.util.concurrent.Executors
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.modules.SerializersModule
import okhttp3.OkHttpClient
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toOkioPath
import okio.Path.Companion.toPath

public fun TreehouseLauncher(
  context: Context,
  httpClient: OkHttpClient,
  manifestVerifier: ManifestVerifier,
  serializersModule: SerializersModule,
  embeddedDir: Path = "/".toPath(),
  embeddedFileSystem: FileSystem = FileSystem.SYSTEM,
): TreehouseLauncher = TreehouseLauncher(
  AndroidTreehousePlatform(
    context,
    httpClient,
    manifestVerifier,
    serializersModule,
    embeddedDir,
    embeddedFileSystem,
  ),
)

internal class AndroidTreehousePlatform(
  private val context: Context,
  private val httpClient: OkHttpClient,
  private val manifestVerifier: ManifestVerifier,
  override val serializersModule: SerializersModule,
  private val embeddedDir: Path,
  private val embeddedFileSystem: FileSystem,
) : TreehousePlatform {
  override val dispatchers = AndroidTreehouseDispatchers()
  override val cacheDirectory: Path = context.cacheDir.toOkioPath() / "zipline"

  override fun logInfo(message: String, throwable: Throwable?) {
    Log.i("Zipline", message, throwable)
  }

  override fun logWarning(message: String, throwable: Throwable?) {
    Log.w("Zipline", message, throwable)
  }

  override fun newZiplineLoader(): ZiplineLoader {
    return ZiplineLoader(
      context = context,
      dispatcher = dispatchers.zipline,
      manifestVerifier = manifestVerifier,
      httpClient = httpClient,
      eventListener = TreehouseEventListener(this),
      serializersModule = serializersModule,
    ).withCache(
      fileSystem = FileSystem.SYSTEM,
      directory = cacheDirectory,
      maxSizeInBytes = 50L * 1024L * 1024L,
    ).withEmbedded(
      embeddedDir = embeddedDir,
      embeddedFileSystem = embeddedFileSystem,
    )
  }
}

/**
 * Implements [TreehouseDispatchers] suitable for production Android use. This creates a background
 * thread for all Zipline work.
 */
internal class AndroidTreehouseDispatchers : TreehouseDispatchers {
  private lateinit var ziplineThread: Thread

  /** The single thread that runs all JavaScript. We only have one QuickJS instance at a time. */
  private val executorService = Executors.newSingleThreadExecutor { runnable ->
    Thread(runnable, "Treehouse")
      .also { ziplineThread = it }
  }

  override val main: CoroutineDispatcher = Dispatchers.Main
  override val zipline: CoroutineDispatcher = executorService.asCoroutineDispatcher()

  override fun checkMain() {
    check(Looper.myLooper() == Looper.getMainLooper())
  }

  override fun checkZipline() {
    check(Thread.currentThread() == ziplineThread)
  }
}

internal class ProtocolDisplayRoot(
  override val value: TreehouseWidgetView<*>,
) : DiffConsumingWidget<View> {
  private val children = ViewGroupChildren(value)

  override var layoutModifiers: LayoutModifier = LayoutModifier

  override fun updateLayoutModifier(value: JsonArray) {
  }

  override fun apply(diff: PropertyDiff, eventSink: EventSink) {
    error("unexpected update on view root: $diff")
  }

  override fun children(tag: Int) = children
}
