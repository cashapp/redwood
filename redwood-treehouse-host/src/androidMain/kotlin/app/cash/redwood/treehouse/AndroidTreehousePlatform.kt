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

import android.content.Context
import android.util.Log
import app.cash.zipline.loader.ZiplineCache
import okio.FileSystem
import okio.Path.Companion.toOkioPath

internal class AndroidTreehousePlatform(
  private val context: Context,
) : TreehousePlatform {
  override fun logInfo(message: String, throwable: Throwable?) {
    Log.i("Zipline", message, throwable)
  }

  override fun logWarning(message: String, throwable: Throwable?) {
    Log.w("Zipline", message, throwable)
  }

  override fun newCache(name: String, maxSizeInBytes: Long) = ZiplineCache(
    context = context,
    fileSystem = FileSystem.SYSTEM,
    directory = context.cacheDir.toOkioPath() / name,
    maxSizeInBytes = maxSizeInBytes,
  )
}
