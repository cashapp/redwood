/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package app.cash.redwood.yoga.internal.detail

import app.cash.redwood.yoga.internal.YGValue
import app.cash.redwood.yoga.internal.enums.YGEdge

internal class Values<T> {
  private val values_: MutableList<CompactValue>

  constructor() {
    values_ = ArrayList()
  }

  constructor(defaultValue: YGValue) {
    values_ = ArrayList()
    values_.add(CompactValue.createCompactValue(defaultValue))
  }

  private fun getValue(i: Int): CompactValue {
    while (values_.size < i + 1) {
      values_.add(CompactValue.ofUndefined())
    }
    return values_[i]
  }

  operator fun set(i: Int, value: CompactValue) {
    values_[i] = value
  }

  fun getCompactValue(edge: YGEdge): CompactValue {
    return getCompactValue(edge.ordinal)
  }

  fun getCompactValue(i: Int): CompactValue {
    return CompactValue.createCompactValue(getValue(i).convertToYgValue())
  }

  operator fun get(i: Int): YGValue {
    return getValue(i).convertToYgValue()
  }

  operator fun set(i: Int, value: YGValue) {
    values_[i] = CompactValue.createCompactValue(value)
  }
}
