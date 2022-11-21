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
package example.values

import kotlinx.serialization.Serializable

@Serializable
data class TextFieldState(
  val text: String = "",
  val selectionStart: Int = 0,
  val selectionEnd: Int = 0,
  val userEditCount: Long = 0L,
) {
  init {
    require(selectionStart in 0..text.length)
    require(selectionEnd in 0..text.length)
  }

  /** Returns a copy of the state initiated by a user edit. */
  fun userEdit(
    text: String = this.text,
    selectionStart: Int = this.selectionStart,
    selectionEnd: Int = this.selectionEnd,
  ) = copy(
    text = text,
    selectionStart = selectionStart.coerceIn(0, text.length),
    selectionEnd = selectionEnd.coerceIn(0, text.length),
    userEditCount = userEditCount + 1L,
  )

  /**
   * Returns true if [other] and this are equal ignoring version metadata.
   * Use this to skip no-op user edits.
   */
  fun contentEquals(other: TextFieldState): Boolean =
    copy(userEditCount = other.userEditCount) == other
}
