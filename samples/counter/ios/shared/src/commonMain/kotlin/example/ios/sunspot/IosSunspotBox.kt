/*
 * Copyright (C) 2021 Square, Inc.
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
package example.ios.sunspot

import app.cash.redwood.LayoutModifier
import app.cash.redwood.widget.MutableListChildren
import example.sunspot.widget.SunspotBox
import platform.UIKit.UILayoutConstraintAxisHorizontal
import platform.UIKit.UIStackView
import platform.UIKit.UIStackViewDistributionFillEqually
import platform.UIKit.UIView
import platform.UIKit.removeFromSuperview
import platform.UIKit.subviews

class IosSunspotBox(
  override val value: UIStackView = UIStackView().apply {
    distribution = UIStackViewDistributionFillEqually
    axis = UILayoutConstraintAxisHorizontal
  },
) : SunspotBox<UIView> {
  override var layoutModifiers: LayoutModifier = LayoutModifier

  override val children = MutableListChildren { children ->
    @Suppress("UNCHECKED_CAST") // cinterop loses the generic.
    (value.subviews as List<UIView>).forEach(UIView::removeFromSuperview)
    children.forEach { value.addArrangedSubview(it.value) }
  }
}
