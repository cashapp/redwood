/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package app.cash.redwood.yoga.internal.detail

import app.cash.redwood.yoga.internal.YGConfig
import app.cash.redwood.yoga.internal.YGNode
import app.cash.redwood.yoga.internal.enums.YGLogLevel

internal object Log {
  fun log(
    node: YGNode?,
    level: YGLogLevel,
    context: Any?,
    format: String,
    vararg args: Any?,
  ) {
    GlobalMembers.vlog(node?.config, node, level, context, format, *args)
  }

  fun log(
    config: YGConfig?,
    level: YGLogLevel,
    context: Any?,
    format: String,
    vararg args: Any?,
  ) {
    GlobalMembers.vlog(config, null, level, context, format, *args)
  }
}
