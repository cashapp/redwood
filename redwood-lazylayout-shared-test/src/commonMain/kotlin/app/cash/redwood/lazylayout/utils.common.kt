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
@file:Suppress("ktlint:standard:property-naming")

package app.cash.redwood.lazylayout

import app.cash.redwood.widget.Widget

const val Green: Int = 0xff00ff00.toInt()

fun argb(
  alpha: Int,
  red: Int,
  green: Int,
  blue: Int,
): Int {
  return (alpha shl 24) or (red shl 16) or (green shl 8) or (blue)
}

interface Text<T : Any> : Widget<T> {
  fun text(text: String)
  fun bgColor(color: Int)
}
