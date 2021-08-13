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

package example.android.views

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import example.android.views.databinding.ItemBinding
import example.android.views.databinding.ScrollableColumnBinding
import example.android.views.databinding.ToolbarBinding
import example.schema.widget.TodoWidgetFactory

class ViewWidgetFactory(
  private val context: Context,
) : TodoWidgetFactory<View> {
  private val inflater = LayoutInflater.from(context)

  override fun Toolbar() = ViewToolbar(ToolbarBinding.inflate(inflater))
  override fun ScrollableColumn() = ViewScrollableColumn(ScrollableColumnBinding.inflate(inflater))
  override fun Item() = ViewItem(ItemBinding.inflate(inflater))
  override fun Column() = ViewColumn(LinearLayout(context))
}
