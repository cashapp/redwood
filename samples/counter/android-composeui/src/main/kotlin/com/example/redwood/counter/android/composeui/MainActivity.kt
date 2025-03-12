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
package com.example.redwood.counter.android.composeui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import app.cash.redwood.basic.composeui.ComposeUiRedwoodBasicWidgetFactory
import app.cash.redwood.basic.composeui.RedwoodBasicTheme
import app.cash.redwood.basic.widget.RedwoodBasicWidgetSystem
import app.cash.redwood.composeui.RedwoodContent
import app.cash.redwood.layout.composeui.ComposeUiRedwoodLayoutWidgetFactory
import app.cash.redwood.lazylayout.composeui.ComposeUiRedwoodLazyLayoutWidgetFactory
import coil3.ImageLoader
import com.example.redwood.counter.presenter.Counter

class MainActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val widgetSystem = RedwoodBasicWidgetSystem(
      RedwoodBasic = ComposeUiRedwoodBasicWidgetFactory(ImageLoader(this)),
      RedwoodLayout = ComposeUiRedwoodLayoutWidgetFactory(),
      RedwoodLazyLayout = ComposeUiRedwoodLazyLayoutWidgetFactory(),
    )

    setContent {
      RedwoodBasicTheme {
        RedwoodContent(widgetSystem) {
          Counter()
        }
      }
    }
  }
}
