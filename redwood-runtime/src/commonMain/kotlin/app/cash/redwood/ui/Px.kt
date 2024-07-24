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
package app.cash.redwood.ui

import androidx.compose.runtime.Immutable
import kotlin.jvm.JvmInline
import kotlinx.serialization.Serializable

/**
 * Represents a raw pixel value in the host's native coordinate system units.
 */
@[Immutable JvmInline Serializable]
public value class Px(
  public val value: Double,
) {
  init {
    require(value >= 0) {
      "value must be non-negative: $value"
    }
  }

  override fun toString(): String = "$value.px"

  /** Empty companion object used for extensions. */
  public companion object
}
