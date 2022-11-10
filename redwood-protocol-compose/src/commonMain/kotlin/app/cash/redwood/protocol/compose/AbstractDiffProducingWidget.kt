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
package app.cash.redwood.protocol.compose

import app.cash.redwood.protocol.Event
import app.cash.redwood.protocol.Id
import app.cash.redwood.protocol.LayoutModifiers
import app.cash.redwood.protocol.PropertyDiff
import app.cash.redwood.widget.Widget.Children

/**
 * @suppress For generated code usage only.
 */
public abstract class AbstractDiffProducingWidget(
  public val type: Int,
) : DiffProducingWidget {
  override val value: Nothing
    get() = throw AssertionError()

  public var id: Id = Id(ULong.MAX_VALUE)
    internal set

  @Suppress("PropertyName") // Avoiding potential collision with subtype properties.
  internal lateinit var _diffAppender: DiffAppender

  protected fun appendDiff(layoutModifiers: LayoutModifiers) {
    _diffAppender.append(layoutModifiers)
  }

  protected fun appendDiff(diff: PropertyDiff) {
    _diffAppender.append(diff)
  }

  protected fun diffProducingWidgetChildren(tag: UInt): Children<Nothing> {
    return DiffProducingWidgetChildren(id, tag, _diffAppender)
  }

  public abstract fun sendEvent(event: Event)
}
