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

import android.content.Context
import app.cash.zipline.loader.ManifestVerifier
import app.cash.zipline.loader.asZiplineHttpClient
import okhttp3.OkHttpClient
import okio.FileSystem
import okio.Path

public fun TreehouseAppFactory(
  context: Context,
  httpClient: OkHttpClient,
  manifestVerifier: ManifestVerifier,
  eventListener: EventListener = EventListener(),
  embeddedDir: Path? = null,
  embeddedFileSystem: FileSystem? = null,
  cacheName: String = "zipline",
  cacheMaxSizeInBytes: Long = 50L * 1024L * 1024L,
  concurrentDownloads: Int = 8,
  stateStore: StateStore = MemoryStateStore(),
): TreehouseApp.Factory = TreehouseApp.Factory(
  platform = AndroidTreehousePlatform(context),
  dispatchers = AndroidTreehouseDispatchers(),
  eventListener = eventListener,
  httpClient = httpClient.asZiplineHttpClient(),
  frameClockFactory = AndroidChoreographerFrameClock.Factory(),
  manifestVerifier = manifestVerifier,
  embeddedDir = embeddedDir,
  embeddedFileSystem = embeddedFileSystem,
  cacheName = cacheName,
  cacheMaxSizeInBytes = cacheMaxSizeInBytes,
  concurrentDownloads = concurrentDownloads,
  stateStore = stateStore,
)
