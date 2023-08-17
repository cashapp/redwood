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

import dev.drewhamilton.poko.Poko
import kotlinx.serialization.Serializable

/** A two-dimensional size measured in density-independent pixels. */
@Poko
@Serializable
public class Size(
  public val width: Dp,
  public val height: Dp,
) {
  public companion object {
    public val Zero: Size = Size(0.dp, 0.dp)
  }
}
