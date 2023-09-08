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
package com.example.redwood.testing.treehouse

import app.cash.redwood.treehouse.StandardAppLifecycle
import app.cash.redwood.treehouse.ZiplineTreehouseUi
import app.cash.redwood.treehouse.asZiplineTreehouseUi
import com.example.redwood.testing.compose.TestSchemaProtocolBridge
import kotlinx.serialization.json.Json

class RealTestAppPresenter(
  private val hostApi: HostApi,
  private val json: Json,
) : TestAppPresenter {
  override val appLifecycle = StandardAppLifecycle(
    protocolBridgeFactory = TestSchemaProtocolBridge,
    json = json,
    widgetVersion = 0U,
  )

  override fun launch(): ZiplineTreehouseUi {
    val treehouseUi = TestAppTreehouseUi(hostApi::httpCall)
    return treehouseUi.asZiplineTreehouseUi(appLifecycle)
  }
}
