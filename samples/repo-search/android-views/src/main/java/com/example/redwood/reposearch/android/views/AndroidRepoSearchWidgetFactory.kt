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
package com.example.redwood.reposearch.android.views

import android.content.Context
import android.view.View
import android.widget.TextView
import app.cash.redwood.treehouse.AppService
import app.cash.redwood.treehouse.TreehouseApp
import com.example.redwood.reposearch.widget.RepoSearchWidgetFactory
import com.example.redwood.reposearch.widget.Text

class AndroidRepoSearchWidgetFactory<A : AppService>(
  private val context: Context,
  private val treehouseApp: TreehouseApp<A>,
) : RepoSearchWidgetFactory<View> {
  override fun Text(): Text<View> = ViewText(TextView(context))
}
