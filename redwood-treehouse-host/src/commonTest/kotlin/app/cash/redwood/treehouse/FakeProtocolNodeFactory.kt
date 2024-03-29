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
package app.cash.redwood.treehouse

import app.cash.redwood.Modifier
import app.cash.redwood.RedwoodCodegenApi
import app.cash.redwood.protocol.ChildrenTag
import app.cash.redwood.protocol.ModifierElement
import app.cash.redwood.protocol.WidgetTag
import app.cash.redwood.protocol.host.GeneratedProtocolFactory
import app.cash.redwood.protocol.host.ProtocolNode
import app.cash.redwood.widget.WidgetSystem

@OptIn(RedwoodCodegenApi::class)
internal class FakeProtocolNodeFactory : GeneratedProtocolFactory<FakeWidget> {
  override val widgetSystem: WidgetSystem<FakeWidget> = FakeWidgetSystem()
  override fun createNode(tag: WidgetTag): ProtocolNode<FakeWidget> = FakeProtocolNode()
  override fun createModifier(element: ModifierElement): Modifier = Modifier
  override fun widgetChildren(tag: WidgetTag): List<ChildrenTag> = emptyList()
}
