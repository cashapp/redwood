/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
@file:Suppress("unused")

package app.cash.redwood.yoga.event

import app.cash.redwood.yoga.YGConfig
import app.cash.redwood.yoga.enums.YGMeasureMode

sealed interface CallableEvent

class LayoutData : CallableEvent {
  var layouts = 0
  var measures = 0
  var maxMeasureCache = 0
  var cachedLayouts = 0
  var cachedMeasures = 0
  var measureCallbacks = 0
  val measureCallbackReasonsCount = MutableList(LayoutPassReason.COUNT) { 0 }
}

class LayoutPassStartEventData(
  val layoutContext: Any?,
) : CallableEvent

class LayoutPassEndEventData(
  val layoutContext: Any?,
  val layoutData: LayoutData,
) : CallableEvent

class MeasureCallbackEndEventData(
  val layoutContext: Any?,
  val width: Float,
  val widthMeasureMode: YGMeasureMode,
  val height: Float,
  val heightMeasureMode: YGMeasureMode,
  val measuredWidth: Float,
  val measuredHeight: Float,
  val reason: LayoutPassReason,
) : CallableEvent

class NodeAllocationEventData(
  val config: YGConfig?,
) : CallableEvent

class NodeDeallocationEventData(
  val config: YGConfig?,
) : CallableEvent

class NodeLayoutEventData(
  val layoutType: LayoutType,
  val layoutContext: Any?,
) : CallableEvent

object EmptyEventData : CallableEvent
