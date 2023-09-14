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
package com.example.redwood.testing.android.views

import android.content.Context
import android.view.View
import android.widget.Button as ButtonWidget
import android.widget.TextView
import com.example.redwood.testing.widget.Button
import com.example.redwood.testing.widget.TestSchemaWidgetFactory
import com.example.redwood.testing.widget.Text

class AndroidTestSchemaWidgetFactory(
  private val context: Context,
) : TestSchemaWidgetFactory<View> {
  override fun Text(): Text<View> = ViewText(TextView(context))
  override fun TestRow() = throw UnsupportedOperationException()
  override fun ScopedTestRow() = throw UnsupportedOperationException()
  override fun Button(): Button<View> = ViewButton(ButtonWidget(context))
  override fun Button2() = TODO()
  override fun TextInput() = TODO()
}
