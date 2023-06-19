/*
 * Copyright (C) 2023 Square, Inc.
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

import kotlin.coroutines.CoroutineContext
import kotlinx.cinterop.convert
import kotlinx.coroutines.CloseableCoroutineDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.runBlocking
import platform.Foundation.NSThread

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

  override fun close() {
    zipline_.close()
  }
}

/** A CoroutineDispatcher that's confined to a single thread, appropriate for executing QuickJS. */
private class SingleThreadCoroutineDispatcher : CloseableCoroutineDispatcher() {
  /**
   * On Apple platforms we need to explicitly set the stack size for background threads; otherwise
   * we get the default of 512 KiB which isn't sufficient for our QuickJS programs.
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
