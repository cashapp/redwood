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
package com.example.redwood.emojisearch.android.composeui

import com.example.redwood.emojisearch.treehouse.HostApi
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Headers.Companion.toHeaders
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException

/** Exception for an unexpected, non-2xx HTTP response. */
class HttpException(response: Response) :
  RuntimeException("HTTP ${response.code} ${response.message}")

class RealHostApi(
  private val client: OkHttpClient,
  private val openUrl: (url: String) -> Unit,
) : HostApi {
  override suspend fun httpCall(url: String, headers: Map<String, String>): String {
    return suspendCancellableCoroutine { continuation ->
      val call = client.newCall(
        Request.Builder()
          .url(url)
          .headers(headers.toHeaders())
          .build(),
      )
      call.enqueue(
        object : Callback {
          override fun onFailure(call: Call, e: IOException) {
            continuation.resumeWith(Result.failure(e))
          }

          override fun onResponse(call: Call, response: Response) {
            if (response.isSuccessful) {
              continuation.resume(response.body!!.string())
            } else {
              continuation.resumeWithException(HttpException(response))
            }
          }
        },
      )
      continuation.invokeOnCancellation { call.cancel() }
    }
  }

  override fun openUrl(url: String) {
    openUrl.invoke(url)
  }
}
