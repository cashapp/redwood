/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package app.cash.redwood.yoga.internal

import app.cash.redwood.yoga.internal.enums.YGUnit

internal data class YGValue(
  val value: Float,
  val unit: YGUnit,
)
