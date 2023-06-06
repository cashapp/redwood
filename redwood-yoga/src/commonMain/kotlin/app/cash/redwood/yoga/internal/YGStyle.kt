/*
 * Copyright (C) 2023 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@file:Suppress("unused")

package app.cash.redwood.yoga.internal

import app.cash.redwood.yoga.internal.detail.CompactValue
import app.cash.redwood.yoga.internal.detail.GlobalMembers
import app.cash.redwood.yoga.internal.detail.Values
import app.cash.redwood.yoga.internal.enums.YGAlign
import app.cash.redwood.yoga.internal.enums.YGDimension
import app.cash.redwood.yoga.internal.enums.YGDirection
import app.cash.redwood.yoga.internal.enums.YGDisplay
import app.cash.redwood.yoga.internal.enums.YGEdge
import app.cash.redwood.yoga.internal.enums.YGFlexDirection
import app.cash.redwood.yoga.internal.enums.YGJustify
import app.cash.redwood.yoga.internal.enums.YGOverflow
import app.cash.redwood.yoga.internal.enums.YGPositionType
import app.cash.redwood.yoga.internal.enums.YGWrap
import kotlin.reflect.KClass

internal class YGStyle {
  val margin = Values<YGEdge>()
  val position = Values<YGEdge>()
  val padding = Values<YGEdge>()
  val border = Values<YGEdge>()
  val dimensions = Values<YGDimension>(CompactValue.ofAuto().convertToYgValue())
  val minDimensions = Values<YGDimension>()
  val maxDimensions = Values<YGDimension>()
  var aspectRatio = YGFloatOptional()
  var flex = YGFloatOptional()
  var flexGrow = YGFloatOptional()
  var flexShrink = YGFloatOptional()
  var flexBasis = CompactValue.ofAuto()
  private val flags = mutableMapOf<Any?, Any>()

  init {
    GlobalMembers.setEnumData(
      YGAlign::class, flags, alignContentOffset, YGAlign.YGAlignFlexStart,
    )
    GlobalMembers.setEnumData(
      YGAlign::class, flags, alignItemsOffset, YGAlign.YGAlignStretch,
    )
  }

  fun direction(): YGDirection {
    return GlobalMembers.getEnumData(
      YGDirection::class, flags, directionOffset,
    )
  }

  fun directionBitfieldRef(): BitfieldRef<YGDirection> {
    return BitfieldRef(this, directionOffset)
  }

  fun flexDirection(): YGFlexDirection {
    return GlobalMembers.getEnumData(
      YGFlexDirection::class, flags, flexdirectionOffset,
    )
  }

  fun flexDirectionBitfieldRef(): BitfieldRef<YGFlexDirection> {
    return BitfieldRef(this, flexdirectionOffset)
  }

  fun justifyContent(): YGJustify {
    return GlobalMembers.getEnumData(
      YGJustify::class, flags, justifyContentOffset,
    )
  }

  fun justifyContentBitfieldRef(): BitfieldRef<YGJustify> {
    return BitfieldRef(this, justifyContentOffset)
  }

  fun alignContent(): YGAlign {
    return GlobalMembers.getEnumData(
      YGAlign::class, flags, alignContentOffset,
    )
  }

  fun alignContentBitfieldRef(): BitfieldRef<YGAlign> {
    return BitfieldRef(this, alignContentOffset)
  }

  fun alignItems(): YGAlign {
    return GlobalMembers.getEnumData(
      YGAlign::class, flags, alignItemsOffset,
    )
  }

  fun alignItemsBitfieldRef(): BitfieldRef<YGAlign> {
    return BitfieldRef(this, alignItemsOffset)
  }

  fun alignSelf(): YGAlign {
    return GlobalMembers.getEnumData(
      YGAlign::class, flags, alignSelfOffset,
    )
  }

  fun alignSelfBitfieldRef(): BitfieldRef<YGAlign> {
    return BitfieldRef(this, alignSelfOffset)
  }

  fun positionType(): YGPositionType {
    return GlobalMembers.getEnumData(
      YGPositionType::class, flags, positionTypeOffset,
    )
  }

  fun positionTypeBitfieldRef(): BitfieldRef<YGPositionType> {
    return BitfieldRef(this, positionTypeOffset)
  }

  fun flexWrap(): YGWrap {
    return GlobalMembers.getEnumData(
      YGWrap::class, flags, flexWrapOffset,
    )
  }

  fun flexWrapBitfieldRef(): BitfieldRef<YGWrap> {
    return BitfieldRef(this, flexWrapOffset)
  }

  fun overflow(): YGOverflow {
    return GlobalMembers.getEnumData(
      YGOverflow::class, flags, overflowOffset,
    )
  }

  fun overflowBitfieldRef(): BitfieldRef<YGOverflow> {
    return BitfieldRef(this, overflowOffset)
  }

  fun display(): YGDisplay {
    return GlobalMembers.getEnumData(
      YGDisplay::class, flags, displayOffset,
    )
  }

  fun displayBitfieldRef(): BitfieldRef<YGDisplay> {
    return BitfieldRef(this, displayOffset)
  }

  class BitfieldRef<T : Enum<T>>(
    val style: YGStyle,
    val offset: Int,
    private val enumValues: Array<T>,
  ) {
    fun getValue(enumClazz: KClass<T>): T {
      return GlobalMembers.getEnumData(enumClazz, enumValues, style.flags, offset)
    }

    fun setValue(x: T): BitfieldRef<T> {
      GlobalMembers.setEnumData(x::class, style.flags, offset, x)
      return this
    }
  }

  inline fun <reified T : Enum<T>> BitfieldRef(style: YGStyle, offset: Int) =
    BitfieldRef<T>(style, offset, enumValues())

  companion object {
    private const val directionOffset = 0
    private val flexdirectionOffset: Int =
      directionOffset + GlobalMembers.bitWidthFn<YGDirection>()
    private val justifyContentOffset: Int =
      flexdirectionOffset + GlobalMembers.bitWidthFn<YGFlexDirection>()
    private val alignContentOffset: Int =
      justifyContentOffset + GlobalMembers.bitWidthFn<YGJustify>()

    //  ~YGStyle() = default;
    private val alignItemsOffset: Int =
      alignContentOffset + GlobalMembers.bitWidthFn<YGAlign>()
    private val alignSelfOffset: Int =
      alignItemsOffset + GlobalMembers.bitWidthFn<YGAlign>()
    private val positionTypeOffset: Int =
      alignSelfOffset + GlobalMembers.bitWidthFn<YGAlign>()
    private val flexWrapOffset: Int =
      positionTypeOffset + GlobalMembers.bitWidthFn<YGPositionType>()
    private val overflowOffset: Int =
      flexWrapOffset + GlobalMembers.bitWidthFn<YGWrap>()
    private val displayOffset: Int =
      overflowOffset + GlobalMembers.bitWidthFn<YGOverflow>()
  }
}
