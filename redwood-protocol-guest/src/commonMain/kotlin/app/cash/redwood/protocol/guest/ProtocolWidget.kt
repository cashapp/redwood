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
package app.cash.redwood.protocol.guest

import app.cash.redwood.protocol.Event
import app.cash.redwood.protocol.Id
import app.cash.redwood.protocol.WidgetTag
import app.cash.redwood.widget.Widget

/**
 * A [Widget] with no platform-specific representation which instead produces protocol changes
 * based on its properties.
 *
 * @suppress For generated code use only.
 */
public interface ProtocolWidget : Widget<Unit> {
  public val id: Id
  public val tag: WidgetTag

  override val value: Unit get() = Unit

  public fun sendEvent(event: Event)
}
