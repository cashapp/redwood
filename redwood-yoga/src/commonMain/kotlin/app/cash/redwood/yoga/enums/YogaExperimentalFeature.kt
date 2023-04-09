/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package app.cash.redwood.yoga.enums

enum class YogaExperimentalFeature {
  WEB_FLEX_BASIS;

  companion object {
    fun fromInt(value: Int) = values()[value]
  }
}
