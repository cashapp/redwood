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
package app.cash.redwood.yoga

import app.cash.redwood.yoga.enums.YGMeasureMode

class YGCachedMeasurement {
  var availableWidth = -1f
  var availableHeight = -1f
  var widthMeasureMode = YGMeasureMode.YGMeasureModeUndefined
  var heightMeasureMode = YGMeasureMode.YGMeasureModeUndefined
  var computedWidth = -1f
  var computedHeight = -1f

  fun equalsTo(measurement: YGCachedMeasurement): Boolean {
    var isEqual = widthMeasureMode == measurement.widthMeasureMode && heightMeasureMode == measurement.heightMeasureMode
    if (!GlobalMembers.isUndefined(availableWidth) || !GlobalMembers.isUndefined(measurement.availableWidth)) {
      isEqual = isEqual && availableWidth == measurement.availableWidth
    }
    if (!GlobalMembers.isUndefined(availableHeight) || !GlobalMembers.isUndefined(measurement.availableHeight)) {
      isEqual = isEqual && availableHeight == measurement.availableHeight
    }
    if (!GlobalMembers.isUndefined(computedWidth) || !GlobalMembers.isUndefined(measurement.computedWidth)) {
      isEqual = isEqual && computedWidth == measurement.computedWidth
    }
    if (!GlobalMembers.isUndefined(computedHeight) || !GlobalMembers.isUndefined(measurement.computedHeight)) {
      isEqual = isEqual && computedHeight == measurement.computedHeight
    }
    return isEqual
  }
}
