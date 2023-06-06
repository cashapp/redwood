/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
@file:Suppress("unused")

package app.cash.redwood.yoga.internal

import app.cash.redwood.yoga.detail.GlobalMembers
import app.cash.redwood.yoga.enums.YGDirection

class YGLayout {
  private val flags = mutableMapOf<Any?, Any>()
  val position = MutableList(4) { 0f }
  val dimensions = MutableList(2) { Yoga.YGUndefined }
  val margin = MutableList(4) { 0f }
  val border = MutableList(4) { 0f }
  val padding = MutableList(4) { 0f }
  val cachedMeasurements = MutableList(YG_MAX_CACHED_RESULT_COUNT) { YGCachedMeasurement() }
  val measuredDimensions = MutableList(2) { Yoga.YGUndefined }
  val cachedLayout = YGCachedMeasurement()
  var computedFlexBasisGeneration = 0
  var computedFlexBasis = YGFloatOptional()
  var generationCount = 0
  var lastOwnerDirection = YGDirection.YGDirectionInherit
  var nextCachedMeasurementsIndex = 0

  fun direction(): YGDirection {
    return GlobalMembers.getEnumData(
      YGDirection::class,
      flags,
      directionOffset,
    )
  }

  fun setDirection(direction: YGDirection) {
    GlobalMembers.setEnumData(
      YGDirection::class,
      flags,
      directionOffset,
      direction,
    )
  }

  fun didUseLegacyFlag(): Boolean {
    return GlobalMembers.getBooleanData(
      flags,
      didUseLegacyFlagOffset,
    )
  }

  fun setDidUseLegacyFlag(value: Boolean) {
    GlobalMembers.setBooleanData(
      flags,
      didUseLegacyFlagOffset,
      value,
    )
  }

  fun doesLegacyStretchFlagAffectsLayout(): Boolean {
    return GlobalMembers.getBooleanData(
      flags,
      doesLegacyStretchFlagAffectsLayoutOffset,
    )
  }

  fun setDoesLegacyStretchFlagAffectsLayout(value: Boolean) {
    GlobalMembers.setBooleanData(
      flags,
      doesLegacyStretchFlagAffectsLayoutOffset,
      value,
    )
  }

  fun hadOverflow(): Boolean {
    return GlobalMembers.getBooleanData(
      flags,
      hadOverflowOffset,
    )
  }

  fun setHadOverflow(hadOverflow: Boolean) {
    GlobalMembers.setBooleanData(
      flags,
      hadOverflowOffset,
      hadOverflow,
    )
  }

  companion object {
    // This value was chosen based on empirical data:
    // 98% of analyzed layouts require less than 8 entries.
    const val YG_MAX_CACHED_RESULT_COUNT = 8
    private const val directionOffset = 0
    private val didUseLegacyFlagOffset: Int =
      directionOffset + GlobalMembers.bitWidthFn<YGDirection>()
    private val doesLegacyStretchFlagAffectsLayoutOffset = didUseLegacyFlagOffset + 1
    private val hadOverflowOffset = doesLegacyStretchFlagAffectsLayoutOffset + 1
  }
}
