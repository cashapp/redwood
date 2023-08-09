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
package app.cash.redwood.lazylayout.api

import androidx.compose.runtime.Immutable
import dev.drewhamilton.poko.Poko
import kotlinx.serialization.Serializable

/**
 * @param id Should only be used to trigger recompositions.
 */
@[Immutable Serializable]
@Poko
public class ScrollItemIndex(
  @Suppress("unused") private val id: Int,
  public val index: Int,
)
