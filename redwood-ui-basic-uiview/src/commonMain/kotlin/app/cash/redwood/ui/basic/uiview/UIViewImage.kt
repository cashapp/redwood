/*
 * Copyright (C) 2025 Square, Inc.
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
package app.cash.redwood.ui.basic.uiview

import app.cash.redwood.Modifier
import app.cash.redwood.ui.basic.widget.Image
import kotlinx.cinterop.ObjCAction
import platform.Foundation.NSSelectorFromString
import platform.Foundation.NSURL
import platform.UIKit.UIImageView
import platform.UIKit.UILayoutConstraintAxisHorizontal
import platform.UIKit.UILayoutPriorityRequired
import platform.UIKit.UITapGestureRecognizer
import platform.UIKit.UIView
import platform.UIKit.UIViewContentMode.UIViewContentModeScaleAspectFit
import platform.darwin.NSObject

internal class UIViewImage(
  private val imageLoader: RemoteImageLoader,
) : Image<UIView> {
  override val value = UIImageView().apply {
    contentMode = UIViewContentModeScaleAspectFit
    setContentHuggingPriority(UILayoutPriorityRequired, UILayoutConstraintAxisHorizontal)
    setUserInteractionEnabled(true)
  }

  private var lastUrl: NSURL? = null
  private var onClick: (() -> Unit)? = null
  private var onClickGestureRecognizer: UITapGestureRecognizer? = null

  private val clickedTarget = ClickedTarget(this)
  private val clickedPointer = NSSelectorFromString(ClickedTarget::clicked.name)

  override fun url(url: String) {
    value.image = null

    val url = NSURL.URLWithString(url) ?: return
    lastUrl = url

    imageLoader.loadImage(url) { url, image ->
      if (url == lastUrl) {
        value.image = image
      }
    }
  }

  override fun onClick(onClick: (() -> Unit)?) {
    if (this.onClick == null) {
      if (onClick != null) {
        onClickGestureRecognizer = UITapGestureRecognizer().apply {
          addTarget(clickedTarget, clickedPointer)
          value.addGestureRecognizer(this)
        }
      }
    } else if (onClick == null) {
      value.removeGestureRecognizer(onClickGestureRecognizer!!)
      onClickGestureRecognizer = null
    }
    this.onClick = onClick
  }

  private fun clicked() {
    onClick?.invoke()
  }

  /**
   * Selectors can only target subtypes of [NSObject] which is why this helper exists.
   * We cannot subclass it on [UIViewImage] directly because it implements a Kotlin
   * interface, but we also don't want to leak it into public API. The contained function
   * must be public for the selector to work.
   */
  private class ClickedTarget(private val target: UIViewImage) : NSObject() {
    @ObjCAction
    fun clicked() {
      target.clicked()
    }
  }

  override var modifier: Modifier = Modifier
}
