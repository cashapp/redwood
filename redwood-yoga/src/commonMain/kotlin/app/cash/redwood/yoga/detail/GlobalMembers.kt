/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package app.cash.redwood.yoga.detail

import app.cash.redwood.yoga.GlobalMembers
import app.cash.redwood.yoga.YGConfig
import app.cash.redwood.yoga.YGNode
import app.cash.redwood.yoga.enums.YGLogLevel
import kotlin.reflect.KClass

object GlobalMembers {
  fun vlog(
    config: YGConfig?,
    node: YGNode?,
    level: YGLogLevel,
    context: Any?,
    format: String,
    vararg args: Any?,
  ) {
    val logConfig = config ?: GlobalMembers.YGConfigGetDefault()
    logConfig.log(logConfig, node, level, context, format, *args)
  }

  fun log2ceilFn(n: Int): Int {
    return if (n < 1) 0 else 1 + log2ceilFn(n / 2)
  }

  fun mask(bitWidth: Int, index: Int): Int {
    return (1 shl bitWidth) - 1 shl index
  }

  inline fun <reified E : Enum<E>> bitWidthFn(): Int {
    return bitWidthFn(enumValues<E>())
  }

  fun <E : Enum<E>> bitWidthFn(values: Array<E>): Int {
    return log2ceilFn(values.size - 1)
  }

  inline fun <reified E : Enum<E>> getEnumData(
    e: KClass<E>,
    flags: Map<Any?, Any>,
    index: Int?,
  ): E {
    return getEnumData(e, enumValues(), flags, index)
  }

  fun <E : Enum<E>> getEnumData(
    e: KClass<E>,
    values: Array<E>,
    flags: Map<Any?, Any>,
    index: Int?,
  ): E {
    return (flags[StyleEnumFlagsKey(e, index!!)] ?: values.first()) as E
  }

  fun <E : Enum<E>> setEnumData(
    e: KClass<out E>,
    flags: MutableMap<Any?, Any>,
    index: Int,
    newValue: E,
  ): Int {
    flags[StyleEnumFlagsKey(e, index)] = newValue
    return 0
  }

  fun getBooleanData(flags: Map<Any?, Any>, index: Int?): Boolean {
    return (flags[index] ?: false) as Boolean
  }

  fun setBooleanData(flags: MutableMap<Any?, Any>, index: Int, value: Boolean): Int {
    flags[index] = value
    return 0
  }
}
