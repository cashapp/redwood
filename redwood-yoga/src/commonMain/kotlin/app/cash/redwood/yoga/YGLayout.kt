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

import app.cash.redwood.yoga.enums.YGDirection

class YGLayout {
  private val flags: MutableMap<Any?, Any> = mutableMapOf()
  val position = createEmptyFloatArray()
  val dimensions = arrayListOf(GlobalMembers.YGUndefined, GlobalMembers.YGUndefined)
  val margin = createEmptyFloatArray()
  val border = createEmptyFloatArray()
  val padding = createEmptyFloatArray()
  var computedFlexBasisGeneration = 0
  var computedFlexBasis = YGFloatOptional()
  var generationCount = 0
  var lastOwnerDirection = YGDirection.YGDirectionInherit
  var nextCachedMeasurementsIndex = 0
  val cachedMeasurements = ArrayList<YGCachedMeasurement>(YG_MAX_CACHED_RESULT_COUNT).apply {
    repeat(YG_MAX_CACHED_RESULT_COUNT) { add(YGCachedMeasurement()) }
  }
  val measuredDimensions = arrayListOf(GlobalMembers.YGUndefined, GlobalMembers.YGUndefined)
  val cachedLayout = YGCachedMeasurement()

  private fun createEmptyFloatArray(): ArrayList<Float> {
    return arrayListOf(0f, 0f, 0f, 0f)
  }

  fun direction(): YGDirection {
    return app.cash.redwood.yoga.detail.GlobalMembers.getEnumData(
      YGDirection::class,
      flags,
      directionOffset,
    )
  }

  fun setDirection(direction: YGDirection) {
    app.cash.redwood.yoga.detail.GlobalMembers.setEnumData(
      YGDirection::class,
      flags,
      directionOffset,
      direction,
    )
  }

  fun didUseLegacyFlag(): Boolean {
    return app.cash.redwood.yoga.detail.GlobalMembers.getBooleanData(
      flags,
      didUseLegacyFlagOffset,
    )
  }

  fun setDidUseLegacyFlag(value: Boolean) {
    app.cash.redwood.yoga.detail.GlobalMembers.setBooleanData(
      flags,
      didUseLegacyFlagOffset,
      value,
    )
  }

  fun doesLegacyStretchFlagAffectsLayout(): Boolean {
    return app.cash.redwood.yoga.detail.GlobalMembers.getBooleanData(
      flags,
      doesLegacyStretchFlagAffectsLayoutOffset,
    )
  }

  fun setDoesLegacyStretchFlagAffectsLayout(value: Boolean) {
    app.cash.redwood.yoga.detail.GlobalMembers.setBooleanData(
      flags,
      doesLegacyStretchFlagAffectsLayoutOffset,
      value,
    )
  }

  fun hadOverflow(): Boolean {
    return app.cash.redwood.yoga.detail.GlobalMembers.getBooleanData(
      flags,
      hadOverflowOffset,
    )
  }

  fun setHadOverflow(hadOverflow: Boolean) {
    app.cash.redwood.yoga.detail.GlobalMembers.setBooleanData(
      flags,
      hadOverflowOffset,
      hadOverflow,
    )
  }

  fun equalsTo(layout: YGLayout): Boolean {
    var isEqual = GlobalMembers.YGFloatArrayEqual(position, layout.position) &&
      GlobalMembers.YGFloatArrayEqual(dimensions, layout.dimensions) &&
      GlobalMembers.YGFloatArrayEqual(margin, layout.margin) &&
      GlobalMembers.YGFloatArrayEqual(border, layout.border) &&
      GlobalMembers.YGFloatArrayEqual(padding, layout.padding) &&
      direction() == layout.direction() &&
      hadOverflow() == layout.hadOverflow() &&
      lastOwnerDirection == layout.lastOwnerDirection &&
      nextCachedMeasurementsIndex == layout.nextCachedMeasurementsIndex &&
      cachedLayout.equalsTo(layout.cachedLayout) &&
      computedFlexBasis == layout.computedFlexBasis
    var i = 0
    while (i < YG_MAX_CACHED_RESULT_COUNT && isEqual) {
      //TODO: Verify if this is correct
      isEqual = cachedMeasurements[i].equalsTo(layout.cachedMeasurements[i])
      ++i
    }
    if (!GlobalMembers.isUndefined(measuredDimensions[0]) ||
      !GlobalMembers.isUndefined(layout.measuredDimensions[0])) {
      isEqual = isEqual && measuredDimensions[0] == layout.measuredDimensions[0]
    }
    if (!GlobalMembers.isUndefined(measuredDimensions[1]) ||
      !GlobalMembers.isUndefined(layout.measuredDimensions[1])) {
      isEqual = isEqual && measuredDimensions[1] == layout.measuredDimensions[1]
    }
    return isEqual
  }

  companion object {
    // This value was chosen based on empirical data:
    // 98% of analyzed layouts require less than 8 entries.
    const val YG_MAX_CACHED_RESULT_COUNT = 8
    private const val directionOffset = 0
    private val didUseLegacyFlagOffset: Int =
      directionOffset + app.cash.redwood.yoga.detail.GlobalMembers.bitWidthFn<YGDirection>()
    private val doesLegacyStretchFlagAffectsLayoutOffset = didUseLegacyFlagOffset + 1
    private val hadOverflowOffset = doesLegacyStretchFlagAffectsLayoutOffset + 1
  }
}
