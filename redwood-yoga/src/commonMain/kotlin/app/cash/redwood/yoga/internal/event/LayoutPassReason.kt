/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package app.cash.redwood.yoga.event

enum class LayoutPassReason {
  kInitial,
  kAbsLayout,
  kStretch,
  kMultilineStretch,
  kFlexLayout,
  kMeasureChild,
  kAbsMeasureChild,
  kFlexMeasure;

  companion object {
    val COUNT = values().size
  }
}
