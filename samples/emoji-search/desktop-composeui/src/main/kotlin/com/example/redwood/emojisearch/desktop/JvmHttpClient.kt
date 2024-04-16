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
package com.example.redwood.emojisearch.desktop

import com.example.redwood.emojisearch.presenter.HttpClient
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CompletionHandler
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Headers.Companion.toHeaders
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException

class JvmHttpClient(
  private val okHttpClient: OkHttpClient,
) : HttpClient {

  override suspend fun call(url: String, headers: Map<String, String>): String {
    val request = Request.Builder()
      .url(url)
      .headers(headers.toHeaders())
      .build()
    val response = okHttpClient.newCall(request).await()
    return response.body?.string().orEmpty()
  }
}

private suspend fun Call.await(): Response {
  return suspendCancellableCoroutine { continuation ->
    val callback = ContinuationCallback(this, continuation)
    enqueue(callback)
    continuation.invokeOnCancellation(callback)
  }
}

private class ContinuationCallback(
  private val call: Call,
  private val continuation: CancellableContinuation<Response>,
) : Callback, CompletionHandler {

  override fun onResponse(call: Call, response: Response) {
    continuation.resume(response)
  }

  override fun onFailure(call: Call, e: IOException) {
    if (!call.isCanceled()) {
      continuation.resumeWithException(e)
    }
  }

  override fun invoke(cause: Throwable?) {
    try {
      call.cancel()
    } catch (_: Throwable) {}
  }
}
