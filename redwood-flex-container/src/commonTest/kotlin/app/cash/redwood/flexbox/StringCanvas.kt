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
package app.cash.redwood.flexbox

/**
 * A 2D grid of characters. Attempts to draw beyond the bounds are silently ignored.
 *
 * The default content contains the '·' character; this is chosen over ' ' as its less likely to
 * be trimmed by an editor when used as expected output.
 */
class StringCanvas(
  private val width: Int,
  private val height: Int,
) {
  private val lines = Array(height) { CharArray(width) { '·' } }

  operator fun set(x: Int, y: Int, value: Char) {
    if (x in 0 until width && y in 0 until height) {
      lines[y][x] = value
    }
  }

  override fun toString() = lines.joinToString(separator = "\n") { it.concatToString() }
}
