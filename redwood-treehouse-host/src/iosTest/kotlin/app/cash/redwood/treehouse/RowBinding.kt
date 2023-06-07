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
package app.cash.redwood.treehouse

import app.cash.redwood.Modifier
import app.cash.redwood.widget.UIViewChildren
import app.cash.redwood.widget.Widget
import example.redwood.widget.Row
import platform.UIKit.UIStackView
import platform.UIKit.UIView

/** Note: this doesn't bother to arrange child views horizontally! */
class RowBinding : Row<UIView> {
  override val value = UIStackView()

  override var modifier: Modifier = Modifier

  override val children: Widget.Children<UIView> = UIViewChildren(value)
}
