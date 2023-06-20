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
import kotlin.coroutines.CoroutineContext
import kotlinx.cinterop.convert
import kotlinx.coroutines.CloseableCoroutineDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.runBlocking
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import platform.Foundation.NSLog
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSThread

public fun TreehouseAppFactory(
  httpClient: ZiplineHttpClient,
  manifestVerifier: ManifestVerifier,
  eventListener: EventListener = EventListener(),
  embeddedDir: Path? = null,
  embeddedFileSystem: FileSystem? = null,
  cacheName: String = "zipline",
  cacheMaxSizeInBytes: Long = 50L * 1024L * 1024L,
  concurrentDownloads: Int = 8,
  stateStore: StateStore = MemoryStateStore(),
): TreehouseApp.Factory = TreehouseApp.Factory(
  platform = IosTreehousePlatform(),
  dispatchers = IosTreehouseDispatchers(),
  eventListener = eventListener,
  httpClient = httpClient,
  frameClock = IosDisplayLinkClock(),
  manifestVerifier = manifestVerifier,
  embeddedDir = embeddedDir,
  embeddedFileSystem = embeddedFileSystem,
  cacheName = cacheName,
  cacheMaxSizeInBytes = cacheMaxSizeInBytes,
  concurrentDownloads = concurrentDownloads,
  stateStore = stateStore,
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

internal class IosTreehouseDispatchers : TreehouseDispatchers {
  override val ui: CoroutineDispatcher get() = Dispatchers.Main

  private val zipline_ = SingleThreadCoroutineDispatcher().also {
    it.thread.start()
  }

  override val zipline: CoroutineDispatcher get() = zipline_

  override fun checkUi() {
    check(NSThread.isMainThread)
  }

  override fun checkZipline() {
    check(NSThread.currentThread == zipline_.thread)
  }
}

/** A CoroutineDispatcher that's confined to a single thread, appropriate for executing QuickJS. */
private class SingleThreadCoroutineDispatcher : CloseableCoroutineDispatcher() {
  /**
   * On Apple platforms we need to explicitly set the stack size for background threads; otherwise we
   * get the default of 512 KiB which isn't sufficient for our QuickJS programs.
   *
   * 8 MiB is more than sufficient.
   */
  private val stackSize = 8 * 1024 * 1024
  private val channel = Channel<Runnable>(capacity = Channel.UNLIMITED)

  val thread = NSThread {
    runBlocking {
      while (true) {
        try {
          val runnable = channel.receive()
          // TODO(jwilson): handle uncaught exceptions.
          runnable.run()
        } catch (e: ClosedReceiveChannelException) {
          break
        }
      }
    }
  }.apply {
    this.name = "Treehouse"
    this.stackSize = this@SingleThreadCoroutineDispatcher.stackSize.convert()
  }

  override fun dispatch(context: CoroutineContext, block: Runnable) {
    channel.trySend(block)
  }

  override fun close() {
    channel.close()
  }
}
