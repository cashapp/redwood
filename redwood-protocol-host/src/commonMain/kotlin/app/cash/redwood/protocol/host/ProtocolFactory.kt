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
package app.cash.redwood.protocol.host

import app.cash.redwood.Modifier
import app.cash.redwood.RedwoodCodegenApi
import app.cash.redwood.protocol.ChildrenTag
import app.cash.redwood.protocol.ModifierElement
import app.cash.redwood.protocol.WidgetTag
import app.cash.redwood.widget.WidgetSystem
import kotlin.native.ObjCName

/**
 * A marker interface for the widget-side factory of the protocol.
 *
 * @see ProtocolBridge
 */
@ObjCName("ProtocolFactory", exact = true)
public interface ProtocolFactory<W : Any> {
  public val widgetSystem: WidgetSystem<W>
}

/**
 * [ProtocolFactory] but containing codegen APIs.
 *
 * @suppress
 */
@RedwoodCodegenApi
public interface GeneratedProtocolFactory<W : Any> : ProtocolFactory<W> {
  /**
   * Create a new protocol node of the specified [tag].
   *
   * Invalid [tag] values can either produce an exception or result in `null` being returned.
   * If `null` is returned, the caller should make every effort to ignore this node and
   * continue executing.
   */
  public fun createNode(tag: WidgetTag): ProtocolNode<W>?

  /**
   * Create a new modifier from the specified [element].
   *
   * Invalid [`element.tag`][ModifierElement.tag] values can either produce an exception
   * or result in the unit [`Modifier`][Modifier.Companion] being returned.
   */
  public fun createModifier(element: ModifierElement): Modifier

  /**
   * A map that reflects the tree structure for this factory.
   *
   * This map's keys are all known widget tags. Each entry's values are the known children tags
   * for that widget tag.
   */
  public val childrenTags: Map<WidgetTag, List<ChildrenTag>>
}
