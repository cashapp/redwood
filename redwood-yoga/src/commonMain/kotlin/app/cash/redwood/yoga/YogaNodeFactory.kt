/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package app.cash.redwood.yoga

import kotlin.jvm.JvmStatic

object YogaNodeFactory {
  @JvmStatic
  fun create(): YogaNode {
    return YogaNodeWrapper()
  }

  @JvmStatic
  fun create(config: YogaConfig): YogaNode {
    return YogaNodeWrapper(config)
  }
}
