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

import app.cash.zipline.loader.ZiplineLoader
import kotlinx.serialization.modules.SerializersModule
import okio.Path

internal interface TreehousePlatform {
  val dispatchers: TreehouseDispatchers

  /** Directory for the cache shared by all Treehouse applications. */
  val cacheDirectory: Path

  // TODO(jwilson): move this to TreehouseLauncher.Spec.
  val serializersModule: SerializersModule

  fun logInfo(message: String, throwable: Throwable?)

  fun logWarning(message: String, throwable: Throwable?)

  fun newZiplineLoader(): ZiplineLoader
}
