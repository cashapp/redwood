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
package app.cash.redwood.ui

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import dev.drewhamilton.poko.Poko
import kotlinx.serialization.Serializable

@[Immutable Serializable]
@Poko public class Margin(
  public val start: Dp = 0.dp,
  public val end: Dp = 0.dp,
  public val top: Dp = 0.dp,
  public val bottom: Dp = 0.dp,
) {

  override fun toString(): String = when {
    start != end || top != bottom -> {
      "Margin(start=$start, end=$end, top=$top, bottom=$bottom)"
    }
    start != top -> {
      "Margin(horizontal=$start, vertical=$top)"
    }
    else -> "Margin(all=$start)"
  }

  public companion object {
    public val Zero: Margin = Margin()
  }
}

@Stable
public fun Margin(
  horizontal: Dp = 0.dp,
  vertical: Dp = 0.dp,
): Margin = Margin(horizontal, horizontal, vertical, vertical)

@Stable
public fun Margin(
  all: Dp = 0.dp,
): Margin = Margin(all, all, all, all)
