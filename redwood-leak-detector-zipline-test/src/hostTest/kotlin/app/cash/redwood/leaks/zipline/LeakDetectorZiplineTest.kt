/*
 * Copyright (C) 2024 Square, Inc.
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
package app.cash.redwood.leaks.zipline

import app.cash.redwood.leaks.zipline.LeakDetectorTestService.Companion.SERVICE_NAME
import app.cash.zipline.Zipline
import app.cash.zipline.ZiplineManifest
import app.cash.zipline.loader.FreshnessChecker
import app.cash.zipline.loader.LoadResult.Failure
import app.cash.zipline.loader.LoadResult.Success
import app.cash.zipline.loader.ManifestVerifier.Companion.NO_SIGNATURE_CHECKS
import app.cash.zipline.loader.ZiplineHttpClient
import app.cash.zipline.loader.ZiplineLoader
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlinx.coroutines.Dispatchers.Unconfined
import kotlinx.coroutines.runBlocking
import okio.FileSystem.Companion.SYSTEM
import okio.Path.Companion.toPath

private val ziplineDir = System.getProperty("ziplineDir")!!.toPath()
private val ziplineAppName = System.getProperty("ziplineAppName")!!

class LeakDetectorZiplineTest {
  private val loader = ZiplineLoader(
    dispatcher = Unconfined,
    manifestVerifier = NO_SIGNATURE_CHECKS,
    httpClient = object : ZiplineHttpClient() {
      override suspend fun download(
        url: String,
        requestHeaders: List<Pair<String, String>>,
      ) = throw UnsupportedOperationException()
    },
  ).withEmbedded(SYSTEM, ziplineDir)

  private lateinit var zipline: Zipline
  private lateinit var service: LeakDetectorTestService

  @BeforeTest fun before() = runBlocking {
    val result = loader.loadOnce(
      applicationName = ziplineAppName,
      freshnessChecker = object : FreshnessChecker {
        override fun isFresh(manifest: ZiplineManifest, freshAtEpochMs: Long) = true
      },
      manifestUrl = "http://0.0.0.0:0",
    )

    when (result) {
      is Failure -> throw result.exception
      is Success -> {
        zipline = result.zipline
      }
    }

    service = zipline.take(SERVICE_NAME)
  }

  @AfterTest fun after() {
    zipline.close()
  }

  @Test fun leakDetectorDisabled() = runBlocking {
    service.leakDetectorDisabled()
  }
}
