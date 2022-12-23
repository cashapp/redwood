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
package com.example.redwood.counter.ios

import com.example.redwood.counter.widget.Box
import com.example.redwood.counter.widget.Button
import com.example.redwood.counter.widget.SchemaWidgetFactory
import com.example.redwood.counter.widget.Text
import platform.UIKit.UIView

object IosWidgetFactory : SchemaWidgetFactory<UIView> {
  override fun Box(): Box<UIView> = IosBox()
  override fun Text(): Text<UIView> = IosText()
  override fun Button(): Button<UIView> = IosButton()
}
