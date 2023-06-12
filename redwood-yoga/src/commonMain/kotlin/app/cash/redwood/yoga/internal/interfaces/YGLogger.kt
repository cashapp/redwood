/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package app.cash.redwood.yoga.internal.interfaces

import app.cash.redwood.yoga.internal.YGConfig
import app.cash.redwood.yoga.internal.YGNode
import app.cash.redwood.yoga.internal.enums.YGLogLevel

internal fun interface YGLogger {
  operator fun invoke(
    config: YGConfig?,
    node: YGNode?,
    level: YGLogLevel,
    format: String,
    vararg args: Any?,
  ): Int
}
