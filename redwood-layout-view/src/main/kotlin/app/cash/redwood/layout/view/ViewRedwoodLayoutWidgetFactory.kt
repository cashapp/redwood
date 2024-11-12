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
package app.cash.redwood.layout.view

import android.content.Context
import android.view.View
import app.cash.redwood.layout.widget.Box
import app.cash.redwood.layout.widget.Column
import app.cash.redwood.layout.widget.RedwoodLayoutWidgetFactory
import app.cash.redwood.layout.widget.Row
import app.cash.redwood.layout.widget.Spacer

public class ViewRedwoodLayoutWidgetFactory(
  private val context: Context,
) : RedwoodLayoutWidgetFactory<View> {
  override fun Box(): Box<View> = ViewBox(context)

  override fun Column(): Column<View> = ViewColumn(context)

  override fun Row(): Row<View> = ViewRow(context)

  override fun Spacer(): Spacer<View> = ViewSpacer(context)
}
