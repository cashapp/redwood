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
package com.example.redwood.emojisearch.android.views

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity.CENTER
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.appcompat.view.ContextThemeWrapper
import app.cash.redwood.Modifier
import app.cash.redwood.treehouse.Loading

@SuppressLint("ViewConstructor")
internal class LoadingView(
  context: Context,
) : FrameLayout(context),
  Loading<View> {
  override val value = this
  override var modifier: Modifier = Modifier

  init {
    addView(
      ProgressBar(
        ContextThemeWrapper(context, android.R.style.Widget_ProgressBar_Small),
      ),
      LayoutParams(WRAP_CONTENT, WRAP_CONTENT, CENTER),
    )
  }
}
