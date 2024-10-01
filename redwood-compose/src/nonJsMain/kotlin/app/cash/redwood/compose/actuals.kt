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
package app.cash.redwood.compose

import androidx.compose.runtime.Composer
import app.cash.redwood.RedwoodCodegenApi
import app.cash.redwood.widget.WidgetFactoryOwner

@PublishedApi
@RedwoodCodegenApi
@Suppress("NOTHING_TO_INLINE")
internal actual inline fun <V : Any> Composer.redwoodApplier(): RedwoodApplier<V> {
  // Perform an explicit !! on the return value to avoid the Kotlin compiler inserting a huge
  // string into the output as an error message for an otherwise implicit null check.
  @Suppress(
    // Safe so long as you use generated composition function.
    "UNCHECKED_CAST",
    "UNNECESSARY_NOT_NULL_ASSERTION",
  )
  return applier!! as RedwoodApplier<V>
}

@PublishedApi
@RedwoodCodegenApi
@Suppress("NOTHING_TO_INLINE")
internal actual inline fun <O : WidgetFactoryOwner<V>, V : Any> RedwoodApplier<V>.widgetSystem(): O {
  @Suppress("UNCHECKED_CAST")
  return widgetSystem as O
}
