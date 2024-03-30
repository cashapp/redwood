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
package app.cash.redwood.protocol.host

import app.cash.redwood.Modifier
import com.example.redwood.testing.modifier.BackgroundColor
import com.example.redwood.testing.modifier.Reuse
import com.example.redwood.testing.widget.Button
import com.example.redwood.testing.widget.Button2
import com.example.redwood.testing.widget.Rectangle
import com.example.redwood.testing.widget.ScopedTestRow
import com.example.redwood.testing.widget.Split
import com.example.redwood.testing.widget.TestRow
import com.example.redwood.testing.widget.TestSchemaWidgetFactory
import com.example.redwood.testing.widget.Text
import com.example.redwood.testing.widget.TextInput

open class EmptyTestSchemaWidgetFactory : TestSchemaWidgetFactory<Unit> {
  override fun TestRow(): TestRow<Unit> = TODO()
  override fun ScopedTestRow(): ScopedTestRow<Unit> = TODO()
  override fun Text(): Text<Unit> = TODO()
  override fun Button() = object : Button<Unit> {
    override val value get() = Unit
    override var modifier: Modifier = Modifier

    override fun text(text: String?) = TODO()
    override fun onClick(onClick: (() -> Unit)?) = TODO()
  }
  override fun Button2(): Button2<Unit> = TODO()
  override fun TextInput(): TextInput<Unit> = TODO()
  override fun Rectangle(): Rectangle<Unit> = TODO()
  override fun BackgroundColor(value: Unit, modifier: BackgroundColor) {
  }
  override fun Split(): Split<Unit> = TODO()
  override fun Reuse(value: Unit, modifier: Reuse) {
  }
}
