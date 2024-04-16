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

import app.cash.redwood.protocol.ChildrenTag
import app.cash.redwood.protocol.ModifierTag
import app.cash.redwood.protocol.PropertyTag
import app.cash.redwood.protocol.WidgetTag

class RecordingProtocolMismatchHandler : ProtocolMismatchHandler {
  val events = mutableListOf<String>()

  override fun onUnknownWidget(tag: WidgetTag) {
    events += "Unknown widget ${tag.value}"
  }

  override fun onUnknownModifier(tag: ModifierTag) {
    events += "Unknown layout modifier ${tag.value}"
  }

  override fun onUnknownChildren(widgetTag: WidgetTag, tag: ChildrenTag) {
    events += "Unknown children ${tag.value} for ${widgetTag.value}"
  }

  override fun onUnknownProperty(widgetTag: WidgetTag, tag: PropertyTag) {
    events += "Unknown property ${tag.value} for ${widgetTag.value}"
  }
}
