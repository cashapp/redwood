/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package app.cash.redwood.yoga.internal

class YGCollectFlexItemsRowValues {
  var itemsOnLine = 0
  var sizeConsumedOnCurrentLine = 0f
  var totalFlexGrowFactors = 0f
  var totalFlexShrinkScaledFactors = 0f
  var endOfLineIndex = 0
  val relativeChildren = ArrayList<YGNode>()
  var remainingFreeSpace = 0f
  var mainDim = 0f
  var crossDim = 0f
}
