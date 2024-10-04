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
import app.cash.redwood.protocol.Id
import app.cash.redwood.protocol.ModifierElement
import app.cash.redwood.protocol.WidgetTag
import app.cash.redwood.widget.WidgetSystem
import kotlin.native.ObjCName

/**
 * A marker interface for the widget-side factory of the protocol.
 *
 * @see HostProtocolAdapter
 */
@ObjCName("ProtocolFactory", exact = true)
public sealed interface ProtocolFactory<W : Any> {
  public val widgetSystem: WidgetSystem<W>
}

/**
 * [ProtocolFactory] but containing codegen APIs for a schema.
 *
 * @suppress
 */
@RedwoodCodegenApi
public interface GeneratedHostProtocol<W : Any> : ProtocolFactory<W> {
  /**
   * Look up host protocol information for a widget with the given [tag].
   *
   * Invalid [tag] values can either produce an exception or result in `null` being returned.
   * If `null` is returned, the caller should make every effort to ignore this node and
   * continue executing.
   */
  public fun widget(tag: WidgetTag): WidgetHostProtocol<W>?

  /**
   * Create a new modifier from the specified [element].
   *
   * Invalid [`element.tag`][ModifierElement.tag] values can either produce an exception
   * or result in the unit [`Modifier`][Modifier.Companion] being returned.
   */
  public fun createModifier(element: ModifierElement): Modifier
}

/**
 * Protocol APIs for a widget definition.
 *
 * @suppress
 */
@RedwoodCodegenApi
public interface WidgetHostProtocol<W : Any> {
  /** Create an instance of this widget wrapped as a [ProtocolNode] with the given [id]. */
  public fun createNode(id: Id): ProtocolNode<W>

  /**
   * Look up known children tags for this widget. These are stored as a bare [IntArray]
   * for efficiency, but are otherwise an array of [ChildrenTag] instances. A value of
   * `null` indicates no children.
   */
  public val childrenTags: IntArray?
}
