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
package app.cash.redwood.lazylayout.view

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import app.cash.redwood.lazylayout.widget.LazyList
import app.cash.redwood.lazylayout.widget.RedwoodLazyLayoutWidgetFactory
import app.cash.redwood.lazylayout.widget.RefreshableLazyList

public class ViewRedwoodLazyLayoutWidgetFactory(
  private val context: Context,
) : RedwoodLazyLayoutWidgetFactory<View> {
  public override fun LazyList(): LazyList<View> = ViewLazyList(
    recyclerViewFactory = { RecyclerView(context) },
  )

  public override fun RefreshableLazyList(): RefreshableLazyList<View> = ViewRefreshableLazyList(
    recyclerViewFactory = { RecyclerView(context) },
    swipeRefreshLayoutFactory = { SwipeRefreshLayout(context) },
  )
}
