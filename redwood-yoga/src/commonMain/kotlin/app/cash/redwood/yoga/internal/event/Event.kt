/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
@file:Suppress("unused", "UNUSED_PARAMETER")

package app.cash.redwood.yoga.event

import app.cash.redwood.yoga.YGNode
import kotlin.reflect.KClass

object Event {
  private val listeners = mutableMapOf<KClass<*>, MutableList<(CallableEvent) -> Unit>>()

  @Suppress("UNCHECKED_CAST")
  fun <T : CallableEvent> subscribe(
    clazz: KClass<T>,
    listener: (T) -> Unit,
  ) {
    listeners.getOrPut(clazz) { mutableListOf() } += listener as (CallableEvent) -> Unit
  }

  fun publish(node: YGNode?, eventData: CallableEvent = EmptyEventData) {
    val listeners = listeners[eventData::class].orEmpty()
    for (listener in listeners) {
      listener(eventData)
    }
  }

  fun reset() {
    listeners.clear()
  }
}
