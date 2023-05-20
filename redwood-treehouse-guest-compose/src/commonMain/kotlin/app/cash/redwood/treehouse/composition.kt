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

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import app.cash.redwood.compose.RedwoodComposition
import kotlinx.coroutines.flow.StateFlow

// Inline at callsite once https://github.com/Kotlin/kotlinx.serialization/issues/1454 is fixed.
public fun RedwoodComposition.bind(
  treehouseUi: TreehouseUi,
  hostConfigurations: StateFlow<HostConfiguration>,
) {
  setContent {
    val hostConfiguration by hostConfigurations.collectAsState()
    CompositionLocalProvider(LocalHostConfiguration provides hostConfiguration) {
      treehouseUi.Show()
    }
  }
}
