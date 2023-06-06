/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package app.cash.redwood.yoga.internal

internal class YGSize(
  val width: Float,
  val height: Float,
)

internal fun YGSize(width: Int, height: Int): YGSize {
  return YGSize(width.toFloat(), height.toFloat())
}
