/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package app.cash.redwood.yoga.internal.interfaces

import app.cash.redwood.yoga.internal.YGNode

internal fun interface CloneWithContextFn {
  operator fun invoke(
    node: YGNode,
    owner: YGNode?,
    childIndex: Int,
    cloneContext: Any?
  ): YGNode
}
