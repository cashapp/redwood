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
package app.cash.redwood.yoga.internal

import app.cash.redwood.yoga.internal.enums.YGAlign
import app.cash.redwood.yoga.internal.enums.YGDirection
import app.cash.redwood.yoga.internal.enums.YGEdge
import app.cash.redwood.yoga.internal.enums.YGFlexDirection
import app.cash.redwood.yoga.internal.enums.YGMeasureMode
import kotlin.test.Test
import kotlin.test.assertEquals

class YGAlignBaselineTest {
    // Test case for bug in T32999822
    @Test
    fun align_baseline_parent_ht_not_specified() {
        val config = GlobalMembers.YGConfigNew()
        val root = GlobalMembers.YGNodeNewWithConfig(config)
        GlobalMembers.YGNodeStyleSetFlexDirection(root, YGFlexDirection.YGFlexDirectionRow)
        GlobalMembers.YGNodeStyleSetAlignContent(root, YGAlign.YGAlignStretch)
        GlobalMembers.YGNodeStyleSetAlignItems(root, YGAlign.YGAlignBaseline)
        GlobalMembers.YGNodeStyleSetWidth(root, 340f)
        GlobalMembers.YGNodeStyleSetMaxHeight(root, 170f)
        GlobalMembers.YGNodeStyleSetMinHeight(root, 0f)
        val root_child0 = GlobalMembers.YGNodeNewWithConfig(config)
        GlobalMembers.YGNodeStyleSetFlexGrow(root_child0, 0f)
        GlobalMembers.YGNodeStyleSetFlexShrink(root_child0, 1f)
        GlobalMembers.YGNodeSetMeasureFunc(root_child0) { node: YGNode?, width: Float, widthMode: YGMeasureMode?, height: Float, heightMode: YGMeasureMode? ->
            _measure1(
                node,
                width,
                widthMode,
                height,
                heightMode
            )
        }
        GlobalMembers.YGNodeInsertChild(root, root_child0, 0)
        val root_child1 = GlobalMembers.YGNodeNewWithConfig(config)
        GlobalMembers.YGNodeStyleSetFlexGrow(root_child1, 0f)
        GlobalMembers.YGNodeStyleSetFlexShrink(root_child1, 1f)
        GlobalMembers.YGNodeSetMeasureFunc(root_child1) { node: YGNode?, width: Float, widthMode: YGMeasureMode?, height: Float, heightMode: YGMeasureMode? ->
            _measure2(
                node,
                width,
                widthMode,
                height,
                heightMode
            )
        }
        GlobalMembers.YGNodeInsertChild(root, root_child1, 1)
        GlobalMembers.YGNodeCalculateLayout(
            root,
            GlobalMembers.YGUndefined,
            GlobalMembers.YGUndefined,
            YGDirection.YGDirectionLTR
        )
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
        assertEquals(340f, GlobalMembers.YGNodeLayoutGetWidth(root))
        assertEquals(126f, GlobalMembers.YGNodeLayoutGetHeight(root))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
        assertEquals(42f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
        assertEquals(50f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
        assertEquals(76f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
        assertEquals(42f, GlobalMembers.YGNodeLayoutGetLeft(root_child1))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child1))
        assertEquals(279f, GlobalMembers.YGNodeLayoutGetWidth(root_child1))
        assertEquals(126f, GlobalMembers.YGNodeLayoutGetHeight(root_child1))
        GlobalMembers.YGNodeFreeRecursive(root)
        GlobalMembers.YGConfigFree(config)
    }

    @Test
    fun align_baseline_with_no_parent_ht() {
        val config = GlobalMembers.YGConfigNew()
        val root = GlobalMembers.YGNodeNewWithConfig(config)
        GlobalMembers.YGNodeStyleSetFlexDirection(root, YGFlexDirection.YGFlexDirectionRow)
        GlobalMembers.YGNodeStyleSetAlignItems(root, YGAlign.YGAlignBaseline)
        GlobalMembers.YGNodeStyleSetWidth(root, 150f)
        val root_child0 = GlobalMembers.YGNodeNewWithConfig(config)
        GlobalMembers.YGNodeStyleSetWidth(root_child0, 50f)
        GlobalMembers.YGNodeStyleSetHeight(root_child0, 50f)
        GlobalMembers.YGNodeInsertChild(root, root_child0, 0)
        val root_child1 = GlobalMembers.YGNodeNewWithConfig(config)
        GlobalMembers.YGNodeStyleSetWidth(root_child1, 50f)
        GlobalMembers.YGNodeStyleSetHeight(root_child1, 40f)
        GlobalMembers.YGNodeSetBaselineFunc(root_child1) { node: YGNode?, width: Float, height: Float ->
            _baselineFunc(
                node,
                width,
                height
            )
        }
        GlobalMembers.YGNodeInsertChild(root, root_child1, 1)
        GlobalMembers.YGNodeCalculateLayout(
            root,
            GlobalMembers.YGUndefined,
            GlobalMembers.YGUndefined,
            YGDirection.YGDirectionLTR
        )
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
        assertEquals(150f, GlobalMembers.YGNodeLayoutGetWidth(root))
        assertEquals(70f, GlobalMembers.YGNodeLayoutGetHeight(root))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
        assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
        assertEquals(50f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
        assertEquals(50f, GlobalMembers.YGNodeLayoutGetLeft(root_child1))
        assertEquals(30f, GlobalMembers.YGNodeLayoutGetTop(root_child1))
        assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child1))
        assertEquals(40f, GlobalMembers.YGNodeLayoutGetHeight(root_child1))
        GlobalMembers.YGNodeFreeRecursive(root)
        GlobalMembers.YGConfigFree(config)
    }

