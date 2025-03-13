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
package com.example.redwood.counter.android.views

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import app.cash.redwood.basic.view.ViewRedwoodBasicWidgetSystem
import app.cash.redwood.compose.AndroidUiDispatcher
import app.cash.redwood.compose.RedwoodComposition
import app.cash.redwood.widget.RedwoodLayout
import coil3.ImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.serviceLoaderEnabled
import com.example.redwood.counter.presenter.Counter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel

class MainActivity : AppCompatActivity() {
  private val scope = CoroutineScope(AndroidUiDispatcher.Main)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val redwoodView = RedwoodLayout(this, onBackPressedDispatcher)
    setContentView(redwoodView)

    val imageLoader = ImageLoader.Builder(this)
      .serviceLoaderEnabled(false)
      .components {
        add(OkHttpNetworkFetcherFactory())
      }
      .build()

    val composition = RedwoodComposition(
      scope = scope,
      view = redwoodView,
      widgetSystem = ViewRedwoodBasicWidgetSystem(this, imageLoader),
    )
    composition.setContent {
      Counter()
    }
  }

  override fun onDestroy() {
    scope.cancel()
    super.onDestroy()
  }
}
