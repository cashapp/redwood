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
package app.cash.redwood.layout

import app.cash.redwood.widget.Widget

/**
 * Row widget interface implemented by the platform widget adapter.
 * This should exactly mirror the interface generated from the schema.
 */
public interface RowWidget<T : Any> : Widget<T> {
  public val children: Widget.Children<T>
  public fun padding(padding: Padding)
  public fun overflow(overflow: Overflow)
  public fun horizontalAlignment(horizontalAlignment: MainAxisAlignment)
  public fun verticalAlignment(verticalAlignment: CrossAxisAlignment)
}

/**
 * Column widget interface implemented by the platform widget adapter.
 * This should exactly mirror the interface generated from the schema.
 */
public interface ColumnWidget<T : Any> : Widget<T> {
  public val children: Widget.Children<T>
  public fun padding(padding: Padding)
  public fun overflow(overflow: Overflow)
  public fun horizontalAlignment(horizontalAlignment: CrossAxisAlignment)
  public fun verticalAlignment(verticalAlignment: MainAxisAlignment)
}