    @Test
    fun align_baseline_with_no_baseline_func_and_no_parent_ht() {
        val config = GlobalMembers.YGConfigNew()
        val root = GlobalMembers.YGNodeNewWithConfig(config)
        GlobalMembers.YGNodeStyleSetFlexDirection(root, YGFlexDirection.YGFlexDirectionRow)
        GlobalMembers.YGNodeStyleSetAlignItems(root, YGAlign.YGAlignBaseline)
        GlobalMembers.YGNodeStyleSetWidth(root, 150f)
        val root_child0 = GlobalMembers.YGNodeNewWithConfig(config)
        GlobalMembers.YGNodeStyleSetWidth(root_child0, 50f)
        GlobalMembers.YGNodeStyleSetHeight(root_child0, 80f)
        GlobalMembers.YGNodeInsertChild(root, root_child0, 0)
        val root_child1 = GlobalMembers.YGNodeNewWithConfig(config)
        GlobalMembers.YGNodeStyleSetWidth(root_child1, 50f)
        GlobalMembers.YGNodeStyleSetHeight(root_child1, 50f)
        GlobalMembers.YGNodeInsertChild(root, root_child1, 1)
        GlobalMembers.YGNodeCalculateLayout(
            root,
            GlobalMembers.YGUndefined,
            GlobalMembers.YGUndefined,
            YGDirection.YGDirectionLTR
        )
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
        assertEquals(150f, GlobalMembers.YGNodeLayoutGetWidth(root))
        assertEquals(80f, GlobalMembers.YGNodeLayoutGetHeight(root))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
        assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
        assertEquals(80f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
        assertEquals(50f, GlobalMembers.YGNodeLayoutGetLeft(root_child1))
        assertEquals(30f, GlobalMembers.YGNodeLayoutGetTop(root_child1))
        assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child1))
        assertEquals(50f, GlobalMembers.YGNodeLayoutGetHeight(root_child1))
        GlobalMembers.YGNodeFreeRecursive(root)
        GlobalMembers.YGConfigFree(config)
    }

    @Test
    fun align_baseline_parent_using_child_in_column_as_reference() {
        val config = GlobalMembers.YGConfigNew()
        val root = createYGNode(config, YGFlexDirection.YGFlexDirectionRow, 1000, 1000, true)
        val root_child0 =
            createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 600, false)
        GlobalMembers.YGNodeInsertChild(root, root_child0, 0)
        val root_child1 =
            createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 800, false)
        GlobalMembers.YGNodeInsertChild(root, root_child1, 1)
        val root_child1_child0 =
            createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 300, false)
        GlobalMembers.YGNodeInsertChild(root_child1, root_child1_child0, 0)
        val root_child1_child1 =
            createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 400, false)
        root_child1_child1.setBaselineFunc { node: YGNode?, width: Float, height: Float ->
            _baselineFunc(
                node,
                width,
                height
            )
        }
        GlobalMembers.YGNodeSetIsReferenceBaseline(root_child1_child1, true)
        GlobalMembers.YGNodeInsertChild(root_child1, root_child1_child1, 1)
        GlobalMembers.YGNodeCalculateLayout(
            root,
            GlobalMembers.YGUndefined,
            GlobalMembers.YGUndefined,
            YGDirection.YGDirectionLTR
        )
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
        assertEquals(500f, GlobalMembers.YGNodeLayoutGetLeft(root_child1))
        assertEquals(100f, GlobalMembers.YGNodeLayoutGetTop(root_child1))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child1_child0))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child1_child0))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child1_child1))
        assertEquals(300f, GlobalMembers.YGNodeLayoutGetTop(root_child1_child1))
        GlobalMembers.YGNodeFreeRecursive(root)
        GlobalMembers.YGConfigFree(config)
    }

    @Test
    fun align_baseline_parent_using_child_with_padding_in_column_as_reference() {
        val config = GlobalMembers.YGConfigNew()
        val root = createYGNode(config, YGFlexDirection.YGFlexDirectionRow, 1000, 1000, true)
        val root_child0 =
            createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 600, false)
        GlobalMembers.YGNodeInsertChild(root, root_child0, 0)
        val root_child1 =
            createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 800, false)
        GlobalMembers.YGNodeInsertChild(root, root_child1, 1)
        val root_child1_child0 =
            createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 300, false)
        GlobalMembers.YGNodeInsertChild(root_child1, root_child1_child0, 0)
        val root_child1_child1 =
            createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 400, false)
        root_child1_child1.setBaselineFunc { node: YGNode?, width: Float, height: Float ->
            _baselineFunc(
                node,
                width,
                height
            )
        }
        GlobalMembers.YGNodeSetIsReferenceBaseline(root_child1_child1, true)
        GlobalMembers.YGNodeStyleSetPadding(root_child1_child1, YGEdge.YGEdgeLeft, 100f)
        GlobalMembers.YGNodeStyleSetPadding(root_child1_child1, YGEdge.YGEdgeRight, 100f)
        GlobalMembers.YGNodeStyleSetPadding(root_child1_child1, YGEdge.YGEdgeTop, 100f)
        GlobalMembers.YGNodeStyleSetPadding(root_child1_child1, YGEdge.YGEdgeBottom, 100f)
        GlobalMembers.YGNodeInsertChild(root_child1, root_child1_child1, 1)
        GlobalMembers.YGNodeCalculateLayout(
            root,
            GlobalMembers.YGUndefined,
            GlobalMembers.YGUndefined,
            YGDirection.YGDirectionLTR
        )
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
        assertEquals(500f, GlobalMembers.YGNodeLayoutGetLeft(root_child1))
        assertEquals(100f, GlobalMembers.YGNodeLayoutGetTop(root_child1))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child1_child0))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child1_child0))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child1_child1))
        assertEquals(300f, GlobalMembers.YGNodeLayoutGetTop(root_child1_child1))
        GlobalMembers.YGNodeFreeRecursive(root)
        GlobalMembers.YGConfigFree(config)
    }

    @Test
    fun align_baseline_parent_with_padding_using_child_in_column_as_reference() {
        val config = GlobalMembers.YGConfigNew()
        val root = createYGNode(config, YGFlexDirection.YGFlexDirectionRow, 1000, 1000, true)
        val root_child0 =
            createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 600, false)
        GlobalMembers.YGNodeInsertChild(root, root_child0, 0)
        val root_child1 =
            createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 800, false)
        GlobalMembers.YGNodeStyleSetPadding(root_child1, YGEdge.YGEdgeLeft, 100f)
        GlobalMembers.YGNodeStyleSetPadding(root_child1, YGEdge.YGEdgeRight, 100f)
        GlobalMembers.YGNodeStyleSetPadding(root_child1, YGEdge.YGEdgeTop, 100f)
        GlobalMembers.YGNodeStyleSetPadding(root_child1, YGEdge.YGEdgeBottom, 100f)
        GlobalMembers.YGNodeInsertChild(root, root_child1, 1)
        val root_child1_child0 =
            createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 300, false)
        GlobalMembers.YGNodeInsertChild(root_child1, root_child1_child0, 0)
        val root_child1_child1 =
            createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 400, false)
        root_child1_child1.setBaselineFunc { node: YGNode?, width: Float, height: Float ->
            _baselineFunc(
                node,
                width,
                height
            )
        }
        GlobalMembers.YGNodeSetIsReferenceBaseline(root_child1_child1, true)
        GlobalMembers.YGNodeInsertChild(root_child1, root_child1_child1, 1)
        GlobalMembers.YGNodeCalculateLayout(
            root,
            GlobalMembers.YGUndefined,
            GlobalMembers.YGUndefined,
            YGDirection.YGDirectionLTR
        )
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
        assertEquals(500f, GlobalMembers.YGNodeLayoutGetLeft(root_child1))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child1))
        assertEquals(100f, GlobalMembers.YGNodeLayoutGetLeft(root_child1_child0))
        assertEquals(100f, GlobalMembers.YGNodeLayoutGetTop(root_child1_child0))
        assertEquals(100f, GlobalMembers.YGNodeLayoutGetLeft(root_child1_child1))
        assertEquals(400f, GlobalMembers.YGNodeLayoutGetTop(root_child1_child1))
        GlobalMembers.YGNodeFreeRecursive(root)
        GlobalMembers.YGConfigFree(config)
    }

    @Test
    fun align_baseline_parent_with_margin_using_child_in_column_as_reference() {
        val config = GlobalMembers.YGConfigNew()
        val root = createYGNode(config, YGFlexDirection.YGFlexDirectionRow, 1000, 1000, true)
        val root_child0 =
            createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 600, false)
        GlobalMembers.YGNodeInsertChild(root, root_child0, 0)
        val root_child1 =
            createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 800, false)
        GlobalMembers.YGNodeStyleSetMargin(root_child1, YGEdge.YGEdgeLeft, 100f)
        GlobalMembers.YGNodeStyleSetMargin(root_child1, YGEdge.YGEdgeRight, 100f)
        GlobalMembers.YGNodeStyleSetMargin(root_child1, YGEdge.YGEdgeTop, 100f)
        GlobalMembers.YGNodeStyleSetMargin(root_child1, YGEdge.YGEdgeBottom, 100f)
        GlobalMembers.YGNodeInsertChild(root, root_child1, 1)
        val root_child1_child0 =
            createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 300, false)
        GlobalMembers.YGNodeInsertChild(root_child1, root_child1_child0, 0)
        val root_child1_child1 =
            createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 400, false)
        root_child1_child1.setBaselineFunc { node: YGNode?, width: Float, height: Float ->
            _baselineFunc(
                node,
                width,
                height
            )
        }
        GlobalMembers.YGNodeSetIsReferenceBaseline(root_child1_child1, true)
        GlobalMembers.YGNodeInsertChild(root_child1, root_child1_child1, 1)
        GlobalMembers.YGNodeCalculateLayout(
            root,
            GlobalMembers.YGUndefined,
            GlobalMembers.YGUndefined,
            YGDirection.YGDirectionLTR
        )
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
        assertEquals(600f, GlobalMembers.YGNodeLayoutGetLeft(root_child1))
        assertEquals(100f, GlobalMembers.YGNodeLayoutGetTop(root_child1))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child1_child0))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child1_child0))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child1_child1))
        assertEquals(300f, GlobalMembers.YGNodeLayoutGetTop(root_child1_child1))
        GlobalMembers.YGNodeFreeRecursive(root)
        GlobalMembers.YGConfigFree(config)
    }

    @Test
    fun align_baseline_parent_using_child_with_margin_in_column_as_reference() {
        val config = GlobalMembers.YGConfigNew()
        val root = createYGNode(config, YGFlexDirection.YGFlexDirectionRow, 1000, 1000, true)
        val root_child0 =
            createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 600, false)
        GlobalMembers.YGNodeInsertChild(root, root_child0, 0)
        val root_child1 =
            createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 800, false)
        GlobalMembers.YGNodeInsertChild(root, root_child1, 1)
        val root_child1_child0 =
            createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 300, false)
        GlobalMembers.YGNodeInsertChild(root_child1, root_child1_child0, 0)
        val root_child1_child1 =
            createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 400, false)
        root_child1_child1.setBaselineFunc { node: YGNode?, width: Float, height: Float ->
            _baselineFunc(
                node,
                width,
                height
            )
        }
        GlobalMembers.YGNodeSetIsReferenceBaseline(root_child1_child1, true)
        GlobalMembers.YGNodeStyleSetMargin(root_child1_child1, YGEdge.YGEdgeLeft, 100f)
        GlobalMembers.YGNodeStyleSetMargin(root_child1_child1, YGEdge.YGEdgeRight, 100f)
        GlobalMembers.YGNodeStyleSetMargin(root_child1_child1, YGEdge.YGEdgeTop, 100f)
        GlobalMembers.YGNodeStyleSetMargin(root_child1_child1, YGEdge.YGEdgeBottom, 100f)
        GlobalMembers.YGNodeInsertChild(root_child1, root_child1_child1, 1)
        GlobalMembers.YGNodeCalculateLayout(
            root,
            GlobalMembers.YGUndefined,
            GlobalMembers.YGUndefined,
            YGDirection.YGDirectionLTR
        )
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
        assertEquals(500f, GlobalMembers.YGNodeLayoutGetLeft(root_child1))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child1))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child1_child0))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child1_child0))
        assertEquals(100f, GlobalMembers.YGNodeLayoutGetLeft(root_child1_child1))
        assertEquals(400f, GlobalMembers.YGNodeLayoutGetTop(root_child1_child1))
        GlobalMembers.YGNodeFreeRecursive(root)
        GlobalMembers.YGConfigFree(config)
    }

    @Test
    fun align_baseline_parent_using_child_in_row_as_reference() {
        val config = GlobalMembers.YGConfigNew()
        val root = createYGNode(config, YGFlexDirection.YGFlexDirectionRow, 1000, 1000, true)
        val root_child0 =
            createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 600, false)
        GlobalMembers.YGNodeInsertChild(root, root_child0, 0)
        val root_child1 = createYGNode(config, YGFlexDirection.YGFlexDirectionRow, 500, 800, true)
        GlobalMembers.YGNodeInsertChild(root, root_child1, 1)
        val root_child1_child0 =
            createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 500, false)
        GlobalMembers.YGNodeInsertChild(root_child1, root_child1_child0, 0)
        val root_child1_child1 =
            createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 400, false)
        root_child1_child1.setBaselineFunc { node: YGNode?, width: Float, height: Float ->
            _baselineFunc(
                node,
                width,
                height
            )
        }
        GlobalMembers.YGNodeSetIsReferenceBaseline(root_child1_child1, true)
        GlobalMembers.YGNodeInsertChild(root_child1, root_child1_child1, 1)
        GlobalMembers.YGNodeCalculateLayout(
            root,
            GlobalMembers.YGUndefined,
            GlobalMembers.YGUndefined,
            YGDirection.YGDirectionLTR
        )
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
        assertEquals(500f, GlobalMembers.YGNodeLayoutGetLeft(root_child1))
        assertEquals(100f, GlobalMembers.YGNodeLayoutGetTop(root_child1))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child1_child0))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child1_child0))
        assertEquals(500f, GlobalMembers.YGNodeLayoutGetLeft(root_child1_child1))
        assertEquals(300f, GlobalMembers.YGNodeLayoutGetTop(root_child1_child1))
        GlobalMembers.YGNodeFreeRecursive(root)
        GlobalMembers.YGConfigFree(config)
    }

    @Test
    fun align_baseline_parent_using_child_with_padding_in_row_as_reference() {
        val config = GlobalMembers.YGConfigNew()
        val root = createYGNode(config, YGFlexDirection.YGFlexDirectionRow, 1000, 1000, true)
        val root_child0 =
            createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 600, false)
        GlobalMembers.YGNodeInsertChild(root, root_child0, 0)
        val root_child1 = createYGNode(config, YGFlexDirection.YGFlexDirectionRow, 500, 800, true)
        GlobalMembers.YGNodeInsertChild(root, root_child1, 1)
        val root_child1_child0 =
            createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 500, false)
        GlobalMembers.YGNodeInsertChild(root_child1, root_child1_child0, 0)
        val root_child1_child1 =
            createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 400, false)
        root_child1_child1.setBaselineFunc { node: YGNode?, width: Float, height: Float ->
            _baselineFunc(
                node,
                width,
                height
            )
        }
        GlobalMembers.YGNodeSetIsReferenceBaseline(root_child1_child1, true)
        GlobalMembers.YGNodeStyleSetPadding(root_child1_child1, YGEdge.YGEdgeLeft, 100f)
        GlobalMembers.YGNodeStyleSetPadding(root_child1_child1, YGEdge.YGEdgeRight, 100f)
        GlobalMembers.YGNodeStyleSetPadding(root_child1_child1, YGEdge.YGEdgeTop, 100f)
        GlobalMembers.YGNodeStyleSetPadding(root_child1_child1, YGEdge.YGEdgeBottom, 100f)
        GlobalMembers.YGNodeInsertChild(root_child1, root_child1_child1, 1)
        GlobalMembers.YGNodeCalculateLayout(
            root,
            GlobalMembers.YGUndefined,
            GlobalMembers.YGUndefined,
            YGDirection.YGDirectionLTR
        )
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
        assertEquals(500f, GlobalMembers.YGNodeLayoutGetLeft(root_child1))
        assertEquals(100f, GlobalMembers.YGNodeLayoutGetTop(root_child1))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child1_child0))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child1_child0))
        assertEquals(500f, GlobalMembers.YGNodeLayoutGetLeft(root_child1_child1))
        assertEquals(300f, GlobalMembers.YGNodeLayoutGetTop(root_child1_child1))
        GlobalMembers.YGNodeFreeRecursive(root)
        GlobalMembers.YGConfigFree(config)
    }

    @Test
    fun align_baseline_parent_using_child_with_margin_in_row_as_reference() {
        val config = GlobalMembers.YGConfigNew()
        val root = createYGNode(config, YGFlexDirection.YGFlexDirectionRow, 1000, 1000, true)
        val root_child0 =
            createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 600, false)
        GlobalMembers.YGNodeInsertChild(root, root_child0, 0)
        val root_child1 = createYGNode(config, YGFlexDirection.YGFlexDirectionRow, 500, 800, true)
        GlobalMembers.YGNodeInsertChild(root, root_child1, 1)
        val root_child1_child0 =
            createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 500, false)
        GlobalMembers.YGNodeInsertChild(root_child1, root_child1_child0, 0)
        val root_child1_child1 =
            createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 400, false)
        root_child1_child1.setBaselineFunc { node: YGNode?, width: Float, height: Float ->
            _baselineFunc(
                node,
                width,
                height
            )
        }
        GlobalMembers.YGNodeSetIsReferenceBaseline(root_child1_child1, true)
        GlobalMembers.YGNodeStyleSetMargin(root_child1_child1, YGEdge.YGEdgeLeft, 100f)
        GlobalMembers.YGNodeStyleSetMargin(root_child1_child1, YGEdge.YGEdgeRight, 100f)
        GlobalMembers.YGNodeStyleSetMargin(root_child1_child1, YGEdge.YGEdgeTop, 100f)
        GlobalMembers.YGNodeStyleSetMargin(root_child1_child1, YGEdge.YGEdgeBottom, 100f)
        GlobalMembers.YGNodeInsertChild(root_child1, root_child1_child1, 1)
        GlobalMembers.YGNodeCalculateLayout(
            root,
            GlobalMembers.YGUndefined,
            GlobalMembers.YGUndefined,
            YGDirection.YGDirectionLTR
        )
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
        assertEquals(500f, GlobalMembers.YGNodeLayoutGetLeft(root_child1))
        assertEquals(100f, GlobalMembers.YGNodeLayoutGetTop(root_child1))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child1_child0))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child1_child0))
        assertEquals(600f, GlobalMembers.YGNodeLayoutGetLeft(root_child1_child1))
        assertEquals(300f, GlobalMembers.YGNodeLayoutGetTop(root_child1_child1))
        GlobalMembers.YGNodeFreeRecursive(root)
        GlobalMembers.YGConfigFree(config)
    }

    @Test
    fun align_baseline_parent_using_child_in_column_as_reference_with_no_baseline_func() {
        val config = GlobalMembers.YGConfigNew()
        val root = createYGNode(config, YGFlexDirection.YGFlexDirectionRow, 1000, 1000, true)
        val root_child0 =
            createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 600, false)
        GlobalMembers.YGNodeInsertChild(root, root_child0, 0)
        val root_child1 =
            createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 800, false)
        GlobalMembers.YGNodeInsertChild(root, root_child1, 1)
        val root_child1_child0 =
            createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 300, false)
        GlobalMembers.YGNodeInsertChild(root_child1, root_child1_child0, 0)
        val root_child1_child1 =
            createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 400, false)
        GlobalMembers.YGNodeSetIsReferenceBaseline(root_child1_child1, true)
        GlobalMembers.YGNodeInsertChild(root_child1, root_child1_child1, 1)
        GlobalMembers.YGNodeCalculateLayout(
            root,
            GlobalMembers.YGUndefined,
            GlobalMembers.YGUndefined,
            YGDirection.YGDirectionLTR
        )
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
        assertEquals(100f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
        assertEquals(500f, GlobalMembers.YGNodeLayoutGetLeft(root_child1))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child1))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child1_child0))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child1_child0))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child1_child1))
        assertEquals(300f, GlobalMembers.YGNodeLayoutGetTop(root_child1_child1))
        GlobalMembers.YGNodeFreeRecursive(root)
        GlobalMembers.YGConfigFree(config)
    }

    @Test
    fun align_baseline_parent_using_child_in_row_as_reference_with_no_baseline_func() {
        val config = GlobalMembers.YGConfigNew()
        val root = createYGNode(config, YGFlexDirection.YGFlexDirectionRow, 1000, 1000, true)
        val root_child0 =
            createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 600, false)
        GlobalMembers.YGNodeInsertChild(root, root_child0, 0)
        val root_child1 = createYGNode(config, YGFlexDirection.YGFlexDirectionRow, 500, 800, true)
        GlobalMembers.YGNodeInsertChild(root, root_child1, 1)
        val root_child1_child0 =
            createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 500, false)
        GlobalMembers.YGNodeInsertChild(root_child1, root_child1_child0, 0)
        val root_child1_child1 =
            createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 400, false)
        GlobalMembers.YGNodeSetIsReferenceBaseline(root_child1_child1, true)
        GlobalMembers.YGNodeInsertChild(root_child1, root_child1_child1, 1)
        GlobalMembers.YGNodeCalculateLayout(
            root,
            GlobalMembers.YGUndefined,
            GlobalMembers.YGUndefined,
            YGDirection.YGDirectionLTR
        )
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
        assertEquals(500f, GlobalMembers.YGNodeLayoutGetLeft(root_child1))
        assertEquals(100f, GlobalMembers.YGNodeLayoutGetTop(root_child1))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child1_child0))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child1_child0))
        assertEquals(500f, GlobalMembers.YGNodeLayoutGetLeft(root_child1_child1))
        assertEquals(100f, GlobalMembers.YGNodeLayoutGetTop(root_child1_child1))
        GlobalMembers.YGNodeFreeRecursive(root)
        GlobalMembers.YGConfigFree(config)
    }

    @Test
    fun align_baseline_parent_using_child_in_column_as_reference_with_height_not_specified() {
        val config = GlobalMembers.YGConfigNew()
        val root = GlobalMembers.YGNodeNewWithConfig(config)
        GlobalMembers.YGNodeStyleSetFlexDirection(root, YGFlexDirection.YGFlexDirectionRow)
        GlobalMembers.YGNodeStyleSetAlignItems(root, YGAlign.YGAlignBaseline)
        GlobalMembers.YGNodeStyleSetWidth(root, 1000f)
        val root_child0 =
            createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 600, false)
        GlobalMembers.YGNodeInsertChild(root, root_child0, 0)
        val root_child1 = GlobalMembers.YGNodeNewWithConfig(config)
        GlobalMembers.YGNodeStyleSetFlexDirection(
            root_child1,
            YGFlexDirection.YGFlexDirectionColumn
        )
        GlobalMembers.YGNodeStyleSetWidth(root_child1, 500f)
        GlobalMembers.YGNodeInsertChild(root, root_child1, 1)
        val root_child1_child0 =
            createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 300, false)
        GlobalMembers.YGNodeInsertChild(root_child1, root_child1_child0, 0)
        val root_child1_child1 =
            createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 400, false)
        root_child1_child1.setBaselineFunc { node: YGNode?, width: Float, height: Float ->
            _baselineFunc(
                node,
                width,
                height
            )
        }
        GlobalMembers.YGNodeSetIsReferenceBaseline(root_child1_child1, true)
        GlobalMembers.YGNodeInsertChild(root_child1, root_child1_child1, 1)
        GlobalMembers.YGNodeCalculateLayout(
            root,
            GlobalMembers.YGUndefined,
            GlobalMembers.YGUndefined,
            YGDirection.YGDirectionLTR
        )
        assertEquals(800f, GlobalMembers.YGNodeLayoutGetHeight(root))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
        assertEquals(500f, GlobalMembers.YGNodeLayoutGetLeft(root_child1))
        assertEquals(100f, GlobalMembers.YGNodeLayoutGetTop(root_child1))
        assertEquals(700f, GlobalMembers.YGNodeLayoutGetHeight(root_child1))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child1_child0))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child1_child0))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child1_child1))
        assertEquals(300f, GlobalMembers.YGNodeLayoutGetTop(root_child1_child1))
        GlobalMembers.YGNodeFreeRecursive(root)
        GlobalMembers.YGConfigFree(config)
    }

    @Test
    fun align_baseline_parent_using_child_in_row_as_reference_with_height_not_specified() {
        val config = GlobalMembers.YGConfigNew()
        val root = GlobalMembers.YGNodeNewWithConfig(config)
        GlobalMembers.YGNodeStyleSetFlexDirection(root, YGFlexDirection.YGFlexDirectionRow)
        GlobalMembers.YGNodeStyleSetAlignItems(root, YGAlign.YGAlignBaseline)
        GlobalMembers.YGNodeStyleSetWidth(root, 1000f)
        val root_child0 =
            createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 600, false)
        GlobalMembers.YGNodeInsertChild(root, root_child0, 0)
        val root_child1 = GlobalMembers.YGNodeNewWithConfig(config)
        GlobalMembers.YGNodeStyleSetFlexDirection(root_child1, YGFlexDirection.YGFlexDirectionRow)
        GlobalMembers.YGNodeStyleSetWidth(root_child1, 500f)
        GlobalMembers.YGNodeInsertChild(root, root_child1, 1)
        val root_child1_child0 =
            createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 500, false)
        GlobalMembers.YGNodeInsertChild(root_child1, root_child1_child0, 0)
        val root_child1_child1 =
            createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 400, false)
        root_child1_child1.setBaselineFunc { node: YGNode?, width: Float, height: Float ->
            _baselineFunc(
                node,
                width,
                height
            )
        }
        GlobalMembers.YGNodeSetIsReferenceBaseline(root_child1_child1, true)
        GlobalMembers.YGNodeInsertChild(root_child1, root_child1_child1, 1)
        GlobalMembers.YGNodeCalculateLayout(
            root,
            GlobalMembers.YGUndefined,
            GlobalMembers.YGUndefined,
            YGDirection.YGDirectionLTR
        )
        assertEquals(900f, GlobalMembers.YGNodeLayoutGetHeight(root))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
        assertEquals(500f, GlobalMembers.YGNodeLayoutGetLeft(root_child1))
        assertEquals(400f, GlobalMembers.YGNodeLayoutGetTop(root_child1))
        assertEquals(500f, GlobalMembers.YGNodeLayoutGetHeight(root_child1))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child1_child0))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child1_child0))
        assertEquals(500f, GlobalMembers.YGNodeLayoutGetLeft(root_child1_child1))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child1_child1))
        GlobalMembers.YGNodeFreeRecursive(root)
        GlobalMembers.YGConfigFree(config)
    }

    @Test
    fun align_baseline_parent_using_child_in_column_as_reference_with_no_baseline_func_and_height_not_specified() {
        val config = GlobalMembers.YGConfigNew()
        val root = GlobalMembers.YGNodeNewWithConfig(config)
        GlobalMembers.YGNodeStyleSetFlexDirection(root, YGFlexDirection.YGFlexDirectionRow)
        GlobalMembers.YGNodeStyleSetAlignItems(root, YGAlign.YGAlignBaseline)
        GlobalMembers.YGNodeStyleSetWidth(root, 1000f)
        val root_child0 =
            createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 600, false)
        GlobalMembers.YGNodeInsertChild(root, root_child0, 0)
        val root_child1 = GlobalMembers.YGNodeNewWithConfig(config)
        GlobalMembers.YGNodeStyleSetFlexDirection(
            root_child1,
            YGFlexDirection.YGFlexDirectionColumn
        )
        GlobalMembers.YGNodeStyleSetWidth(root_child1, 500f)
        GlobalMembers.YGNodeInsertChild(root, root_child1, 1)
        val root_child1_child0 =
            createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 300, false)
        GlobalMembers.YGNodeInsertChild(root_child1, root_child1_child0, 0)
        val root_child1_child1 =
            createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 400, false)
        GlobalMembers.YGNodeSetIsReferenceBaseline(root_child1_child1, true)
        GlobalMembers.YGNodeInsertChild(root_child1, root_child1_child1, 1)
        GlobalMembers.YGNodeCalculateLayout(
            root,
            GlobalMembers.YGUndefined,
            GlobalMembers.YGUndefined,
            YGDirection.YGDirectionLTR
        )
        assertEquals(700f, GlobalMembers.YGNodeLayoutGetHeight(root))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
        assertEquals(100f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
        assertEquals(500f, GlobalMembers.YGNodeLayoutGetLeft(root_child1))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child1))
        assertEquals(700f, GlobalMembers.YGNodeLayoutGetHeight(root_child1))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child1_child0))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child1_child0))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child1_child1))
        assertEquals(300f, GlobalMembers.YGNodeLayoutGetTop(root_child1_child1))
        GlobalMembers.YGNodeFreeRecursive(root)
        GlobalMembers.YGConfigFree(config)
    }

    @Test
    fun align_baseline_parent_using_child_in_row_as_reference_with_no_baseline_func_and_height_not_specified() {
        val config = GlobalMembers.YGConfigNew()
        val root = GlobalMembers.YGNodeNewWithConfig(config)
        GlobalMembers.YGNodeStyleSetFlexDirection(root, YGFlexDirection.YGFlexDirectionRow)
        GlobalMembers.YGNodeStyleSetAlignItems(root, YGAlign.YGAlignBaseline)
        GlobalMembers.YGNodeStyleSetWidth(root, 1000f)
        val root_child0 =
            createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 600, false)
        GlobalMembers.YGNodeInsertChild(root, root_child0, 0)
        val root_child1 = GlobalMembers.YGNodeNewWithConfig(config)
        GlobalMembers.YGNodeStyleSetFlexDirection(root_child1, YGFlexDirection.YGFlexDirectionRow)
        GlobalMembers.YGNodeStyleSetWidth(root_child1, 500f)
        GlobalMembers.YGNodeInsertChild(root, root_child1, 1)
        val root_child1_child0 =
            createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 500, false)
        GlobalMembers.YGNodeInsertChild(root_child1, root_child1_child0, 0)
        val root_child1_child1 =
            createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 400, false)
        GlobalMembers.YGNodeSetIsReferenceBaseline(root_child1_child1, true)
        GlobalMembers.YGNodeInsertChild(root_child1, root_child1_child1, 1)
        GlobalMembers.YGNodeCalculateLayout(
            root,
            GlobalMembers.YGUndefined,
            GlobalMembers.YGUndefined,
            YGDirection.YGDirectionLTR
        )
        assertEquals(700f, GlobalMembers.YGNodeLayoutGetHeight(root))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
        assertEquals(500f, GlobalMembers.YGNodeLayoutGetLeft(root_child1))
        assertEquals(200f, GlobalMembers.YGNodeLayoutGetTop(root_child1))
        assertEquals(500f, GlobalMembers.YGNodeLayoutGetHeight(root_child1))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child1_child0))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child1_child0))
        assertEquals(500f, GlobalMembers.YGNodeLayoutGetLeft(root_child1_child1))
        assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child1_child1))
        GlobalMembers.YGNodeFreeRecursive(root)
        GlobalMembers.YGConfigFree(config)
    }

    companion object {
        private fun _baselineFunc(node: YGNode?, width: Float, height: Float): Float {
            return height / 2
        }

        private fun _measure1(
          node: YGNode?,
          width: Float,
          widthMode: YGMeasureMode?,
          height: Float,
          heightMode: YGMeasureMode?
        ): YGSize {
            return YGSize(42f, 50f)
        }

        private fun _measure2(
          node: YGNode?,
          width: Float,
          widthMode: YGMeasureMode?,
          height: Float,
          heightMode: YGMeasureMode?
        ): YGSize {
            return YGSize(279f, 126f)
        }

        private fun createYGNode(
          config: YGConfig,
          direction: YGFlexDirection,
          width: Int,
          height: Int,
          alignBaseline: Boolean
        ): YGNode {
            val node = GlobalMembers.YGNodeNewWithConfig(config)
            GlobalMembers.YGNodeStyleSetFlexDirection(node, direction)
            if (alignBaseline) {
                GlobalMembers.YGNodeStyleSetAlignItems(node, YGAlign.YGAlignBaseline)
            }
            GlobalMembers.YGNodeStyleSetWidth(node, width.toFloat())
            GlobalMembers.YGNodeStyleSetHeight(node, height.toFloat())
            return YGNode(node)
        }
    }
}
