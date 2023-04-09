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
