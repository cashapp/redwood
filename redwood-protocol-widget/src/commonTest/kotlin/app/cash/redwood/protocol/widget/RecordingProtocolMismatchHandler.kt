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
package app.cash.redwood.protocol.widget

class RecordingProtocolMismatchHandler : ProtocolMismatchHandler {
  val events = mutableListOf<String>()

  override fun onUnknownWidget(kind: Int) {
    events += "Unknown widget $kind"
  }

  override fun onUnknownLayoutModifier(tag: Int) {
    events += "Unknown layout modifier $tag"
  }

  override fun onUnknownChildren(kind: Int, tag: Int) {
    events += "Unknown children $tag for $kind"
  }

  override fun onUnknownProperty(kind: Int, tag: Int) {
    events += "Unknown property $tag for $kind"
  }
}
