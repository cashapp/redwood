/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package app.cash.redwood.yoga.internal.detail

import kotlin.reflect.KClass

internal data class StyleEnumFlagsKey(
  val enumClazz: KClass<*>,
  val index: Int,
)
