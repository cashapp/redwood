/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package app.cash.redwood.yoga

import app.cash.redwood.yoga.detail.CompactValue
import app.cash.redwood.yoga.detail.GlobalMembers
import app.cash.redwood.yoga.detail.Values
import app.cash.redwood.yoga.enums.YGAlign
import app.cash.redwood.yoga.enums.YGDimension
import app.cash.redwood.yoga.enums.YGDirection
import app.cash.redwood.yoga.enums.YGDisplay
import app.cash.redwood.yoga.enums.YGEdge
import app.cash.redwood.yoga.enums.YGFlexDirection
import app.cash.redwood.yoga.enums.YGJustify
import app.cash.redwood.yoga.enums.YGOverflow
import app.cash.redwood.yoga.enums.YGPositionType
import app.cash.redwood.yoga.enums.YGWrap
import kotlin.reflect.KClass

class YGStyle {
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
