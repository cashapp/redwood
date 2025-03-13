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
package app.cash.redwood.basic.view

import android.content.Context
import android.view.View
import android.widget.Button as PlatformButton
import android.widget.ImageView
import android.widget.TextView
import app.cash.redwood.basic.modifier.Reuse
import app.cash.redwood.basic.widget.Button
import app.cash.redwood.basic.widget.Image
import app.cash.redwood.basic.widget.RedwoodBasicWidgetFactory
import app.cash.redwood.basic.widget.RedwoodBasicWidgetSystem
import app.cash.redwood.basic.widget.Text
import app.cash.redwood.basic.widget.TextInput
import app.cash.redwood.layout.view.ViewRedwoodLayoutWidgetFactory
import app.cash.redwood.lazylayout.view.ViewRedwoodLazyLayoutWidgetFactory
import coil3.ImageLoader

public class ViewRedwoodBasicWidgetFactory(
  private val context: Context,
  private val imageLoader: ImageLoader,
) : RedwoodBasicWidgetFactory<View> {
  override fun TextInput(): TextInput<View> = ViewTextInput(context)
  override fun Text(): Text<View> = ViewText(TextView(context))
  override fun Image(): Image<View> = ViewImage(ImageView(context), imageLoader)
  override fun Button(): Button<View> = ViewButton(PlatformButton(context))
  override fun Reuse(value: View, modifier: Reuse) {
  }
}

@Suppress("FunctionName") // Acting like a type.
public fun ViewRedwoodBasicWidgetSystem(
  context: Context,
  imageLoader: ImageLoader,
): RedwoodBasicWidgetSystem<View> {
  return RedwoodBasicWidgetSystem(
    RedwoodBasic = ViewRedwoodBasicWidgetFactory(context, imageLoader),
    RedwoodLayout = ViewRedwoodLayoutWidgetFactory(context),
    RedwoodLazyLayout = ViewRedwoodLazyLayoutWidgetFactory(context),
  )
}
