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
        val config = GlobalMembers.YGConfigNew()
        val root = GlobalMembers.YGNodeNewWithConfig(config)
        GlobalMembers.YGNodeStyleSetWidth(root, 100f)
        GlobalMembers.YGNodeStyleSetHeight(root, 100f)
        val root_child0 = GlobalMembers.YGNodeNewWithConfig(config)
        GlobalMembers.YGNodeStyleSetPositionType(root_child0, YGPositionType.YGPositionTypeAbsolute)
        GlobalMembers.YGNodeStyleSetPosition(root_child0, YGEdge.YGEdgeStart, 10f)
        GlobalMembers.YGNodeStyleSetPosition(root_child0, YGEdge.YGEdgeTop, 10f)
        GlobalMembers.YGNodeStyleSetWidth(root_child0, 10f)
        GlobalMembers.YGNodeStyleSetHeight(root_child0, 10f)
        GlobalMembers.YGNodeInsertChild(root, root_child0, 0)
        GlobalMembers.YGNodeCalculateLayout(
            root,
            GlobalMembers.YGUndefined,
            GlobalMembers.YGUndefined,
            YGDirection.YGDirectionLTR
        )
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
        assertEquals(100f, GlobalMembers.YGNodeLayoutGetWidth(root))
        assertEquals(100f, GlobalMembers.YGNodeLayoutGetHeight(root))
        assertEquals(10f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
        assertEquals(10f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
        assertEquals(10f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
        assertEquals(10f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
        GlobalMembers.YGNodeCalculateLayout(
            root,
            GlobalMembers.YGUndefined,
            GlobalMembers.YGUndefined,
            YGDirection.YGDirectionRTL
        )
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
        assertEquals(100f, GlobalMembers.YGNodeLayoutGetWidth(root))
        assertEquals(100f, GlobalMembers.YGNodeLayoutGetHeight(root))
        assertEquals(80f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
        assertEquals(10f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
        assertEquals(10f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
        assertEquals(10f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
        GlobalMembers.YGNodeFreeRecursive(root)
        GlobalMembers.YGConfigFree(config)
    }

    @Test
    fun absolute_layout_width_height_end_bottom() {
        val config = GlobalMembers.YGConfigNew()
        val root = GlobalMembers.YGNodeNewWithConfig(config)
        GlobalMembers.YGNodeStyleSetWidth(root, 100f)
        GlobalMembers.YGNodeStyleSetHeight(root, 100f)
        val root_child0 = GlobalMembers.YGNodeNewWithConfig(config)
        GlobalMembers.YGNodeStyleSetPositionType(root_child0, YGPositionType.YGPositionTypeAbsolute)
        GlobalMembers.YGNodeStyleSetPosition(root_child0, YGEdge.YGEdgeEnd, 10f)
        GlobalMembers.YGNodeStyleSetPosition(root_child0, YGEdge.YGEdgeBottom, 10f)
        GlobalMembers.YGNodeStyleSetWidth(root_child0, 10f)
        GlobalMembers.YGNodeStyleSetHeight(root_child0, 10f)
        GlobalMembers.YGNodeInsertChild(root, root_child0, 0)
        GlobalMembers.YGNodeCalculateLayout(
            root,
            GlobalMembers.YGUndefined,
            GlobalMembers.YGUndefined,
            YGDirection.YGDirectionLTR
        )
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
        assertEquals(100f, GlobalMembers.YGNodeLayoutGetWidth(root))
        assertEquals(100f, GlobalMembers.YGNodeLayoutGetHeight(root))
        assertEquals(80f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
        assertEquals(80f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
        assertEquals(10f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
        assertEquals(10f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
        GlobalMembers.YGNodeCalculateLayout(
            root,
            GlobalMembers.YGUndefined,
            GlobalMembers.YGUndefined,
            YGDirection.YGDirectionRTL
        )
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
        assertEquals(100f, GlobalMembers.YGNodeLayoutGetWidth(root))
        assertEquals(100f, GlobalMembers.YGNodeLayoutGetHeight(root))
        assertEquals(10f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
        assertEquals(80f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
        assertEquals(10f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
        assertEquals(10f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
        GlobalMembers.YGNodeFreeRecursive(root)
        GlobalMembers.YGConfigFree(config)
    }

    @Test
    fun absolute_layout_start_top_end_bottom() {
        val config = GlobalMembers.YGConfigNew()
        val root = GlobalMembers.YGNodeNewWithConfig(config)
        GlobalMembers.YGNodeStyleSetWidth(root, 100f)
        GlobalMembers.YGNodeStyleSetHeight(root, 100f)
        val root_child0 = GlobalMembers.YGNodeNewWithConfig(config)
        GlobalMembers.YGNodeStyleSetPositionType(root_child0, YGPositionType.YGPositionTypeAbsolute)
        GlobalMembers.YGNodeStyleSetPosition(root_child0, YGEdge.YGEdgeStart, 10f)
        GlobalMembers.YGNodeStyleSetPosition(root_child0, YGEdge.YGEdgeTop, 10f)
        GlobalMembers.YGNodeStyleSetPosition(root_child0, YGEdge.YGEdgeEnd, 10f)
        GlobalMembers.YGNodeStyleSetPosition(root_child0, YGEdge.YGEdgeBottom, 10f)
        GlobalMembers.YGNodeInsertChild(root, root_child0, 0)
        GlobalMembers.YGNodeCalculateLayout(
            root,
            GlobalMembers.YGUndefined,
            GlobalMembers.YGUndefined,
            YGDirection.YGDirectionLTR
        )
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
        assertEquals(100f, GlobalMembers.YGNodeLayoutGetWidth(root))
        assertEquals(100f, GlobalMembers.YGNodeLayoutGetHeight(root))
        assertEquals(10f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
        assertEquals(10f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
        assertEquals(80f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
        assertEquals(80f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
        GlobalMembers.YGNodeCalculateLayout(
            root,
            GlobalMembers.YGUndefined,
            GlobalMembers.YGUndefined,
            YGDirection.YGDirectionRTL
        )
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
        assertEquals(100f, GlobalMembers.YGNodeLayoutGetWidth(root))
        assertEquals(100f, GlobalMembers.YGNodeLayoutGetHeight(root))
        assertEquals(10f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
        assertEquals(10f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
        assertEquals(80f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
        assertEquals(80f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
        GlobalMembers.YGNodeFreeRecursive(root)
        GlobalMembers.YGConfigFree(config)
    }

    @Test
    fun absolute_layout_width_height_start_top_end_bottom() {
        val config = GlobalMembers.YGConfigNew()
        val root = GlobalMembers.YGNodeNewWithConfig(config)
        GlobalMembers.YGNodeStyleSetWidth(root, 100f)
        GlobalMembers.YGNodeStyleSetHeight(root, 100f)
        val root_child0 = GlobalMembers.YGNodeNewWithConfig(config)
        GlobalMembers.YGNodeStyleSetPositionType(root_child0, YGPositionType.YGPositionTypeAbsolute)
        GlobalMembers.YGNodeStyleSetPosition(root_child0, YGEdge.YGEdgeStart, 10f)
        GlobalMembers.YGNodeStyleSetPosition(root_child0, YGEdge.YGEdgeTop, 10f)
        GlobalMembers.YGNodeStyleSetPosition(root_child0, YGEdge.YGEdgeEnd, 10f)
        GlobalMembers.YGNodeStyleSetPosition(root_child0, YGEdge.YGEdgeBottom, 10f)
        GlobalMembers.YGNodeStyleSetWidth(root_child0, 10f)
        GlobalMembers.YGNodeStyleSetHeight(root_child0, 10f)
        GlobalMembers.YGNodeInsertChild(root, root_child0, 0)
        GlobalMembers.YGNodeCalculateLayout(
            root,
            GlobalMembers.YGUndefined,
            GlobalMembers.YGUndefined,
            YGDirection.YGDirectionLTR
        )
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
        assertEquals(100f, GlobalMembers.YGNodeLayoutGetWidth(root))
        assertEquals(100f, GlobalMembers.YGNodeLayoutGetHeight(root))
        assertEquals(10f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
        assertEquals(10f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
        assertEquals(10f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
        assertEquals(10f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
        GlobalMembers.YGNodeCalculateLayout(
            root,
            GlobalMembers.YGUndefined,
            GlobalMembers.YGUndefined,
            YGDirection.YGDirectionRTL
        )
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
        assertEquals(100f, GlobalMembers.YGNodeLayoutGetWidth(root))
        assertEquals(100f, GlobalMembers.YGNodeLayoutGetHeight(root))
        assertEquals(80f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
        assertEquals(10f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
        assertEquals(10f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
        assertEquals(10f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
        GlobalMembers.YGNodeFreeRecursive(root)
        GlobalMembers.YGConfigFree(config)
    }

    @Test
    fun do_not_clamp_height_of_absolute_node_to_height_of_its_overflow_hidden_parent() {
        val config = GlobalMembers.YGConfigNew()
        val root = GlobalMembers.YGNodeNewWithConfig(config)
        GlobalMembers.YGNodeStyleSetFlexDirection(root, YGFlexDirection.YGFlexDirectionRow)
        GlobalMembers.YGNodeStyleSetOverflow(root, YGOverflow.YGOverflowHidden)
        GlobalMembers.YGNodeStyleSetWidth(root, 50f)
        GlobalMembers.YGNodeStyleSetHeight(root, 50f)
        val root_child0 = GlobalMembers.YGNodeNewWithConfig(config)
        GlobalMembers.YGNodeStyleSetPositionType(root_child0, YGPositionType.YGPositionTypeAbsolute)
        GlobalMembers.YGNodeStyleSetPosition(root_child0, YGEdge.YGEdgeStart, 0f)
        GlobalMembers.YGNodeStyleSetPosition(root_child0, YGEdge.YGEdgeTop, 0f)
        GlobalMembers.YGNodeInsertChild(root, root_child0, 0)
        val root_child0_child0 = GlobalMembers.YGNodeNewWithConfig(config)
        GlobalMembers.YGNodeStyleSetWidth(root_child0_child0, 100f)
        GlobalMembers.YGNodeStyleSetHeight(root_child0_child0, 100f)
        GlobalMembers.YGNodeInsertChild(root_child0, root_child0_child0, 0)
        GlobalMembers.YGNodeCalculateLayout(
            root,
            GlobalMembers.YGUndefined,
            GlobalMembers.YGUndefined,
            YGDirection.YGDirectionLTR
        )
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
        assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root))
        assertEquals(50f, GlobalMembers.YGNodeLayoutGetHeight(root))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
        assertEquals(100f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
        assertEquals(100f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child0_child0))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0_child0))
        assertEquals(100f, GlobalMembers.YGNodeLayoutGetWidth(root_child0_child0))
        assertEquals(100f, GlobalMembers.YGNodeLayoutGetHeight(root_child0_child0))
        GlobalMembers.YGNodeCalculateLayout(
            root,
            GlobalMembers.YGUndefined,
            GlobalMembers.YGUndefined,
            YGDirection.YGDirectionRTL
        )
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
        assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root))
        assertEquals(50f, GlobalMembers.YGNodeLayoutGetHeight(root))
        assertEquals(-50f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
        assertEquals(100f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
        assertEquals(100f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child0_child0))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0_child0))
        assertEquals(100f, GlobalMembers.YGNodeLayoutGetWidth(root_child0_child0))
        assertEquals(100f, GlobalMembers.YGNodeLayoutGetHeight(root_child0_child0))
        GlobalMembers.YGNodeFreeRecursive(root)
        GlobalMembers.YGConfigFree(config)
    }

    @Test
    fun absolute_layout_within_border() {
        val config = GlobalMembers.YGConfigNew()
        val root = GlobalMembers.YGNodeNewWithConfig(config)
        GlobalMembers.YGNodeStyleSetMargin(root, YGEdge.YGEdgeLeft, 10f)
        GlobalMembers.YGNodeStyleSetMargin(root, YGEdge.YGEdgeTop, 10f)
        GlobalMembers.YGNodeStyleSetMargin(root, YGEdge.YGEdgeRight, 10f)
        GlobalMembers.YGNodeStyleSetMargin(root, YGEdge.YGEdgeBottom, 10f)
        GlobalMembers.YGNodeStyleSetPadding(root, YGEdge.YGEdgeLeft, 10f)
        GlobalMembers.YGNodeStyleSetPadding(root, YGEdge.YGEdgeTop, 10f)
        GlobalMembers.YGNodeStyleSetPadding(root, YGEdge.YGEdgeRight, 10f)
        GlobalMembers.YGNodeStyleSetPadding(root, YGEdge.YGEdgeBottom, 10f)
        GlobalMembers.YGNodeStyleSetBorder(root, YGEdge.YGEdgeLeft, 10f)
        GlobalMembers.YGNodeStyleSetBorder(root, YGEdge.YGEdgeTop, 10f)
        GlobalMembers.YGNodeStyleSetBorder(root, YGEdge.YGEdgeRight, 10f)
        GlobalMembers.YGNodeStyleSetBorder(root, YGEdge.YGEdgeBottom, 10f)
        GlobalMembers.YGNodeStyleSetWidth(root, 100f)
        GlobalMembers.YGNodeStyleSetHeight(root, 100f)
        val root_child0 = GlobalMembers.YGNodeNewWithConfig(config)
        GlobalMembers.YGNodeStyleSetPositionType(root_child0, YGPositionType.YGPositionTypeAbsolute)
        GlobalMembers.YGNodeStyleSetPosition(root_child0, YGEdge.YGEdgeLeft, 0f)
        GlobalMembers.YGNodeStyleSetPosition(root_child0, YGEdge.YGEdgeTop, 0f)
        GlobalMembers.YGNodeStyleSetWidth(root_child0, 50f)
        GlobalMembers.YGNodeStyleSetHeight(root_child0, 50f)
        GlobalMembers.YGNodeInsertChild(root, root_child0, 0)
        val root_child1 = GlobalMembers.YGNodeNewWithConfig(config)
        GlobalMembers.YGNodeStyleSetPositionType(root_child1, YGPositionType.YGPositionTypeAbsolute)
        GlobalMembers.YGNodeStyleSetPosition(root_child1, YGEdge.YGEdgeRight, 0f)
        GlobalMembers.YGNodeStyleSetPosition(root_child1, YGEdge.YGEdgeBottom, 0f)
        GlobalMembers.YGNodeStyleSetWidth(root_child1, 50f)
        GlobalMembers.YGNodeStyleSetHeight(root_child1, 50f)
        GlobalMembers.YGNodeInsertChild(root, root_child1, 1)
        val root_child2 = GlobalMembers.YGNodeNewWithConfig(config)
        GlobalMembers.YGNodeStyleSetPositionType(root_child2, YGPositionType.YGPositionTypeAbsolute)
        GlobalMembers.YGNodeStyleSetPosition(root_child2, YGEdge.YGEdgeLeft, 0f)
        GlobalMembers.YGNodeStyleSetPosition(root_child2, YGEdge.YGEdgeTop, 0f)
        GlobalMembers.YGNodeStyleSetMargin(root_child2, YGEdge.YGEdgeLeft, 10f)
        GlobalMembers.YGNodeStyleSetMargin(root_child2, YGEdge.YGEdgeTop, 10f)
        GlobalMembers.YGNodeStyleSetMargin(root_child2, YGEdge.YGEdgeRight, 10f)
        GlobalMembers.YGNodeStyleSetMargin(root_child2, YGEdge.YGEdgeBottom, 10f)
        GlobalMembers.YGNodeStyleSetWidth(root_child2, 50f)
        GlobalMembers.YGNodeStyleSetHeight(root_child2, 50f)
        GlobalMembers.YGNodeInsertChild(root, root_child2, 2)
        val root_child3 = GlobalMembers.YGNodeNewWithConfig(config)
        GlobalMembers.YGNodeStyleSetPositionType(root_child3, YGPositionType.YGPositionTypeAbsolute)
        GlobalMembers.YGNodeStyleSetPosition(root_child3, YGEdge.YGEdgeRight, 0f)
        GlobalMembers.YGNodeStyleSetPosition(root_child3, YGEdge.YGEdgeBottom, 0f)
        GlobalMembers.YGNodeStyleSetMargin(root_child3, YGEdge.YGEdgeLeft, 10f)
        GlobalMembers.YGNodeStyleSetMargin(root_child3, YGEdge.YGEdgeTop, 10f)
        GlobalMembers.YGNodeStyleSetMargin(root_child3, YGEdge.YGEdgeRight, 10f)
        GlobalMembers.YGNodeStyleSetMargin(root_child3, YGEdge.YGEdgeBottom, 10f)
        GlobalMembers.YGNodeStyleSetWidth(root_child3, 50f)
        GlobalMembers.YGNodeStyleSetHeight(root_child3, 50f)
        GlobalMembers.YGNodeInsertChild(root, root_child3, 3)
        GlobalMembers.YGNodeCalculateLayout(
            root,
            GlobalMembers.YGUndefined,
            GlobalMembers.YGUndefined,
            YGDirection.YGDirectionLTR
        )
        assertEquals(10f, GlobalMembers.YGNodeLayoutGetLeft(root))
        assertEquals(10f, GlobalMembers.YGNodeLayoutGetTop(root))
        assertEquals(100f, GlobalMembers.YGNodeLayoutGetWidth(root))
        assertEquals(100f, GlobalMembers.YGNodeLayoutGetHeight(root))
        assertEquals(10f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
        assertEquals(10f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
        assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
        assertEquals(50f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
        assertEquals(40f, GlobalMembers.YGNodeLayoutGetLeft(root_child1))
        assertEquals(40f, GlobalMembers.YGNodeLayoutGetTop(root_child1))
        assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child1))
        assertEquals(50f, GlobalMembers.YGNodeLayoutGetHeight(root_child1))
        assertEquals(20f, GlobalMembers.YGNodeLayoutGetLeft(root_child2))
        assertEquals(20f, GlobalMembers.YGNodeLayoutGetTop(root_child2))
        assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child2))
        assertEquals(50f, GlobalMembers.YGNodeLayoutGetHeight(root_child2))
        assertEquals(30f, GlobalMembers.YGNodeLayoutGetLeft(root_child3))
        assertEquals(30f, GlobalMembers.YGNodeLayoutGetTop(root_child3))
        assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child3))
        assertEquals(50f, GlobalMembers.YGNodeLayoutGetHeight(root_child3))
        GlobalMembers.YGNodeCalculateLayout(
            root,
            GlobalMembers.YGUndefined,
            GlobalMembers.YGUndefined,
            YGDirection.YGDirectionRTL
        )
        assertEquals(10f, GlobalMembers.YGNodeLayoutGetLeft(root))
        assertEquals(10f, GlobalMembers.YGNodeLayoutGetTop(root))
        assertEquals(100f, GlobalMembers.YGNodeLayoutGetWidth(root))
        assertEquals(100f, GlobalMembers.YGNodeLayoutGetHeight(root))
        assertEquals(10f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
        assertEquals(10f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
        assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
        assertEquals(50f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
        assertEquals(40f, GlobalMembers.YGNodeLayoutGetLeft(root_child1))
        assertEquals(40f, GlobalMembers.YGNodeLayoutGetTop(root_child1))
        assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child1))
        assertEquals(50f, GlobalMembers.YGNodeLayoutGetHeight(root_child1))
        assertEquals(20f, GlobalMembers.YGNodeLayoutGetLeft(root_child2))
        assertEquals(20f, GlobalMembers.YGNodeLayoutGetTop(root_child2))
        assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child2))
        assertEquals(50f, GlobalMembers.YGNodeLayoutGetHeight(root_child2))
        assertEquals(30f, GlobalMembers.YGNodeLayoutGetLeft(root_child3))
        assertEquals(30f, GlobalMembers.YGNodeLayoutGetTop(root_child3))
        assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child3))
        assertEquals(50f, GlobalMembers.YGNodeLayoutGetHeight(root_child3))
        GlobalMembers.YGNodeFreeRecursive(root)
        GlobalMembers.YGConfigFree(config)
    }

    @Test
    fun absolute_layout_align_items_and_justify_content_center() {
        val config = GlobalMembers.YGConfigNew()
        val root = GlobalMembers.YGNodeNewWithConfig(config)
        GlobalMembers.YGNodeStyleSetJustifyContent(root, YGJustify.YGJustifyCenter)
        GlobalMembers.YGNodeStyleSetAlignItems(root, YGAlign.YGAlignCenter)
        GlobalMembers.YGNodeStyleSetFlexGrow(root, 1f)
        GlobalMembers.YGNodeStyleSetWidth(root, 110f)
        GlobalMembers.YGNodeStyleSetHeight(root, 100f)
        val root_child0 = GlobalMembers.YGNodeNewWithConfig(config)
        GlobalMembers.YGNodeStyleSetPositionType(root_child0, YGPositionType.YGPositionTypeAbsolute)
        GlobalMembers.YGNodeStyleSetWidth(root_child0, 60f)
        GlobalMembers.YGNodeStyleSetHeight(root_child0, 40f)
        GlobalMembers.YGNodeInsertChild(root, root_child0, 0)
        GlobalMembers.YGNodeCalculateLayout(
            root,
            GlobalMembers.YGUndefined,
            GlobalMembers.YGUndefined,
            YGDirection.YGDirectionLTR
        )
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
        assertEquals(110f, GlobalMembers.YGNodeLayoutGetWidth(root))
        assertEquals(100f, GlobalMembers.YGNodeLayoutGetHeight(root))
        assertEquals(25f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
        assertEquals(30f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
        assertEquals(60f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
        assertEquals(40f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
        GlobalMembers.YGNodeCalculateLayout(
            root,
            GlobalMembers.YGUndefined,
            GlobalMembers.YGUndefined,
            YGDirection.YGDirectionRTL
        )
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
        assertEquals(110f, GlobalMembers.YGNodeLayoutGetWidth(root))
        assertEquals(100f, GlobalMembers.YGNodeLayoutGetHeight(root))
        assertEquals(25f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
        assertEquals(30f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
        assertEquals(60f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
        assertEquals(40f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
        GlobalMembers.YGNodeFreeRecursive(root)
        GlobalMembers.YGConfigFree(config)
    }

    @Test
    fun absolute_layout_align_items_and_justify_content_flex_end() {
        val config = GlobalMembers.YGConfigNew()
        val root = GlobalMembers.YGNodeNewWithConfig(config)
        GlobalMembers.YGNodeStyleSetJustifyContent(root, YGJustify.YGJustifyFlexEnd)
        GlobalMembers.YGNodeStyleSetAlignItems(root, YGAlign.YGAlignFlexEnd)
        GlobalMembers.YGNodeStyleSetFlexGrow(root, 1f)
        GlobalMembers.YGNodeStyleSetWidth(root, 110f)
        GlobalMembers.YGNodeStyleSetHeight(root, 100f)
        val root_child0 = GlobalMembers.YGNodeNewWithConfig(config)
        GlobalMembers.YGNodeStyleSetPositionType(root_child0, YGPositionType.YGPositionTypeAbsolute)
        GlobalMembers.YGNodeStyleSetWidth(root_child0, 60f)
        GlobalMembers.YGNodeStyleSetHeight(root_child0, 40f)
        GlobalMembers.YGNodeInsertChild(root, root_child0, 0)
        GlobalMembers.YGNodeCalculateLayout(
            root,
            GlobalMembers.YGUndefined,
            GlobalMembers.YGUndefined,
            YGDirection.YGDirectionLTR
        )
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
        assertEquals(110f, GlobalMembers.YGNodeLayoutGetWidth(root))
        assertEquals(100f, GlobalMembers.YGNodeLayoutGetHeight(root))
        assertEquals(50f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
        assertEquals(60f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
        assertEquals(60f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
        assertEquals(40f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
        GlobalMembers.YGNodeCalculateLayout(
            root,
            GlobalMembers.YGUndefined,
            GlobalMembers.YGUndefined,
            YGDirection.YGDirectionRTL
        )
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
        assertEquals(110f, GlobalMembers.YGNodeLayoutGetWidth(root))
        assertEquals(100f, GlobalMembers.YGNodeLayoutGetHeight(root))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
        assertEquals(60f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
        assertEquals(60f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
        assertEquals(40f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
        GlobalMembers.YGNodeFreeRecursive(root)
        GlobalMembers.YGConfigFree(config)
    }

    @Test
    fun absolute_layout_justify_content_center() {
        val config = GlobalMembers.YGConfigNew()
        val root = GlobalMembers.YGNodeNewWithConfig(config)
        GlobalMembers.YGNodeStyleSetJustifyContent(root, YGJustify.YGJustifyCenter)
        GlobalMembers.YGNodeStyleSetFlexGrow(root, 1f)
        GlobalMembers.YGNodeStyleSetWidth(root, 110f)
        GlobalMembers.YGNodeStyleSetHeight(root, 100f)
        val root_child0 = GlobalMembers.YGNodeNewWithConfig(config)
        GlobalMembers.YGNodeStyleSetPositionType(root_child0, YGPositionType.YGPositionTypeAbsolute)
        GlobalMembers.YGNodeStyleSetWidth(root_child0, 60f)
        GlobalMembers.YGNodeStyleSetHeight(root_child0, 40f)
        GlobalMembers.YGNodeInsertChild(root, root_child0, 0)
        GlobalMembers.YGNodeCalculateLayout(
            root,
            GlobalMembers.YGUndefined,
            GlobalMembers.YGUndefined,
            YGDirection.YGDirectionLTR
        )
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
        assertEquals(110f, GlobalMembers.YGNodeLayoutGetWidth(root))
        assertEquals(100f, GlobalMembers.YGNodeLayoutGetHeight(root))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
        assertEquals(30f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
        assertEquals(60f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
        assertEquals(40f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
        GlobalMembers.YGNodeCalculateLayout(
            root,
            GlobalMembers.YGUndefined,
            GlobalMembers.YGUndefined,
            YGDirection.YGDirectionRTL
        )
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
        assertEquals(110f, GlobalMembers.YGNodeLayoutGetWidth(root))
        assertEquals(100f, GlobalMembers.YGNodeLayoutGetHeight(root))
        assertEquals(50f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
        assertEquals(30f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
        assertEquals(60f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
        assertEquals(40f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
        GlobalMembers.YGNodeFreeRecursive(root)
        GlobalMembers.YGConfigFree(config)
    }

    @Test
    fun absolute_layout_align_items_center() {
        val config = GlobalMembers.YGConfigNew()
        val root = GlobalMembers.YGNodeNewWithConfig(config)
        GlobalMembers.YGNodeStyleSetAlignItems(root, YGAlign.YGAlignCenter)
        GlobalMembers.YGNodeStyleSetFlexGrow(root, 1f)
        GlobalMembers.YGNodeStyleSetWidth(root, 110f)
        GlobalMembers.YGNodeStyleSetHeight(root, 100f)
        val root_child0 = GlobalMembers.YGNodeNewWithConfig(config)
        GlobalMembers.YGNodeStyleSetPositionType(root_child0, YGPositionType.YGPositionTypeAbsolute)
        GlobalMembers.YGNodeStyleSetWidth(root_child0, 60f)
        GlobalMembers.YGNodeStyleSetHeight(root_child0, 40f)
        GlobalMembers.YGNodeInsertChild(root, root_child0, 0)
        GlobalMembers.YGNodeCalculateLayout(
            root,
            GlobalMembers.YGUndefined,
            GlobalMembers.YGUndefined,
            YGDirection.YGDirectionLTR
        )
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
        assertEquals(110f, GlobalMembers.YGNodeLayoutGetWidth(root))
        assertEquals(100f, GlobalMembers.YGNodeLayoutGetHeight(root))
        assertEquals(25f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
        assertEquals(60f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
        assertEquals(40f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
        GlobalMembers.YGNodeCalculateLayout(
            root,
            GlobalMembers.YGUndefined,
            GlobalMembers.YGUndefined,
            YGDirection.YGDirectionRTL
        )
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
        assertEquals(110f, GlobalMembers.YGNodeLayoutGetWidth(root))
        assertEquals(100f, GlobalMembers.YGNodeLayoutGetHeight(root))
        assertEquals(25f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
        assertEquals(60f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
        assertEquals(40f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
        GlobalMembers.YGNodeFreeRecursive(root)
        GlobalMembers.YGConfigFree(config)
    }

    @Test
    fun absolute_layout_align_items_center_on_child_only() {
        val config = GlobalMembers.YGConfigNew()
        val root = GlobalMembers.YGNodeNewWithConfig(config)
        GlobalMembers.YGNodeStyleSetFlexGrow(root, 1f)
        GlobalMembers.YGNodeStyleSetWidth(root, 110f)
        GlobalMembers.YGNodeStyleSetHeight(root, 100f)
        val root_child0 = GlobalMembers.YGNodeNewWithConfig(config)
        GlobalMembers.YGNodeStyleSetAlignSelf(root_child0, YGAlign.YGAlignCenter)
        GlobalMembers.YGNodeStyleSetPositionType(root_child0, YGPositionType.YGPositionTypeAbsolute)
        GlobalMembers.YGNodeStyleSetWidth(root_child0, 60f)
        GlobalMembers.YGNodeStyleSetHeight(root_child0, 40f)
        GlobalMembers.YGNodeInsertChild(root, root_child0, 0)
        GlobalMembers.YGNodeCalculateLayout(
            root,
            GlobalMembers.YGUndefined,
            GlobalMembers.YGUndefined,
            YGDirection.YGDirectionLTR
        )
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
        assertEquals(110f, GlobalMembers.YGNodeLayoutGetWidth(root))
        assertEquals(100f, GlobalMembers.YGNodeLayoutGetHeight(root))
        assertEquals(25f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
        assertEquals(60f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
        assertEquals(40f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
        GlobalMembers.YGNodeCalculateLayout(
            root,
            GlobalMembers.YGUndefined,
            GlobalMembers.YGUndefined,
            YGDirection.YGDirectionRTL
        )
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
        assertEquals(110f, GlobalMembers.YGNodeLayoutGetWidth(root))
        assertEquals(100f, GlobalMembers.YGNodeLayoutGetHeight(root))
        assertEquals(25f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
        assertEquals(60f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
        assertEquals(40f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
        GlobalMembers.YGNodeFreeRecursive(root)
        GlobalMembers.YGConfigFree(config)
    }

    @Test
    fun absolute_layout_align_items_and_justify_content_center_and_top_position() {
        val config = GlobalMembers.YGConfigNew()
        val root = GlobalMembers.YGNodeNewWithConfig(config)
        GlobalMembers.YGNodeStyleSetJustifyContent(root, YGJustify.YGJustifyCenter)
        GlobalMembers.YGNodeStyleSetAlignItems(root, YGAlign.YGAlignCenter)
        GlobalMembers.YGNodeStyleSetFlexGrow(root, 1f)
        GlobalMembers.YGNodeStyleSetWidth(root, 110f)
        GlobalMembers.YGNodeStyleSetHeight(root, 100f)
        val root_child0 = GlobalMembers.YGNodeNewWithConfig(config)
        GlobalMembers.YGNodeStyleSetPositionType(root_child0, YGPositionType.YGPositionTypeAbsolute)
        GlobalMembers.YGNodeStyleSetPosition(root_child0, YGEdge.YGEdgeTop, 10f)
        GlobalMembers.YGNodeStyleSetWidth(root_child0, 60f)
        GlobalMembers.YGNodeStyleSetHeight(root_child0, 40f)
        GlobalMembers.YGNodeInsertChild(root, root_child0, 0)
        GlobalMembers.YGNodeCalculateLayout(
            root,
            GlobalMembers.YGUndefined,
            GlobalMembers.YGUndefined,
            YGDirection.YGDirectionLTR
        )
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
        assertEquals(110f, GlobalMembers.YGNodeLayoutGetWidth(root))
        assertEquals(100f, GlobalMembers.YGNodeLayoutGetHeight(root))
        assertEquals(25f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
        assertEquals(10f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
        assertEquals(60f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
        assertEquals(40f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
        GlobalMembers.YGNodeCalculateLayout(
            root,
            GlobalMembers.YGUndefined,
            GlobalMembers.YGUndefined,
            YGDirection.YGDirectionRTL
        )
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
        assertEquals(110f, GlobalMembers.YGNodeLayoutGetWidth(root))
        assertEquals(100f, GlobalMembers.YGNodeLayoutGetHeight(root))
        assertEquals(25f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
        assertEquals(10f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
        assertEquals(60f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
        assertEquals(40f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
        GlobalMembers.YGNodeFreeRecursive(root)
        GlobalMembers.YGConfigFree(config)
    }

    @Test
    fun absolute_layout_align_items_and_justify_content_center_and_bottom_position() {
        val config = GlobalMembers.YGConfigNew()
        val root = GlobalMembers.YGNodeNewWithConfig(config)
        GlobalMembers.YGNodeStyleSetJustifyContent(root, YGJustify.YGJustifyCenter)
        GlobalMembers.YGNodeStyleSetAlignItems(root, YGAlign.YGAlignCenter)
        GlobalMembers.YGNodeStyleSetFlexGrow(root, 1f)
        GlobalMembers.YGNodeStyleSetWidth(root, 110f)
        GlobalMembers.YGNodeStyleSetHeight(root, 100f)
        val root_child0 = GlobalMembers.YGNodeNewWithConfig(config)
        GlobalMembers.YGNodeStyleSetPositionType(root_child0, YGPositionType.YGPositionTypeAbsolute)
        GlobalMembers.YGNodeStyleSetPosition(root_child0, YGEdge.YGEdgeBottom, 10f)
        GlobalMembers.YGNodeStyleSetWidth(root_child0, 60f)
        GlobalMembers.YGNodeStyleSetHeight(root_child0, 40f)
        GlobalMembers.YGNodeInsertChild(root, root_child0, 0)
        GlobalMembers.YGNodeCalculateLayout(
            root,
            GlobalMembers.YGUndefined,
            GlobalMembers.YGUndefined,
            YGDirection.YGDirectionLTR
        )
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
        assertEquals(110f, GlobalMembers.YGNodeLayoutGetWidth(root))
        assertEquals(100f, GlobalMembers.YGNodeLayoutGetHeight(root))
        assertEquals(25f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
        assertEquals(50f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
        assertEquals(60f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
        assertEquals(40f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
        GlobalMembers.YGNodeCalculateLayout(
            root,
            GlobalMembers.YGUndefined,
            GlobalMembers.YGUndefined,
            YGDirection.YGDirectionRTL
        )
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
        assertEquals(110f, GlobalMembers.YGNodeLayoutGetWidth(root))
        assertEquals(100f, GlobalMembers.YGNodeLayoutGetHeight(root))
        assertEquals(25f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
        assertEquals(50f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
        assertEquals(60f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
        assertEquals(40f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
        GlobalMembers.YGNodeFreeRecursive(root)
        GlobalMembers.YGConfigFree(config)
    }

    @Test
    fun absolute_layout_align_items_and_justify_content_center_and_left_position() {
        val config = GlobalMembers.YGConfigNew()
        val root = GlobalMembers.YGNodeNewWithConfig(config)
        GlobalMembers.YGNodeStyleSetJustifyContent(root, YGJustify.YGJustifyCenter)
        GlobalMembers.YGNodeStyleSetAlignItems(root, YGAlign.YGAlignCenter)
        GlobalMembers.YGNodeStyleSetFlexGrow(root, 1f)
        GlobalMembers.YGNodeStyleSetWidth(root, 110f)
        GlobalMembers.YGNodeStyleSetHeight(root, 100f)
        val root_child0 = GlobalMembers.YGNodeNewWithConfig(config)
        GlobalMembers.YGNodeStyleSetPositionType(root_child0, YGPositionType.YGPositionTypeAbsolute)
        GlobalMembers.YGNodeStyleSetPosition(root_child0, YGEdge.YGEdgeLeft, 5f)
        GlobalMembers.YGNodeStyleSetWidth(root_child0, 60f)
        GlobalMembers.YGNodeStyleSetHeight(root_child0, 40f)
        GlobalMembers.YGNodeInsertChild(root, root_child0, 0)
        GlobalMembers.YGNodeCalculateLayout(
            root,
            GlobalMembers.YGUndefined,
            GlobalMembers.YGUndefined,
            YGDirection.YGDirectionLTR
        )
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
        assertEquals(110f, GlobalMembers.YGNodeLayoutGetWidth(root))
        assertEquals(100f, GlobalMembers.YGNodeLayoutGetHeight(root))
        assertEquals(5f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
        assertEquals(30f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
        assertEquals(60f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
        assertEquals(40f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
        GlobalMembers.YGNodeCalculateLayout(
            root,
            GlobalMembers.YGUndefined,
            GlobalMembers.YGUndefined,
            YGDirection.YGDirectionRTL
        )
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
        assertEquals(110f, GlobalMembers.YGNodeLayoutGetWidth(root))
        assertEquals(100f, GlobalMembers.YGNodeLayoutGetHeight(root))
        assertEquals(5f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
        assertEquals(30f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
        assertEquals(60f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
        assertEquals(40f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
        GlobalMembers.YGNodeFreeRecursive(root)
        GlobalMembers.YGConfigFree(config)
    }

    @Test
    fun absolute_layout_align_items_and_justify_content_center_and_right_position() {
        val config = GlobalMembers.YGConfigNew()
        val root = GlobalMembers.YGNodeNewWithConfig(config)
        GlobalMembers.YGNodeStyleSetJustifyContent(root, YGJustify.YGJustifyCenter)
        GlobalMembers.YGNodeStyleSetAlignItems(root, YGAlign.YGAlignCenter)
        GlobalMembers.YGNodeStyleSetFlexGrow(root, 1f)
        GlobalMembers.YGNodeStyleSetWidth(root, 110f)
        GlobalMembers.YGNodeStyleSetHeight(root, 100f)
        val root_child0 = GlobalMembers.YGNodeNewWithConfig(config)
        GlobalMembers.YGNodeStyleSetPositionType(root_child0, YGPositionType.YGPositionTypeAbsolute)
        GlobalMembers.YGNodeStyleSetPosition(root_child0, YGEdge.YGEdgeRight, 5f)
        GlobalMembers.YGNodeStyleSetWidth(root_child0, 60f)
        GlobalMembers.YGNodeStyleSetHeight(root_child0, 40f)
        GlobalMembers.YGNodeInsertChild(root, root_child0, 0)
        GlobalMembers.YGNodeCalculateLayout(
            root,
            GlobalMembers.YGUndefined,
            GlobalMembers.YGUndefined,
            YGDirection.YGDirectionLTR
        )
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
        assertEquals(110f, GlobalMembers.YGNodeLayoutGetWidth(root))
        assertEquals(100f, GlobalMembers.YGNodeLayoutGetHeight(root))
        assertEquals(45f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
        assertEquals(30f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
        assertEquals(60f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
        assertEquals(40f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
        GlobalMembers.YGNodeCalculateLayout(
            root,
            GlobalMembers.YGUndefined,
            GlobalMembers.YGUndefined,
            YGDirection.YGDirectionRTL
        )
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
        assertEquals(110f, GlobalMembers.YGNodeLayoutGetWidth(root))
        assertEquals(100f, GlobalMembers.YGNodeLayoutGetHeight(root))
        assertEquals(45f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
        assertEquals(30f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
        assertEquals(60f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
        assertEquals(40f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
        GlobalMembers.YGNodeFreeRecursive(root)
        GlobalMembers.YGConfigFree(config)
    }

    @Test
    fun position_root_with_rtl_should_position_withoutdirection() {
        val config = GlobalMembers.YGConfigNew()
        val root = GlobalMembers.YGNodeNewWithConfig(config)
        GlobalMembers.YGNodeStyleSetPosition(root, YGEdge.YGEdgeLeft, 72f)
        GlobalMembers.YGNodeStyleSetWidth(root, 52f)
        GlobalMembers.YGNodeStyleSetHeight(root, 52f)
        GlobalMembers.YGNodeCalculateLayout(
            root,
            GlobalMembers.YGUndefined,
            GlobalMembers.YGUndefined,
            YGDirection.YGDirectionLTR
        )
        assertEquals(72f, GlobalMembers.YGNodeLayoutGetLeft(root))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
        assertEquals(52f, GlobalMembers.YGNodeLayoutGetWidth(root))
        assertEquals(52f, GlobalMembers.YGNodeLayoutGetHeight(root))
        GlobalMembers.YGNodeCalculateLayout(
            root,
            GlobalMembers.YGUndefined,
            GlobalMembers.YGUndefined,
            YGDirection.YGDirectionRTL
        )
        assertEquals(72f, GlobalMembers.YGNodeLayoutGetLeft(root))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
        assertEquals(52f, GlobalMembers.YGNodeLayoutGetWidth(root))
        assertEquals(52f, GlobalMembers.YGNodeLayoutGetHeight(root))
        GlobalMembers.YGNodeFreeRecursive(root)
        GlobalMembers.YGConfigFree(config)
    }

    @Test
    fun absolute_layout_percentage_bottom_based_on_parent_height() {
        val config = GlobalMembers.YGConfigNew()
        val root = GlobalMembers.YGNodeNewWithConfig(config)
        GlobalMembers.YGNodeStyleSetWidth(root, 100f)
        GlobalMembers.YGNodeStyleSetHeight(root, 200f)
        val root_child0 = GlobalMembers.YGNodeNewWithConfig(config)
        GlobalMembers.YGNodeStyleSetPositionType(root_child0, YGPositionType.YGPositionTypeAbsolute)
        GlobalMembers.YGNodeStyleSetPositionPercent(root_child0, YGEdge.YGEdgeTop, 50f)
        GlobalMembers.YGNodeStyleSetWidth(root_child0, 10f)
        GlobalMembers.YGNodeStyleSetHeight(root_child0, 10f)
        GlobalMembers.YGNodeInsertChild(root, root_child0, 0)
        val root_child1 = GlobalMembers.YGNodeNewWithConfig(config)
        GlobalMembers.YGNodeStyleSetPositionType(root_child1, YGPositionType.YGPositionTypeAbsolute)
        GlobalMembers.YGNodeStyleSetPositionPercent(root_child1, YGEdge.YGEdgeBottom, 50f)
        GlobalMembers.YGNodeStyleSetWidth(root_child1, 10f)
        GlobalMembers.YGNodeStyleSetHeight(root_child1, 10f)
        GlobalMembers.YGNodeInsertChild(root, root_child1, 1)
        val root_child2 = GlobalMembers.YGNodeNewWithConfig(config)
        GlobalMembers.YGNodeStyleSetPositionType(root_child2, YGPositionType.YGPositionTypeAbsolute)
        GlobalMembers.YGNodeStyleSetPositionPercent(root_child2, YGEdge.YGEdgeTop, 10f)
        GlobalMembers.YGNodeStyleSetPositionPercent(root_child2, YGEdge.YGEdgeBottom, 10f)
        GlobalMembers.YGNodeStyleSetWidth(root_child2, 10f)
        GlobalMembers.YGNodeInsertChild(root, root_child2, 2)
        GlobalMembers.YGNodeCalculateLayout(
            root,
            GlobalMembers.YGUndefined,
            GlobalMembers.YGUndefined,
            YGDirection.YGDirectionLTR
        )
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
        assertEquals(100f, GlobalMembers.YGNodeLayoutGetWidth(root))
        assertEquals(200f, GlobalMembers.YGNodeLayoutGetHeight(root))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
        assertEquals(100f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
        assertEquals(10f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
        assertEquals(10f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child1))
        assertEquals(90f, GlobalMembers.YGNodeLayoutGetTop(root_child1))
        assertEquals(10f, GlobalMembers.YGNodeLayoutGetWidth(root_child1))
        assertEquals(10f, GlobalMembers.YGNodeLayoutGetHeight(root_child1))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child2))
        assertEquals(20f, GlobalMembers.YGNodeLayoutGetTop(root_child2))
        assertEquals(10f, GlobalMembers.YGNodeLayoutGetWidth(root_child2))
        assertEquals(160f, GlobalMembers.YGNodeLayoutGetHeight(root_child2))
        GlobalMembers.YGNodeCalculateLayout(
            root,
            GlobalMembers.YGUndefined,
            GlobalMembers.YGUndefined,
            YGDirection.YGDirectionRTL
        )
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
        assertEquals(100f, GlobalMembers.YGNodeLayoutGetWidth(root))
        assertEquals(200f, GlobalMembers.YGNodeLayoutGetHeight(root))
        assertEquals(90f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
        assertEquals(100f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
        assertEquals(10f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
        assertEquals(10f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
        assertEquals(90f, GlobalMembers.YGNodeLayoutGetLeft(root_child1))
        assertEquals(90f, GlobalMembers.YGNodeLayoutGetTop(root_child1))
        assertEquals(10f, GlobalMembers.YGNodeLayoutGetWidth(root_child1))
        assertEquals(10f, GlobalMembers.YGNodeLayoutGetHeight(root_child1))
        assertEquals(90f, GlobalMembers.YGNodeLayoutGetLeft(root_child2))
        assertEquals(20f, GlobalMembers.YGNodeLayoutGetTop(root_child2))
        assertEquals(10f, GlobalMembers.YGNodeLayoutGetWidth(root_child2))
        assertEquals(160f, GlobalMembers.YGNodeLayoutGetHeight(root_child2))
        GlobalMembers.YGNodeFreeRecursive(root)
        GlobalMembers.YGConfigFree(config)
    }

    @Test
    fun absolute_layout_in_wrap_reverse_column_container() {
        val config = GlobalMembers.YGConfigNew()
        val root = GlobalMembers.YGNodeNewWithConfig(config)
        GlobalMembers.YGNodeStyleSetFlexWrap(root, YGWrap.YGWrapWrapReverse)
        GlobalMembers.YGNodeStyleSetWidth(root, 100f)
        GlobalMembers.YGNodeStyleSetHeight(root, 100f)
        val root_child0 = GlobalMembers.YGNodeNewWithConfig(config)
        GlobalMembers.YGNodeStyleSetPositionType(root_child0, YGPositionType.YGPositionTypeAbsolute)
        GlobalMembers.YGNodeStyleSetWidth(root_child0, 20f)
        GlobalMembers.YGNodeStyleSetHeight(root_child0, 20f)
        GlobalMembers.YGNodeInsertChild(root, root_child0, 0)
        GlobalMembers.YGNodeCalculateLayout(
            root,
            GlobalMembers.YGUndefined,
            GlobalMembers.YGUndefined,
            YGDirection.YGDirectionLTR
        )
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
        assertEquals(100f, GlobalMembers.YGNodeLayoutGetWidth(root))
        assertEquals(100f, GlobalMembers.YGNodeLayoutGetHeight(root))
        assertEquals(80f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
        assertEquals(20f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
        assertEquals(20f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
        GlobalMembers.YGNodeCalculateLayout(
            root,
            GlobalMembers.YGUndefined,
            GlobalMembers.YGUndefined,
            YGDirection.YGDirectionRTL
        )
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
        assertEquals(100f, GlobalMembers.YGNodeLayoutGetWidth(root))
        assertEquals(100f, GlobalMembers.YGNodeLayoutGetHeight(root))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
        assertEquals(20f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
        assertEquals(20f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
        GlobalMembers.YGNodeFreeRecursive(root)
        GlobalMembers.YGConfigFree(config)
    }

    @Test
    fun absolute_layout_in_wrap_reverse_row_container() {
        val config = GlobalMembers.YGConfigNew()
        val root = GlobalMembers.YGNodeNewWithConfig(config)
        GlobalMembers.YGNodeStyleSetFlexDirection(root, YGFlexDirection.YGFlexDirectionRow)
        GlobalMembers.YGNodeStyleSetFlexWrap(root, YGWrap.YGWrapWrapReverse)
        GlobalMembers.YGNodeStyleSetWidth(root, 100f)
        GlobalMembers.YGNodeStyleSetHeight(root, 100f)
        val root_child0 = GlobalMembers.YGNodeNewWithConfig(config)
        GlobalMembers.YGNodeStyleSetPositionType(root_child0, YGPositionType.YGPositionTypeAbsolute)
        GlobalMembers.YGNodeStyleSetWidth(root_child0, 20f)
        GlobalMembers.YGNodeStyleSetHeight(root_child0, 20f)
        GlobalMembers.YGNodeInsertChild(root, root_child0, 0)
        GlobalMembers.YGNodeCalculateLayout(
            root,
            GlobalMembers.YGUndefined,
            GlobalMembers.YGUndefined,
            YGDirection.YGDirectionLTR
        )
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
        assertEquals(100f, GlobalMembers.YGNodeLayoutGetWidth(root))
        assertEquals(100f, GlobalMembers.YGNodeLayoutGetHeight(root))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
        assertEquals(80f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
        assertEquals(20f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
        assertEquals(20f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
        GlobalMembers.YGNodeCalculateLayout(
            root,
            GlobalMembers.YGUndefined,
            GlobalMembers.YGUndefined,
            YGDirection.YGDirectionRTL
        )
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
        assertEquals(100f, GlobalMembers.YGNodeLayoutGetWidth(root))
        assertEquals(100f, GlobalMembers.YGNodeLayoutGetHeight(root))
        assertEquals(80f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
        assertEquals(80f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
        assertEquals(20f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
        assertEquals(20f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
        GlobalMembers.YGNodeFreeRecursive(root)
        GlobalMembers.YGConfigFree(config)
    }

    @Test
    fun absolute_layout_in_wrap_reverse_column_container_flex_end() {
        val config = GlobalMembers.YGConfigNew()
        val root = GlobalMembers.YGNodeNewWithConfig(config)
        GlobalMembers.YGNodeStyleSetFlexWrap(root, YGWrap.YGWrapWrapReverse)
        GlobalMembers.YGNodeStyleSetWidth(root, 100f)
        GlobalMembers.YGNodeStyleSetHeight(root, 100f)
        val root_child0 = GlobalMembers.YGNodeNewWithConfig(config)
        GlobalMembers.YGNodeStyleSetAlignSelf(root_child0, YGAlign.YGAlignFlexEnd)
        GlobalMembers.YGNodeStyleSetPositionType(root_child0, YGPositionType.YGPositionTypeAbsolute)
        GlobalMembers.YGNodeStyleSetWidth(root_child0, 20f)
        GlobalMembers.YGNodeStyleSetHeight(root_child0, 20f)
        GlobalMembers.YGNodeInsertChild(root, root_child0, 0)
        GlobalMembers.YGNodeCalculateLayout(
            root,
            GlobalMembers.YGUndefined,
            GlobalMembers.YGUndefined,
            YGDirection.YGDirectionLTR
        )
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
        assertEquals(100f, GlobalMembers.YGNodeLayoutGetWidth(root))
        assertEquals(100f, GlobalMembers.YGNodeLayoutGetHeight(root))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
        assertEquals(20f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
        assertEquals(20f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
        GlobalMembers.YGNodeCalculateLayout(
            root,
            GlobalMembers.YGUndefined,
            GlobalMembers.YGUndefined,
            YGDirection.YGDirectionRTL
        )
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
        assertEquals(100f, GlobalMembers.YGNodeLayoutGetWidth(root))
        assertEquals(100f, GlobalMembers.YGNodeLayoutGetHeight(root))
        assertEquals(80f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
        assertEquals(20f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
        assertEquals(20f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
        GlobalMembers.YGNodeFreeRecursive(root)
        GlobalMembers.YGConfigFree(config)
    }

    @Test
    fun absolute_layout_in_wrap_reverse_row_container_flex_end() {
        val config = GlobalMembers.YGConfigNew()
        val root = GlobalMembers.YGNodeNewWithConfig(config)
        GlobalMembers.YGNodeStyleSetFlexDirection(root, YGFlexDirection.YGFlexDirectionRow)
        GlobalMembers.YGNodeStyleSetFlexWrap(root, YGWrap.YGWrapWrapReverse)
        GlobalMembers.YGNodeStyleSetWidth(root, 100f)
        GlobalMembers.YGNodeStyleSetHeight(root, 100f)
        val root_child0 = GlobalMembers.YGNodeNewWithConfig(config)
        GlobalMembers.YGNodeStyleSetAlignSelf(root_child0, YGAlign.YGAlignFlexEnd)
        GlobalMembers.YGNodeStyleSetPositionType(root_child0, YGPositionType.YGPositionTypeAbsolute)
        GlobalMembers.YGNodeStyleSetWidth(root_child0, 20f)
        GlobalMembers.YGNodeStyleSetHeight(root_child0, 20f)
        GlobalMembers.YGNodeInsertChild(root, root_child0, 0)
        GlobalMembers.YGNodeCalculateLayout(
            root,
            GlobalMembers.YGUndefined,
            GlobalMembers.YGUndefined,
            YGDirection.YGDirectionLTR
        )
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
        assertEquals(100f, GlobalMembers.YGNodeLayoutGetWidth(root))
        assertEquals(100f, GlobalMembers.YGNodeLayoutGetHeight(root))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
        assertEquals(20f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
        assertEquals(20f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
        GlobalMembers.YGNodeCalculateLayout(
            root,
            GlobalMembers.YGUndefined,
            GlobalMembers.YGUndefined,
            YGDirection.YGDirectionRTL
        )
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
        assertEquals(100f, GlobalMembers.YGNodeLayoutGetWidth(root))
        assertEquals(100f, GlobalMembers.YGNodeLayoutGetHeight(root))
        assertEquals(80f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
        assertEquals(20f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
        assertEquals(20f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
        GlobalMembers.YGNodeFreeRecursive(root)
        GlobalMembers.YGConfigFree(config)
    }
}
