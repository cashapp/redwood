/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package app.cash.redwood.yoga

import app.cash.redwood.yoga.enums.YGAlign
import app.cash.redwood.yoga.enums.YGDirection
import app.cash.redwood.yoga.enums.YGEdge
import app.cash.redwood.yoga.enums.YGFlexDirection
import app.cash.redwood.yoga.enums.YGMeasureMode
import kotlin.test.Test
import kotlin.test.assertEquals

class YGAlignBaselineTest {
  // Test case for bug in T32999822
  @Test
  fun align_baseline_parent_ht_not_specified() {
    val config = Yoga.YGConfigNew()
    val root = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetFlexDirection(root, YGFlexDirection.YGFlexDirectionRow)
    Yoga.YGNodeStyleSetAlignContent(root, YGAlign.YGAlignStretch)
    Yoga.YGNodeStyleSetAlignItems(root, YGAlign.YGAlignBaseline)
    Yoga.YGNodeStyleSetWidth(root, 340f)
    Yoga.YGNodeStyleSetMaxHeight(root, 170f)
    Yoga.YGNodeStyleSetMinHeight(root, 0f)
    val root_child0 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetFlexGrow(root_child0, 0f)
    Yoga.YGNodeStyleSetFlexShrink(root_child0, 1f)
    Yoga.YGNodeSetMeasureFunc(root_child0, ::_measure1)
    Yoga.YGNodeInsertChild(root, root_child0, 0)
    val root_child1 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetFlexGrow(root_child1, 0f)
    Yoga.YGNodeStyleSetFlexShrink(root_child1, 1f)
    Yoga.YGNodeSetMeasureFunc(root_child1, ::_measure2)
    Yoga.YGNodeInsertChild(root, root_child1, 1)
    Yoga.YGNodeCalculateLayout(
      root,
      Yoga.YGUndefined,
      Yoga.YGUndefined,
      YGDirection.YGDirectionLTR,
    )
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root))
    assertEquals(340f, Yoga.YGNodeLayoutGetWidth(root))
    assertEquals(126f, Yoga.YGNodeLayoutGetHeight(root))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(42f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetHeight(root_child0))
    assertEquals(76f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(42f, Yoga.YGNodeLayoutGetLeft(root_child1))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child1))
    assertEquals(279f, Yoga.YGNodeLayoutGetWidth(root_child1))
    assertEquals(126f, Yoga.YGNodeLayoutGetHeight(root_child1))
    Yoga.YGNodeFreeRecursive(root)
  }

  @Test
  fun align_baseline_with_no_parent_ht() {
    val config = Yoga.YGConfigNew()
    val root = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetFlexDirection(root, YGFlexDirection.YGFlexDirectionRow)
    Yoga.YGNodeStyleSetAlignItems(root, YGAlign.YGAlignBaseline)
    Yoga.YGNodeStyleSetWidth(root, 150f)
    val root_child0 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetWidth(root_child0, 50f)
    Yoga.YGNodeStyleSetHeight(root_child0, 50f)
    Yoga.YGNodeInsertChild(root, root_child0, 0)
    val root_child1 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetWidth(root_child1, 50f)
    Yoga.YGNodeStyleSetHeight(root_child1, 40f)
    Yoga.YGNodeSetBaselineFunc(root_child1, ::_baselineFunc)
    Yoga.YGNodeInsertChild(root, root_child1, 1)
    Yoga.YGNodeCalculateLayout(
      root,
      Yoga.YGUndefined,
      Yoga.YGUndefined,
      YGDirection.YGDirectionLTR,
    )
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root))
    assertEquals(150f, Yoga.YGNodeLayoutGetWidth(root))
    assertEquals(70f, Yoga.YGNodeLayoutGetHeight(root))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetHeight(root_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetLeft(root_child1))
    assertEquals(30f, Yoga.YGNodeLayoutGetTop(root_child1))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child1))
    assertEquals(40f, Yoga.YGNodeLayoutGetHeight(root_child1))
    Yoga.YGNodeFreeRecursive(root)
  }

  @Test
  fun align_baseline_with_no_baseline_func_and_no_parent_ht() {
    val config = Yoga.YGConfigNew()
    val root = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetFlexDirection(root, YGFlexDirection.YGFlexDirectionRow)
    Yoga.YGNodeStyleSetAlignItems(root, YGAlign.YGAlignBaseline)
    Yoga.YGNodeStyleSetWidth(root, 150f)
    val root_child0 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetWidth(root_child0, 50f)
    Yoga.YGNodeStyleSetHeight(root_child0, 80f)
    Yoga.YGNodeInsertChild(root, root_child0, 0)
    val root_child1 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetWidth(root_child1, 50f)
    Yoga.YGNodeStyleSetHeight(root_child1, 50f)
    Yoga.YGNodeInsertChild(root, root_child1, 1)
    Yoga.YGNodeCalculateLayout(
      root,
      Yoga.YGUndefined,
      Yoga.YGUndefined,
      YGDirection.YGDirectionLTR,
    )
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root))
    assertEquals(150f, Yoga.YGNodeLayoutGetWidth(root))
    assertEquals(80f, Yoga.YGNodeLayoutGetHeight(root))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(80f, Yoga.YGNodeLayoutGetHeight(root_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetLeft(root_child1))
    assertEquals(30f, Yoga.YGNodeLayoutGetTop(root_child1))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child1))
    assertEquals(50f, Yoga.YGNodeLayoutGetHeight(root_child1))
    Yoga.YGNodeFreeRecursive(root)
  }

  @Test
  fun align_baseline_parent_using_child_in_column_as_reference() {
    val config = Yoga.YGConfigNew()
    val root = createYGNode(config, YGFlexDirection.YGFlexDirectionRow, 1000, 1000, true)
    val root_child0 =
      createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 600, false)
    Yoga.YGNodeInsertChild(root, root_child0, 0)
    val root_child1 =
      createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 800, false)
    Yoga.YGNodeInsertChild(root, root_child1, 1)
    val root_child1_child0 =
      createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 300, false)
    Yoga.YGNodeInsertChild(root_child1, root_child1_child0, 0)
    val root_child1_child1 =
      createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 400, false)
    root_child1_child1.setBaselineFunc(::_baselineFunc)
    Yoga.YGNodeSetIsReferenceBaseline(root_child1_child1, true)
    Yoga.YGNodeInsertChild(root_child1, root_child1_child1, 1)
    Yoga.YGNodeCalculateLayout(
      root,
      Yoga.YGUndefined,
      Yoga.YGUndefined,
      YGDirection.YGDirectionLTR,
    )
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(500f, Yoga.YGNodeLayoutGetLeft(root_child1))
    assertEquals(100f, Yoga.YGNodeLayoutGetTop(root_child1))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child1_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child1_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child1_child1))
    assertEquals(300f, Yoga.YGNodeLayoutGetTop(root_child1_child1))
    Yoga.YGNodeFreeRecursive(root)
  }

  @Test
  fun align_baseline_parent_using_child_with_padding_in_column_as_reference() {
    val config = Yoga.YGConfigNew()
    val root = createYGNode(config, YGFlexDirection.YGFlexDirectionRow, 1000, 1000, true)
    val root_child0 =
      createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 600, false)
    Yoga.YGNodeInsertChild(root, root_child0, 0)
    val root_child1 =
      createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 800, false)
    Yoga.YGNodeInsertChild(root, root_child1, 1)
    val root_child1_child0 =
      createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 300, false)
    Yoga.YGNodeInsertChild(root_child1, root_child1_child0, 0)
    val root_child1_child1 =
      createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 400, false)
    root_child1_child1.setBaselineFunc(::_baselineFunc)
    Yoga.YGNodeSetIsReferenceBaseline(root_child1_child1, true)
    Yoga.YGNodeStyleSetPadding(root_child1_child1, YGEdge.YGEdgeLeft, 100f)
    Yoga.YGNodeStyleSetPadding(root_child1_child1, YGEdge.YGEdgeRight, 100f)
    Yoga.YGNodeStyleSetPadding(root_child1_child1, YGEdge.YGEdgeTop, 100f)
    Yoga.YGNodeStyleSetPadding(root_child1_child1, YGEdge.YGEdgeBottom, 100f)
    Yoga.YGNodeInsertChild(root_child1, root_child1_child1, 1)
    Yoga.YGNodeCalculateLayout(
      root,
      Yoga.YGUndefined,
      Yoga.YGUndefined,
      YGDirection.YGDirectionLTR,
    )
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(500f, Yoga.YGNodeLayoutGetLeft(root_child1))
    assertEquals(100f, Yoga.YGNodeLayoutGetTop(root_child1))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child1_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child1_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child1_child1))
    assertEquals(300f, Yoga.YGNodeLayoutGetTop(root_child1_child1))
    Yoga.YGNodeFreeRecursive(root)
  }

  @Test
  fun align_baseline_parent_with_padding_using_child_in_column_as_reference() {
    val config = Yoga.YGConfigNew()
    val root = createYGNode(config, YGFlexDirection.YGFlexDirectionRow, 1000, 1000, true)
    val root_child0 =
      createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 600, false)
    Yoga.YGNodeInsertChild(root, root_child0, 0)
    val root_child1 =
      createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 800, false)
    Yoga.YGNodeStyleSetPadding(root_child1, YGEdge.YGEdgeLeft, 100f)
    Yoga.YGNodeStyleSetPadding(root_child1, YGEdge.YGEdgeRight, 100f)
    Yoga.YGNodeStyleSetPadding(root_child1, YGEdge.YGEdgeTop, 100f)
    Yoga.YGNodeStyleSetPadding(root_child1, YGEdge.YGEdgeBottom, 100f)
    Yoga.YGNodeInsertChild(root, root_child1, 1)
    val root_child1_child0 =
      createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 300, false)
    Yoga.YGNodeInsertChild(root_child1, root_child1_child0, 0)
    val root_child1_child1 =
      createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 400, false)
    root_child1_child1.setBaselineFunc(::_baselineFunc)
    Yoga.YGNodeSetIsReferenceBaseline(root_child1_child1, true)
    Yoga.YGNodeInsertChild(root_child1, root_child1_child1, 1)
    Yoga.YGNodeCalculateLayout(
      root,
      Yoga.YGUndefined,
      Yoga.YGUndefined,
      YGDirection.YGDirectionLTR,
    )
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(500f, Yoga.YGNodeLayoutGetLeft(root_child1))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child1))
    assertEquals(100f, Yoga.YGNodeLayoutGetLeft(root_child1_child0))
    assertEquals(100f, Yoga.YGNodeLayoutGetTop(root_child1_child0))
    assertEquals(100f, Yoga.YGNodeLayoutGetLeft(root_child1_child1))
    assertEquals(400f, Yoga.YGNodeLayoutGetTop(root_child1_child1))
    Yoga.YGNodeFreeRecursive(root)
  }

  @Test
  fun align_baseline_parent_with_margin_using_child_in_column_as_reference() {
    val config = Yoga.YGConfigNew()
    val root = createYGNode(config, YGFlexDirection.YGFlexDirectionRow, 1000, 1000, true)
    val root_child0 =
      createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 600, false)
    Yoga.YGNodeInsertChild(root, root_child0, 0)
    val root_child1 =
      createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 800, false)
    Yoga.YGNodeStyleSetMargin(root_child1, YGEdge.YGEdgeLeft, 100f)
    Yoga.YGNodeStyleSetMargin(root_child1, YGEdge.YGEdgeRight, 100f)
    Yoga.YGNodeStyleSetMargin(root_child1, YGEdge.YGEdgeTop, 100f)
    Yoga.YGNodeStyleSetMargin(root_child1, YGEdge.YGEdgeBottom, 100f)
    Yoga.YGNodeInsertChild(root, root_child1, 1)
    val root_child1_child0 =
      createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 300, false)
    Yoga.YGNodeInsertChild(root_child1, root_child1_child0, 0)
    val root_child1_child1 =
      createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 400, false)
    root_child1_child1.setBaselineFunc(::_baselineFunc)
    Yoga.YGNodeSetIsReferenceBaseline(root_child1_child1, true)
    Yoga.YGNodeInsertChild(root_child1, root_child1_child1, 1)
    Yoga.YGNodeCalculateLayout(
      root,
      Yoga.YGUndefined,
      Yoga.YGUndefined,
      YGDirection.YGDirectionLTR,
    )
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(600f, Yoga.YGNodeLayoutGetLeft(root_child1))
    assertEquals(100f, Yoga.YGNodeLayoutGetTop(root_child1))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child1_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child1_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child1_child1))
    assertEquals(300f, Yoga.YGNodeLayoutGetTop(root_child1_child1))
    Yoga.YGNodeFreeRecursive(root)
  }

  @Test
  fun align_baseline_parent_using_child_with_margin_in_column_as_reference() {
    val config = Yoga.YGConfigNew()
    val root = createYGNode(config, YGFlexDirection.YGFlexDirectionRow, 1000, 1000, true)
    val root_child0 =
      createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 600, false)
    Yoga.YGNodeInsertChild(root, root_child0, 0)
    val root_child1 =
      createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 800, false)
    Yoga.YGNodeInsertChild(root, root_child1, 1)
    val root_child1_child0 =
      createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 300, false)
    Yoga.YGNodeInsertChild(root_child1, root_child1_child0, 0)
    val root_child1_child1 =
      createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 400, false)
    root_child1_child1.setBaselineFunc(::_baselineFunc)
    Yoga.YGNodeSetIsReferenceBaseline(root_child1_child1, true)
    Yoga.YGNodeStyleSetMargin(root_child1_child1, YGEdge.YGEdgeLeft, 100f)
    Yoga.YGNodeStyleSetMargin(root_child1_child1, YGEdge.YGEdgeRight, 100f)
    Yoga.YGNodeStyleSetMargin(root_child1_child1, YGEdge.YGEdgeTop, 100f)
    Yoga.YGNodeStyleSetMargin(root_child1_child1, YGEdge.YGEdgeBottom, 100f)
    Yoga.YGNodeInsertChild(root_child1, root_child1_child1, 1)
    Yoga.YGNodeCalculateLayout(
      root,
      Yoga.YGUndefined,
      Yoga.YGUndefined,
      YGDirection.YGDirectionLTR,
    )
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(500f, Yoga.YGNodeLayoutGetLeft(root_child1))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child1))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child1_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child1_child0))
    assertEquals(100f, Yoga.YGNodeLayoutGetLeft(root_child1_child1))
    assertEquals(400f, Yoga.YGNodeLayoutGetTop(root_child1_child1))
    Yoga.YGNodeFreeRecursive(root)
  }

  @Test
  fun align_baseline_parent_using_child_in_row_as_reference() {
    val config = Yoga.YGConfigNew()
    val root = createYGNode(config, YGFlexDirection.YGFlexDirectionRow, 1000, 1000, true)
    val root_child0 =
      createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 600, false)
    Yoga.YGNodeInsertChild(root, root_child0, 0)
    val root_child1 = createYGNode(config, YGFlexDirection.YGFlexDirectionRow, 500, 800, true)
    Yoga.YGNodeInsertChild(root, root_child1, 1)
    val root_child1_child0 =
      createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 500, false)
    Yoga.YGNodeInsertChild(root_child1, root_child1_child0, 0)
    val root_child1_child1 =
      createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 400, false)
    root_child1_child1.setBaselineFunc(::_baselineFunc)
    Yoga.YGNodeSetIsReferenceBaseline(root_child1_child1, true)
    Yoga.YGNodeInsertChild(root_child1, root_child1_child1, 1)
    Yoga.YGNodeCalculateLayout(
      root,
      Yoga.YGUndefined,
      Yoga.YGUndefined,
      YGDirection.YGDirectionLTR,
    )
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(500f, Yoga.YGNodeLayoutGetLeft(root_child1))
    assertEquals(100f, Yoga.YGNodeLayoutGetTop(root_child1))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child1_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child1_child0))
    assertEquals(500f, Yoga.YGNodeLayoutGetLeft(root_child1_child1))
    assertEquals(300f, Yoga.YGNodeLayoutGetTop(root_child1_child1))
    Yoga.YGNodeFreeRecursive(root)
  }

  @Test
  fun align_baseline_parent_using_child_with_padding_in_row_as_reference() {
    val config = Yoga.YGConfigNew()
    val root = createYGNode(config, YGFlexDirection.YGFlexDirectionRow, 1000, 1000, true)
    val root_child0 =
      createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 600, false)
    Yoga.YGNodeInsertChild(root, root_child0, 0)
    val root_child1 = createYGNode(config, YGFlexDirection.YGFlexDirectionRow, 500, 800, true)
    Yoga.YGNodeInsertChild(root, root_child1, 1)
    val root_child1_child0 =
      createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 500, false)
    Yoga.YGNodeInsertChild(root_child1, root_child1_child0, 0)
    val root_child1_child1 =
      createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 400, false)
    root_child1_child1.setBaselineFunc(::_baselineFunc)
    Yoga.YGNodeSetIsReferenceBaseline(root_child1_child1, true)
    Yoga.YGNodeStyleSetPadding(root_child1_child1, YGEdge.YGEdgeLeft, 100f)
    Yoga.YGNodeStyleSetPadding(root_child1_child1, YGEdge.YGEdgeRight, 100f)
    Yoga.YGNodeStyleSetPadding(root_child1_child1, YGEdge.YGEdgeTop, 100f)
    Yoga.YGNodeStyleSetPadding(root_child1_child1, YGEdge.YGEdgeBottom, 100f)
    Yoga.YGNodeInsertChild(root_child1, root_child1_child1, 1)
    Yoga.YGNodeCalculateLayout(
      root,
      Yoga.YGUndefined,
      Yoga.YGUndefined,
      YGDirection.YGDirectionLTR,
    )
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(500f, Yoga.YGNodeLayoutGetLeft(root_child1))
    assertEquals(100f, Yoga.YGNodeLayoutGetTop(root_child1))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child1_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child1_child0))
    assertEquals(500f, Yoga.YGNodeLayoutGetLeft(root_child1_child1))
    assertEquals(300f, Yoga.YGNodeLayoutGetTop(root_child1_child1))
    Yoga.YGNodeFreeRecursive(root)
  }

  @Test
  fun align_baseline_parent_using_child_with_margin_in_row_as_reference() {
    val config = Yoga.YGConfigNew()
    val root = createYGNode(config, YGFlexDirection.YGFlexDirectionRow, 1000, 1000, true)
    val root_child0 =
      createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 600, false)
    Yoga.YGNodeInsertChild(root, root_child0, 0)
    val root_child1 = createYGNode(config, YGFlexDirection.YGFlexDirectionRow, 500, 800, true)
    Yoga.YGNodeInsertChild(root, root_child1, 1)
    val root_child1_child0 =
      createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 500, false)
    Yoga.YGNodeInsertChild(root_child1, root_child1_child0, 0)
    val root_child1_child1 =
      createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 400, false)
    root_child1_child1.setBaselineFunc(::_baselineFunc)
    Yoga.YGNodeSetIsReferenceBaseline(root_child1_child1, true)
    Yoga.YGNodeStyleSetMargin(root_child1_child1, YGEdge.YGEdgeLeft, 100f)
    Yoga.YGNodeStyleSetMargin(root_child1_child1, YGEdge.YGEdgeRight, 100f)
    Yoga.YGNodeStyleSetMargin(root_child1_child1, YGEdge.YGEdgeTop, 100f)
    Yoga.YGNodeStyleSetMargin(root_child1_child1, YGEdge.YGEdgeBottom, 100f)
    Yoga.YGNodeInsertChild(root_child1, root_child1_child1, 1)
    Yoga.YGNodeCalculateLayout(
      root,
      Yoga.YGUndefined,
      Yoga.YGUndefined,
      YGDirection.YGDirectionLTR,
    )
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(500f, Yoga.YGNodeLayoutGetLeft(root_child1))
    assertEquals(100f, Yoga.YGNodeLayoutGetTop(root_child1))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child1_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child1_child0))
    assertEquals(600f, Yoga.YGNodeLayoutGetLeft(root_child1_child1))
    assertEquals(300f, Yoga.YGNodeLayoutGetTop(root_child1_child1))
    Yoga.YGNodeFreeRecursive(root)
  }

  @Test
  fun align_baseline_parent_using_child_in_column_as_reference_with_no_baseline_func() {
    val config = Yoga.YGConfigNew()
    val root = createYGNode(config, YGFlexDirection.YGFlexDirectionRow, 1000, 1000, true)
    val root_child0 =
      createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 600, false)
    Yoga.YGNodeInsertChild(root, root_child0, 0)
    val root_child1 =
      createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 800, false)
    Yoga.YGNodeInsertChild(root, root_child1, 1)
    val root_child1_child0 =
      createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 300, false)
    Yoga.YGNodeInsertChild(root_child1, root_child1_child0, 0)
    val root_child1_child1 =
      createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 400, false)
    Yoga.YGNodeSetIsReferenceBaseline(root_child1_child1, true)
    Yoga.YGNodeInsertChild(root_child1, root_child1_child1, 1)
    Yoga.YGNodeCalculateLayout(
      root,
      Yoga.YGUndefined,
      Yoga.YGUndefined,
      YGDirection.YGDirectionLTR,
    )
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(100f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(500f, Yoga.YGNodeLayoutGetLeft(root_child1))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child1))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child1_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child1_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child1_child1))
    assertEquals(300f, Yoga.YGNodeLayoutGetTop(root_child1_child1))
    Yoga.YGNodeFreeRecursive(root)
  }

  @Test
  fun align_baseline_parent_using_child_in_row_as_reference_with_no_baseline_func() {
    val config = Yoga.YGConfigNew()
    val root = createYGNode(config, YGFlexDirection.YGFlexDirectionRow, 1000, 1000, true)
    val root_child0 =
      createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 600, false)
    Yoga.YGNodeInsertChild(root, root_child0, 0)
    val root_child1 = createYGNode(config, YGFlexDirection.YGFlexDirectionRow, 500, 800, true)
    Yoga.YGNodeInsertChild(root, root_child1, 1)
    val root_child1_child0 =
      createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 500, false)
    Yoga.YGNodeInsertChild(root_child1, root_child1_child0, 0)
    val root_child1_child1 =
      createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 400, false)
    Yoga.YGNodeSetIsReferenceBaseline(root_child1_child1, true)
    Yoga.YGNodeInsertChild(root_child1, root_child1_child1, 1)
    Yoga.YGNodeCalculateLayout(
      root,
      Yoga.YGUndefined,
      Yoga.YGUndefined,
      YGDirection.YGDirectionLTR,
    )
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(500f, Yoga.YGNodeLayoutGetLeft(root_child1))
    assertEquals(100f, Yoga.YGNodeLayoutGetTop(root_child1))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child1_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child1_child0))
    assertEquals(500f, Yoga.YGNodeLayoutGetLeft(root_child1_child1))
    assertEquals(100f, Yoga.YGNodeLayoutGetTop(root_child1_child1))
    Yoga.YGNodeFreeRecursive(root)
  }

  @Test
  fun align_baseline_parent_using_child_in_column_as_reference_with_height_not_specified() {
    val config = Yoga.YGConfigNew()
    val root = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetFlexDirection(root, YGFlexDirection.YGFlexDirectionRow)
    Yoga.YGNodeStyleSetAlignItems(root, YGAlign.YGAlignBaseline)
    Yoga.YGNodeStyleSetWidth(root, 1000f)
    val root_child0 =
      createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 600, false)
    Yoga.YGNodeInsertChild(root, root_child0, 0)
    val root_child1 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetFlexDirection(
      root_child1,
      YGFlexDirection.YGFlexDirectionColumn,
    )
    Yoga.YGNodeStyleSetWidth(root_child1, 500f)
    Yoga.YGNodeInsertChild(root, root_child1, 1)
    val root_child1_child0 =
      createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 300, false)
    Yoga.YGNodeInsertChild(root_child1, root_child1_child0, 0)
    val root_child1_child1 =
      createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 400, false)
    root_child1_child1.setBaselineFunc(::_baselineFunc)
    Yoga.YGNodeSetIsReferenceBaseline(root_child1_child1, true)
    Yoga.YGNodeInsertChild(root_child1, root_child1_child1, 1)
    Yoga.YGNodeCalculateLayout(
      root,
      Yoga.YGUndefined,
      Yoga.YGUndefined,
      YGDirection.YGDirectionLTR,
    )
    assertEquals(800f, Yoga.YGNodeLayoutGetHeight(root))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(500f, Yoga.YGNodeLayoutGetLeft(root_child1))
    assertEquals(100f, Yoga.YGNodeLayoutGetTop(root_child1))
    assertEquals(700f, Yoga.YGNodeLayoutGetHeight(root_child1))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child1_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child1_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child1_child1))
    assertEquals(300f, Yoga.YGNodeLayoutGetTop(root_child1_child1))
    Yoga.YGNodeFreeRecursive(root)
  }

  @Test
  fun align_baseline_parent_using_child_in_row_as_reference_with_height_not_specified() {
    val config = Yoga.YGConfigNew()
    val root = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetFlexDirection(root, YGFlexDirection.YGFlexDirectionRow)
    Yoga.YGNodeStyleSetAlignItems(root, YGAlign.YGAlignBaseline)
    Yoga.YGNodeStyleSetWidth(root, 1000f)
    val root_child0 =
      createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 600, false)
    Yoga.YGNodeInsertChild(root, root_child0, 0)
    val root_child1 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetFlexDirection(root_child1, YGFlexDirection.YGFlexDirectionRow)
    Yoga.YGNodeStyleSetWidth(root_child1, 500f)
    Yoga.YGNodeInsertChild(root, root_child1, 1)
    val root_child1_child0 =
      createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 500, false)
    Yoga.YGNodeInsertChild(root_child1, root_child1_child0, 0)
    val root_child1_child1 =
      createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 400, false)
    root_child1_child1.setBaselineFunc(::_baselineFunc)
    Yoga.YGNodeSetIsReferenceBaseline(root_child1_child1, true)
    Yoga.YGNodeInsertChild(root_child1, root_child1_child1, 1)
    Yoga.YGNodeCalculateLayout(
      root,
      Yoga.YGUndefined,
      Yoga.YGUndefined,
      YGDirection.YGDirectionLTR,
    )
    assertEquals(900f, Yoga.YGNodeLayoutGetHeight(root))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(500f, Yoga.YGNodeLayoutGetLeft(root_child1))
    assertEquals(400f, Yoga.YGNodeLayoutGetTop(root_child1))
    assertEquals(500f, Yoga.YGNodeLayoutGetHeight(root_child1))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child1_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child1_child0))
    assertEquals(500f, Yoga.YGNodeLayoutGetLeft(root_child1_child1))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child1_child1))
    Yoga.YGNodeFreeRecursive(root)
  }

  @Test
  fun align_baseline_parent_using_child_in_column_as_reference_with_no_baseline_func_and_height_not_specified() {
    val config = Yoga.YGConfigNew()
    val root = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetFlexDirection(root, YGFlexDirection.YGFlexDirectionRow)
    Yoga.YGNodeStyleSetAlignItems(root, YGAlign.YGAlignBaseline)
    Yoga.YGNodeStyleSetWidth(root, 1000f)
    val root_child0 =
      createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 600, false)
    Yoga.YGNodeInsertChild(root, root_child0, 0)
    val root_child1 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetFlexDirection(
      root_child1,
      YGFlexDirection.YGFlexDirectionColumn,
    )
    Yoga.YGNodeStyleSetWidth(root_child1, 500f)
    Yoga.YGNodeInsertChild(root, root_child1, 1)
    val root_child1_child0 =
      createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 300, false)
    Yoga.YGNodeInsertChild(root_child1, root_child1_child0, 0)
    val root_child1_child1 =
      createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 400, false)
    Yoga.YGNodeSetIsReferenceBaseline(root_child1_child1, true)
    Yoga.YGNodeInsertChild(root_child1, root_child1_child1, 1)
    Yoga.YGNodeCalculateLayout(
      root,
      Yoga.YGUndefined,
      Yoga.YGUndefined,
      YGDirection.YGDirectionLTR,
    )
    assertEquals(700f, Yoga.YGNodeLayoutGetHeight(root))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(100f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(500f, Yoga.YGNodeLayoutGetLeft(root_child1))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child1))
    assertEquals(700f, Yoga.YGNodeLayoutGetHeight(root_child1))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child1_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child1_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child1_child1))
    assertEquals(300f, Yoga.YGNodeLayoutGetTop(root_child1_child1))
    Yoga.YGNodeFreeRecursive(root)
  }

  @Test
  fun align_baseline_parent_using_child_in_row_as_reference_with_no_baseline_func_and_height_not_specified() {
    val config = Yoga.YGConfigNew()
    val root = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetFlexDirection(root, YGFlexDirection.YGFlexDirectionRow)
    Yoga.YGNodeStyleSetAlignItems(root, YGAlign.YGAlignBaseline)
    Yoga.YGNodeStyleSetWidth(root, 1000f)
    val root_child0 =
      createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 600, false)
    Yoga.YGNodeInsertChild(root, root_child0, 0)
    val root_child1 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetFlexDirection(root_child1, YGFlexDirection.YGFlexDirectionRow)
    Yoga.YGNodeStyleSetWidth(root_child1, 500f)
    Yoga.YGNodeInsertChild(root, root_child1, 1)
    val root_child1_child0 =
      createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 500, false)
    Yoga.YGNodeInsertChild(root_child1, root_child1_child0, 0)
    val root_child1_child1 =
      createYGNode(config, YGFlexDirection.YGFlexDirectionColumn, 500, 400, false)
    Yoga.YGNodeSetIsReferenceBaseline(root_child1_child1, true)
    Yoga.YGNodeInsertChild(root_child1, root_child1_child1, 1)
    Yoga.YGNodeCalculateLayout(
      root,
      Yoga.YGUndefined,
      Yoga.YGUndefined,
      YGDirection.YGDirectionLTR,
    )
    assertEquals(700f, Yoga.YGNodeLayoutGetHeight(root))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(500f, Yoga.YGNodeLayoutGetLeft(root_child1))
    assertEquals(200f, Yoga.YGNodeLayoutGetTop(root_child1))
    assertEquals(500f, Yoga.YGNodeLayoutGetHeight(root_child1))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child1_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child1_child0))
    assertEquals(500f, Yoga.YGNodeLayoutGetLeft(root_child1_child1))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child1_child1))
    Yoga.YGNodeFreeRecursive(root)
  }

  @Suppress("UNUSED_PARAMETER")
  companion object {
    private fun _baselineFunc(
      node: YGNode?,
      width: Float,
      height: Float,
    ): Float {
      return height / 2
    }

    private fun _measure1(
      node: YGNode?,
      width: Float,
      widthMode: YGMeasureMode?,
      height: Float,
      heightMode: YGMeasureMode?,
    ): YGSize {
      return YGSize(42f, 50f)
    }

    private fun _measure2(
      node: YGNode?,
      width: Float,
      widthMode: YGMeasureMode?,
      height: Float,
      heightMode: YGMeasureMode?,
    ): YGSize {
      return YGSize(279f, 126f)
    }

    private fun createYGNode(
      config: YGConfig,
      direction: YGFlexDirection,
      width: Int,
      height: Int,
      alignBaseline: Boolean,
    ): YGNode {
      val node = Yoga.YGNodeNewWithConfig(config)
      Yoga.YGNodeStyleSetFlexDirection(node, direction)
      if (alignBaseline) {
        Yoga.YGNodeStyleSetAlignItems(node, YGAlign.YGAlignBaseline)
      }
      Yoga.YGNodeStyleSetWidth(node, width.toFloat())
      Yoga.YGNodeStyleSetHeight(node, height.toFloat())
      return YGNode(node)
    }
  }
}
