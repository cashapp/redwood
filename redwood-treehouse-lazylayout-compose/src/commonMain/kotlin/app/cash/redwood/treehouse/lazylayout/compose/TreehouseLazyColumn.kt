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
package app.cash.redwood.treehouse.lazylayout.compose

import androidx.compose.runtime.Composable
import app.cash.redwood.compose.LocalWidgetVersion
import app.cash.redwood.protocol.compose.ProtocolBridge

@Composable
public fun ProtocolBridge.LazyColumn(content: LazyListScope.() -> Unit) {
  val widgetVersion = LocalWidgetVersion.current
  val scope = LazyListIntervalContent(this, widgetVersion)
  content(scope)
  LazyColumn(scope.intervals)
}
