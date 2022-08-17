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
package app.cash.treehouse

import kotlinx.cinterop.cValue
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import platform.CoreGraphics.CGRectZero
import platform.UIKit.UIStackView
import platform.UIKit.UIView

public class TreehouseUIKitView<T : Any>(
  private val content: TreehouseContent<T>,
) : TreehouseView<T> {
  public val view: UIView = UIStackView(frame = cValue { CGRectZero })
  private var treehouseHost: TreehouseHost<T>? = null

  // TODO(jwilson): track when this view is detached from screen
  override val boundContent: TreehouseContent<T>? = content

  public fun register(treehouseHost: TreehouseHost<T>?) {
    this.treehouseHost = treehouseHost
    treehouseHost?.onContentChanged(this)
  }
}

// TODO(jwilson): we're currently doing everything on the main thread on iOS.
public class IosTreehouseDispatchers : TreehouseDispatchers {
  override val main: CoroutineDispatcher = Dispatchers.Main
  override val zipline: CoroutineDispatcher = Dispatchers.Main

  override fun checkMain() {
  }

  override fun checkZipline() {
  }
}
