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

import android.os.Looper
import java.util.concurrent.Executors
import kotlinx.coroutines.CloseableCoroutineDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher

/**
 * Implements [TreehouseDispatchers] suitable for production Android use. This creates a background
 * thread for all Zipline work.
 */
internal class AndroidTreehouseDispatchers(applicationName: String) : TreehouseDispatchers {
  private var ziplineThread: Thread? = null

  /** The single thread that runs all JavaScript. We only have one QuickJS instance at a time. */
  private val executorService = Executors.newSingleThreadExecutor { runnable ->
    Thread(null, runnable, "Treehouse $applicationName", ZIPLINE_THREAD_STACK_SIZE.toLong())
      .also { ziplineThread = it }
  }

  override val ui: CoroutineDispatcher get() = Dispatchers.Main
  override val zipline: CoroutineDispatcher = executorService.asCoroutineDispatcher()

  override fun checkUi() {
    check(Looper.myLooper() == Looper.getMainLooper())
  }

  override fun checkZipline() {
    check(Thread.currentThread() == ziplineThread)
  }

  override fun close() {
    executorService.shutdown()
  }
}

@OptIn(ExperimentalCoroutinesApi::class) // CloseableCoroutineDispatcher is experimental.
internal fun ziplineLoaderDispatcher(): CloseableCoroutineDispatcher {
  val executorService = Executors.newSingleThreadExecutor { runnable ->
    Thread(null, runnable, "ZiplineLoader")
  }
  return executorService.asCoroutineDispatcher()
}
