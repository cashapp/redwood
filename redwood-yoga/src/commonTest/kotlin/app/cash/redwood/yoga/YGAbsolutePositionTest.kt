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
package app.cash.redwood.yoga

import app.cash.redwood.yoga.enums.YGAlign
import app.cash.redwood.yoga.enums.YGDirection
import app.cash.redwood.yoga.enums.YGEdge
import app.cash.redwood.yoga.enums.YGFlexDirection
import app.cash.redwood.yoga.enums.YGJustify
import app.cash.redwood.yoga.enums.YGOverflow
import app.cash.redwood.yoga.enums.YGPositionType
import app.cash.redwood.yoga.enums.YGWrap
import kotlin.test.Test
import kotlin.test.assertEquals

class YGAbsolutePositionTest {
  @Test
  fun absolute_layout_width_height_start_top() {
    val config = Yoga.YGConfigNew()
    val root = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetWidth(root, 100f)
    Yoga.YGNodeStyleSetHeight(root, 100f)
    val root_child0 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetPositionType(root_child0, YGPositionType.YGPositionTypeAbsolute)
    Yoga.YGNodeStyleSetPosition(root_child0, YGEdge.YGEdgeStart, 10f)
    Yoga.YGNodeStyleSetPosition(root_child0, YGEdge.YGEdgeTop, 10f)
    Yoga.YGNodeStyleSetWidth(root_child0, 10f)
    Yoga.YGNodeStyleSetHeight(root_child0, 10f)
    Yoga.YGNodeInsertChild(root, root_child0, 0)
    Yoga.YGNodeCalculateLayout(
      root,
      Yoga.YGUndefined,
      Yoga.YGUndefined,
      YGDirection.YGDirectionLTR,
    )
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root))
    assertEquals(100f, Yoga.YGNodeLayoutGetWidth(root))
    assertEquals(100f, Yoga.YGNodeLayoutGetHeight(root))
    assertEquals(10f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(10f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(10f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(10f, Yoga.YGNodeLayoutGetHeight(root_child0))
    Yoga.YGNodeCalculateLayout(
      root,
      Yoga.YGUndefined,
      Yoga.YGUndefined,
      YGDirection.YGDirectionRTL,
    )
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root))
    assertEquals(100f, Yoga.YGNodeLayoutGetWidth(root))
    assertEquals(100f, Yoga.YGNodeLayoutGetHeight(root))
    assertEquals(80f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(10f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(10f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(10f, Yoga.YGNodeLayoutGetHeight(root_child0))
    Yoga.YGNodeFreeRecursive(root)
  }

  @Test
  fun absolute_layout_width_height_end_bottom() {
    val config = Yoga.YGConfigNew()
    val root = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetWidth(root, 100f)
    Yoga.YGNodeStyleSetHeight(root, 100f)
    val root_child0 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetPositionType(root_child0, YGPositionType.YGPositionTypeAbsolute)
    Yoga.YGNodeStyleSetPosition(root_child0, YGEdge.YGEdgeEnd, 10f)
    Yoga.YGNodeStyleSetPosition(root_child0, YGEdge.YGEdgeBottom, 10f)
    Yoga.YGNodeStyleSetWidth(root_child0, 10f)
    Yoga.YGNodeStyleSetHeight(root_child0, 10f)
    Yoga.YGNodeInsertChild(root, root_child0, 0)
    Yoga.YGNodeCalculateLayout(
      root,
      Yoga.YGUndefined,
      Yoga.YGUndefined,
      YGDirection.YGDirectionLTR,
    )
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root))
    assertEquals(100f, Yoga.YGNodeLayoutGetWidth(root))
    assertEquals(100f, Yoga.YGNodeLayoutGetHeight(root))
    assertEquals(80f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(80f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(10f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(10f, Yoga.YGNodeLayoutGetHeight(root_child0))
    Yoga.YGNodeCalculateLayout(
      root,
      Yoga.YGUndefined,
      Yoga.YGUndefined,
      YGDirection.YGDirectionRTL,
    )
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root))
    assertEquals(100f, Yoga.YGNodeLayoutGetWidth(root))
    assertEquals(100f, Yoga.YGNodeLayoutGetHeight(root))
    assertEquals(10f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(80f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(10f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(10f, Yoga.YGNodeLayoutGetHeight(root_child0))
    Yoga.YGNodeFreeRecursive(root)
  }

  @Test
  fun absolute_layout_start_top_end_bottom() {
    val config = Yoga.YGConfigNew()
    val root = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetWidth(root, 100f)
    Yoga.YGNodeStyleSetHeight(root, 100f)
    val root_child0 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetPositionType(root_child0, YGPositionType.YGPositionTypeAbsolute)
    Yoga.YGNodeStyleSetPosition(root_child0, YGEdge.YGEdgeStart, 10f)
    Yoga.YGNodeStyleSetPosition(root_child0, YGEdge.YGEdgeTop, 10f)
    Yoga.YGNodeStyleSetPosition(root_child0, YGEdge.YGEdgeEnd, 10f)
    Yoga.YGNodeStyleSetPosition(root_child0, YGEdge.YGEdgeBottom, 10f)
    Yoga.YGNodeInsertChild(root, root_child0, 0)
    Yoga.YGNodeCalculateLayout(
      root,
      Yoga.YGUndefined,
      Yoga.YGUndefined,
      YGDirection.YGDirectionLTR,
    )
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root))
    assertEquals(100f, Yoga.YGNodeLayoutGetWidth(root))
    assertEquals(100f, Yoga.YGNodeLayoutGetHeight(root))
    assertEquals(10f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(10f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(80f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(80f, Yoga.YGNodeLayoutGetHeight(root_child0))
    Yoga.YGNodeCalculateLayout(
      root,
      Yoga.YGUndefined,
      Yoga.YGUndefined,
      YGDirection.YGDirectionRTL,
    )
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root))
    assertEquals(100f, Yoga.YGNodeLayoutGetWidth(root))
    assertEquals(100f, Yoga.YGNodeLayoutGetHeight(root))
    assertEquals(10f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(10f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(80f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(80f, Yoga.YGNodeLayoutGetHeight(root_child0))
    Yoga.YGNodeFreeRecursive(root)
  }

  @Test
  fun absolute_layout_width_height_start_top_end_bottom() {
    val config = Yoga.YGConfigNew()
    val root = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetWidth(root, 100f)
    Yoga.YGNodeStyleSetHeight(root, 100f)
    val root_child0 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetPositionType(root_child0, YGPositionType.YGPositionTypeAbsolute)
    Yoga.YGNodeStyleSetPosition(root_child0, YGEdge.YGEdgeStart, 10f)
    Yoga.YGNodeStyleSetPosition(root_child0, YGEdge.YGEdgeTop, 10f)
    Yoga.YGNodeStyleSetPosition(root_child0, YGEdge.YGEdgeEnd, 10f)
    Yoga.YGNodeStyleSetPosition(root_child0, YGEdge.YGEdgeBottom, 10f)
    Yoga.YGNodeStyleSetWidth(root_child0, 10f)
    Yoga.YGNodeStyleSetHeight(root_child0, 10f)
    Yoga.YGNodeInsertChild(root, root_child0, 0)
    Yoga.YGNodeCalculateLayout(
      root,
      Yoga.YGUndefined,
      Yoga.YGUndefined,
      YGDirection.YGDirectionLTR,
    )
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root))
    assertEquals(100f, Yoga.YGNodeLayoutGetWidth(root))
    assertEquals(100f, Yoga.YGNodeLayoutGetHeight(root))
    assertEquals(10f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(10f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(10f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(10f, Yoga.YGNodeLayoutGetHeight(root_child0))
    Yoga.YGNodeCalculateLayout(
      root,
      Yoga.YGUndefined,
      Yoga.YGUndefined,
      YGDirection.YGDirectionRTL,
    )
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root))
    assertEquals(100f, Yoga.YGNodeLayoutGetWidth(root))
    assertEquals(100f, Yoga.YGNodeLayoutGetHeight(root))
    assertEquals(80f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(10f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(10f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(10f, Yoga.YGNodeLayoutGetHeight(root_child0))
    Yoga.YGNodeFreeRecursive(root)
  }

  @Test
  fun do_not_clamp_height_of_absolute_node_to_height_of_its_overflow_hidden_parent() {
    val config = Yoga.YGConfigNew()
    val root = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetFlexDirection(root, YGFlexDirection.YGFlexDirectionRow)
    Yoga.YGNodeStyleSetOverflow(root, YGOverflow.YGOverflowHidden)
    Yoga.YGNodeStyleSetWidth(root, 50f)
    Yoga.YGNodeStyleSetHeight(root, 50f)
    val root_child0 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetPositionType(root_child0, YGPositionType.YGPositionTypeAbsolute)
    Yoga.YGNodeStyleSetPosition(root_child0, YGEdge.YGEdgeStart, 0f)
    Yoga.YGNodeStyleSetPosition(root_child0, YGEdge.YGEdgeTop, 0f)
    Yoga.YGNodeInsertChild(root, root_child0, 0)
    val root_child0_child0 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetWidth(root_child0_child0, 100f)
    Yoga.YGNodeStyleSetHeight(root_child0_child0, 100f)
    Yoga.YGNodeInsertChild(root_child0, root_child0_child0, 0)
    Yoga.YGNodeCalculateLayout(
      root,
      Yoga.YGUndefined,
      Yoga.YGUndefined,
      YGDirection.YGDirectionLTR,
    )
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root))
    assertEquals(50f, Yoga.YGNodeLayoutGetHeight(root))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(100f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(100f, Yoga.YGNodeLayoutGetHeight(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child0_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0_child0))
    assertEquals(100f, Yoga.YGNodeLayoutGetWidth(root_child0_child0))
    assertEquals(100f, Yoga.YGNodeLayoutGetHeight(root_child0_child0))
    Yoga.YGNodeCalculateLayout(
      root,
      Yoga.YGUndefined,
      Yoga.YGUndefined,
      YGDirection.YGDirectionRTL,
    )
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root))
    assertEquals(50f, Yoga.YGNodeLayoutGetHeight(root))
    assertEquals(-50f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(100f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(100f, Yoga.YGNodeLayoutGetHeight(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child0_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0_child0))
    assertEquals(100f, Yoga.YGNodeLayoutGetWidth(root_child0_child0))
    assertEquals(100f, Yoga.YGNodeLayoutGetHeight(root_child0_child0))
    Yoga.YGNodeFreeRecursive(root)
  }

  @Test
  fun absolute_layout_within_border() {
    val config = Yoga.YGConfigNew()
    val root = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetMargin(root, YGEdge.YGEdgeLeft, 10f)
    Yoga.YGNodeStyleSetMargin(root, YGEdge.YGEdgeTop, 10f)
    Yoga.YGNodeStyleSetMargin(root, YGEdge.YGEdgeRight, 10f)
    Yoga.YGNodeStyleSetMargin(root, YGEdge.YGEdgeBottom, 10f)
    Yoga.YGNodeStyleSetPadding(root, YGEdge.YGEdgeLeft, 10f)
    Yoga.YGNodeStyleSetPadding(root, YGEdge.YGEdgeTop, 10f)
    Yoga.YGNodeStyleSetPadding(root, YGEdge.YGEdgeRight, 10f)
    Yoga.YGNodeStyleSetPadding(root, YGEdge.YGEdgeBottom, 10f)
    Yoga.YGNodeStyleSetBorder(root, YGEdge.YGEdgeLeft, 10f)
    Yoga.YGNodeStyleSetBorder(root, YGEdge.YGEdgeTop, 10f)
    Yoga.YGNodeStyleSetBorder(root, YGEdge.YGEdgeRight, 10f)
    Yoga.YGNodeStyleSetBorder(root, YGEdge.YGEdgeBottom, 10f)
    Yoga.YGNodeStyleSetWidth(root, 100f)
    Yoga.YGNodeStyleSetHeight(root, 100f)
    val root_child0 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetPositionType(root_child0, YGPositionType.YGPositionTypeAbsolute)
    Yoga.YGNodeStyleSetPosition(root_child0, YGEdge.YGEdgeLeft, 0f)
    Yoga.YGNodeStyleSetPosition(root_child0, YGEdge.YGEdgeTop, 0f)
    Yoga.YGNodeStyleSetWidth(root_child0, 50f)
    Yoga.YGNodeStyleSetHeight(root_child0, 50f)
    Yoga.YGNodeInsertChild(root, root_child0, 0)
    val root_child1 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetPositionType(root_child1, YGPositionType.YGPositionTypeAbsolute)
    Yoga.YGNodeStyleSetPosition(root_child1, YGEdge.YGEdgeRight, 0f)
    Yoga.YGNodeStyleSetPosition(root_child1, YGEdge.YGEdgeBottom, 0f)
    Yoga.YGNodeStyleSetWidth(root_child1, 50f)
    Yoga.YGNodeStyleSetHeight(root_child1, 50f)
    Yoga.YGNodeInsertChild(root, root_child1, 1)
    val root_child2 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetPositionType(root_child2, YGPositionType.YGPositionTypeAbsolute)
    Yoga.YGNodeStyleSetPosition(root_child2, YGEdge.YGEdgeLeft, 0f)
    Yoga.YGNodeStyleSetPosition(root_child2, YGEdge.YGEdgeTop, 0f)
    Yoga.YGNodeStyleSetMargin(root_child2, YGEdge.YGEdgeLeft, 10f)
    Yoga.YGNodeStyleSetMargin(root_child2, YGEdge.YGEdgeTop, 10f)
    Yoga.YGNodeStyleSetMargin(root_child2, YGEdge.YGEdgeRight, 10f)
    Yoga.YGNodeStyleSetMargin(root_child2, YGEdge.YGEdgeBottom, 10f)
    Yoga.YGNodeStyleSetWidth(root_child2, 50f)
    Yoga.YGNodeStyleSetHeight(root_child2, 50f)
    Yoga.YGNodeInsertChild(root, root_child2, 2)
    val root_child3 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetPositionType(root_child3, YGPositionType.YGPositionTypeAbsolute)
    Yoga.YGNodeStyleSetPosition(root_child3, YGEdge.YGEdgeRight, 0f)
    Yoga.YGNodeStyleSetPosition(root_child3, YGEdge.YGEdgeBottom, 0f)
    Yoga.YGNodeStyleSetMargin(root_child3, YGEdge.YGEdgeLeft, 10f)
    Yoga.YGNodeStyleSetMargin(root_child3, YGEdge.YGEdgeTop, 10f)
    Yoga.YGNodeStyleSetMargin(root_child3, YGEdge.YGEdgeRight, 10f)
    Yoga.YGNodeStyleSetMargin(root_child3, YGEdge.YGEdgeBottom, 10f)
    Yoga.YGNodeStyleSetWidth(root_child3, 50f)
    Yoga.YGNodeStyleSetHeight(root_child3, 50f)
    Yoga.YGNodeInsertChild(root, root_child3, 3)
    Yoga.YGNodeCalculateLayout(
      root,
      Yoga.YGUndefined,
      Yoga.YGUndefined,
      YGDirection.YGDirectionLTR,
    )
    assertEquals(10f, Yoga.YGNodeLayoutGetLeft(root))
    assertEquals(10f, Yoga.YGNodeLayoutGetTop(root))
    assertEquals(100f, Yoga.YGNodeLayoutGetWidth(root))
    assertEquals(100f, Yoga.YGNodeLayoutGetHeight(root))
    assertEquals(10f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(10f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetHeight(root_child0))
    assertEquals(40f, Yoga.YGNodeLayoutGetLeft(root_child1))
    assertEquals(40f, Yoga.YGNodeLayoutGetTop(root_child1))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child1))
    assertEquals(50f, Yoga.YGNodeLayoutGetHeight(root_child1))
    assertEquals(20f, Yoga.YGNodeLayoutGetLeft(root_child2))
    assertEquals(20f, Yoga.YGNodeLayoutGetTop(root_child2))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child2))
    assertEquals(50f, Yoga.YGNodeLayoutGetHeight(root_child2))
    assertEquals(30f, Yoga.YGNodeLayoutGetLeft(root_child3))
    assertEquals(30f, Yoga.YGNodeLayoutGetTop(root_child3))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child3))
    assertEquals(50f, Yoga.YGNodeLayoutGetHeight(root_child3))
    Yoga.YGNodeCalculateLayout(
      root,
      Yoga.YGUndefined,
      Yoga.YGUndefined,
      YGDirection.YGDirectionRTL,
    )
    assertEquals(10f, Yoga.YGNodeLayoutGetLeft(root))
    assertEquals(10f, Yoga.YGNodeLayoutGetTop(root))
    assertEquals(100f, Yoga.YGNodeLayoutGetWidth(root))
    assertEquals(100f, Yoga.YGNodeLayoutGetHeight(root))
    assertEquals(10f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(10f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetHeight(root_child0))
    assertEquals(40f, Yoga.YGNodeLayoutGetLeft(root_child1))
    assertEquals(40f, Yoga.YGNodeLayoutGetTop(root_child1))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child1))
    assertEquals(50f, Yoga.YGNodeLayoutGetHeight(root_child1))
    assertEquals(20f, Yoga.YGNodeLayoutGetLeft(root_child2))
    assertEquals(20f, Yoga.YGNodeLayoutGetTop(root_child2))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child2))
    assertEquals(50f, Yoga.YGNodeLayoutGetHeight(root_child2))
    assertEquals(30f, Yoga.YGNodeLayoutGetLeft(root_child3))
    assertEquals(30f, Yoga.YGNodeLayoutGetTop(root_child3))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child3))
    assertEquals(50f, Yoga.YGNodeLayoutGetHeight(root_child3))
    Yoga.YGNodeFreeRecursive(root)
  }

  @Test
  fun absolute_layout_align_items_and_justify_content_center() {
    val config = Yoga.YGConfigNew()
    val root = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetJustifyContent(root, YGJustify.YGJustifyCenter)
    Yoga.YGNodeStyleSetAlignItems(root, YGAlign.YGAlignCenter)
    Yoga.YGNodeStyleSetFlexGrow(root, 1f)
    Yoga.YGNodeStyleSetWidth(root, 110f)
    Yoga.YGNodeStyleSetHeight(root, 100f)
    val root_child0 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetPositionType(root_child0, YGPositionType.YGPositionTypeAbsolute)
    Yoga.YGNodeStyleSetWidth(root_child0, 60f)
    Yoga.YGNodeStyleSetHeight(root_child0, 40f)
    Yoga.YGNodeInsertChild(root, root_child0, 0)
    Yoga.YGNodeCalculateLayout(
      root,
      Yoga.YGUndefined,
      Yoga.YGUndefined,
      YGDirection.YGDirectionLTR,
    )
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root))
    assertEquals(110f, Yoga.YGNodeLayoutGetWidth(root))
    assertEquals(100f, Yoga.YGNodeLayoutGetHeight(root))
    assertEquals(25f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(30f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(60f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(40f, Yoga.YGNodeLayoutGetHeight(root_child0))
    Yoga.YGNodeCalculateLayout(
      root,
      Yoga.YGUndefined,
      Yoga.YGUndefined,
      YGDirection.YGDirectionRTL,
    )
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root))
    assertEquals(110f, Yoga.YGNodeLayoutGetWidth(root))
    assertEquals(100f, Yoga.YGNodeLayoutGetHeight(root))
    assertEquals(25f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(30f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(60f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(40f, Yoga.YGNodeLayoutGetHeight(root_child0))
    Yoga.YGNodeFreeRecursive(root)
  }

  @Test
  fun absolute_layout_align_items_and_justify_content_flex_end() {
    val config = Yoga.YGConfigNew()
    val root = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetJustifyContent(root, YGJustify.YGJustifyFlexEnd)
    Yoga.YGNodeStyleSetAlignItems(root, YGAlign.YGAlignFlexEnd)
    Yoga.YGNodeStyleSetFlexGrow(root, 1f)
    Yoga.YGNodeStyleSetWidth(root, 110f)
    Yoga.YGNodeStyleSetHeight(root, 100f)
    val root_child0 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetPositionType(root_child0, YGPositionType.YGPositionTypeAbsolute)
    Yoga.YGNodeStyleSetWidth(root_child0, 60f)
    Yoga.YGNodeStyleSetHeight(root_child0, 40f)
    Yoga.YGNodeInsertChild(root, root_child0, 0)
    Yoga.YGNodeCalculateLayout(
      root,
      Yoga.YGUndefined,
      Yoga.YGUndefined,
      YGDirection.YGDirectionLTR,
    )
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root))
    assertEquals(110f, Yoga.YGNodeLayoutGetWidth(root))
    assertEquals(100f, Yoga.YGNodeLayoutGetHeight(root))
    assertEquals(50f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(60f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(60f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(40f, Yoga.YGNodeLayoutGetHeight(root_child0))
    Yoga.YGNodeCalculateLayout(
      root,
      Yoga.YGUndefined,
      Yoga.YGUndefined,
      YGDirection.YGDirectionRTL,
    )
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root))
    assertEquals(110f, Yoga.YGNodeLayoutGetWidth(root))
    assertEquals(100f, Yoga.YGNodeLayoutGetHeight(root))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(60f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(60f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(40f, Yoga.YGNodeLayoutGetHeight(root_child0))
    Yoga.YGNodeFreeRecursive(root)
  }

  @Test
  fun absolute_layout_justify_content_center() {
    val config = Yoga.YGConfigNew()
    val root = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetJustifyContent(root, YGJustify.YGJustifyCenter)
    Yoga.YGNodeStyleSetFlexGrow(root, 1f)
    Yoga.YGNodeStyleSetWidth(root, 110f)
    Yoga.YGNodeStyleSetHeight(root, 100f)
    val root_child0 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetPositionType(root_child0, YGPositionType.YGPositionTypeAbsolute)
    Yoga.YGNodeStyleSetWidth(root_child0, 60f)
    Yoga.YGNodeStyleSetHeight(root_child0, 40f)
    Yoga.YGNodeInsertChild(root, root_child0, 0)
    Yoga.YGNodeCalculateLayout(
      root,
      Yoga.YGUndefined,
      Yoga.YGUndefined,
      YGDirection.YGDirectionLTR,
    )
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root))
    assertEquals(110f, Yoga.YGNodeLayoutGetWidth(root))
    assertEquals(100f, Yoga.YGNodeLayoutGetHeight(root))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(30f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(60f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(40f, Yoga.YGNodeLayoutGetHeight(root_child0))
    Yoga.YGNodeCalculateLayout(
      root,
      Yoga.YGUndefined,
      Yoga.YGUndefined,
      YGDirection.YGDirectionRTL,
    )
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root))
    assertEquals(110f, Yoga.YGNodeLayoutGetWidth(root))
    assertEquals(100f, Yoga.YGNodeLayoutGetHeight(root))
    assertEquals(50f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(30f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(60f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(40f, Yoga.YGNodeLayoutGetHeight(root_child0))
    Yoga.YGNodeFreeRecursive(root)
  }

  @Test
  fun absolute_layout_align_items_center() {
    val config = Yoga.YGConfigNew()
    val root = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetAlignItems(root, YGAlign.YGAlignCenter)
    Yoga.YGNodeStyleSetFlexGrow(root, 1f)
    Yoga.YGNodeStyleSetWidth(root, 110f)
    Yoga.YGNodeStyleSetHeight(root, 100f)
    val root_child0 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetPositionType(root_child0, YGPositionType.YGPositionTypeAbsolute)
    Yoga.YGNodeStyleSetWidth(root_child0, 60f)
    Yoga.YGNodeStyleSetHeight(root_child0, 40f)
    Yoga.YGNodeInsertChild(root, root_child0, 0)
    Yoga.YGNodeCalculateLayout(
      root,
      Yoga.YGUndefined,
      Yoga.YGUndefined,
      YGDirection.YGDirectionLTR,
    )
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root))
    assertEquals(110f, Yoga.YGNodeLayoutGetWidth(root))
    assertEquals(100f, Yoga.YGNodeLayoutGetHeight(root))
    assertEquals(25f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(60f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(40f, Yoga.YGNodeLayoutGetHeight(root_child0))
    Yoga.YGNodeCalculateLayout(
      root,
      Yoga.YGUndefined,
      Yoga.YGUndefined,
      YGDirection.YGDirectionRTL,
    )
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root))
    assertEquals(110f, Yoga.YGNodeLayoutGetWidth(root))
    assertEquals(100f, Yoga.YGNodeLayoutGetHeight(root))
    assertEquals(25f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(60f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(40f, Yoga.YGNodeLayoutGetHeight(root_child0))
    Yoga.YGNodeFreeRecursive(root)
  }

  @Test
  fun absolute_layout_align_items_center_on_child_only() {
    val config = Yoga.YGConfigNew()
    val root = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetFlexGrow(root, 1f)
    Yoga.YGNodeStyleSetWidth(root, 110f)
    Yoga.YGNodeStyleSetHeight(root, 100f)
    val root_child0 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetAlignSelf(root_child0, YGAlign.YGAlignCenter)
    Yoga.YGNodeStyleSetPositionType(root_child0, YGPositionType.YGPositionTypeAbsolute)
    Yoga.YGNodeStyleSetWidth(root_child0, 60f)
    Yoga.YGNodeStyleSetHeight(root_child0, 40f)
    Yoga.YGNodeInsertChild(root, root_child0, 0)
    Yoga.YGNodeCalculateLayout(
      root,
      Yoga.YGUndefined,
      Yoga.YGUndefined,
      YGDirection.YGDirectionLTR,
    )
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root))
    assertEquals(110f, Yoga.YGNodeLayoutGetWidth(root))
    assertEquals(100f, Yoga.YGNodeLayoutGetHeight(root))
    assertEquals(25f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(60f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(40f, Yoga.YGNodeLayoutGetHeight(root_child0))
    Yoga.YGNodeCalculateLayout(
      root,
      Yoga.YGUndefined,
      Yoga.YGUndefined,
      YGDirection.YGDirectionRTL,
    )
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root))
    assertEquals(110f, Yoga.YGNodeLayoutGetWidth(root))
    assertEquals(100f, Yoga.YGNodeLayoutGetHeight(root))
    assertEquals(25f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(60f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(40f, Yoga.YGNodeLayoutGetHeight(root_child0))
    Yoga.YGNodeFreeRecursive(root)
  }

  @Test
  fun absolute_layout_align_items_and_justify_content_center_and_top_position() {
    val config = Yoga.YGConfigNew()
    val root = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetJustifyContent(root, YGJustify.YGJustifyCenter)
    Yoga.YGNodeStyleSetAlignItems(root, YGAlign.YGAlignCenter)
    Yoga.YGNodeStyleSetFlexGrow(root, 1f)
    Yoga.YGNodeStyleSetWidth(root, 110f)
    Yoga.YGNodeStyleSetHeight(root, 100f)
    val root_child0 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetPositionType(root_child0, YGPositionType.YGPositionTypeAbsolute)
    Yoga.YGNodeStyleSetPosition(root_child0, YGEdge.YGEdgeTop, 10f)
    Yoga.YGNodeStyleSetWidth(root_child0, 60f)
    Yoga.YGNodeStyleSetHeight(root_child0, 40f)
    Yoga.YGNodeInsertChild(root, root_child0, 0)
    Yoga.YGNodeCalculateLayout(
      root,
      Yoga.YGUndefined,
      Yoga.YGUndefined,
      YGDirection.YGDirectionLTR,
    )
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root))
    assertEquals(110f, Yoga.YGNodeLayoutGetWidth(root))
    assertEquals(100f, Yoga.YGNodeLayoutGetHeight(root))
    assertEquals(25f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(10f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(60f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(40f, Yoga.YGNodeLayoutGetHeight(root_child0))
    Yoga.YGNodeCalculateLayout(
      root,
      Yoga.YGUndefined,
      Yoga.YGUndefined,
      YGDirection.YGDirectionRTL,
    )
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root))
    assertEquals(110f, Yoga.YGNodeLayoutGetWidth(root))
    assertEquals(100f, Yoga.YGNodeLayoutGetHeight(root))
    assertEquals(25f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(10f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(60f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(40f, Yoga.YGNodeLayoutGetHeight(root_child0))
    Yoga.YGNodeFreeRecursive(root)
  }

  @Test
  fun absolute_layout_align_items_and_justify_content_center_and_bottom_position() {
    val config = Yoga.YGConfigNew()
    val root = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetJustifyContent(root, YGJustify.YGJustifyCenter)
    Yoga.YGNodeStyleSetAlignItems(root, YGAlign.YGAlignCenter)
    Yoga.YGNodeStyleSetFlexGrow(root, 1f)
    Yoga.YGNodeStyleSetWidth(root, 110f)
    Yoga.YGNodeStyleSetHeight(root, 100f)
    val root_child0 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetPositionType(root_child0, YGPositionType.YGPositionTypeAbsolute)
    Yoga.YGNodeStyleSetPosition(root_child0, YGEdge.YGEdgeBottom, 10f)
    Yoga.YGNodeStyleSetWidth(root_child0, 60f)
    Yoga.YGNodeStyleSetHeight(root_child0, 40f)
    Yoga.YGNodeInsertChild(root, root_child0, 0)
    Yoga.YGNodeCalculateLayout(
      root,
      Yoga.YGUndefined,
      Yoga.YGUndefined,
      YGDirection.YGDirectionLTR,
    )
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root))
    assertEquals(110f, Yoga.YGNodeLayoutGetWidth(root))
    assertEquals(100f, Yoga.YGNodeLayoutGetHeight(root))
    assertEquals(25f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(60f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(40f, Yoga.YGNodeLayoutGetHeight(root_child0))
    Yoga.YGNodeCalculateLayout(
      root,
      Yoga.YGUndefined,
      Yoga.YGUndefined,
      YGDirection.YGDirectionRTL,
    )
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root))
    assertEquals(110f, Yoga.YGNodeLayoutGetWidth(root))
    assertEquals(100f, Yoga.YGNodeLayoutGetHeight(root))
    assertEquals(25f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(60f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(40f, Yoga.YGNodeLayoutGetHeight(root_child0))
    Yoga.YGNodeFreeRecursive(root)
  }

  @Test
  fun absolute_layout_align_items_and_justify_content_center_and_left_position() {
    val config = Yoga.YGConfigNew()
    val root = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetJustifyContent(root, YGJustify.YGJustifyCenter)
    Yoga.YGNodeStyleSetAlignItems(root, YGAlign.YGAlignCenter)
    Yoga.YGNodeStyleSetFlexGrow(root, 1f)
    Yoga.YGNodeStyleSetWidth(root, 110f)
    Yoga.YGNodeStyleSetHeight(root, 100f)
    val root_child0 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetPositionType(root_child0, YGPositionType.YGPositionTypeAbsolute)
    Yoga.YGNodeStyleSetPosition(root_child0, YGEdge.YGEdgeLeft, 5f)
    Yoga.YGNodeStyleSetWidth(root_child0, 60f)
    Yoga.YGNodeStyleSetHeight(root_child0, 40f)
    Yoga.YGNodeInsertChild(root, root_child0, 0)
    Yoga.YGNodeCalculateLayout(
      root,
      Yoga.YGUndefined,
      Yoga.YGUndefined,
      YGDirection.YGDirectionLTR,
    )
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root))
    assertEquals(110f, Yoga.YGNodeLayoutGetWidth(root))
    assertEquals(100f, Yoga.YGNodeLayoutGetHeight(root))
    assertEquals(5f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(30f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(60f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(40f, Yoga.YGNodeLayoutGetHeight(root_child0))
    Yoga.YGNodeCalculateLayout(
      root,
      Yoga.YGUndefined,
      Yoga.YGUndefined,
      YGDirection.YGDirectionRTL,
    )
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root))
    assertEquals(110f, Yoga.YGNodeLayoutGetWidth(root))
    assertEquals(100f, Yoga.YGNodeLayoutGetHeight(root))
    assertEquals(5f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(30f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(60f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(40f, Yoga.YGNodeLayoutGetHeight(root_child0))
    Yoga.YGNodeFreeRecursive(root)
  }

  @Test
  fun absolute_layout_align_items_and_justify_content_center_and_right_position() {
    val config = Yoga.YGConfigNew()
    val root = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetJustifyContent(root, YGJustify.YGJustifyCenter)
    Yoga.YGNodeStyleSetAlignItems(root, YGAlign.YGAlignCenter)
    Yoga.YGNodeStyleSetFlexGrow(root, 1f)
    Yoga.YGNodeStyleSetWidth(root, 110f)
    Yoga.YGNodeStyleSetHeight(root, 100f)
    val root_child0 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetPositionType(root_child0, YGPositionType.YGPositionTypeAbsolute)
    Yoga.YGNodeStyleSetPosition(root_child0, YGEdge.YGEdgeRight, 5f)
    Yoga.YGNodeStyleSetWidth(root_child0, 60f)
    Yoga.YGNodeStyleSetHeight(root_child0, 40f)
    Yoga.YGNodeInsertChild(root, root_child0, 0)
    Yoga.YGNodeCalculateLayout(
      root,
      Yoga.YGUndefined,
      Yoga.YGUndefined,
      YGDirection.YGDirectionLTR,
    )
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root))
    assertEquals(110f, Yoga.YGNodeLayoutGetWidth(root))
    assertEquals(100f, Yoga.YGNodeLayoutGetHeight(root))
    assertEquals(45f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(30f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(60f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(40f, Yoga.YGNodeLayoutGetHeight(root_child0))
    Yoga.YGNodeCalculateLayout(
      root,
      Yoga.YGUndefined,
      Yoga.YGUndefined,
      YGDirection.YGDirectionRTL,
    )
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root))
    assertEquals(110f, Yoga.YGNodeLayoutGetWidth(root))
    assertEquals(100f, Yoga.YGNodeLayoutGetHeight(root))
    assertEquals(45f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(30f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(60f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(40f, Yoga.YGNodeLayoutGetHeight(root_child0))
    Yoga.YGNodeFreeRecursive(root)
  }

  @Test
  fun position_root_with_rtl_should_position_withoutdirection() {
    val config = Yoga.YGConfigNew()
    val root = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetPosition(root, YGEdge.YGEdgeLeft, 72f)
    Yoga.YGNodeStyleSetWidth(root, 52f)
    Yoga.YGNodeStyleSetHeight(root, 52f)
    Yoga.YGNodeCalculateLayout(
      root,
      Yoga.YGUndefined,
      Yoga.YGUndefined,
      YGDirection.YGDirectionLTR,
    )
    assertEquals(72f, Yoga.YGNodeLayoutGetLeft(root))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root))
    assertEquals(52f, Yoga.YGNodeLayoutGetWidth(root))
    assertEquals(52f, Yoga.YGNodeLayoutGetHeight(root))
    Yoga.YGNodeCalculateLayout(
      root,
      Yoga.YGUndefined,
      Yoga.YGUndefined,
      YGDirection.YGDirectionRTL,
    )
    assertEquals(72f, Yoga.YGNodeLayoutGetLeft(root))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root))
    assertEquals(52f, Yoga.YGNodeLayoutGetWidth(root))
    assertEquals(52f, Yoga.YGNodeLayoutGetHeight(root))
    Yoga.YGNodeFreeRecursive(root)
  }

  @Test
  fun absolute_layout_percentage_bottom_based_on_parent_height() {
    val config = Yoga.YGConfigNew()
    val root = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetWidth(root, 100f)
    Yoga.YGNodeStyleSetHeight(root, 200f)
    val root_child0 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetPositionType(root_child0, YGPositionType.YGPositionTypeAbsolute)
    Yoga.YGNodeStyleSetPositionPercent(root_child0, YGEdge.YGEdgeTop, 50f)
    Yoga.YGNodeStyleSetWidth(root_child0, 10f)
    Yoga.YGNodeStyleSetHeight(root_child0, 10f)
    Yoga.YGNodeInsertChild(root, root_child0, 0)
    val root_child1 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetPositionType(root_child1, YGPositionType.YGPositionTypeAbsolute)
    Yoga.YGNodeStyleSetPositionPercent(root_child1, YGEdge.YGEdgeBottom, 50f)
    Yoga.YGNodeStyleSetWidth(root_child1, 10f)
    Yoga.YGNodeStyleSetHeight(root_child1, 10f)
    Yoga.YGNodeInsertChild(root, root_child1, 1)
    val root_child2 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetPositionType(root_child2, YGPositionType.YGPositionTypeAbsolute)
    Yoga.YGNodeStyleSetPositionPercent(root_child2, YGEdge.YGEdgeTop, 10f)
    Yoga.YGNodeStyleSetPositionPercent(root_child2, YGEdge.YGEdgeBottom, 10f)
    Yoga.YGNodeStyleSetWidth(root_child2, 10f)
    Yoga.YGNodeInsertChild(root, root_child2, 2)
    Yoga.YGNodeCalculateLayout(
      root,
      Yoga.YGUndefined,
      Yoga.YGUndefined,
      YGDirection.YGDirectionLTR,
    )
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root))
    assertEquals(100f, Yoga.YGNodeLayoutGetWidth(root))
    assertEquals(200f, Yoga.YGNodeLayoutGetHeight(root))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(100f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(10f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(10f, Yoga.YGNodeLayoutGetHeight(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child1))
    assertEquals(90f, Yoga.YGNodeLayoutGetTop(root_child1))
    assertEquals(10f, Yoga.YGNodeLayoutGetWidth(root_child1))
    assertEquals(10f, Yoga.YGNodeLayoutGetHeight(root_child1))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child2))
    assertEquals(20f, Yoga.YGNodeLayoutGetTop(root_child2))
    assertEquals(10f, Yoga.YGNodeLayoutGetWidth(root_child2))
    assertEquals(160f, Yoga.YGNodeLayoutGetHeight(root_child2))
    Yoga.YGNodeCalculateLayout(
      root,
      Yoga.YGUndefined,
      Yoga.YGUndefined,
      YGDirection.YGDirectionRTL,
    )
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root))
    assertEquals(100f, Yoga.YGNodeLayoutGetWidth(root))
    assertEquals(200f, Yoga.YGNodeLayoutGetHeight(root))
    assertEquals(90f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(100f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(10f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(10f, Yoga.YGNodeLayoutGetHeight(root_child0))
    assertEquals(90f, Yoga.YGNodeLayoutGetLeft(root_child1))
    assertEquals(90f, Yoga.YGNodeLayoutGetTop(root_child1))
    assertEquals(10f, Yoga.YGNodeLayoutGetWidth(root_child1))
    assertEquals(10f, Yoga.YGNodeLayoutGetHeight(root_child1))
    assertEquals(90f, Yoga.YGNodeLayoutGetLeft(root_child2))
    assertEquals(20f, Yoga.YGNodeLayoutGetTop(root_child2))
    assertEquals(10f, Yoga.YGNodeLayoutGetWidth(root_child2))
    assertEquals(160f, Yoga.YGNodeLayoutGetHeight(root_child2))
    Yoga.YGNodeFreeRecursive(root)
  }

  @Test
  fun absolute_layout_in_wrap_reverse_column_container() {
    val config = Yoga.YGConfigNew()
    val root = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetFlexWrap(root, YGWrap.YGWrapWrapReverse)
    Yoga.YGNodeStyleSetWidth(root, 100f)
    Yoga.YGNodeStyleSetHeight(root, 100f)
    val root_child0 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetPositionType(root_child0, YGPositionType.YGPositionTypeAbsolute)
    Yoga.YGNodeStyleSetWidth(root_child0, 20f)
    Yoga.YGNodeStyleSetHeight(root_child0, 20f)
    Yoga.YGNodeInsertChild(root, root_child0, 0)
    Yoga.YGNodeCalculateLayout(
      root,
      Yoga.YGUndefined,
      Yoga.YGUndefined,
      YGDirection.YGDirectionLTR,
    )
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root))
    assertEquals(100f, Yoga.YGNodeLayoutGetWidth(root))
    assertEquals(100f, Yoga.YGNodeLayoutGetHeight(root))
    assertEquals(80f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(20f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(20f, Yoga.YGNodeLayoutGetHeight(root_child0))
    Yoga.YGNodeCalculateLayout(
      root,
      Yoga.YGUndefined,
      Yoga.YGUndefined,
      YGDirection.YGDirectionRTL,
    )
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root))
    assertEquals(100f, Yoga.YGNodeLayoutGetWidth(root))
    assertEquals(100f, Yoga.YGNodeLayoutGetHeight(root))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(20f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(20f, Yoga.YGNodeLayoutGetHeight(root_child0))
    Yoga.YGNodeFreeRecursive(root)
  }

  @Test
  fun absolute_layout_in_wrap_reverse_row_container() {
    val config = Yoga.YGConfigNew()
    val root = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetFlexDirection(root, YGFlexDirection.YGFlexDirectionRow)
    Yoga.YGNodeStyleSetFlexWrap(root, YGWrap.YGWrapWrapReverse)
    Yoga.YGNodeStyleSetWidth(root, 100f)
    Yoga.YGNodeStyleSetHeight(root, 100f)
    val root_child0 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetPositionType(root_child0, YGPositionType.YGPositionTypeAbsolute)
    Yoga.YGNodeStyleSetWidth(root_child0, 20f)
    Yoga.YGNodeStyleSetHeight(root_child0, 20f)
    Yoga.YGNodeInsertChild(root, root_child0, 0)
    Yoga.YGNodeCalculateLayout(
      root,
      Yoga.YGUndefined,
      Yoga.YGUndefined,
      YGDirection.YGDirectionLTR,
    )
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root))
    assertEquals(100f, Yoga.YGNodeLayoutGetWidth(root))
    assertEquals(100f, Yoga.YGNodeLayoutGetHeight(root))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(80f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(20f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(20f, Yoga.YGNodeLayoutGetHeight(root_child0))
    Yoga.YGNodeCalculateLayout(
      root,
      Yoga.YGUndefined,
      Yoga.YGUndefined,
      YGDirection.YGDirectionRTL,
    )
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root))
    assertEquals(100f, Yoga.YGNodeLayoutGetWidth(root))
    assertEquals(100f, Yoga.YGNodeLayoutGetHeight(root))
    assertEquals(80f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(80f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(20f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(20f, Yoga.YGNodeLayoutGetHeight(root_child0))
    Yoga.YGNodeFreeRecursive(root)
  }

  @Test
  fun absolute_layout_in_wrap_reverse_column_container_flex_end() {
    val config = Yoga.YGConfigNew()
    val root = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetFlexWrap(root, YGWrap.YGWrapWrapReverse)
    Yoga.YGNodeStyleSetWidth(root, 100f)
    Yoga.YGNodeStyleSetHeight(root, 100f)
    val root_child0 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetAlignSelf(root_child0, YGAlign.YGAlignFlexEnd)
    Yoga.YGNodeStyleSetPositionType(root_child0, YGPositionType.YGPositionTypeAbsolute)
    Yoga.YGNodeStyleSetWidth(root_child0, 20f)
    Yoga.YGNodeStyleSetHeight(root_child0, 20f)
    Yoga.YGNodeInsertChild(root, root_child0, 0)
    Yoga.YGNodeCalculateLayout(
      root,
      Yoga.YGUndefined,
      Yoga.YGUndefined,
      YGDirection.YGDirectionLTR,
    )
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root))
    assertEquals(100f, Yoga.YGNodeLayoutGetWidth(root))
    assertEquals(100f, Yoga.YGNodeLayoutGetHeight(root))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(20f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(20f, Yoga.YGNodeLayoutGetHeight(root_child0))
    Yoga.YGNodeCalculateLayout(
      root,
      Yoga.YGUndefined,
      Yoga.YGUndefined,
      YGDirection.YGDirectionRTL,
    )
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root))
    assertEquals(100f, Yoga.YGNodeLayoutGetWidth(root))
    assertEquals(100f, Yoga.YGNodeLayoutGetHeight(root))
    assertEquals(80f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(20f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(20f, Yoga.YGNodeLayoutGetHeight(root_child0))
    Yoga.YGNodeFreeRecursive(root)
  }

  @Test
  fun absolute_layout_in_wrap_reverse_row_container_flex_end() {
    val config = Yoga.YGConfigNew()
    val root = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetFlexDirection(root, YGFlexDirection.YGFlexDirectionRow)
    Yoga.YGNodeStyleSetFlexWrap(root, YGWrap.YGWrapWrapReverse)
    Yoga.YGNodeStyleSetWidth(root, 100f)
    Yoga.YGNodeStyleSetHeight(root, 100f)
    val root_child0 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetAlignSelf(root_child0, YGAlign.YGAlignFlexEnd)
    Yoga.YGNodeStyleSetPositionType(root_child0, YGPositionType.YGPositionTypeAbsolute)
    Yoga.YGNodeStyleSetWidth(root_child0, 20f)
    Yoga.YGNodeStyleSetHeight(root_child0, 20f)
    Yoga.YGNodeInsertChild(root, root_child0, 0)
    Yoga.YGNodeCalculateLayout(
      root,
      Yoga.YGUndefined,
      Yoga.YGUndefined,
      YGDirection.YGDirectionLTR,
    )
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root))
    assertEquals(100f, Yoga.YGNodeLayoutGetWidth(root))
    assertEquals(100f, Yoga.YGNodeLayoutGetHeight(root))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(20f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(20f, Yoga.YGNodeLayoutGetHeight(root_child0))
    Yoga.YGNodeCalculateLayout(
      root,
      Yoga.YGUndefined,
      Yoga.YGUndefined,
      YGDirection.YGDirectionRTL,
    )
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root))
    assertEquals(100f, Yoga.YGNodeLayoutGetWidth(root))
    assertEquals(100f, Yoga.YGNodeLayoutGetHeight(root))
    assertEquals(80f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(20f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(20f, Yoga.YGNodeLayoutGetHeight(root_child0))
    Yoga.YGNodeFreeRecursive(root)
  }
}
