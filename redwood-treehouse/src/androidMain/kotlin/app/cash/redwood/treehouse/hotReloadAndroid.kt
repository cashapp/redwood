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

import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

@OptIn(ExperimentalCoroutinesApi::class)
internal actual fun hotReloadFlow(flow: Flow<String>): Flow<String> {
  return flow {
    val wsClient: OkHttpClient = OkHttpClient.Builder()
      .readTimeout(0, TimeUnit.MILLISECONDS)
      .connectTimeout(1, TimeUnit.SECONDS)
      .build()

    emitAll(
      flow.flatMapLatest { manifestUrl ->
        var currentlyConnectedToWebSocket = false

        val request: Request = Request.Builder()
          .url(manifestUrl.toHttpUrl().resolve("/ws")!!)
          .build()

        return@flatMapLatest callbackFlow {
          var ws: WebSocket? = null
          launch {
            while (true) {
              if (!currentlyConnectedToWebSocket) {
                trySendBlocking(manifestUrl)
                println("we are here")
                ws = wsClient.newWebSocket(
                  request,
                  object : WebSocketListener() {
                    override fun onOpen(webSocket: WebSocket, response: Response) {
                      currentlyConnectedToWebSocket = true
                    }

                    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                      currentlyConnectedToWebSocket = false
                      webSocket.close(1000, null)
                    }

                    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                      currentlyConnectedToWebSocket = false
                      webSocket.close(1000, null)
                    }

                    override fun onMessage(webSocket: WebSocket, text: String) {
                      if (text == "reload") {
                        trySendBlocking(manifestUrl)
                      }
                    }
                  },
                )
              }
              delay(500.milliseconds)
            }
          }

          awaitClose {
            ws?.close(1000, null)
          }
        }
      },
    )
  }
}
