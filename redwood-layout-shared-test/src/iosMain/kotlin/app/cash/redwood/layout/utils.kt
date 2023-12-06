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
package app.cash.redwood.layout

import platform.UIKit.UIColor

fun Int.toUIColor(): UIColor {
  return UIColor(
    red = ((this shr 16) and 0xff) / 255.0,
    green = ((this shr 8) and 0xff) / 255.0,
    blue = (this and 0xff) / 255.0,
    alpha = ((this shr 24) and 0xff) / 255.0,
  )
}
