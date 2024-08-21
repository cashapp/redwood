/*
 * Copyright (C) 2024 Square, Inc.
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

import app.cash.redwood.Modifier
import app.cash.redwood.RedwoodCodegenApi
import app.cash.redwood.protocol.ModifierTag
import app.cash.redwood.widget.WidgetSystem
import kotlinx.serialization.KSerializer

public interface ProtocolWidgetSystemFactory {
  /** Create a new [WidgetSystem] connected to a host via [guestAdapter]. */
  public fun create(
    guestAdapter: GuestProtocolAdapter,
    mismatchHandler: ProtocolMismatchHandler = ProtocolMismatchHandler.Throwing,
  ): WidgetSystem<Unit>

  @RedwoodCodegenApi
  public fun modifierTag(element: Modifier.Element): ModifierTag

  /** Returns null if the modifier is stateless and should serialize as null. */
  @RedwoodCodegenApi
  public fun <T : Modifier.Element> modifierSerializer(element: T): KSerializer<T>?
}
