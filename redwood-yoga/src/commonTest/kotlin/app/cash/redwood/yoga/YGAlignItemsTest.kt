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
import app.cash.redwood.yoga.enums.YGWrap
import kotlin.test.Test
import kotlin.test.assertEquals

class YGAlignItemsTest {
  @Test
  fun align_items_stretch() {
    val config = GlobalMembers.YGConfigNew()
    val root = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetWidth(root, 100f)
    GlobalMembers.YGNodeStyleSetHeight(root, 100f)
    val root_child0 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetHeight(root_child0, 10f)
    GlobalMembers.YGNodeInsertChild(root, root_child0, 0)
    GlobalMembers.YGNodeCalculateLayout(
      root,
      GlobalMembers.YGUndefined,
      GlobalMembers.YGUndefined,
      YGDirection.YGDirectionLTR,
    )
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetWidth(root))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetHeight(root))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
    assertEquals(10f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
    GlobalMembers.YGNodeCalculateLayout(
      root,
      GlobalMembers.YGUndefined,
      GlobalMembers.YGUndefined,
      YGDirection.YGDirectionRTL,
    )
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetWidth(root))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetHeight(root))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
    assertEquals(10f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
    GlobalMembers.YGNodeFreeRecursive(root)
    GlobalMembers.YGConfigFree(config)
  }

  @Test
  fun align_items_center() {
    val config = GlobalMembers.YGConfigNew()
    val root = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetAlignItems(root, YGAlign.YGAlignCenter)
    GlobalMembers.YGNodeStyleSetWidth(root, 100f)
    GlobalMembers.YGNodeStyleSetHeight(root, 100f)
    val root_child0 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetWidth(root_child0, 10f)
    GlobalMembers.YGNodeStyleSetHeight(root_child0, 10f)
    GlobalMembers.YGNodeInsertChild(root, root_child0, 0)
    GlobalMembers.YGNodeCalculateLayout(
      root,
      GlobalMembers.YGUndefined,
      GlobalMembers.YGUndefined,
      YGDirection.YGDirectionLTR,
    )
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetWidth(root))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetHeight(root))
    assertEquals(45f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
    assertEquals(10f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
    assertEquals(10f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
    GlobalMembers.YGNodeCalculateLayout(
      root,
      GlobalMembers.YGUndefined,
      GlobalMembers.YGUndefined,
      YGDirection.YGDirectionRTL,
    )
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetWidth(root))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetHeight(root))
    assertEquals(45f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
    assertEquals(10f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
    assertEquals(10f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
    GlobalMembers.YGNodeFreeRecursive(root)
    GlobalMembers.YGConfigFree(config)
  }

  @Test
  fun align_items_flex_start() {
    val config = GlobalMembers.YGConfigNew()
    val root = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetAlignItems(root, YGAlign.YGAlignFlexStart)
    GlobalMembers.YGNodeStyleSetWidth(root, 100f)
    GlobalMembers.YGNodeStyleSetHeight(root, 100f)
    val root_child0 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetWidth(root_child0, 10f)
    GlobalMembers.YGNodeStyleSetHeight(root_child0, 10f)
    GlobalMembers.YGNodeInsertChild(root, root_child0, 0)
    GlobalMembers.YGNodeCalculateLayout(
      root,
      GlobalMembers.YGUndefined,
      GlobalMembers.YGUndefined,
      YGDirection.YGDirectionLTR,
    )
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetWidth(root))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetHeight(root))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
    assertEquals(10f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
    assertEquals(10f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
    GlobalMembers.YGNodeCalculateLayout(
      root,
      GlobalMembers.YGUndefined,
      GlobalMembers.YGUndefined,
      YGDirection.YGDirectionRTL,
    )
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetWidth(root))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetHeight(root))
    assertEquals(90f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
    assertEquals(10f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
    assertEquals(10f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
    GlobalMembers.YGNodeFreeRecursive(root)
    GlobalMembers.YGConfigFree(config)
  }

  @Test
  fun align_items_flex_end() {
    val config = GlobalMembers.YGConfigNew()
    val root = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetAlignItems(root, YGAlign.YGAlignFlexEnd)
    GlobalMembers.YGNodeStyleSetWidth(root, 100f)
    GlobalMembers.YGNodeStyleSetHeight(root, 100f)
    val root_child0 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetWidth(root_child0, 10f)
    GlobalMembers.YGNodeStyleSetHeight(root_child0, 10f)
    GlobalMembers.YGNodeInsertChild(root, root_child0, 0)
    GlobalMembers.YGNodeCalculateLayout(
      root,
      GlobalMembers.YGUndefined,
      GlobalMembers.YGUndefined,
      YGDirection.YGDirectionLTR,
    )
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetWidth(root))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetHeight(root))
    assertEquals(90f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
    assertEquals(10f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
    assertEquals(10f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
    GlobalMembers.YGNodeCalculateLayout(
      root,
      GlobalMembers.YGUndefined,
      GlobalMembers.YGUndefined,
      YGDirection.YGDirectionRTL,
    )
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetWidth(root))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetHeight(root))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
    assertEquals(10f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
    assertEquals(10f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
    GlobalMembers.YGNodeFreeRecursive(root)
    GlobalMembers.YGConfigFree(config)
  }

  @Test
  fun align_baseline() {
    val config = GlobalMembers.YGConfigNew()
    val root = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetFlexDirection(root, YGFlexDirection.YGFlexDirectionRow)
    GlobalMembers.YGNodeStyleSetAlignItems(root, YGAlign.YGAlignBaseline)
    GlobalMembers.YGNodeStyleSetWidth(root, 100f)
    GlobalMembers.YGNodeStyleSetHeight(root, 100f)
    val root_child0 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetWidth(root_child0, 50f)
    GlobalMembers.YGNodeStyleSetHeight(root_child0, 50f)
    GlobalMembers.YGNodeInsertChild(root, root_child0, 0)
    val root_child1 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetWidth(root_child1, 50f)
    GlobalMembers.YGNodeStyleSetHeight(root_child1, 20f)
    GlobalMembers.YGNodeInsertChild(root, root_child1, 1)
    GlobalMembers.YGNodeCalculateLayout(
      root,
      GlobalMembers.YGUndefined,
      GlobalMembers.YGUndefined,
      YGDirection.YGDirectionLTR,
    )
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetWidth(root))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetHeight(root))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetLeft(root_child1))
    assertEquals(30f, GlobalMembers.YGNodeLayoutGetTop(root_child1))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child1))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetHeight(root_child1))
    GlobalMembers.YGNodeCalculateLayout(
      root,
      GlobalMembers.YGUndefined,
      GlobalMembers.YGUndefined,
      YGDirection.YGDirectionRTL,
    )
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetWidth(root))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetHeight(root))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child1))
    assertEquals(30f, GlobalMembers.YGNodeLayoutGetTop(root_child1))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child1))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetHeight(root_child1))
    GlobalMembers.YGNodeFreeRecursive(root)
    GlobalMembers.YGConfigFree(config)
  }

  @Test
  fun align_baseline_child() {
    val config = GlobalMembers.YGConfigNew()
    val root = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetFlexDirection(root, YGFlexDirection.YGFlexDirectionRow)
    GlobalMembers.YGNodeStyleSetAlignItems(root, YGAlign.YGAlignBaseline)
    GlobalMembers.YGNodeStyleSetWidth(root, 100f)
    GlobalMembers.YGNodeStyleSetHeight(root, 100f)
    val root_child0 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetWidth(root_child0, 50f)
    GlobalMembers.YGNodeStyleSetHeight(root_child0, 50f)
    GlobalMembers.YGNodeInsertChild(root, root_child0, 0)
    val root_child1 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetWidth(root_child1, 50f)
    GlobalMembers.YGNodeStyleSetHeight(root_child1, 20f)
    GlobalMembers.YGNodeInsertChild(root, root_child1, 1)
    val root_child1_child0 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetWidth(root_child1_child0, 50f)
    GlobalMembers.YGNodeStyleSetHeight(root_child1_child0, 10f)
    GlobalMembers.YGNodeInsertChild(root_child1, root_child1_child0, 0)
    GlobalMembers.YGNodeCalculateLayout(
      root,
      GlobalMembers.YGUndefined,
      GlobalMembers.YGUndefined,
      YGDirection.YGDirectionLTR,
    )
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetWidth(root))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetHeight(root))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetLeft(root_child1))
    assertEquals(40f, GlobalMembers.YGNodeLayoutGetTop(root_child1))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child1))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetHeight(root_child1))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child1_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child1_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child1_child0))
    assertEquals(10f, GlobalMembers.YGNodeLayoutGetHeight(root_child1_child0))
    GlobalMembers.YGNodeCalculateLayout(
      root,
      GlobalMembers.YGUndefined,
      GlobalMembers.YGUndefined,
      YGDirection.YGDirectionRTL,
    )
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetWidth(root))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetHeight(root))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child1))
    assertEquals(40f, GlobalMembers.YGNodeLayoutGetTop(root_child1))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child1))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetHeight(root_child1))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child1_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child1_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child1_child0))
    assertEquals(10f, GlobalMembers.YGNodeLayoutGetHeight(root_child1_child0))
    GlobalMembers.YGNodeFreeRecursive(root)
    GlobalMembers.YGConfigFree(config)
  }

  @Test
  fun align_baseline_child_multiline() {
    val config = GlobalMembers.YGConfigNew()
    val root = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetFlexDirection(root, YGFlexDirection.YGFlexDirectionRow)
    GlobalMembers.YGNodeStyleSetAlignItems(root, YGAlign.YGAlignBaseline)
    GlobalMembers.YGNodeStyleSetWidth(root, 100f)
    GlobalMembers.YGNodeStyleSetHeight(root, 100f)
    val root_child0 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetWidth(root_child0, 50f)
    GlobalMembers.YGNodeStyleSetHeight(root_child0, 60f)
    GlobalMembers.YGNodeInsertChild(root, root_child0, 0)
    val root_child1 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetFlexDirection(root_child1, YGFlexDirection.YGFlexDirectionRow)
    GlobalMembers.YGNodeStyleSetFlexWrap(root_child1, YGWrap.YGWrapWrap)
    GlobalMembers.YGNodeStyleSetWidth(root_child1, 50f)
    GlobalMembers.YGNodeStyleSetHeight(root_child1, 25f)
    GlobalMembers.YGNodeInsertChild(root, root_child1, 1)
    val root_child1_child0 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetWidth(root_child1_child0, 25f)
    GlobalMembers.YGNodeStyleSetHeight(root_child1_child0, 20f)
    GlobalMembers.YGNodeInsertChild(root_child1, root_child1_child0, 0)
    val root_child1_child1 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetWidth(root_child1_child1, 25f)
    GlobalMembers.YGNodeStyleSetHeight(root_child1_child1, 10f)
    GlobalMembers.YGNodeInsertChild(root_child1, root_child1_child1, 1)
    val root_child1_child2 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetWidth(root_child1_child2, 25f)
    GlobalMembers.YGNodeStyleSetHeight(root_child1_child2, 20f)
    GlobalMembers.YGNodeInsertChild(root_child1, root_child1_child2, 2)
    val root_child1_child3 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetWidth(root_child1_child3, 25f)
    GlobalMembers.YGNodeStyleSetHeight(root_child1_child3, 10f)
    GlobalMembers.YGNodeInsertChild(root_child1, root_child1_child3, 3)
    GlobalMembers.YGNodeCalculateLayout(
      root,
      GlobalMembers.YGUndefined,
      GlobalMembers.YGUndefined,
      YGDirection.YGDirectionLTR,
    )
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetWidth(root))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetHeight(root))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
    assertEquals(60f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetLeft(root_child1))
    assertEquals(40f, GlobalMembers.YGNodeLayoutGetTop(root_child1))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child1))
    assertEquals(25f, GlobalMembers.YGNodeLayoutGetHeight(root_child1))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child1_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child1_child0))
    assertEquals(25f, GlobalMembers.YGNodeLayoutGetWidth(root_child1_child0))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetHeight(root_child1_child0))
    assertEquals(25f, GlobalMembers.YGNodeLayoutGetLeft(root_child1_child1))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child1_child1))
    assertEquals(25f, GlobalMembers.YGNodeLayoutGetWidth(root_child1_child1))
    assertEquals(10f, GlobalMembers.YGNodeLayoutGetHeight(root_child1_child1))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child1_child2))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetTop(root_child1_child2))
    assertEquals(25f, GlobalMembers.YGNodeLayoutGetWidth(root_child1_child2))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetHeight(root_child1_child2))
    assertEquals(25f, GlobalMembers.YGNodeLayoutGetLeft(root_child1_child3))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetTop(root_child1_child3))
    assertEquals(25f, GlobalMembers.YGNodeLayoutGetWidth(root_child1_child3))
    assertEquals(10f, GlobalMembers.YGNodeLayoutGetHeight(root_child1_child3))
    GlobalMembers.YGNodeCalculateLayout(
      root,
      GlobalMembers.YGUndefined,
      GlobalMembers.YGUndefined,
      YGDirection.YGDirectionRTL,
    )
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetWidth(root))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetHeight(root))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
    assertEquals(60f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child1))
    assertEquals(40f, GlobalMembers.YGNodeLayoutGetTop(root_child1))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child1))
    assertEquals(25f, GlobalMembers.YGNodeLayoutGetHeight(root_child1))
    assertEquals(25f, GlobalMembers.YGNodeLayoutGetLeft(root_child1_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child1_child0))
    assertEquals(25f, GlobalMembers.YGNodeLayoutGetWidth(root_child1_child0))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetHeight(root_child1_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child1_child1))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child1_child1))
    assertEquals(25f, GlobalMembers.YGNodeLayoutGetWidth(root_child1_child1))
    assertEquals(10f, GlobalMembers.YGNodeLayoutGetHeight(root_child1_child1))
    assertEquals(25f, GlobalMembers.YGNodeLayoutGetLeft(root_child1_child2))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetTop(root_child1_child2))
    assertEquals(25f, GlobalMembers.YGNodeLayoutGetWidth(root_child1_child2))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetHeight(root_child1_child2))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child1_child3))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetTop(root_child1_child3))
    assertEquals(25f, GlobalMembers.YGNodeLayoutGetWidth(root_child1_child3))
    assertEquals(10f, GlobalMembers.YGNodeLayoutGetHeight(root_child1_child3))
    GlobalMembers.YGNodeFreeRecursive(root)
    GlobalMembers.YGConfigFree(config)
  }

  @Test
  fun align_baseline_child_multiline_override() {
    val config = GlobalMembers.YGConfigNew()
    val root = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetFlexDirection(root, YGFlexDirection.YGFlexDirectionRow)
    GlobalMembers.YGNodeStyleSetAlignItems(root, YGAlign.YGAlignBaseline)
    GlobalMembers.YGNodeStyleSetWidth(root, 100f)
    GlobalMembers.YGNodeStyleSetHeight(root, 100f)
    val root_child0 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetWidth(root_child0, 50f)
    GlobalMembers.YGNodeStyleSetHeight(root_child0, 60f)
    GlobalMembers.YGNodeInsertChild(root, root_child0, 0)
    val root_child1 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetFlexDirection(root_child1, YGFlexDirection.YGFlexDirectionRow)
    GlobalMembers.YGNodeStyleSetFlexWrap(root_child1, YGWrap.YGWrapWrap)
    GlobalMembers.YGNodeStyleSetWidth(root_child1, 50f)
    GlobalMembers.YGNodeStyleSetHeight(root_child1, 25f)
    GlobalMembers.YGNodeInsertChild(root, root_child1, 1)
    val root_child1_child0 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetWidth(root_child1_child0, 25f)
    GlobalMembers.YGNodeStyleSetHeight(root_child1_child0, 20f)
    GlobalMembers.YGNodeInsertChild(root_child1, root_child1_child0, 0)
    val root_child1_child1 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetAlignSelf(root_child1_child1, YGAlign.YGAlignBaseline)
    GlobalMembers.YGNodeStyleSetWidth(root_child1_child1, 25f)
    GlobalMembers.YGNodeStyleSetHeight(root_child1_child1, 10f)
    GlobalMembers.YGNodeInsertChild(root_child1, root_child1_child1, 1)
    val root_child1_child2 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetWidth(root_child1_child2, 25f)
    GlobalMembers.YGNodeStyleSetHeight(root_child1_child2, 20f)
    GlobalMembers.YGNodeInsertChild(root_child1, root_child1_child2, 2)
    val root_child1_child3 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetAlignSelf(root_child1_child3, YGAlign.YGAlignBaseline)
    GlobalMembers.YGNodeStyleSetWidth(root_child1_child3, 25f)
    GlobalMembers.YGNodeStyleSetHeight(root_child1_child3, 10f)
    GlobalMembers.YGNodeInsertChild(root_child1, root_child1_child3, 3)
    GlobalMembers.YGNodeCalculateLayout(
      root,
      GlobalMembers.YGUndefined,
      GlobalMembers.YGUndefined,
      YGDirection.YGDirectionLTR,
    )
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetWidth(root))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetHeight(root))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
    assertEquals(60f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetLeft(root_child1))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetTop(root_child1))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child1))
    assertEquals(25f, GlobalMembers.YGNodeLayoutGetHeight(root_child1))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child1_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child1_child0))
    assertEquals(25f, GlobalMembers.YGNodeLayoutGetWidth(root_child1_child0))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetHeight(root_child1_child0))
    assertEquals(25f, GlobalMembers.YGNodeLayoutGetLeft(root_child1_child1))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child1_child1))
    assertEquals(25f, GlobalMembers.YGNodeLayoutGetWidth(root_child1_child1))
    assertEquals(10f, GlobalMembers.YGNodeLayoutGetHeight(root_child1_child1))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child1_child2))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetTop(root_child1_child2))
    assertEquals(25f, GlobalMembers.YGNodeLayoutGetWidth(root_child1_child2))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetHeight(root_child1_child2))
    assertEquals(25f, GlobalMembers.YGNodeLayoutGetLeft(root_child1_child3))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetTop(root_child1_child3))
    assertEquals(25f, GlobalMembers.YGNodeLayoutGetWidth(root_child1_child3))
    assertEquals(10f, GlobalMembers.YGNodeLayoutGetHeight(root_child1_child3))
    GlobalMembers.YGNodeCalculateLayout(
      root,
      GlobalMembers.YGUndefined,
      GlobalMembers.YGUndefined,
      YGDirection.YGDirectionRTL,
    )
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetWidth(root))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetHeight(root))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
    assertEquals(60f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child1))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetTop(root_child1))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child1))
    assertEquals(25f, GlobalMembers.YGNodeLayoutGetHeight(root_child1))
    assertEquals(25f, GlobalMembers.YGNodeLayoutGetLeft(root_child1_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child1_child0))
    assertEquals(25f, GlobalMembers.YGNodeLayoutGetWidth(root_child1_child0))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetHeight(root_child1_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child1_child1))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child1_child1))
    assertEquals(25f, GlobalMembers.YGNodeLayoutGetWidth(root_child1_child1))
    assertEquals(10f, GlobalMembers.YGNodeLayoutGetHeight(root_child1_child1))
    assertEquals(25f, GlobalMembers.YGNodeLayoutGetLeft(root_child1_child2))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetTop(root_child1_child2))
    assertEquals(25f, GlobalMembers.YGNodeLayoutGetWidth(root_child1_child2))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetHeight(root_child1_child2))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child1_child3))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetTop(root_child1_child3))
    assertEquals(25f, GlobalMembers.YGNodeLayoutGetWidth(root_child1_child3))
    assertEquals(10f, GlobalMembers.YGNodeLayoutGetHeight(root_child1_child3))
    GlobalMembers.YGNodeFreeRecursive(root)
    GlobalMembers.YGConfigFree(config)
  }

  @Test
  fun align_baseline_child_multiline_no_override_on_secondline() {
    val config = GlobalMembers.YGConfigNew()
    val root = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetFlexDirection(root, YGFlexDirection.YGFlexDirectionRow)
    GlobalMembers.YGNodeStyleSetAlignItems(root, YGAlign.YGAlignBaseline)
    GlobalMembers.YGNodeStyleSetWidth(root, 100f)
    GlobalMembers.YGNodeStyleSetHeight(root, 100f)
    val root_child0 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetWidth(root_child0, 50f)
    GlobalMembers.YGNodeStyleSetHeight(root_child0, 60f)
    GlobalMembers.YGNodeInsertChild(root, root_child0, 0)
    val root_child1 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetFlexDirection(root_child1, YGFlexDirection.YGFlexDirectionRow)
    GlobalMembers.YGNodeStyleSetFlexWrap(root_child1, YGWrap.YGWrapWrap)
    GlobalMembers.YGNodeStyleSetWidth(root_child1, 50f)
    GlobalMembers.YGNodeStyleSetHeight(root_child1, 25f)
    GlobalMembers.YGNodeInsertChild(root, root_child1, 1)
    val root_child1_child0 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetWidth(root_child1_child0, 25f)
    GlobalMembers.YGNodeStyleSetHeight(root_child1_child0, 20f)
    GlobalMembers.YGNodeInsertChild(root_child1, root_child1_child0, 0)
    val root_child1_child1 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetWidth(root_child1_child1, 25f)
    GlobalMembers.YGNodeStyleSetHeight(root_child1_child1, 10f)
    GlobalMembers.YGNodeInsertChild(root_child1, root_child1_child1, 1)
    val root_child1_child2 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetWidth(root_child1_child2, 25f)
    GlobalMembers.YGNodeStyleSetHeight(root_child1_child2, 20f)
    GlobalMembers.YGNodeInsertChild(root_child1, root_child1_child2, 2)
    val root_child1_child3 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetAlignSelf(root_child1_child3, YGAlign.YGAlignBaseline)
    GlobalMembers.YGNodeStyleSetWidth(root_child1_child3, 25f)
    GlobalMembers.YGNodeStyleSetHeight(root_child1_child3, 10f)
    GlobalMembers.YGNodeInsertChild(root_child1, root_child1_child3, 3)
    GlobalMembers.YGNodeCalculateLayout(
      root,
      GlobalMembers.YGUndefined,
      GlobalMembers.YGUndefined,
      YGDirection.YGDirectionLTR,
    )
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetWidth(root))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetHeight(root))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
    assertEquals(60f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetLeft(root_child1))
    assertEquals(40f, GlobalMembers.YGNodeLayoutGetTop(root_child1))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child1))
    assertEquals(25f, GlobalMembers.YGNodeLayoutGetHeight(root_child1))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child1_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child1_child0))
    assertEquals(25f, GlobalMembers.YGNodeLayoutGetWidth(root_child1_child0))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetHeight(root_child1_child0))
    assertEquals(25f, GlobalMembers.YGNodeLayoutGetLeft(root_child1_child1))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child1_child1))
    assertEquals(25f, GlobalMembers.YGNodeLayoutGetWidth(root_child1_child1))
    assertEquals(10f, GlobalMembers.YGNodeLayoutGetHeight(root_child1_child1))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child1_child2))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetTop(root_child1_child2))
    assertEquals(25f, GlobalMembers.YGNodeLayoutGetWidth(root_child1_child2))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetHeight(root_child1_child2))
    assertEquals(25f, GlobalMembers.YGNodeLayoutGetLeft(root_child1_child3))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetTop(root_child1_child3))
    assertEquals(25f, GlobalMembers.YGNodeLayoutGetWidth(root_child1_child3))
    assertEquals(10f, GlobalMembers.YGNodeLayoutGetHeight(root_child1_child3))
    GlobalMembers.YGNodeCalculateLayout(
      root,
      GlobalMembers.YGUndefined,
      GlobalMembers.YGUndefined,
      YGDirection.YGDirectionRTL,
    )
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetWidth(root))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetHeight(root))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
    assertEquals(60f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child1))
    assertEquals(40f, GlobalMembers.YGNodeLayoutGetTop(root_child1))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child1))
    assertEquals(25f, GlobalMembers.YGNodeLayoutGetHeight(root_child1))
    assertEquals(25f, GlobalMembers.YGNodeLayoutGetLeft(root_child1_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child1_child0))
    assertEquals(25f, GlobalMembers.YGNodeLayoutGetWidth(root_child1_child0))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetHeight(root_child1_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child1_child1))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child1_child1))
    assertEquals(25f, GlobalMembers.YGNodeLayoutGetWidth(root_child1_child1))
    assertEquals(10f, GlobalMembers.YGNodeLayoutGetHeight(root_child1_child1))
    assertEquals(25f, GlobalMembers.YGNodeLayoutGetLeft(root_child1_child2))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetTop(root_child1_child2))
    assertEquals(25f, GlobalMembers.YGNodeLayoutGetWidth(root_child1_child2))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetHeight(root_child1_child2))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child1_child3))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetTop(root_child1_child3))
    assertEquals(25f, GlobalMembers.YGNodeLayoutGetWidth(root_child1_child3))
    assertEquals(10f, GlobalMembers.YGNodeLayoutGetHeight(root_child1_child3))
    GlobalMembers.YGNodeFreeRecursive(root)
    GlobalMembers.YGConfigFree(config)
  }

  @Test
  fun align_baseline_child_top() {
    val config = GlobalMembers.YGConfigNew()
    val root = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetFlexDirection(root, YGFlexDirection.YGFlexDirectionRow)
    GlobalMembers.YGNodeStyleSetAlignItems(root, YGAlign.YGAlignBaseline)
    GlobalMembers.YGNodeStyleSetWidth(root, 100f)
    GlobalMembers.YGNodeStyleSetHeight(root, 100f)
    val root_child0 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetPosition(root_child0, YGEdge.YGEdgeTop, 10f)
    GlobalMembers.YGNodeStyleSetWidth(root_child0, 50f)
    GlobalMembers.YGNodeStyleSetHeight(root_child0, 50f)
    GlobalMembers.YGNodeInsertChild(root, root_child0, 0)
    val root_child1 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetWidth(root_child1, 50f)
    GlobalMembers.YGNodeStyleSetHeight(root_child1, 20f)
    GlobalMembers.YGNodeInsertChild(root, root_child1, 1)
    val root_child1_child0 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetWidth(root_child1_child0, 50f)
    GlobalMembers.YGNodeStyleSetHeight(root_child1_child0, 10f)
    GlobalMembers.YGNodeInsertChild(root_child1, root_child1_child0, 0)
    GlobalMembers.YGNodeCalculateLayout(
      root,
      GlobalMembers.YGUndefined,
      GlobalMembers.YGUndefined,
      YGDirection.YGDirectionLTR,
    )
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetWidth(root))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetHeight(root))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
    assertEquals(10f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetLeft(root_child1))
    assertEquals(40f, GlobalMembers.YGNodeLayoutGetTop(root_child1))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child1))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetHeight(root_child1))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child1_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child1_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child1_child0))
    assertEquals(10f, GlobalMembers.YGNodeLayoutGetHeight(root_child1_child0))
    GlobalMembers.YGNodeCalculateLayout(
      root,
      GlobalMembers.YGUndefined,
      GlobalMembers.YGUndefined,
      YGDirection.YGDirectionRTL,
    )
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetWidth(root))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetHeight(root))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
    assertEquals(10f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child1))
    assertEquals(40f, GlobalMembers.YGNodeLayoutGetTop(root_child1))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child1))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetHeight(root_child1))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child1_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child1_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child1_child0))
    assertEquals(10f, GlobalMembers.YGNodeLayoutGetHeight(root_child1_child0))
    GlobalMembers.YGNodeFreeRecursive(root)
    GlobalMembers.YGConfigFree(config)
  }

  @Test
  fun align_baseline_child_top2() {
    val config = GlobalMembers.YGConfigNew()
    val root = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetFlexDirection(root, YGFlexDirection.YGFlexDirectionRow)
    GlobalMembers.YGNodeStyleSetAlignItems(root, YGAlign.YGAlignBaseline)
    GlobalMembers.YGNodeStyleSetWidth(root, 100f)
    GlobalMembers.YGNodeStyleSetHeight(root, 100f)
    val root_child0 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetWidth(root_child0, 50f)
    GlobalMembers.YGNodeStyleSetHeight(root_child0, 50f)
    GlobalMembers.YGNodeInsertChild(root, root_child0, 0)
    val root_child1 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetPosition(root_child1, YGEdge.YGEdgeTop, 5f)
    GlobalMembers.YGNodeStyleSetWidth(root_child1, 50f)
    GlobalMembers.YGNodeStyleSetHeight(root_child1, 20f)
    GlobalMembers.YGNodeInsertChild(root, root_child1, 1)
    val root_child1_child0 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetWidth(root_child1_child0, 50f)
    GlobalMembers.YGNodeStyleSetHeight(root_child1_child0, 10f)
    GlobalMembers.YGNodeInsertChild(root_child1, root_child1_child0, 0)
    GlobalMembers.YGNodeCalculateLayout(
      root,
      GlobalMembers.YGUndefined,
      GlobalMembers.YGUndefined,
      YGDirection.YGDirectionLTR,
    )
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetWidth(root))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetHeight(root))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetLeft(root_child1))
    assertEquals(45f, GlobalMembers.YGNodeLayoutGetTop(root_child1))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child1))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetHeight(root_child1))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child1_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child1_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child1_child0))
    assertEquals(10f, GlobalMembers.YGNodeLayoutGetHeight(root_child1_child0))
    GlobalMembers.YGNodeCalculateLayout(
      root,
      GlobalMembers.YGUndefined,
      GlobalMembers.YGUndefined,
      YGDirection.YGDirectionRTL,
    )
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetWidth(root))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetHeight(root))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child1))
    assertEquals(45f, GlobalMembers.YGNodeLayoutGetTop(root_child1))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child1))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetHeight(root_child1))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child1_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child1_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child1_child0))
    assertEquals(10f, GlobalMembers.YGNodeLayoutGetHeight(root_child1_child0))
    GlobalMembers.YGNodeFreeRecursive(root)
    GlobalMembers.YGConfigFree(config)
  }

  @Test
  fun align_baseline_double_nested_child() {
    val config = GlobalMembers.YGConfigNew()
    val root = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetFlexDirection(root, YGFlexDirection.YGFlexDirectionRow)
    GlobalMembers.YGNodeStyleSetAlignItems(root, YGAlign.YGAlignBaseline)
    GlobalMembers.YGNodeStyleSetWidth(root, 100f)
    GlobalMembers.YGNodeStyleSetHeight(root, 100f)
    val root_child0 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetWidth(root_child0, 50f)
    GlobalMembers.YGNodeStyleSetHeight(root_child0, 50f)
    GlobalMembers.YGNodeInsertChild(root, root_child0, 0)
    val root_child0_child0 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetWidth(root_child0_child0, 50f)
    GlobalMembers.YGNodeStyleSetHeight(root_child0_child0, 20f)
    GlobalMembers.YGNodeInsertChild(root_child0, root_child0_child0, 0)
    val root_child1 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetWidth(root_child1, 50f)
    GlobalMembers.YGNodeStyleSetHeight(root_child1, 20f)
    GlobalMembers.YGNodeInsertChild(root, root_child1, 1)
    val root_child1_child0 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetWidth(root_child1_child0, 50f)
    GlobalMembers.YGNodeStyleSetHeight(root_child1_child0, 15f)
    GlobalMembers.YGNodeInsertChild(root_child1, root_child1_child0, 0)
    GlobalMembers.YGNodeCalculateLayout(
      root,
      GlobalMembers.YGUndefined,
      GlobalMembers.YGUndefined,
      YGDirection.YGDirectionLTR,
    )
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetWidth(root))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetHeight(root))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child0_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child0_child0))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetHeight(root_child0_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetLeft(root_child1))
    assertEquals(5f, GlobalMembers.YGNodeLayoutGetTop(root_child1))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child1))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetHeight(root_child1))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child1_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child1_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child1_child0))
    assertEquals(15f, GlobalMembers.YGNodeLayoutGetHeight(root_child1_child0))
    GlobalMembers.YGNodeCalculateLayout(
      root,
      GlobalMembers.YGUndefined,
      GlobalMembers.YGUndefined,
      YGDirection.YGDirectionRTL,
    )
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetWidth(root))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetHeight(root))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child0_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child0_child0))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetHeight(root_child0_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child1))
    assertEquals(5f, GlobalMembers.YGNodeLayoutGetTop(root_child1))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child1))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetHeight(root_child1))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child1_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child1_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child1_child0))
    assertEquals(15f, GlobalMembers.YGNodeLayoutGetHeight(root_child1_child0))
    GlobalMembers.YGNodeFreeRecursive(root)
    GlobalMembers.YGConfigFree(config)
  }

  @Test
  fun align_baseline_column() {
    val config = GlobalMembers.YGConfigNew()
    val root = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetAlignItems(root, YGAlign.YGAlignBaseline)
    GlobalMembers.YGNodeStyleSetWidth(root, 100f)
    GlobalMembers.YGNodeStyleSetHeight(root, 100f)
    val root_child0 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetWidth(root_child0, 50f)
    GlobalMembers.YGNodeStyleSetHeight(root_child0, 50f)
    GlobalMembers.YGNodeInsertChild(root, root_child0, 0)
    val root_child1 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetWidth(root_child1, 50f)
    GlobalMembers.YGNodeStyleSetHeight(root_child1, 20f)
    GlobalMembers.YGNodeInsertChild(root, root_child1, 1)
    GlobalMembers.YGNodeCalculateLayout(
      root,
      GlobalMembers.YGUndefined,
      GlobalMembers.YGUndefined,
      YGDirection.YGDirectionLTR,
    )
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetWidth(root))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetHeight(root))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child1))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetTop(root_child1))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child1))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetHeight(root_child1))
    GlobalMembers.YGNodeCalculateLayout(
      root,
      GlobalMembers.YGUndefined,
      GlobalMembers.YGUndefined,
      YGDirection.YGDirectionRTL,
    )
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetWidth(root))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetHeight(root))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetLeft(root_child1))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetTop(root_child1))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child1))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetHeight(root_child1))
    GlobalMembers.YGNodeFreeRecursive(root)
    GlobalMembers.YGConfigFree(config)
  }

  @Test
  fun align_baseline_child_margin() {
    val config = GlobalMembers.YGConfigNew()
    val root = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetFlexDirection(root, YGFlexDirection.YGFlexDirectionRow)
    GlobalMembers.YGNodeStyleSetAlignItems(root, YGAlign.YGAlignBaseline)
    GlobalMembers.YGNodeStyleSetWidth(root, 100f)
    GlobalMembers.YGNodeStyleSetHeight(root, 100f)
    val root_child0 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetMargin(root_child0, YGEdge.YGEdgeLeft, 5f)
    GlobalMembers.YGNodeStyleSetMargin(root_child0, YGEdge.YGEdgeTop, 5f)
    GlobalMembers.YGNodeStyleSetMargin(root_child0, YGEdge.YGEdgeRight, 5f)
    GlobalMembers.YGNodeStyleSetMargin(root_child0, YGEdge.YGEdgeBottom, 5f)
    GlobalMembers.YGNodeStyleSetWidth(root_child0, 50f)
    GlobalMembers.YGNodeStyleSetHeight(root_child0, 50f)
    GlobalMembers.YGNodeInsertChild(root, root_child0, 0)
    val root_child1 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetWidth(root_child1, 50f)
    GlobalMembers.YGNodeStyleSetHeight(root_child1, 20f)
    GlobalMembers.YGNodeInsertChild(root, root_child1, 1)
    val root_child1_child0 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetMargin(root_child1_child0, YGEdge.YGEdgeLeft, 1f)
    GlobalMembers.YGNodeStyleSetMargin(root_child1_child0, YGEdge.YGEdgeTop, 1f)
    GlobalMembers.YGNodeStyleSetMargin(root_child1_child0, YGEdge.YGEdgeRight, 1f)
    GlobalMembers.YGNodeStyleSetMargin(root_child1_child0, YGEdge.YGEdgeBottom, 1f)
    GlobalMembers.YGNodeStyleSetWidth(root_child1_child0, 50f)
    GlobalMembers.YGNodeStyleSetHeight(root_child1_child0, 10f)
    GlobalMembers.YGNodeInsertChild(root_child1, root_child1_child0, 0)
    GlobalMembers.YGNodeCalculateLayout(
      root,
      GlobalMembers.YGUndefined,
      GlobalMembers.YGUndefined,
      YGDirection.YGDirectionLTR,
    )
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetWidth(root))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetHeight(root))
    assertEquals(5f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
    assertEquals(5f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
    assertEquals(60f, GlobalMembers.YGNodeLayoutGetLeft(root_child1))
    assertEquals(44f, GlobalMembers.YGNodeLayoutGetTop(root_child1))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child1))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetHeight(root_child1))
    assertEquals(1f, GlobalMembers.YGNodeLayoutGetLeft(root_child1_child0))
    assertEquals(1f, GlobalMembers.YGNodeLayoutGetTop(root_child1_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child1_child0))
    assertEquals(10f, GlobalMembers.YGNodeLayoutGetHeight(root_child1_child0))
    GlobalMembers.YGNodeCalculateLayout(
      root,
      GlobalMembers.YGUndefined,
      GlobalMembers.YGUndefined,
      YGDirection.YGDirectionRTL,
    )
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetWidth(root))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetHeight(root))
    assertEquals(45f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
    assertEquals(5f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
    assertEquals(-10f, GlobalMembers.YGNodeLayoutGetLeft(root_child1))
    assertEquals(44f, GlobalMembers.YGNodeLayoutGetTop(root_child1))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child1))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetHeight(root_child1))
    assertEquals(-1f, GlobalMembers.YGNodeLayoutGetLeft(root_child1_child0))
    assertEquals(1f, GlobalMembers.YGNodeLayoutGetTop(root_child1_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child1_child0))
    assertEquals(10f, GlobalMembers.YGNodeLayoutGetHeight(root_child1_child0))
    GlobalMembers.YGNodeFreeRecursive(root)
    GlobalMembers.YGConfigFree(config)
  }

  @Test
  fun align_baseline_child_padding() {
    val config = GlobalMembers.YGConfigNew()
    val root = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetFlexDirection(root, YGFlexDirection.YGFlexDirectionRow)
    GlobalMembers.YGNodeStyleSetAlignItems(root, YGAlign.YGAlignBaseline)
    GlobalMembers.YGNodeStyleSetPadding(root, YGEdge.YGEdgeLeft, 5f)
    GlobalMembers.YGNodeStyleSetPadding(root, YGEdge.YGEdgeTop, 5f)
    GlobalMembers.YGNodeStyleSetPadding(root, YGEdge.YGEdgeRight, 5f)
    GlobalMembers.YGNodeStyleSetPadding(root, YGEdge.YGEdgeBottom, 5f)
    GlobalMembers.YGNodeStyleSetWidth(root, 100f)
    GlobalMembers.YGNodeStyleSetHeight(root, 100f)
    val root_child0 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetWidth(root_child0, 50f)
    GlobalMembers.YGNodeStyleSetHeight(root_child0, 50f)
    GlobalMembers.YGNodeInsertChild(root, root_child0, 0)
    val root_child1 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetPadding(root_child1, YGEdge.YGEdgeLeft, 5f)
    GlobalMembers.YGNodeStyleSetPadding(root_child1, YGEdge.YGEdgeTop, 5f)
    GlobalMembers.YGNodeStyleSetPadding(root_child1, YGEdge.YGEdgeRight, 5f)
    GlobalMembers.YGNodeStyleSetPadding(root_child1, YGEdge.YGEdgeBottom, 5f)
    GlobalMembers.YGNodeStyleSetWidth(root_child1, 50f)
    GlobalMembers.YGNodeStyleSetHeight(root_child1, 20f)
    GlobalMembers.YGNodeInsertChild(root, root_child1, 1)
    val root_child1_child0 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetWidth(root_child1_child0, 50f)
    GlobalMembers.YGNodeStyleSetHeight(root_child1_child0, 10f)
    GlobalMembers.YGNodeInsertChild(root_child1, root_child1_child0, 0)
    GlobalMembers.YGNodeCalculateLayout(
      root,
      GlobalMembers.YGUndefined,
      GlobalMembers.YGUndefined,
      YGDirection.YGDirectionLTR,
    )
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetWidth(root))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetHeight(root))
    assertEquals(5f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
    assertEquals(5f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
    assertEquals(55f, GlobalMembers.YGNodeLayoutGetLeft(root_child1))
    assertEquals(40f, GlobalMembers.YGNodeLayoutGetTop(root_child1))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child1))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetHeight(root_child1))
    assertEquals(5f, GlobalMembers.YGNodeLayoutGetLeft(root_child1_child0))
    assertEquals(5f, GlobalMembers.YGNodeLayoutGetTop(root_child1_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child1_child0))
    assertEquals(10f, GlobalMembers.YGNodeLayoutGetHeight(root_child1_child0))
    GlobalMembers.YGNodeCalculateLayout(
      root,
      GlobalMembers.YGUndefined,
      GlobalMembers.YGUndefined,
      YGDirection.YGDirectionRTL,
    )
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetWidth(root))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetHeight(root))
    assertEquals(45f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
    assertEquals(5f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
    assertEquals(-5f, GlobalMembers.YGNodeLayoutGetLeft(root_child1))
    assertEquals(40f, GlobalMembers.YGNodeLayoutGetTop(root_child1))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child1))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetHeight(root_child1))
    assertEquals(-5f, GlobalMembers.YGNodeLayoutGetLeft(root_child1_child0))
    assertEquals(5f, GlobalMembers.YGNodeLayoutGetTop(root_child1_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child1_child0))
    assertEquals(10f, GlobalMembers.YGNodeLayoutGetHeight(root_child1_child0))
    GlobalMembers.YGNodeFreeRecursive(root)
    GlobalMembers.YGConfigFree(config)
  }

  @Test
  fun align_baseline_multiline() {
    val config = GlobalMembers.YGConfigNew()
    val root = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetFlexDirection(root, YGFlexDirection.YGFlexDirectionRow)
    GlobalMembers.YGNodeStyleSetAlignItems(root, YGAlign.YGAlignBaseline)
    GlobalMembers.YGNodeStyleSetFlexWrap(root, YGWrap.YGWrapWrap)
    GlobalMembers.YGNodeStyleSetWidth(root, 100f)
    GlobalMembers.YGNodeStyleSetHeight(root, 100f)
    val root_child0 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetWidth(root_child0, 50f)
    GlobalMembers.YGNodeStyleSetHeight(root_child0, 50f)
    GlobalMembers.YGNodeInsertChild(root, root_child0, 0)
    val root_child1 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetWidth(root_child1, 50f)
    GlobalMembers.YGNodeStyleSetHeight(root_child1, 20f)
    GlobalMembers.YGNodeInsertChild(root, root_child1, 1)
    val root_child1_child0 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetWidth(root_child1_child0, 50f)
    GlobalMembers.YGNodeStyleSetHeight(root_child1_child0, 10f)
    GlobalMembers.YGNodeInsertChild(root_child1, root_child1_child0, 0)
    val root_child2 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetWidth(root_child2, 50f)
    GlobalMembers.YGNodeStyleSetHeight(root_child2, 20f)
    GlobalMembers.YGNodeInsertChild(root, root_child2, 2)
    val root_child2_child0 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetWidth(root_child2_child0, 50f)
    GlobalMembers.YGNodeStyleSetHeight(root_child2_child0, 10f)
    GlobalMembers.YGNodeInsertChild(root_child2, root_child2_child0, 0)
    val root_child3 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetWidth(root_child3, 50f)
    GlobalMembers.YGNodeStyleSetHeight(root_child3, 50f)
    GlobalMembers.YGNodeInsertChild(root, root_child3, 3)
    GlobalMembers.YGNodeCalculateLayout(
      root,
      GlobalMembers.YGUndefined,
      GlobalMembers.YGUndefined,
      YGDirection.YGDirectionLTR,
    )
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetWidth(root))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetHeight(root))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetLeft(root_child1))
    assertEquals(40f, GlobalMembers.YGNodeLayoutGetTop(root_child1))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child1))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetHeight(root_child1))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child1_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child1_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child1_child0))
    assertEquals(10f, GlobalMembers.YGNodeLayoutGetHeight(root_child1_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child2))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetTop(root_child2))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child2))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetHeight(root_child2))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child2_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child2_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child2_child0))
    assertEquals(10f, GlobalMembers.YGNodeLayoutGetHeight(root_child2_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetLeft(root_child3))
    assertEquals(60f, GlobalMembers.YGNodeLayoutGetTop(root_child3))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child3))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetHeight(root_child3))
    GlobalMembers.YGNodeCalculateLayout(
      root,
      GlobalMembers.YGUndefined,
      GlobalMembers.YGUndefined,
      YGDirection.YGDirectionRTL,
    )
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetWidth(root))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetHeight(root))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child1))
    assertEquals(40f, GlobalMembers.YGNodeLayoutGetTop(root_child1))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child1))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetHeight(root_child1))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child1_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child1_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child1_child0))
    assertEquals(10f, GlobalMembers.YGNodeLayoutGetHeight(root_child1_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetLeft(root_child2))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetTop(root_child2))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child2))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetHeight(root_child2))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child2_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child2_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child2_child0))
    assertEquals(10f, GlobalMembers.YGNodeLayoutGetHeight(root_child2_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child3))
    assertEquals(60f, GlobalMembers.YGNodeLayoutGetTop(root_child3))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child3))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetHeight(root_child3))
    GlobalMembers.YGNodeFreeRecursive(root)
    GlobalMembers.YGConfigFree(config)
  }

  @Test
  fun align_baseline_multiline_column() {
    val config = GlobalMembers.YGConfigNew()
    val root = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetAlignItems(root, YGAlign.YGAlignBaseline)
    GlobalMembers.YGNodeStyleSetFlexWrap(root, YGWrap.YGWrapWrap)
    GlobalMembers.YGNodeStyleSetWidth(root, 100f)
    GlobalMembers.YGNodeStyleSetHeight(root, 100f)
    val root_child0 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetWidth(root_child0, 50f)
    GlobalMembers.YGNodeStyleSetHeight(root_child0, 50f)
    GlobalMembers.YGNodeInsertChild(root, root_child0, 0)
    val root_child1 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetWidth(root_child1, 30f)
    GlobalMembers.YGNodeStyleSetHeight(root_child1, 50f)
    GlobalMembers.YGNodeInsertChild(root, root_child1, 1)
    val root_child1_child0 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetWidth(root_child1_child0, 20f)
    GlobalMembers.YGNodeStyleSetHeight(root_child1_child0, 20f)
    GlobalMembers.YGNodeInsertChild(root_child1, root_child1_child0, 0)
    val root_child2 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetWidth(root_child2, 40f)
    GlobalMembers.YGNodeStyleSetHeight(root_child2, 70f)
    GlobalMembers.YGNodeInsertChild(root, root_child2, 2)
    val root_child2_child0 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetWidth(root_child2_child0, 10f)
    GlobalMembers.YGNodeStyleSetHeight(root_child2_child0, 10f)
    GlobalMembers.YGNodeInsertChild(root_child2, root_child2_child0, 0)
    val root_child3 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetWidth(root_child3, 50f)
    GlobalMembers.YGNodeStyleSetHeight(root_child3, 20f)
    GlobalMembers.YGNodeInsertChild(root, root_child3, 3)
    GlobalMembers.YGNodeCalculateLayout(
      root,
      GlobalMembers.YGUndefined,
      GlobalMembers.YGUndefined,
      YGDirection.YGDirectionLTR,
    )
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetWidth(root))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetHeight(root))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child1))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetTop(root_child1))
    assertEquals(30f, GlobalMembers.YGNodeLayoutGetWidth(root_child1))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetHeight(root_child1))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child1_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child1_child0))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetWidth(root_child1_child0))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetHeight(root_child1_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetLeft(root_child2))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child2))
    assertEquals(40f, GlobalMembers.YGNodeLayoutGetWidth(root_child2))
    assertEquals(70f, GlobalMembers.YGNodeLayoutGetHeight(root_child2))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child2_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child2_child0))
    assertEquals(10f, GlobalMembers.YGNodeLayoutGetWidth(root_child2_child0))
    assertEquals(10f, GlobalMembers.YGNodeLayoutGetHeight(root_child2_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetLeft(root_child3))
    assertEquals(70f, GlobalMembers.YGNodeLayoutGetTop(root_child3))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child3))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetHeight(root_child3))
    GlobalMembers.YGNodeCalculateLayout(
      root,
      GlobalMembers.YGUndefined,
      GlobalMembers.YGUndefined,
      YGDirection.YGDirectionRTL,
    )
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetWidth(root))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetHeight(root))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
    assertEquals(70f, GlobalMembers.YGNodeLayoutGetLeft(root_child1))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetTop(root_child1))
    assertEquals(30f, GlobalMembers.YGNodeLayoutGetWidth(root_child1))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetHeight(root_child1))
    assertEquals(10f, GlobalMembers.YGNodeLayoutGetLeft(root_child1_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child1_child0))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetWidth(root_child1_child0))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetHeight(root_child1_child0))
    assertEquals(10f, GlobalMembers.YGNodeLayoutGetLeft(root_child2))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child2))
    assertEquals(40f, GlobalMembers.YGNodeLayoutGetWidth(root_child2))
    assertEquals(70f, GlobalMembers.YGNodeLayoutGetHeight(root_child2))
    assertEquals(30f, GlobalMembers.YGNodeLayoutGetLeft(root_child2_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child2_child0))
    assertEquals(10f, GlobalMembers.YGNodeLayoutGetWidth(root_child2_child0))
    assertEquals(10f, GlobalMembers.YGNodeLayoutGetHeight(root_child2_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child3))
    assertEquals(70f, GlobalMembers.YGNodeLayoutGetTop(root_child3))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child3))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetHeight(root_child3))
    GlobalMembers.YGNodeFreeRecursive(root)
    GlobalMembers.YGConfigFree(config)
  }

  @Test
  fun align_baseline_multiline_column2() {
    val config = GlobalMembers.YGConfigNew()
    val root = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetAlignItems(root, YGAlign.YGAlignBaseline)
    GlobalMembers.YGNodeStyleSetFlexWrap(root, YGWrap.YGWrapWrap)
    GlobalMembers.YGNodeStyleSetWidth(root, 100f)
    GlobalMembers.YGNodeStyleSetHeight(root, 100f)
    val root_child0 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetWidth(root_child0, 50f)
    GlobalMembers.YGNodeStyleSetHeight(root_child0, 50f)
    GlobalMembers.YGNodeInsertChild(root, root_child0, 0)
    val root_child1 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetWidth(root_child1, 30f)
    GlobalMembers.YGNodeStyleSetHeight(root_child1, 50f)
    GlobalMembers.YGNodeInsertChild(root, root_child1, 1)
    val root_child1_child0 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetWidth(root_child1_child0, 20f)
    GlobalMembers.YGNodeStyleSetHeight(root_child1_child0, 20f)
    GlobalMembers.YGNodeInsertChild(root_child1, root_child1_child0, 0)
    val root_child2 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetWidth(root_child2, 40f)
    GlobalMembers.YGNodeStyleSetHeight(root_child2, 70f)
    GlobalMembers.YGNodeInsertChild(root, root_child2, 2)
    val root_child2_child0 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetWidth(root_child2_child0, 10f)
    GlobalMembers.YGNodeStyleSetHeight(root_child2_child0, 10f)
    GlobalMembers.YGNodeInsertChild(root_child2, root_child2_child0, 0)
    val root_child3 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetWidth(root_child3, 50f)
    GlobalMembers.YGNodeStyleSetHeight(root_child3, 20f)
    GlobalMembers.YGNodeInsertChild(root, root_child3, 3)
    GlobalMembers.YGNodeCalculateLayout(
      root,
      GlobalMembers.YGUndefined,
      GlobalMembers.YGUndefined,
      YGDirection.YGDirectionLTR,
    )
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetWidth(root))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetHeight(root))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child1))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetTop(root_child1))
    assertEquals(30f, GlobalMembers.YGNodeLayoutGetWidth(root_child1))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetHeight(root_child1))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child1_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child1_child0))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetWidth(root_child1_child0))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetHeight(root_child1_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetLeft(root_child2))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child2))
    assertEquals(40f, GlobalMembers.YGNodeLayoutGetWidth(root_child2))
    assertEquals(70f, GlobalMembers.YGNodeLayoutGetHeight(root_child2))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child2_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child2_child0))
    assertEquals(10f, GlobalMembers.YGNodeLayoutGetWidth(root_child2_child0))
    assertEquals(10f, GlobalMembers.YGNodeLayoutGetHeight(root_child2_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetLeft(root_child3))
    assertEquals(70f, GlobalMembers.YGNodeLayoutGetTop(root_child3))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child3))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetHeight(root_child3))
    GlobalMembers.YGNodeCalculateLayout(
      root,
      GlobalMembers.YGUndefined,
      GlobalMembers.YGUndefined,
      YGDirection.YGDirectionRTL,
    )
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetWidth(root))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetHeight(root))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
    assertEquals(70f, GlobalMembers.YGNodeLayoutGetLeft(root_child1))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetTop(root_child1))
    assertEquals(30f, GlobalMembers.YGNodeLayoutGetWidth(root_child1))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetHeight(root_child1))
    assertEquals(10f, GlobalMembers.YGNodeLayoutGetLeft(root_child1_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child1_child0))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetWidth(root_child1_child0))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetHeight(root_child1_child0))
    assertEquals(10f, GlobalMembers.YGNodeLayoutGetLeft(root_child2))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child2))
    assertEquals(40f, GlobalMembers.YGNodeLayoutGetWidth(root_child2))
    assertEquals(70f, GlobalMembers.YGNodeLayoutGetHeight(root_child2))
    assertEquals(30f, GlobalMembers.YGNodeLayoutGetLeft(root_child2_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child2_child0))
    assertEquals(10f, GlobalMembers.YGNodeLayoutGetWidth(root_child2_child0))
    assertEquals(10f, GlobalMembers.YGNodeLayoutGetHeight(root_child2_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child3))
    assertEquals(70f, GlobalMembers.YGNodeLayoutGetTop(root_child3))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child3))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetHeight(root_child3))
    GlobalMembers.YGNodeFreeRecursive(root)
    GlobalMembers.YGConfigFree(config)
  }

  @Test
  fun align_baseline_multiline_row_and_column() {
    val config = GlobalMembers.YGConfigNew()
    val root = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetFlexDirection(root, YGFlexDirection.YGFlexDirectionRow)
    GlobalMembers.YGNodeStyleSetAlignItems(root, YGAlign.YGAlignBaseline)
    GlobalMembers.YGNodeStyleSetFlexWrap(root, YGWrap.YGWrapWrap)
    GlobalMembers.YGNodeStyleSetWidth(root, 100f)
    GlobalMembers.YGNodeStyleSetHeight(root, 100f)
    val root_child0 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetWidth(root_child0, 50f)
    GlobalMembers.YGNodeStyleSetHeight(root_child0, 50f)
    GlobalMembers.YGNodeInsertChild(root, root_child0, 0)
    val root_child1 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetWidth(root_child1, 50f)
    GlobalMembers.YGNodeStyleSetHeight(root_child1, 50f)
    GlobalMembers.YGNodeInsertChild(root, root_child1, 1)
    val root_child1_child0 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetWidth(root_child1_child0, 50f)
    GlobalMembers.YGNodeStyleSetHeight(root_child1_child0, 10f)
    GlobalMembers.YGNodeInsertChild(root_child1, root_child1_child0, 0)
    val root_child2 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetWidth(root_child2, 50f)
    GlobalMembers.YGNodeStyleSetHeight(root_child2, 20f)
    GlobalMembers.YGNodeInsertChild(root, root_child2, 2)
    val root_child2_child0 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetWidth(root_child2_child0, 50f)
    GlobalMembers.YGNodeStyleSetHeight(root_child2_child0, 10f)
    GlobalMembers.YGNodeInsertChild(root_child2, root_child2_child0, 0)
    val root_child3 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetWidth(root_child3, 50f)
    GlobalMembers.YGNodeStyleSetHeight(root_child3, 20f)
    GlobalMembers.YGNodeInsertChild(root, root_child3, 3)
    GlobalMembers.YGNodeCalculateLayout(
      root,
      GlobalMembers.YGUndefined,
      GlobalMembers.YGUndefined,
      YGDirection.YGDirectionLTR,
    )
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetWidth(root))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetHeight(root))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetLeft(root_child1))
    assertEquals(40f, GlobalMembers.YGNodeLayoutGetTop(root_child1))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child1))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetHeight(root_child1))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child1_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child1_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child1_child0))
    assertEquals(10f, GlobalMembers.YGNodeLayoutGetHeight(root_child1_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child2))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetTop(root_child2))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child2))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetHeight(root_child2))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child2_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child2_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child2_child0))
    assertEquals(10f, GlobalMembers.YGNodeLayoutGetHeight(root_child2_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetLeft(root_child3))
    assertEquals(90f, GlobalMembers.YGNodeLayoutGetTop(root_child3))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child3))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetHeight(root_child3))
    GlobalMembers.YGNodeCalculateLayout(
      root,
      GlobalMembers.YGUndefined,
      GlobalMembers.YGUndefined,
      YGDirection.YGDirectionRTL,
    )
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetWidth(root))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetHeight(root))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child1))
    assertEquals(40f, GlobalMembers.YGNodeLayoutGetTop(root_child1))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child1))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetHeight(root_child1))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child1_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child1_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child1_child0))
    assertEquals(10f, GlobalMembers.YGNodeLayoutGetHeight(root_child1_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetLeft(root_child2))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetTop(root_child2))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child2))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetHeight(root_child2))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child2_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child2_child0))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child2_child0))
    assertEquals(10f, GlobalMembers.YGNodeLayoutGetHeight(root_child2_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child3))
    assertEquals(90f, GlobalMembers.YGNodeLayoutGetTop(root_child3))
    assertEquals(50f, GlobalMembers.YGNodeLayoutGetWidth(root_child3))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetHeight(root_child3))
    GlobalMembers.YGNodeFreeRecursive(root)
    GlobalMembers.YGConfigFree(config)
  }

  @Test
  fun align_items_center_child_with_margin_bigger_than_parent() {
    val config = GlobalMembers.YGConfigNew()
    val root = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetJustifyContent(root, YGJustify.YGJustifyCenter)
    GlobalMembers.YGNodeStyleSetAlignItems(root, YGAlign.YGAlignCenter)
    GlobalMembers.YGNodeStyleSetWidth(root, 52f)
    GlobalMembers.YGNodeStyleSetHeight(root, 52f)
    val root_child0 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetAlignItems(root_child0, YGAlign.YGAlignCenter)
    GlobalMembers.YGNodeInsertChild(root, root_child0, 0)
    val root_child0_child0 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetMargin(root_child0_child0, YGEdge.YGEdgeLeft, 10f)
    GlobalMembers.YGNodeStyleSetMargin(root_child0_child0, YGEdge.YGEdgeRight, 10f)
    GlobalMembers.YGNodeStyleSetWidth(root_child0_child0, 52f)
    GlobalMembers.YGNodeStyleSetHeight(root_child0_child0, 52f)
    GlobalMembers.YGNodeInsertChild(root_child0, root_child0_child0, 0)
    GlobalMembers.YGNodeCalculateLayout(
      root,
      GlobalMembers.YGUndefined,
      GlobalMembers.YGUndefined,
      YGDirection.YGDirectionLTR,
    )
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
    assertEquals(52f, GlobalMembers.YGNodeLayoutGetWidth(root))
    assertEquals(52f, GlobalMembers.YGNodeLayoutGetHeight(root))
    assertEquals(-10f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
    assertEquals(72f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
    assertEquals(52f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
    assertEquals(10f, GlobalMembers.YGNodeLayoutGetLeft(root_child0_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0_child0))
    assertEquals(52f, GlobalMembers.YGNodeLayoutGetWidth(root_child0_child0))
    assertEquals(52f, GlobalMembers.YGNodeLayoutGetHeight(root_child0_child0))
    GlobalMembers.YGNodeCalculateLayout(
      root,
      GlobalMembers.YGUndefined,
      GlobalMembers.YGUndefined,
      YGDirection.YGDirectionRTL,
    )
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
    assertEquals(52f, GlobalMembers.YGNodeLayoutGetWidth(root))
    assertEquals(52f, GlobalMembers.YGNodeLayoutGetHeight(root))
    assertEquals(-10f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
    assertEquals(72f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
    assertEquals(52f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
    assertEquals(10f, GlobalMembers.YGNodeLayoutGetLeft(root_child0_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0_child0))
    assertEquals(52f, GlobalMembers.YGNodeLayoutGetWidth(root_child0_child0))
    assertEquals(52f, GlobalMembers.YGNodeLayoutGetHeight(root_child0_child0))
    GlobalMembers.YGNodeFreeRecursive(root)
    GlobalMembers.YGConfigFree(config)
  }

  @Test
  fun align_items_flex_end_child_with_margin_bigger_than_parent() {
    val config = GlobalMembers.YGConfigNew()
    val root = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetJustifyContent(root, YGJustify.YGJustifyCenter)
    GlobalMembers.YGNodeStyleSetAlignItems(root, YGAlign.YGAlignCenter)
    GlobalMembers.YGNodeStyleSetWidth(root, 52f)
    GlobalMembers.YGNodeStyleSetHeight(root, 52f)
    val root_child0 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetAlignItems(root_child0, YGAlign.YGAlignFlexEnd)
    GlobalMembers.YGNodeInsertChild(root, root_child0, 0)
    val root_child0_child0 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetMargin(root_child0_child0, YGEdge.YGEdgeLeft, 10f)
    GlobalMembers.YGNodeStyleSetMargin(root_child0_child0, YGEdge.YGEdgeRight, 10f)
    GlobalMembers.YGNodeStyleSetWidth(root_child0_child0, 52f)
    GlobalMembers.YGNodeStyleSetHeight(root_child0_child0, 52f)
    GlobalMembers.YGNodeInsertChild(root_child0, root_child0_child0, 0)
    GlobalMembers.YGNodeCalculateLayout(
      root,
      GlobalMembers.YGUndefined,
      GlobalMembers.YGUndefined,
      YGDirection.YGDirectionLTR,
    )
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
    assertEquals(52f, GlobalMembers.YGNodeLayoutGetWidth(root))
    assertEquals(52f, GlobalMembers.YGNodeLayoutGetHeight(root))
    assertEquals(-10f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
    assertEquals(72f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
    assertEquals(52f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
    assertEquals(10f, GlobalMembers.YGNodeLayoutGetLeft(root_child0_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0_child0))
    assertEquals(52f, GlobalMembers.YGNodeLayoutGetWidth(root_child0_child0))
    assertEquals(52f, GlobalMembers.YGNodeLayoutGetHeight(root_child0_child0))
    GlobalMembers.YGNodeCalculateLayout(
      root,
      GlobalMembers.YGUndefined,
      GlobalMembers.YGUndefined,
      YGDirection.YGDirectionRTL,
    )
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
    assertEquals(52f, GlobalMembers.YGNodeLayoutGetWidth(root))
    assertEquals(52f, GlobalMembers.YGNodeLayoutGetHeight(root))
    assertEquals(-10f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
    assertEquals(72f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
    assertEquals(52f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
    assertEquals(10f, GlobalMembers.YGNodeLayoutGetLeft(root_child0_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0_child0))
    assertEquals(52f, GlobalMembers.YGNodeLayoutGetWidth(root_child0_child0))
    assertEquals(52f, GlobalMembers.YGNodeLayoutGetHeight(root_child0_child0))
    GlobalMembers.YGNodeFreeRecursive(root)
    GlobalMembers.YGConfigFree(config)
  }

  @Test
  fun align_items_center_child_without_margin_bigger_than_parent() {
    val config = GlobalMembers.YGConfigNew()
    val root = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetJustifyContent(root, YGJustify.YGJustifyCenter)
    GlobalMembers.YGNodeStyleSetAlignItems(root, YGAlign.YGAlignCenter)
    GlobalMembers.YGNodeStyleSetWidth(root, 52f)
    GlobalMembers.YGNodeStyleSetHeight(root, 52f)
    val root_child0 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetAlignItems(root_child0, YGAlign.YGAlignCenter)
    GlobalMembers.YGNodeInsertChild(root, root_child0, 0)
    val root_child0_child0 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetWidth(root_child0_child0, 72f)
    GlobalMembers.YGNodeStyleSetHeight(root_child0_child0, 72f)
    GlobalMembers.YGNodeInsertChild(root_child0, root_child0_child0, 0)
    GlobalMembers.YGNodeCalculateLayout(
      root,
      GlobalMembers.YGUndefined,
      GlobalMembers.YGUndefined,
      YGDirection.YGDirectionLTR,
    )
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
    assertEquals(52f, GlobalMembers.YGNodeLayoutGetWidth(root))
    assertEquals(52f, GlobalMembers.YGNodeLayoutGetHeight(root))
    assertEquals(-10f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
    assertEquals(-10f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
    assertEquals(72f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
    assertEquals(72f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child0_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0_child0))
    assertEquals(72f, GlobalMembers.YGNodeLayoutGetWidth(root_child0_child0))
    assertEquals(72f, GlobalMembers.YGNodeLayoutGetHeight(root_child0_child0))
    GlobalMembers.YGNodeCalculateLayout(
      root,
      GlobalMembers.YGUndefined,
      GlobalMembers.YGUndefined,
      YGDirection.YGDirectionRTL,
    )
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
    assertEquals(52f, GlobalMembers.YGNodeLayoutGetWidth(root))
    assertEquals(52f, GlobalMembers.YGNodeLayoutGetHeight(root))
    assertEquals(-10f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
    assertEquals(-10f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
    assertEquals(72f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
    assertEquals(72f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child0_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0_child0))
    assertEquals(72f, GlobalMembers.YGNodeLayoutGetWidth(root_child0_child0))
    assertEquals(72f, GlobalMembers.YGNodeLayoutGetHeight(root_child0_child0))
    GlobalMembers.YGNodeFreeRecursive(root)
    GlobalMembers.YGConfigFree(config)
  }

  @Test
  fun align_items_flex_end_child_without_margin_bigger_than_parent() {
    val config = GlobalMembers.YGConfigNew()
    val root = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetJustifyContent(root, YGJustify.YGJustifyCenter)
    GlobalMembers.YGNodeStyleSetAlignItems(root, YGAlign.YGAlignCenter)
    GlobalMembers.YGNodeStyleSetWidth(root, 52f)
    GlobalMembers.YGNodeStyleSetHeight(root, 52f)
    val root_child0 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetAlignItems(root_child0, YGAlign.YGAlignFlexEnd)
    GlobalMembers.YGNodeInsertChild(root, root_child0, 0)
    val root_child0_child0 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetWidth(root_child0_child0, 72f)
    GlobalMembers.YGNodeStyleSetHeight(root_child0_child0, 72f)
    GlobalMembers.YGNodeInsertChild(root_child0, root_child0_child0, 0)
    GlobalMembers.YGNodeCalculateLayout(
      root,
      GlobalMembers.YGUndefined,
      GlobalMembers.YGUndefined,
      YGDirection.YGDirectionLTR,
    )
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
    assertEquals(52f, GlobalMembers.YGNodeLayoutGetWidth(root))
    assertEquals(52f, GlobalMembers.YGNodeLayoutGetHeight(root))
    assertEquals(-10f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
    assertEquals(-10f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
    assertEquals(72f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
    assertEquals(72f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child0_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0_child0))
    assertEquals(72f, GlobalMembers.YGNodeLayoutGetWidth(root_child0_child0))
    assertEquals(72f, GlobalMembers.YGNodeLayoutGetHeight(root_child0_child0))
    GlobalMembers.YGNodeCalculateLayout(
      root,
      GlobalMembers.YGUndefined,
      GlobalMembers.YGUndefined,
      YGDirection.YGDirectionRTL,
    )
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
    assertEquals(52f, GlobalMembers.YGNodeLayoutGetWidth(root))
    assertEquals(52f, GlobalMembers.YGNodeLayoutGetHeight(root))
    assertEquals(-10f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
    assertEquals(-10f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
    assertEquals(72f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
    assertEquals(72f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child0_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0_child0))
    assertEquals(72f, GlobalMembers.YGNodeLayoutGetWidth(root_child0_child0))
    assertEquals(72f, GlobalMembers.YGNodeLayoutGetHeight(root_child0_child0))
    GlobalMembers.YGNodeFreeRecursive(root)
    GlobalMembers.YGConfigFree(config)
  }

  @Test
  fun align_center_should_size_based_on_content() {
    val config = GlobalMembers.YGConfigNew()
    val root = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetAlignItems(root, YGAlign.YGAlignCenter)
    GlobalMembers.YGNodeStyleSetMargin(root, YGEdge.YGEdgeTop, 20f)
    GlobalMembers.YGNodeStyleSetWidth(root, 100f)
    GlobalMembers.YGNodeStyleSetHeight(root, 100f)
    val root_child0 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetJustifyContent(root_child0, YGJustify.YGJustifyCenter)
    GlobalMembers.YGNodeStyleSetFlexShrink(root_child0, 1f)
    GlobalMembers.YGNodeInsertChild(root, root_child0, 0)
    val root_child0_child0 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetFlexGrow(root_child0_child0, 1f)
    GlobalMembers.YGNodeStyleSetFlexShrink(root_child0_child0, 1f)
    GlobalMembers.YGNodeInsertChild(root_child0, root_child0_child0, 0)
    val root_child0_child0_child0 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetWidth(root_child0_child0_child0, 20f)
    GlobalMembers.YGNodeStyleSetHeight(root_child0_child0_child0, 20f)
    GlobalMembers.YGNodeInsertChild(root_child0_child0, root_child0_child0_child0, 0)
    GlobalMembers.YGNodeCalculateLayout(
      root,
      GlobalMembers.YGUndefined,
      GlobalMembers.YGUndefined,
      YGDirection.YGDirectionLTR,
    )
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetTop(root))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetWidth(root))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetHeight(root))
    assertEquals(40f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child0_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0_child0))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetWidth(root_child0_child0))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetHeight(root_child0_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child0_child0_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0_child0_child0))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetWidth(root_child0_child0_child0))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetHeight(root_child0_child0_child0))
    GlobalMembers.YGNodeCalculateLayout(
      root,
      GlobalMembers.YGUndefined,
      GlobalMembers.YGUndefined,
      YGDirection.YGDirectionRTL,
    )
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetTop(root))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetWidth(root))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetHeight(root))
    assertEquals(40f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child0_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0_child0))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetWidth(root_child0_child0))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetHeight(root_child0_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child0_child0_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0_child0_child0))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetWidth(root_child0_child0_child0))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetHeight(root_child0_child0_child0))
    GlobalMembers.YGNodeFreeRecursive(root)
    GlobalMembers.YGConfigFree(config)
  }

  @Test
  fun align_strech_should_size_based_on_parent() {
    val config = GlobalMembers.YGConfigNew()
    val root = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetMargin(root, YGEdge.YGEdgeTop, 20f)
    GlobalMembers.YGNodeStyleSetWidth(root, 100f)
    GlobalMembers.YGNodeStyleSetHeight(root, 100f)
    val root_child0 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetJustifyContent(root_child0, YGJustify.YGJustifyCenter)
    GlobalMembers.YGNodeStyleSetFlexShrink(root_child0, 1f)
    GlobalMembers.YGNodeInsertChild(root, root_child0, 0)
    val root_child0_child0 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetFlexGrow(root_child0_child0, 1f)
    GlobalMembers.YGNodeStyleSetFlexShrink(root_child0_child0, 1f)
    GlobalMembers.YGNodeInsertChild(root_child0, root_child0_child0, 0)
    val root_child0_child0_child0 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetWidth(root_child0_child0_child0, 20f)
    GlobalMembers.YGNodeStyleSetHeight(root_child0_child0_child0, 20f)
    GlobalMembers.YGNodeInsertChild(root_child0_child0, root_child0_child0_child0, 0)
    GlobalMembers.YGNodeCalculateLayout(
      root,
      GlobalMembers.YGUndefined,
      GlobalMembers.YGUndefined,
      YGDirection.YGDirectionLTR,
    )
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetTop(root))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetWidth(root))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetHeight(root))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child0_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0_child0))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetWidth(root_child0_child0))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetHeight(root_child0_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child0_child0_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0_child0_child0))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetWidth(root_child0_child0_child0))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetHeight(root_child0_child0_child0))
    GlobalMembers.YGNodeCalculateLayout(
      root,
      GlobalMembers.YGUndefined,
      GlobalMembers.YGUndefined,
      YGDirection.YGDirectionRTL,
    )
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetTop(root))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetWidth(root))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetHeight(root))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child0_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0_child0))
    assertEquals(100f, GlobalMembers.YGNodeLayoutGetWidth(root_child0_child0))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetHeight(root_child0_child0))
    assertEquals(80f, GlobalMembers.YGNodeLayoutGetLeft(root_child0_child0_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0_child0_child0))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetWidth(root_child0_child0_child0))
    assertEquals(20f, GlobalMembers.YGNodeLayoutGetHeight(root_child0_child0_child0))
    GlobalMembers.YGNodeFreeRecursive(root)
    GlobalMembers.YGConfigFree(config)
  }

  @Test
  fun align_flex_start_with_shrinking_children() {
    val config = GlobalMembers.YGConfigNew()
    val root = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetWidth(root, 500f)
    GlobalMembers.YGNodeStyleSetHeight(root, 500f)
    val root_child0 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetAlignItems(root_child0, YGAlign.YGAlignFlexStart)
    GlobalMembers.YGNodeInsertChild(root, root_child0, 0)
    val root_child0_child0 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetFlexGrow(root_child0_child0, 1f)
    GlobalMembers.YGNodeStyleSetFlexShrink(root_child0_child0, 1f)
    GlobalMembers.YGNodeInsertChild(root_child0, root_child0_child0, 0)
    val root_child0_child0_child0 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetFlexGrow(root_child0_child0_child0, 1f)
    GlobalMembers.YGNodeStyleSetFlexShrink(root_child0_child0_child0, 1f)
    GlobalMembers.YGNodeInsertChild(root_child0_child0, root_child0_child0_child0, 0)
    GlobalMembers.YGNodeCalculateLayout(
      root,
      GlobalMembers.YGUndefined,
      GlobalMembers.YGUndefined,
      YGDirection.YGDirectionLTR,
    )
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
    assertEquals(500f, GlobalMembers.YGNodeLayoutGetWidth(root))
    assertEquals(500f, GlobalMembers.YGNodeLayoutGetHeight(root))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
    assertEquals(500f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child0_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetWidth(root_child0_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetHeight(root_child0_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child0_child0_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0_child0_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetWidth(root_child0_child0_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetHeight(root_child0_child0_child0))
    GlobalMembers.YGNodeCalculateLayout(
      root,
      GlobalMembers.YGUndefined,
      GlobalMembers.YGUndefined,
      YGDirection.YGDirectionRTL,
    )
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
    assertEquals(500f, GlobalMembers.YGNodeLayoutGetWidth(root))
    assertEquals(500f, GlobalMembers.YGNodeLayoutGetHeight(root))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
    assertEquals(500f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
    assertEquals(500f, GlobalMembers.YGNodeLayoutGetLeft(root_child0_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetWidth(root_child0_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetHeight(root_child0_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child0_child0_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0_child0_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetWidth(root_child0_child0_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetHeight(root_child0_child0_child0))
    GlobalMembers.YGNodeFreeRecursive(root)
    GlobalMembers.YGConfigFree(config)
  }

  @Test
  fun align_flex_start_with_stretching_children() {
    val config = GlobalMembers.YGConfigNew()
    val root = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetWidth(root, 500f)
    GlobalMembers.YGNodeStyleSetHeight(root, 500f)
    val root_child0 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeInsertChild(root, root_child0, 0)
    val root_child0_child0 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetFlexGrow(root_child0_child0, 1f)
    GlobalMembers.YGNodeStyleSetFlexShrink(root_child0_child0, 1f)
    GlobalMembers.YGNodeInsertChild(root_child0, root_child0_child0, 0)
    val root_child0_child0_child0 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetFlexGrow(root_child0_child0_child0, 1f)
    GlobalMembers.YGNodeStyleSetFlexShrink(root_child0_child0_child0, 1f)
    GlobalMembers.YGNodeInsertChild(root_child0_child0, root_child0_child0_child0, 0)
    GlobalMembers.YGNodeCalculateLayout(
      root,
      GlobalMembers.YGUndefined,
      GlobalMembers.YGUndefined,
      YGDirection.YGDirectionLTR,
    )
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
    assertEquals(500f, GlobalMembers.YGNodeLayoutGetWidth(root))
    assertEquals(500f, GlobalMembers.YGNodeLayoutGetHeight(root))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
    assertEquals(500f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child0_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0_child0))
    assertEquals(500f, GlobalMembers.YGNodeLayoutGetWidth(root_child0_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetHeight(root_child0_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child0_child0_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0_child0_child0))
    assertEquals(500f, GlobalMembers.YGNodeLayoutGetWidth(root_child0_child0_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetHeight(root_child0_child0_child0))
    GlobalMembers.YGNodeCalculateLayout(
      root,
      GlobalMembers.YGUndefined,
      GlobalMembers.YGUndefined,
      YGDirection.YGDirectionRTL,
    )
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
    assertEquals(500f, GlobalMembers.YGNodeLayoutGetWidth(root))
    assertEquals(500f, GlobalMembers.YGNodeLayoutGetHeight(root))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
    assertEquals(500f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child0_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0_child0))
    assertEquals(500f, GlobalMembers.YGNodeLayoutGetWidth(root_child0_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetHeight(root_child0_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child0_child0_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0_child0_child0))
    assertEquals(500f, GlobalMembers.YGNodeLayoutGetWidth(root_child0_child0_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetHeight(root_child0_child0_child0))
    GlobalMembers.YGNodeFreeRecursive(root)
    GlobalMembers.YGConfigFree(config)
  }

  @Test
  fun align_flex_start_with_shrinking_children_with_stretch() {
    val config = GlobalMembers.YGConfigNew()
    val root = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetWidth(root, 500f)
    GlobalMembers.YGNodeStyleSetHeight(root, 500f)
    val root_child0 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetAlignItems(root_child0, YGAlign.YGAlignFlexStart)
    GlobalMembers.YGNodeInsertChild(root, root_child0, 0)
    val root_child0_child0 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetFlexGrow(root_child0_child0, 1f)
    GlobalMembers.YGNodeStyleSetFlexShrink(root_child0_child0, 1f)
    GlobalMembers.YGNodeInsertChild(root_child0, root_child0_child0, 0)
    val root_child0_child0_child0 = GlobalMembers.YGNodeNewWithConfig(config)
    GlobalMembers.YGNodeStyleSetFlexGrow(root_child0_child0_child0, 1f)
    GlobalMembers.YGNodeStyleSetFlexShrink(root_child0_child0_child0, 1f)
    GlobalMembers.YGNodeInsertChild(root_child0_child0, root_child0_child0_child0, 0)
    GlobalMembers.YGNodeCalculateLayout(
      root,
      GlobalMembers.YGUndefined,
      GlobalMembers.YGUndefined,
      YGDirection.YGDirectionLTR,
    )
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
    assertEquals(500f, GlobalMembers.YGNodeLayoutGetWidth(root))
    assertEquals(500f, GlobalMembers.YGNodeLayoutGetHeight(root))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
    assertEquals(500f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child0_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetWidth(root_child0_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetHeight(root_child0_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child0_child0_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0_child0_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetWidth(root_child0_child0_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetHeight(root_child0_child0_child0))
    GlobalMembers.YGNodeCalculateLayout(
      root,
      GlobalMembers.YGUndefined,
      GlobalMembers.YGUndefined,
      YGDirection.YGDirectionRTL,
    )
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root))
    assertEquals(500f, GlobalMembers.YGNodeLayoutGetWidth(root))
    assertEquals(500f, GlobalMembers.YGNodeLayoutGetHeight(root))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0))
    assertEquals(500f, GlobalMembers.YGNodeLayoutGetWidth(root_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetHeight(root_child0))
    assertEquals(500f, GlobalMembers.YGNodeLayoutGetLeft(root_child0_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetWidth(root_child0_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetHeight(root_child0_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetLeft(root_child0_child0_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetTop(root_child0_child0_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetWidth(root_child0_child0_child0))
    assertEquals(0f, GlobalMembers.YGNodeLayoutGetHeight(root_child0_child0_child0))
    GlobalMembers.YGNodeFreeRecursive(root)
    GlobalMembers.YGConfigFree(config)
  }
}
