/*
 * Copyright (C) 2025 Square, Inc.
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
package app.cash.redwood.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import app.cash.redwood.ui.core.api.FocusDirector
import app.cash.redwood.ui.core.api.FocusRequester

@Composable
public fun rememberFocusRequester(): FocusRequester {
  val focusDirector = LocalFocusDirector.current
  return remember { focusDirector.newFocusRequester() }
}

public val LocalFocusDirector: ProvidableCompositionLocal<FocusDirector> =
  staticCompositionLocalOf {
    throw AssertionError("FocusDirector was not provided!")
  }
