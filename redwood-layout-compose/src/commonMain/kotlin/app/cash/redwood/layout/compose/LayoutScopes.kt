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
package app.cash.redwood.layout.compose

import androidx.compose.runtime.Stable
import app.cash.redwood.Modifier

/**
 * Equivalent to `modifier.grow(value).shrink(value)`.
 *
 * Call this in [ColumnScope] or [RowScope].
 */
@Stable
@Deprecated(
  message = "This extension function is obselete now that RowScope and " +
    "ColumnScope support flex directly. Remove the import for this function.",
  replaceWith = ReplaceWith("flex(value)"),
  level = DeprecationLevel.ERROR,
)
public fun Modifier.flex(`value`: Double): Modifier =
  then(GrowImpl(`value`)).then(ShrinkImpl(`value`))
