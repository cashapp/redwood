/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package app.cash.redwood.yoga.internal

import app.cash.redwood.yoga.internal.enums.YGMeasureMode

internal class YGCachedMeasurement {
  var availableWidth = -1f
  var availableHeight = -1f
  var widthMeasureMode = YGMeasureMode.YGMeasureModeUndefined
  var heightMeasureMode = YGMeasureMode.YGMeasureModeUndefined
  var computedWidth = -1f
  var computedHeight = -1f
}
