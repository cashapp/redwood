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
package app.cash.redwood.snapshot.testing

import app.cash.redwood.widget.Widget

/**
 * A label that wraps across multiple lines if necessary.
 *
 * The text is left-aligned on LTR locales and right-aligned on RTL locales.
 *
 * The text is centered vertically.
 */
interface Text<W : Any> : Widget<W> {
  val measureCount: Int
  fun text(text: String)
  fun bgColor(color: Int)
}
