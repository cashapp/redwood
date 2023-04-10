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
    val config = Yoga.YGConfigNew()
    val root = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetWidth(root, 100f)
    Yoga.YGNodeStyleSetHeight(root, 100f)
    val root_child0 = Yoga.YGNodeNewWithConfig(config)
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
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(100f, Yoga.YGNodeLayoutGetWidth(root_child0))
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
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(100f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(10f, Yoga.YGNodeLayoutGetHeight(root_child0))
    Yoga.YGNodeFreeRecursive(root)
  }

  @Test
  fun align_items_center() {
    val config = Yoga.YGConfigNew()
    val root = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetAlignItems(root, YGAlign.YGAlignCenter)
    Yoga.YGNodeStyleSetWidth(root, 100f)
    Yoga.YGNodeStyleSetHeight(root, 100f)
    val root_child0 = Yoga.YGNodeNewWithConfig(config)
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
    assertEquals(45f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0))
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
    assertEquals(45f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(10f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(10f, Yoga.YGNodeLayoutGetHeight(root_child0))
    Yoga.YGNodeFreeRecursive(root)
  }

  @Test
  fun align_items_flex_start() {
    val config = Yoga.YGConfigNew()
    val root = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetAlignItems(root, YGAlign.YGAlignFlexStart)
    Yoga.YGNodeStyleSetWidth(root, 100f)
    Yoga.YGNodeStyleSetHeight(root, 100f)
    val root_child0 = Yoga.YGNodeNewWithConfig(config)
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
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0))
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
    assertEquals(90f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(10f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(10f, Yoga.YGNodeLayoutGetHeight(root_child0))
    Yoga.YGNodeFreeRecursive(root)
  }

  @Test
  fun align_items_flex_end() {
    val config = Yoga.YGConfigNew()
    val root = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetAlignItems(root, YGAlign.YGAlignFlexEnd)
    Yoga.YGNodeStyleSetWidth(root, 100f)
    Yoga.YGNodeStyleSetHeight(root, 100f)
    val root_child0 = Yoga.YGNodeNewWithConfig(config)
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
    assertEquals(90f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0))
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
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(10f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(10f, Yoga.YGNodeLayoutGetHeight(root_child0))
    Yoga.YGNodeFreeRecursive(root)
  }

  @Test
  fun align_baseline() {
    val config = Yoga.YGConfigNew()
    val root = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetFlexDirection(root, YGFlexDirection.YGFlexDirectionRow)
    Yoga.YGNodeStyleSetAlignItems(root, YGAlign.YGAlignBaseline)
    Yoga.YGNodeStyleSetWidth(root, 100f)
    Yoga.YGNodeStyleSetHeight(root, 100f)
    val root_child0 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetWidth(root_child0, 50f)
    Yoga.YGNodeStyleSetHeight(root_child0, 50f)
    Yoga.YGNodeInsertChild(root, root_child0, 0)
    val root_child1 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetWidth(root_child1, 50f)
    Yoga.YGNodeStyleSetHeight(root_child1, 20f)
    Yoga.YGNodeInsertChild(root, root_child1, 1)
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
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetHeight(root_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetLeft(root_child1))
    assertEquals(30f, Yoga.YGNodeLayoutGetTop(root_child1))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child1))
    assertEquals(20f, Yoga.YGNodeLayoutGetHeight(root_child1))
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
    assertEquals(50f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetHeight(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child1))
    assertEquals(30f, Yoga.YGNodeLayoutGetTop(root_child1))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child1))
    assertEquals(20f, Yoga.YGNodeLayoutGetHeight(root_child1))
    Yoga.YGNodeFreeRecursive(root)
  }

  @Test
  fun align_baseline_child() {
    val config = Yoga.YGConfigNew()
    val root = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetFlexDirection(root, YGFlexDirection.YGFlexDirectionRow)
    Yoga.YGNodeStyleSetAlignItems(root, YGAlign.YGAlignBaseline)
    Yoga.YGNodeStyleSetWidth(root, 100f)
    Yoga.YGNodeStyleSetHeight(root, 100f)
    val root_child0 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetWidth(root_child0, 50f)
    Yoga.YGNodeStyleSetHeight(root_child0, 50f)
    Yoga.YGNodeInsertChild(root, root_child0, 0)
    val root_child1 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetWidth(root_child1, 50f)
    Yoga.YGNodeStyleSetHeight(root_child1, 20f)
    Yoga.YGNodeInsertChild(root, root_child1, 1)
    val root_child1_child0 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetWidth(root_child1_child0, 50f)
    Yoga.YGNodeStyleSetHeight(root_child1_child0, 10f)
    Yoga.YGNodeInsertChild(root_child1, root_child1_child0, 0)
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
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetHeight(root_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetLeft(root_child1))
    assertEquals(40f, Yoga.YGNodeLayoutGetTop(root_child1))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child1))
    assertEquals(20f, Yoga.YGNodeLayoutGetHeight(root_child1))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child1_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child1_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child1_child0))
    assertEquals(10f, Yoga.YGNodeLayoutGetHeight(root_child1_child0))
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
    assertEquals(50f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetHeight(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child1))
    assertEquals(40f, Yoga.YGNodeLayoutGetTop(root_child1))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child1))
    assertEquals(20f, Yoga.YGNodeLayoutGetHeight(root_child1))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child1_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child1_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child1_child0))
    assertEquals(10f, Yoga.YGNodeLayoutGetHeight(root_child1_child0))
    Yoga.YGNodeFreeRecursive(root)
  }

  @Test
  fun align_baseline_child_multiline() {
    val config = Yoga.YGConfigNew()
    val root = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetFlexDirection(root, YGFlexDirection.YGFlexDirectionRow)
    Yoga.YGNodeStyleSetAlignItems(root, YGAlign.YGAlignBaseline)
    Yoga.YGNodeStyleSetWidth(root, 100f)
    Yoga.YGNodeStyleSetHeight(root, 100f)
    val root_child0 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetWidth(root_child0, 50f)
    Yoga.YGNodeStyleSetHeight(root_child0, 60f)
    Yoga.YGNodeInsertChild(root, root_child0, 0)
    val root_child1 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetFlexDirection(root_child1, YGFlexDirection.YGFlexDirectionRow)
    Yoga.YGNodeStyleSetFlexWrap(root_child1, YGWrap.YGWrapWrap)
    Yoga.YGNodeStyleSetWidth(root_child1, 50f)
    Yoga.YGNodeStyleSetHeight(root_child1, 25f)
    Yoga.YGNodeInsertChild(root, root_child1, 1)
    val root_child1_child0 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetWidth(root_child1_child0, 25f)
    Yoga.YGNodeStyleSetHeight(root_child1_child0, 20f)
    Yoga.YGNodeInsertChild(root_child1, root_child1_child0, 0)
    val root_child1_child1 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetWidth(root_child1_child1, 25f)
    Yoga.YGNodeStyleSetHeight(root_child1_child1, 10f)
    Yoga.YGNodeInsertChild(root_child1, root_child1_child1, 1)
    val root_child1_child2 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetWidth(root_child1_child2, 25f)
    Yoga.YGNodeStyleSetHeight(root_child1_child2, 20f)
    Yoga.YGNodeInsertChild(root_child1, root_child1_child2, 2)
    val root_child1_child3 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetWidth(root_child1_child3, 25f)
    Yoga.YGNodeStyleSetHeight(root_child1_child3, 10f)
    Yoga.YGNodeInsertChild(root_child1, root_child1_child3, 3)
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
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(60f, Yoga.YGNodeLayoutGetHeight(root_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetLeft(root_child1))
    assertEquals(40f, Yoga.YGNodeLayoutGetTop(root_child1))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child1))
    assertEquals(25f, Yoga.YGNodeLayoutGetHeight(root_child1))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child1_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child1_child0))
    assertEquals(25f, Yoga.YGNodeLayoutGetWidth(root_child1_child0))
    assertEquals(20f, Yoga.YGNodeLayoutGetHeight(root_child1_child0))
    assertEquals(25f, Yoga.YGNodeLayoutGetLeft(root_child1_child1))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child1_child1))
    assertEquals(25f, Yoga.YGNodeLayoutGetWidth(root_child1_child1))
    assertEquals(10f, Yoga.YGNodeLayoutGetHeight(root_child1_child1))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child1_child2))
    assertEquals(20f, Yoga.YGNodeLayoutGetTop(root_child1_child2))
    assertEquals(25f, Yoga.YGNodeLayoutGetWidth(root_child1_child2))
    assertEquals(20f, Yoga.YGNodeLayoutGetHeight(root_child1_child2))
    assertEquals(25f, Yoga.YGNodeLayoutGetLeft(root_child1_child3))
    assertEquals(20f, Yoga.YGNodeLayoutGetTop(root_child1_child3))
    assertEquals(25f, Yoga.YGNodeLayoutGetWidth(root_child1_child3))
    assertEquals(10f, Yoga.YGNodeLayoutGetHeight(root_child1_child3))
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
    assertEquals(50f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(60f, Yoga.YGNodeLayoutGetHeight(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child1))
    assertEquals(40f, Yoga.YGNodeLayoutGetTop(root_child1))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child1))
    assertEquals(25f, Yoga.YGNodeLayoutGetHeight(root_child1))
    assertEquals(25f, Yoga.YGNodeLayoutGetLeft(root_child1_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child1_child0))
    assertEquals(25f, Yoga.YGNodeLayoutGetWidth(root_child1_child0))
    assertEquals(20f, Yoga.YGNodeLayoutGetHeight(root_child1_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child1_child1))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child1_child1))
    assertEquals(25f, Yoga.YGNodeLayoutGetWidth(root_child1_child1))
    assertEquals(10f, Yoga.YGNodeLayoutGetHeight(root_child1_child1))
    assertEquals(25f, Yoga.YGNodeLayoutGetLeft(root_child1_child2))
    assertEquals(20f, Yoga.YGNodeLayoutGetTop(root_child1_child2))
    assertEquals(25f, Yoga.YGNodeLayoutGetWidth(root_child1_child2))
    assertEquals(20f, Yoga.YGNodeLayoutGetHeight(root_child1_child2))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child1_child3))
    assertEquals(20f, Yoga.YGNodeLayoutGetTop(root_child1_child3))
    assertEquals(25f, Yoga.YGNodeLayoutGetWidth(root_child1_child3))
    assertEquals(10f, Yoga.YGNodeLayoutGetHeight(root_child1_child3))
    Yoga.YGNodeFreeRecursive(root)
  }

  @Test
  fun align_baseline_child_multiline_override() {
    val config = Yoga.YGConfigNew()
    val root = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetFlexDirection(root, YGFlexDirection.YGFlexDirectionRow)
    Yoga.YGNodeStyleSetAlignItems(root, YGAlign.YGAlignBaseline)
    Yoga.YGNodeStyleSetWidth(root, 100f)
    Yoga.YGNodeStyleSetHeight(root, 100f)
    val root_child0 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetWidth(root_child0, 50f)
    Yoga.YGNodeStyleSetHeight(root_child0, 60f)
    Yoga.YGNodeInsertChild(root, root_child0, 0)
    val root_child1 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetFlexDirection(root_child1, YGFlexDirection.YGFlexDirectionRow)
    Yoga.YGNodeStyleSetFlexWrap(root_child1, YGWrap.YGWrapWrap)
    Yoga.YGNodeStyleSetWidth(root_child1, 50f)
    Yoga.YGNodeStyleSetHeight(root_child1, 25f)
    Yoga.YGNodeInsertChild(root, root_child1, 1)
    val root_child1_child0 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetWidth(root_child1_child0, 25f)
    Yoga.YGNodeStyleSetHeight(root_child1_child0, 20f)
    Yoga.YGNodeInsertChild(root_child1, root_child1_child0, 0)
    val root_child1_child1 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetAlignSelf(root_child1_child1, YGAlign.YGAlignBaseline)
    Yoga.YGNodeStyleSetWidth(root_child1_child1, 25f)
    Yoga.YGNodeStyleSetHeight(root_child1_child1, 10f)
    Yoga.YGNodeInsertChild(root_child1, root_child1_child1, 1)
    val root_child1_child2 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetWidth(root_child1_child2, 25f)
    Yoga.YGNodeStyleSetHeight(root_child1_child2, 20f)
    Yoga.YGNodeInsertChild(root_child1, root_child1_child2, 2)
    val root_child1_child3 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetAlignSelf(root_child1_child3, YGAlign.YGAlignBaseline)
    Yoga.YGNodeStyleSetWidth(root_child1_child3, 25f)
    Yoga.YGNodeStyleSetHeight(root_child1_child3, 10f)
    Yoga.YGNodeInsertChild(root_child1, root_child1_child3, 3)
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
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(60f, Yoga.YGNodeLayoutGetHeight(root_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetLeft(root_child1))
    assertEquals(50f, Yoga.YGNodeLayoutGetTop(root_child1))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child1))
    assertEquals(25f, Yoga.YGNodeLayoutGetHeight(root_child1))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child1_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child1_child0))
    assertEquals(25f, Yoga.YGNodeLayoutGetWidth(root_child1_child0))
    assertEquals(20f, Yoga.YGNodeLayoutGetHeight(root_child1_child0))
    assertEquals(25f, Yoga.YGNodeLayoutGetLeft(root_child1_child1))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child1_child1))
    assertEquals(25f, Yoga.YGNodeLayoutGetWidth(root_child1_child1))
    assertEquals(10f, Yoga.YGNodeLayoutGetHeight(root_child1_child1))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child1_child2))
    assertEquals(20f, Yoga.YGNodeLayoutGetTop(root_child1_child2))
    assertEquals(25f, Yoga.YGNodeLayoutGetWidth(root_child1_child2))
    assertEquals(20f, Yoga.YGNodeLayoutGetHeight(root_child1_child2))
    assertEquals(25f, Yoga.YGNodeLayoutGetLeft(root_child1_child3))
    assertEquals(20f, Yoga.YGNodeLayoutGetTop(root_child1_child3))
    assertEquals(25f, Yoga.YGNodeLayoutGetWidth(root_child1_child3))
    assertEquals(10f, Yoga.YGNodeLayoutGetHeight(root_child1_child3))
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
    assertEquals(50f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(60f, Yoga.YGNodeLayoutGetHeight(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child1))
    assertEquals(50f, Yoga.YGNodeLayoutGetTop(root_child1))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child1))
    assertEquals(25f, Yoga.YGNodeLayoutGetHeight(root_child1))
    assertEquals(25f, Yoga.YGNodeLayoutGetLeft(root_child1_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child1_child0))
    assertEquals(25f, Yoga.YGNodeLayoutGetWidth(root_child1_child0))
    assertEquals(20f, Yoga.YGNodeLayoutGetHeight(root_child1_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child1_child1))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child1_child1))
    assertEquals(25f, Yoga.YGNodeLayoutGetWidth(root_child1_child1))
    assertEquals(10f, Yoga.YGNodeLayoutGetHeight(root_child1_child1))
    assertEquals(25f, Yoga.YGNodeLayoutGetLeft(root_child1_child2))
    assertEquals(20f, Yoga.YGNodeLayoutGetTop(root_child1_child2))
    assertEquals(25f, Yoga.YGNodeLayoutGetWidth(root_child1_child2))
    assertEquals(20f, Yoga.YGNodeLayoutGetHeight(root_child1_child2))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child1_child3))
    assertEquals(20f, Yoga.YGNodeLayoutGetTop(root_child1_child3))
    assertEquals(25f, Yoga.YGNodeLayoutGetWidth(root_child1_child3))
    assertEquals(10f, Yoga.YGNodeLayoutGetHeight(root_child1_child3))
    Yoga.YGNodeFreeRecursive(root)
  }

  @Test
  fun align_baseline_child_multiline_no_override_on_secondline() {
    val config = Yoga.YGConfigNew()
    val root = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetFlexDirection(root, YGFlexDirection.YGFlexDirectionRow)
    Yoga.YGNodeStyleSetAlignItems(root, YGAlign.YGAlignBaseline)
    Yoga.YGNodeStyleSetWidth(root, 100f)
    Yoga.YGNodeStyleSetHeight(root, 100f)
    val root_child0 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetWidth(root_child0, 50f)
    Yoga.YGNodeStyleSetHeight(root_child0, 60f)
    Yoga.YGNodeInsertChild(root, root_child0, 0)
    val root_child1 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetFlexDirection(root_child1, YGFlexDirection.YGFlexDirectionRow)
    Yoga.YGNodeStyleSetFlexWrap(root_child1, YGWrap.YGWrapWrap)
    Yoga.YGNodeStyleSetWidth(root_child1, 50f)
    Yoga.YGNodeStyleSetHeight(root_child1, 25f)
    Yoga.YGNodeInsertChild(root, root_child1, 1)
    val root_child1_child0 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetWidth(root_child1_child0, 25f)
    Yoga.YGNodeStyleSetHeight(root_child1_child0, 20f)
    Yoga.YGNodeInsertChild(root_child1, root_child1_child0, 0)
    val root_child1_child1 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetWidth(root_child1_child1, 25f)
    Yoga.YGNodeStyleSetHeight(root_child1_child1, 10f)
    Yoga.YGNodeInsertChild(root_child1, root_child1_child1, 1)
    val root_child1_child2 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetWidth(root_child1_child2, 25f)
    Yoga.YGNodeStyleSetHeight(root_child1_child2, 20f)
    Yoga.YGNodeInsertChild(root_child1, root_child1_child2, 2)
    val root_child1_child3 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetAlignSelf(root_child1_child3, YGAlign.YGAlignBaseline)
    Yoga.YGNodeStyleSetWidth(root_child1_child3, 25f)
    Yoga.YGNodeStyleSetHeight(root_child1_child3, 10f)
    Yoga.YGNodeInsertChild(root_child1, root_child1_child3, 3)
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
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(60f, Yoga.YGNodeLayoutGetHeight(root_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetLeft(root_child1))
    assertEquals(40f, Yoga.YGNodeLayoutGetTop(root_child1))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child1))
    assertEquals(25f, Yoga.YGNodeLayoutGetHeight(root_child1))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child1_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child1_child0))
    assertEquals(25f, Yoga.YGNodeLayoutGetWidth(root_child1_child0))
    assertEquals(20f, Yoga.YGNodeLayoutGetHeight(root_child1_child0))
    assertEquals(25f, Yoga.YGNodeLayoutGetLeft(root_child1_child1))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child1_child1))
    assertEquals(25f, Yoga.YGNodeLayoutGetWidth(root_child1_child1))
    assertEquals(10f, Yoga.YGNodeLayoutGetHeight(root_child1_child1))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child1_child2))
    assertEquals(20f, Yoga.YGNodeLayoutGetTop(root_child1_child2))
    assertEquals(25f, Yoga.YGNodeLayoutGetWidth(root_child1_child2))
    assertEquals(20f, Yoga.YGNodeLayoutGetHeight(root_child1_child2))
    assertEquals(25f, Yoga.YGNodeLayoutGetLeft(root_child1_child3))
    assertEquals(20f, Yoga.YGNodeLayoutGetTop(root_child1_child3))
    assertEquals(25f, Yoga.YGNodeLayoutGetWidth(root_child1_child3))
    assertEquals(10f, Yoga.YGNodeLayoutGetHeight(root_child1_child3))
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
    assertEquals(50f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(60f, Yoga.YGNodeLayoutGetHeight(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child1))
    assertEquals(40f, Yoga.YGNodeLayoutGetTop(root_child1))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child1))
    assertEquals(25f, Yoga.YGNodeLayoutGetHeight(root_child1))
    assertEquals(25f, Yoga.YGNodeLayoutGetLeft(root_child1_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child1_child0))
    assertEquals(25f, Yoga.YGNodeLayoutGetWidth(root_child1_child0))
    assertEquals(20f, Yoga.YGNodeLayoutGetHeight(root_child1_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child1_child1))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child1_child1))
    assertEquals(25f, Yoga.YGNodeLayoutGetWidth(root_child1_child1))
    assertEquals(10f, Yoga.YGNodeLayoutGetHeight(root_child1_child1))
    assertEquals(25f, Yoga.YGNodeLayoutGetLeft(root_child1_child2))
    assertEquals(20f, Yoga.YGNodeLayoutGetTop(root_child1_child2))
    assertEquals(25f, Yoga.YGNodeLayoutGetWidth(root_child1_child2))
    assertEquals(20f, Yoga.YGNodeLayoutGetHeight(root_child1_child2))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child1_child3))
    assertEquals(20f, Yoga.YGNodeLayoutGetTop(root_child1_child3))
    assertEquals(25f, Yoga.YGNodeLayoutGetWidth(root_child1_child3))
    assertEquals(10f, Yoga.YGNodeLayoutGetHeight(root_child1_child3))
    Yoga.YGNodeFreeRecursive(root)
  }

  @Test
  fun align_baseline_child_top() {
    val config = Yoga.YGConfigNew()
    val root = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetFlexDirection(root, YGFlexDirection.YGFlexDirectionRow)
    Yoga.YGNodeStyleSetAlignItems(root, YGAlign.YGAlignBaseline)
    Yoga.YGNodeStyleSetWidth(root, 100f)
    Yoga.YGNodeStyleSetHeight(root, 100f)
    val root_child0 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetPosition(root_child0, YGEdge.YGEdgeTop, 10f)
    Yoga.YGNodeStyleSetWidth(root_child0, 50f)
    Yoga.YGNodeStyleSetHeight(root_child0, 50f)
    Yoga.YGNodeInsertChild(root, root_child0, 0)
    val root_child1 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetWidth(root_child1, 50f)
    Yoga.YGNodeStyleSetHeight(root_child1, 20f)
    Yoga.YGNodeInsertChild(root, root_child1, 1)
    val root_child1_child0 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetWidth(root_child1_child0, 50f)
    Yoga.YGNodeStyleSetHeight(root_child1_child0, 10f)
    Yoga.YGNodeInsertChild(root_child1, root_child1_child0, 0)
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
    assertEquals(10f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetHeight(root_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetLeft(root_child1))
    assertEquals(40f, Yoga.YGNodeLayoutGetTop(root_child1))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child1))
    assertEquals(20f, Yoga.YGNodeLayoutGetHeight(root_child1))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child1_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child1_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child1_child0))
    assertEquals(10f, Yoga.YGNodeLayoutGetHeight(root_child1_child0))
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
    assertEquals(50f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(10f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetHeight(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child1))
    assertEquals(40f, Yoga.YGNodeLayoutGetTop(root_child1))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child1))
    assertEquals(20f, Yoga.YGNodeLayoutGetHeight(root_child1))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child1_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child1_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child1_child0))
    assertEquals(10f, Yoga.YGNodeLayoutGetHeight(root_child1_child0))
    Yoga.YGNodeFreeRecursive(root)
  }

  @Test
  fun align_baseline_child_top2() {
    val config = Yoga.YGConfigNew()
    val root = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetFlexDirection(root, YGFlexDirection.YGFlexDirectionRow)
    Yoga.YGNodeStyleSetAlignItems(root, YGAlign.YGAlignBaseline)
    Yoga.YGNodeStyleSetWidth(root, 100f)
    Yoga.YGNodeStyleSetHeight(root, 100f)
    val root_child0 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetWidth(root_child0, 50f)
    Yoga.YGNodeStyleSetHeight(root_child0, 50f)
    Yoga.YGNodeInsertChild(root, root_child0, 0)
    val root_child1 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetPosition(root_child1, YGEdge.YGEdgeTop, 5f)
    Yoga.YGNodeStyleSetWidth(root_child1, 50f)
    Yoga.YGNodeStyleSetHeight(root_child1, 20f)
    Yoga.YGNodeInsertChild(root, root_child1, 1)
    val root_child1_child0 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetWidth(root_child1_child0, 50f)
    Yoga.YGNodeStyleSetHeight(root_child1_child0, 10f)
    Yoga.YGNodeInsertChild(root_child1, root_child1_child0, 0)
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
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetHeight(root_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetLeft(root_child1))
    assertEquals(45f, Yoga.YGNodeLayoutGetTop(root_child1))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child1))
    assertEquals(20f, Yoga.YGNodeLayoutGetHeight(root_child1))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child1_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child1_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child1_child0))
    assertEquals(10f, Yoga.YGNodeLayoutGetHeight(root_child1_child0))
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
    assertEquals(50f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetHeight(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child1))
    assertEquals(45f, Yoga.YGNodeLayoutGetTop(root_child1))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child1))
    assertEquals(20f, Yoga.YGNodeLayoutGetHeight(root_child1))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child1_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child1_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child1_child0))
    assertEquals(10f, Yoga.YGNodeLayoutGetHeight(root_child1_child0))
    Yoga.YGNodeFreeRecursive(root)
  }

  @Test
  fun align_baseline_double_nested_child() {
    val config = Yoga.YGConfigNew()
    val root = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetFlexDirection(root, YGFlexDirection.YGFlexDirectionRow)
    Yoga.YGNodeStyleSetAlignItems(root, YGAlign.YGAlignBaseline)
    Yoga.YGNodeStyleSetWidth(root, 100f)
    Yoga.YGNodeStyleSetHeight(root, 100f)
    val root_child0 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetWidth(root_child0, 50f)
    Yoga.YGNodeStyleSetHeight(root_child0, 50f)
    Yoga.YGNodeInsertChild(root, root_child0, 0)
    val root_child0_child0 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetWidth(root_child0_child0, 50f)
    Yoga.YGNodeStyleSetHeight(root_child0_child0, 20f)
    Yoga.YGNodeInsertChild(root_child0, root_child0_child0, 0)
    val root_child1 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetWidth(root_child1, 50f)
    Yoga.YGNodeStyleSetHeight(root_child1, 20f)
    Yoga.YGNodeInsertChild(root, root_child1, 1)
    val root_child1_child0 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetWidth(root_child1_child0, 50f)
    Yoga.YGNodeStyleSetHeight(root_child1_child0, 15f)
    Yoga.YGNodeInsertChild(root_child1, root_child1_child0, 0)
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
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetHeight(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child0_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child0_child0))
    assertEquals(20f, Yoga.YGNodeLayoutGetHeight(root_child0_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetLeft(root_child1))
    assertEquals(5f, Yoga.YGNodeLayoutGetTop(root_child1))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child1))
    assertEquals(20f, Yoga.YGNodeLayoutGetHeight(root_child1))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child1_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child1_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child1_child0))
    assertEquals(15f, Yoga.YGNodeLayoutGetHeight(root_child1_child0))
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
    assertEquals(50f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetHeight(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child0_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child0_child0))
    assertEquals(20f, Yoga.YGNodeLayoutGetHeight(root_child0_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child1))
    assertEquals(5f, Yoga.YGNodeLayoutGetTop(root_child1))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child1))
    assertEquals(20f, Yoga.YGNodeLayoutGetHeight(root_child1))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child1_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child1_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child1_child0))
    assertEquals(15f, Yoga.YGNodeLayoutGetHeight(root_child1_child0))
    Yoga.YGNodeFreeRecursive(root)
  }

  @Test
  fun align_baseline_column() {
    val config = Yoga.YGConfigNew()
    val root = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetAlignItems(root, YGAlign.YGAlignBaseline)
    Yoga.YGNodeStyleSetWidth(root, 100f)
    Yoga.YGNodeStyleSetHeight(root, 100f)
    val root_child0 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetWidth(root_child0, 50f)
    Yoga.YGNodeStyleSetHeight(root_child0, 50f)
    Yoga.YGNodeInsertChild(root, root_child0, 0)
    val root_child1 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetWidth(root_child1, 50f)
    Yoga.YGNodeStyleSetHeight(root_child1, 20f)
    Yoga.YGNodeInsertChild(root, root_child1, 1)
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
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetHeight(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child1))
    assertEquals(50f, Yoga.YGNodeLayoutGetTop(root_child1))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child1))
    assertEquals(20f, Yoga.YGNodeLayoutGetHeight(root_child1))
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
    assertEquals(50f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetHeight(root_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetLeft(root_child1))
    assertEquals(50f, Yoga.YGNodeLayoutGetTop(root_child1))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child1))
    assertEquals(20f, Yoga.YGNodeLayoutGetHeight(root_child1))
    Yoga.YGNodeFreeRecursive(root)
  }

  @Test
  fun align_baseline_child_margin() {
    val config = Yoga.YGConfigNew()
    val root = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetFlexDirection(root, YGFlexDirection.YGFlexDirectionRow)
    Yoga.YGNodeStyleSetAlignItems(root, YGAlign.YGAlignBaseline)
    Yoga.YGNodeStyleSetWidth(root, 100f)
    Yoga.YGNodeStyleSetHeight(root, 100f)
    val root_child0 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetMargin(root_child0, YGEdge.YGEdgeLeft, 5f)
    Yoga.YGNodeStyleSetMargin(root_child0, YGEdge.YGEdgeTop, 5f)
    Yoga.YGNodeStyleSetMargin(root_child0, YGEdge.YGEdgeRight, 5f)
    Yoga.YGNodeStyleSetMargin(root_child0, YGEdge.YGEdgeBottom, 5f)
    Yoga.YGNodeStyleSetWidth(root_child0, 50f)
    Yoga.YGNodeStyleSetHeight(root_child0, 50f)
    Yoga.YGNodeInsertChild(root, root_child0, 0)
    val root_child1 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetWidth(root_child1, 50f)
    Yoga.YGNodeStyleSetHeight(root_child1, 20f)
    Yoga.YGNodeInsertChild(root, root_child1, 1)
    val root_child1_child0 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetMargin(root_child1_child0, YGEdge.YGEdgeLeft, 1f)
    Yoga.YGNodeStyleSetMargin(root_child1_child0, YGEdge.YGEdgeTop, 1f)
    Yoga.YGNodeStyleSetMargin(root_child1_child0, YGEdge.YGEdgeRight, 1f)
    Yoga.YGNodeStyleSetMargin(root_child1_child0, YGEdge.YGEdgeBottom, 1f)
    Yoga.YGNodeStyleSetWidth(root_child1_child0, 50f)
    Yoga.YGNodeStyleSetHeight(root_child1_child0, 10f)
    Yoga.YGNodeInsertChild(root_child1, root_child1_child0, 0)
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
    assertEquals(5f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(5f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetHeight(root_child0))
    assertEquals(60f, Yoga.YGNodeLayoutGetLeft(root_child1))
    assertEquals(44f, Yoga.YGNodeLayoutGetTop(root_child1))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child1))
    assertEquals(20f, Yoga.YGNodeLayoutGetHeight(root_child1))
    assertEquals(1f, Yoga.YGNodeLayoutGetLeft(root_child1_child0))
    assertEquals(1f, Yoga.YGNodeLayoutGetTop(root_child1_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child1_child0))
    assertEquals(10f, Yoga.YGNodeLayoutGetHeight(root_child1_child0))
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
    assertEquals(45f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(5f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetHeight(root_child0))
    assertEquals(-10f, Yoga.YGNodeLayoutGetLeft(root_child1))
    assertEquals(44f, Yoga.YGNodeLayoutGetTop(root_child1))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child1))
    assertEquals(20f, Yoga.YGNodeLayoutGetHeight(root_child1))
    assertEquals(-1f, Yoga.YGNodeLayoutGetLeft(root_child1_child0))
    assertEquals(1f, Yoga.YGNodeLayoutGetTop(root_child1_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child1_child0))
    assertEquals(10f, Yoga.YGNodeLayoutGetHeight(root_child1_child0))
    Yoga.YGNodeFreeRecursive(root)
  }

  @Test
  fun align_baseline_child_padding() {
    val config = Yoga.YGConfigNew()
    val root = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetFlexDirection(root, YGFlexDirection.YGFlexDirectionRow)
    Yoga.YGNodeStyleSetAlignItems(root, YGAlign.YGAlignBaseline)
    Yoga.YGNodeStyleSetPadding(root, YGEdge.YGEdgeLeft, 5f)
    Yoga.YGNodeStyleSetPadding(root, YGEdge.YGEdgeTop, 5f)
    Yoga.YGNodeStyleSetPadding(root, YGEdge.YGEdgeRight, 5f)
    Yoga.YGNodeStyleSetPadding(root, YGEdge.YGEdgeBottom, 5f)
    Yoga.YGNodeStyleSetWidth(root, 100f)
    Yoga.YGNodeStyleSetHeight(root, 100f)
    val root_child0 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetWidth(root_child0, 50f)
    Yoga.YGNodeStyleSetHeight(root_child0, 50f)
    Yoga.YGNodeInsertChild(root, root_child0, 0)
    val root_child1 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetPadding(root_child1, YGEdge.YGEdgeLeft, 5f)
    Yoga.YGNodeStyleSetPadding(root_child1, YGEdge.YGEdgeTop, 5f)
    Yoga.YGNodeStyleSetPadding(root_child1, YGEdge.YGEdgeRight, 5f)
    Yoga.YGNodeStyleSetPadding(root_child1, YGEdge.YGEdgeBottom, 5f)
    Yoga.YGNodeStyleSetWidth(root_child1, 50f)
    Yoga.YGNodeStyleSetHeight(root_child1, 20f)
    Yoga.YGNodeInsertChild(root, root_child1, 1)
    val root_child1_child0 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetWidth(root_child1_child0, 50f)
    Yoga.YGNodeStyleSetHeight(root_child1_child0, 10f)
    Yoga.YGNodeInsertChild(root_child1, root_child1_child0, 0)
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
    assertEquals(5f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(5f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetHeight(root_child0))
    assertEquals(55f, Yoga.YGNodeLayoutGetLeft(root_child1))
    assertEquals(40f, Yoga.YGNodeLayoutGetTop(root_child1))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child1))
    assertEquals(20f, Yoga.YGNodeLayoutGetHeight(root_child1))
    assertEquals(5f, Yoga.YGNodeLayoutGetLeft(root_child1_child0))
    assertEquals(5f, Yoga.YGNodeLayoutGetTop(root_child1_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child1_child0))
    assertEquals(10f, Yoga.YGNodeLayoutGetHeight(root_child1_child0))
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
    assertEquals(45f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(5f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetHeight(root_child0))
    assertEquals(-5f, Yoga.YGNodeLayoutGetLeft(root_child1))
    assertEquals(40f, Yoga.YGNodeLayoutGetTop(root_child1))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child1))
    assertEquals(20f, Yoga.YGNodeLayoutGetHeight(root_child1))
    assertEquals(-5f, Yoga.YGNodeLayoutGetLeft(root_child1_child0))
    assertEquals(5f, Yoga.YGNodeLayoutGetTop(root_child1_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child1_child0))
    assertEquals(10f, Yoga.YGNodeLayoutGetHeight(root_child1_child0))
    Yoga.YGNodeFreeRecursive(root)
  }

  @Test
  fun align_baseline_multiline() {
    val config = Yoga.YGConfigNew()
    val root = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetFlexDirection(root, YGFlexDirection.YGFlexDirectionRow)
    Yoga.YGNodeStyleSetAlignItems(root, YGAlign.YGAlignBaseline)
    Yoga.YGNodeStyleSetFlexWrap(root, YGWrap.YGWrapWrap)
    Yoga.YGNodeStyleSetWidth(root, 100f)
    Yoga.YGNodeStyleSetHeight(root, 100f)
    val root_child0 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetWidth(root_child0, 50f)
    Yoga.YGNodeStyleSetHeight(root_child0, 50f)
    Yoga.YGNodeInsertChild(root, root_child0, 0)
    val root_child1 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetWidth(root_child1, 50f)
    Yoga.YGNodeStyleSetHeight(root_child1, 20f)
    Yoga.YGNodeInsertChild(root, root_child1, 1)
    val root_child1_child0 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetWidth(root_child1_child0, 50f)
    Yoga.YGNodeStyleSetHeight(root_child1_child0, 10f)
    Yoga.YGNodeInsertChild(root_child1, root_child1_child0, 0)
    val root_child2 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetWidth(root_child2, 50f)
    Yoga.YGNodeStyleSetHeight(root_child2, 20f)
    Yoga.YGNodeInsertChild(root, root_child2, 2)
    val root_child2_child0 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetWidth(root_child2_child0, 50f)
    Yoga.YGNodeStyleSetHeight(root_child2_child0, 10f)
    Yoga.YGNodeInsertChild(root_child2, root_child2_child0, 0)
    val root_child3 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetWidth(root_child3, 50f)
    Yoga.YGNodeStyleSetHeight(root_child3, 50f)
    Yoga.YGNodeInsertChild(root, root_child3, 3)
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
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetHeight(root_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetLeft(root_child1))
    assertEquals(40f, Yoga.YGNodeLayoutGetTop(root_child1))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child1))
    assertEquals(20f, Yoga.YGNodeLayoutGetHeight(root_child1))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child1_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child1_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child1_child0))
    assertEquals(10f, Yoga.YGNodeLayoutGetHeight(root_child1_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child2))
    assertEquals(100f, Yoga.YGNodeLayoutGetTop(root_child2))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child2))
    assertEquals(20f, Yoga.YGNodeLayoutGetHeight(root_child2))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child2_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child2_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child2_child0))
    assertEquals(10f, Yoga.YGNodeLayoutGetHeight(root_child2_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetLeft(root_child3))
    assertEquals(60f, Yoga.YGNodeLayoutGetTop(root_child3))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child3))
    assertEquals(50f, Yoga.YGNodeLayoutGetHeight(root_child3))
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
    assertEquals(50f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetHeight(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child1))
    assertEquals(40f, Yoga.YGNodeLayoutGetTop(root_child1))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child1))
    assertEquals(20f, Yoga.YGNodeLayoutGetHeight(root_child1))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child1_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child1_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child1_child0))
    assertEquals(10f, Yoga.YGNodeLayoutGetHeight(root_child1_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetLeft(root_child2))
    assertEquals(100f, Yoga.YGNodeLayoutGetTop(root_child2))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child2))
    assertEquals(20f, Yoga.YGNodeLayoutGetHeight(root_child2))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child2_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child2_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child2_child0))
    assertEquals(10f, Yoga.YGNodeLayoutGetHeight(root_child2_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child3))
    assertEquals(60f, Yoga.YGNodeLayoutGetTop(root_child3))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child3))
    assertEquals(50f, Yoga.YGNodeLayoutGetHeight(root_child3))
    Yoga.YGNodeFreeRecursive(root)
  }

  @Test
  fun align_baseline_multiline_column() {
    val config = Yoga.YGConfigNew()
    val root = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetAlignItems(root, YGAlign.YGAlignBaseline)
    Yoga.YGNodeStyleSetFlexWrap(root, YGWrap.YGWrapWrap)
    Yoga.YGNodeStyleSetWidth(root, 100f)
    Yoga.YGNodeStyleSetHeight(root, 100f)
    val root_child0 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetWidth(root_child0, 50f)
    Yoga.YGNodeStyleSetHeight(root_child0, 50f)
    Yoga.YGNodeInsertChild(root, root_child0, 0)
    val root_child1 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetWidth(root_child1, 30f)
    Yoga.YGNodeStyleSetHeight(root_child1, 50f)
    Yoga.YGNodeInsertChild(root, root_child1, 1)
    val root_child1_child0 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetWidth(root_child1_child0, 20f)
    Yoga.YGNodeStyleSetHeight(root_child1_child0, 20f)
    Yoga.YGNodeInsertChild(root_child1, root_child1_child0, 0)
    val root_child2 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetWidth(root_child2, 40f)
    Yoga.YGNodeStyleSetHeight(root_child2, 70f)
    Yoga.YGNodeInsertChild(root, root_child2, 2)
    val root_child2_child0 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetWidth(root_child2_child0, 10f)
    Yoga.YGNodeStyleSetHeight(root_child2_child0, 10f)
    Yoga.YGNodeInsertChild(root_child2, root_child2_child0, 0)
    val root_child3 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetWidth(root_child3, 50f)
    Yoga.YGNodeStyleSetHeight(root_child3, 20f)
    Yoga.YGNodeInsertChild(root, root_child3, 3)
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
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetHeight(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child1))
    assertEquals(50f, Yoga.YGNodeLayoutGetTop(root_child1))
    assertEquals(30f, Yoga.YGNodeLayoutGetWidth(root_child1))
    assertEquals(50f, Yoga.YGNodeLayoutGetHeight(root_child1))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child1_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child1_child0))
    assertEquals(20f, Yoga.YGNodeLayoutGetWidth(root_child1_child0))
    assertEquals(20f, Yoga.YGNodeLayoutGetHeight(root_child1_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetLeft(root_child2))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child2))
    assertEquals(40f, Yoga.YGNodeLayoutGetWidth(root_child2))
    assertEquals(70f, Yoga.YGNodeLayoutGetHeight(root_child2))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child2_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child2_child0))
    assertEquals(10f, Yoga.YGNodeLayoutGetWidth(root_child2_child0))
    assertEquals(10f, Yoga.YGNodeLayoutGetHeight(root_child2_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetLeft(root_child3))
    assertEquals(70f, Yoga.YGNodeLayoutGetTop(root_child3))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child3))
    assertEquals(20f, Yoga.YGNodeLayoutGetHeight(root_child3))
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
    assertEquals(50f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetHeight(root_child0))
    assertEquals(70f, Yoga.YGNodeLayoutGetLeft(root_child1))
    assertEquals(50f, Yoga.YGNodeLayoutGetTop(root_child1))
    assertEquals(30f, Yoga.YGNodeLayoutGetWidth(root_child1))
    assertEquals(50f, Yoga.YGNodeLayoutGetHeight(root_child1))
    assertEquals(10f, Yoga.YGNodeLayoutGetLeft(root_child1_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child1_child0))
    assertEquals(20f, Yoga.YGNodeLayoutGetWidth(root_child1_child0))
    assertEquals(20f, Yoga.YGNodeLayoutGetHeight(root_child1_child0))
    assertEquals(10f, Yoga.YGNodeLayoutGetLeft(root_child2))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child2))
    assertEquals(40f, Yoga.YGNodeLayoutGetWidth(root_child2))
    assertEquals(70f, Yoga.YGNodeLayoutGetHeight(root_child2))
    assertEquals(30f, Yoga.YGNodeLayoutGetLeft(root_child2_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child2_child0))
    assertEquals(10f, Yoga.YGNodeLayoutGetWidth(root_child2_child0))
    assertEquals(10f, Yoga.YGNodeLayoutGetHeight(root_child2_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child3))
    assertEquals(70f, Yoga.YGNodeLayoutGetTop(root_child3))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child3))
    assertEquals(20f, Yoga.YGNodeLayoutGetHeight(root_child3))
    Yoga.YGNodeFreeRecursive(root)
  }

  @Test
  fun align_baseline_multiline_column2() {
    val config = Yoga.YGConfigNew()
    val root = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetAlignItems(root, YGAlign.YGAlignBaseline)
    Yoga.YGNodeStyleSetFlexWrap(root, YGWrap.YGWrapWrap)
    Yoga.YGNodeStyleSetWidth(root, 100f)
    Yoga.YGNodeStyleSetHeight(root, 100f)
    val root_child0 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetWidth(root_child0, 50f)
    Yoga.YGNodeStyleSetHeight(root_child0, 50f)
    Yoga.YGNodeInsertChild(root, root_child0, 0)
    val root_child1 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetWidth(root_child1, 30f)
    Yoga.YGNodeStyleSetHeight(root_child1, 50f)
    Yoga.YGNodeInsertChild(root, root_child1, 1)
    val root_child1_child0 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetWidth(root_child1_child0, 20f)
    Yoga.YGNodeStyleSetHeight(root_child1_child0, 20f)
    Yoga.YGNodeInsertChild(root_child1, root_child1_child0, 0)
    val root_child2 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetWidth(root_child2, 40f)
    Yoga.YGNodeStyleSetHeight(root_child2, 70f)
    Yoga.YGNodeInsertChild(root, root_child2, 2)
    val root_child2_child0 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetWidth(root_child2_child0, 10f)
    Yoga.YGNodeStyleSetHeight(root_child2_child0, 10f)
    Yoga.YGNodeInsertChild(root_child2, root_child2_child0, 0)
    val root_child3 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetWidth(root_child3, 50f)
    Yoga.YGNodeStyleSetHeight(root_child3, 20f)
    Yoga.YGNodeInsertChild(root, root_child3, 3)
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
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetHeight(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child1))
    assertEquals(50f, Yoga.YGNodeLayoutGetTop(root_child1))
    assertEquals(30f, Yoga.YGNodeLayoutGetWidth(root_child1))
    assertEquals(50f, Yoga.YGNodeLayoutGetHeight(root_child1))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child1_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child1_child0))
    assertEquals(20f, Yoga.YGNodeLayoutGetWidth(root_child1_child0))
    assertEquals(20f, Yoga.YGNodeLayoutGetHeight(root_child1_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetLeft(root_child2))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child2))
    assertEquals(40f, Yoga.YGNodeLayoutGetWidth(root_child2))
    assertEquals(70f, Yoga.YGNodeLayoutGetHeight(root_child2))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child2_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child2_child0))
    assertEquals(10f, Yoga.YGNodeLayoutGetWidth(root_child2_child0))
    assertEquals(10f, Yoga.YGNodeLayoutGetHeight(root_child2_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetLeft(root_child3))
    assertEquals(70f, Yoga.YGNodeLayoutGetTop(root_child3))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child3))
    assertEquals(20f, Yoga.YGNodeLayoutGetHeight(root_child3))
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
    assertEquals(50f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetHeight(root_child0))
    assertEquals(70f, Yoga.YGNodeLayoutGetLeft(root_child1))
    assertEquals(50f, Yoga.YGNodeLayoutGetTop(root_child1))
    assertEquals(30f, Yoga.YGNodeLayoutGetWidth(root_child1))
    assertEquals(50f, Yoga.YGNodeLayoutGetHeight(root_child1))
    assertEquals(10f, Yoga.YGNodeLayoutGetLeft(root_child1_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child1_child0))
    assertEquals(20f, Yoga.YGNodeLayoutGetWidth(root_child1_child0))
    assertEquals(20f, Yoga.YGNodeLayoutGetHeight(root_child1_child0))
    assertEquals(10f, Yoga.YGNodeLayoutGetLeft(root_child2))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child2))
    assertEquals(40f, Yoga.YGNodeLayoutGetWidth(root_child2))
    assertEquals(70f, Yoga.YGNodeLayoutGetHeight(root_child2))
    assertEquals(30f, Yoga.YGNodeLayoutGetLeft(root_child2_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child2_child0))
    assertEquals(10f, Yoga.YGNodeLayoutGetWidth(root_child2_child0))
    assertEquals(10f, Yoga.YGNodeLayoutGetHeight(root_child2_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child3))
    assertEquals(70f, Yoga.YGNodeLayoutGetTop(root_child3))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child3))
    assertEquals(20f, Yoga.YGNodeLayoutGetHeight(root_child3))
    Yoga.YGNodeFreeRecursive(root)
  }

  @Test
  fun align_baseline_multiline_row_and_column() {
    val config = Yoga.YGConfigNew()
    val root = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetFlexDirection(root, YGFlexDirection.YGFlexDirectionRow)
    Yoga.YGNodeStyleSetAlignItems(root, YGAlign.YGAlignBaseline)
    Yoga.YGNodeStyleSetFlexWrap(root, YGWrap.YGWrapWrap)
    Yoga.YGNodeStyleSetWidth(root, 100f)
    Yoga.YGNodeStyleSetHeight(root, 100f)
    val root_child0 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetWidth(root_child0, 50f)
    Yoga.YGNodeStyleSetHeight(root_child0, 50f)
    Yoga.YGNodeInsertChild(root, root_child0, 0)
    val root_child1 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetWidth(root_child1, 50f)
    Yoga.YGNodeStyleSetHeight(root_child1, 50f)
    Yoga.YGNodeInsertChild(root, root_child1, 1)
    val root_child1_child0 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetWidth(root_child1_child0, 50f)
    Yoga.YGNodeStyleSetHeight(root_child1_child0, 10f)
    Yoga.YGNodeInsertChild(root_child1, root_child1_child0, 0)
    val root_child2 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetWidth(root_child2, 50f)
    Yoga.YGNodeStyleSetHeight(root_child2, 20f)
    Yoga.YGNodeInsertChild(root, root_child2, 2)
    val root_child2_child0 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetWidth(root_child2_child0, 50f)
    Yoga.YGNodeStyleSetHeight(root_child2_child0, 10f)
    Yoga.YGNodeInsertChild(root_child2, root_child2_child0, 0)
    val root_child3 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetWidth(root_child3, 50f)
    Yoga.YGNodeStyleSetHeight(root_child3, 20f)
    Yoga.YGNodeInsertChild(root, root_child3, 3)
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
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetHeight(root_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetLeft(root_child1))
    assertEquals(40f, Yoga.YGNodeLayoutGetTop(root_child1))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child1))
    assertEquals(50f, Yoga.YGNodeLayoutGetHeight(root_child1))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child1_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child1_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child1_child0))
    assertEquals(10f, Yoga.YGNodeLayoutGetHeight(root_child1_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child2))
    assertEquals(100f, Yoga.YGNodeLayoutGetTop(root_child2))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child2))
    assertEquals(20f, Yoga.YGNodeLayoutGetHeight(root_child2))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child2_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child2_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child2_child0))
    assertEquals(10f, Yoga.YGNodeLayoutGetHeight(root_child2_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetLeft(root_child3))
    assertEquals(90f, Yoga.YGNodeLayoutGetTop(root_child3))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child3))
    assertEquals(20f, Yoga.YGNodeLayoutGetHeight(root_child3))
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
    assertEquals(50f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetHeight(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child1))
    assertEquals(40f, Yoga.YGNodeLayoutGetTop(root_child1))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child1))
    assertEquals(50f, Yoga.YGNodeLayoutGetHeight(root_child1))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child1_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child1_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child1_child0))
    assertEquals(10f, Yoga.YGNodeLayoutGetHeight(root_child1_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetLeft(root_child2))
    assertEquals(100f, Yoga.YGNodeLayoutGetTop(root_child2))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child2))
    assertEquals(20f, Yoga.YGNodeLayoutGetHeight(root_child2))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child2_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child2_child0))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child2_child0))
    assertEquals(10f, Yoga.YGNodeLayoutGetHeight(root_child2_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child3))
    assertEquals(90f, Yoga.YGNodeLayoutGetTop(root_child3))
    assertEquals(50f, Yoga.YGNodeLayoutGetWidth(root_child3))
    assertEquals(20f, Yoga.YGNodeLayoutGetHeight(root_child3))
    Yoga.YGNodeFreeRecursive(root)
  }

  @Test
  fun align_items_center_child_with_margin_bigger_than_parent() {
    val config = Yoga.YGConfigNew()
    val root = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetJustifyContent(root, YGJustify.YGJustifyCenter)
    Yoga.YGNodeStyleSetAlignItems(root, YGAlign.YGAlignCenter)
    Yoga.YGNodeStyleSetWidth(root, 52f)
    Yoga.YGNodeStyleSetHeight(root, 52f)
    val root_child0 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetAlignItems(root_child0, YGAlign.YGAlignCenter)
    Yoga.YGNodeInsertChild(root, root_child0, 0)
    val root_child0_child0 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetMargin(root_child0_child0, YGEdge.YGEdgeLeft, 10f)
    Yoga.YGNodeStyleSetMargin(root_child0_child0, YGEdge.YGEdgeRight, 10f)
    Yoga.YGNodeStyleSetWidth(root_child0_child0, 52f)
    Yoga.YGNodeStyleSetHeight(root_child0_child0, 52f)
    Yoga.YGNodeInsertChild(root_child0, root_child0_child0, 0)
    Yoga.YGNodeCalculateLayout(
      root,
      Yoga.YGUndefined,
      Yoga.YGUndefined,
      YGDirection.YGDirectionLTR,
    )
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root))
    assertEquals(52f, Yoga.YGNodeLayoutGetWidth(root))
    assertEquals(52f, Yoga.YGNodeLayoutGetHeight(root))
    assertEquals(-10f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(72f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(52f, Yoga.YGNodeLayoutGetHeight(root_child0))
    assertEquals(10f, Yoga.YGNodeLayoutGetLeft(root_child0_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0_child0))
    assertEquals(52f, Yoga.YGNodeLayoutGetWidth(root_child0_child0))
    assertEquals(52f, Yoga.YGNodeLayoutGetHeight(root_child0_child0))
    Yoga.YGNodeCalculateLayout(
      root,
      Yoga.YGUndefined,
      Yoga.YGUndefined,
      YGDirection.YGDirectionRTL,
    )
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root))
    assertEquals(52f, Yoga.YGNodeLayoutGetWidth(root))
    assertEquals(52f, Yoga.YGNodeLayoutGetHeight(root))
    assertEquals(-10f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(72f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(52f, Yoga.YGNodeLayoutGetHeight(root_child0))
    assertEquals(10f, Yoga.YGNodeLayoutGetLeft(root_child0_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0_child0))
    assertEquals(52f, Yoga.YGNodeLayoutGetWidth(root_child0_child0))
    assertEquals(52f, Yoga.YGNodeLayoutGetHeight(root_child0_child0))
    Yoga.YGNodeFreeRecursive(root)
  }

  @Test
  fun align_items_flex_end_child_with_margin_bigger_than_parent() {
    val config = Yoga.YGConfigNew()
    val root = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetJustifyContent(root, YGJustify.YGJustifyCenter)
    Yoga.YGNodeStyleSetAlignItems(root, YGAlign.YGAlignCenter)
    Yoga.YGNodeStyleSetWidth(root, 52f)
    Yoga.YGNodeStyleSetHeight(root, 52f)
    val root_child0 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetAlignItems(root_child0, YGAlign.YGAlignFlexEnd)
    Yoga.YGNodeInsertChild(root, root_child0, 0)
    val root_child0_child0 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetMargin(root_child0_child0, YGEdge.YGEdgeLeft, 10f)
    Yoga.YGNodeStyleSetMargin(root_child0_child0, YGEdge.YGEdgeRight, 10f)
    Yoga.YGNodeStyleSetWidth(root_child0_child0, 52f)
    Yoga.YGNodeStyleSetHeight(root_child0_child0, 52f)
    Yoga.YGNodeInsertChild(root_child0, root_child0_child0, 0)
    Yoga.YGNodeCalculateLayout(
      root,
      Yoga.YGUndefined,
      Yoga.YGUndefined,
      YGDirection.YGDirectionLTR,
    )
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root))
    assertEquals(52f, Yoga.YGNodeLayoutGetWidth(root))
    assertEquals(52f, Yoga.YGNodeLayoutGetHeight(root))
    assertEquals(-10f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(72f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(52f, Yoga.YGNodeLayoutGetHeight(root_child0))
    assertEquals(10f, Yoga.YGNodeLayoutGetLeft(root_child0_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0_child0))
    assertEquals(52f, Yoga.YGNodeLayoutGetWidth(root_child0_child0))
    assertEquals(52f, Yoga.YGNodeLayoutGetHeight(root_child0_child0))
    Yoga.YGNodeCalculateLayout(
      root,
      Yoga.YGUndefined,
      Yoga.YGUndefined,
      YGDirection.YGDirectionRTL,
    )
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root))
    assertEquals(52f, Yoga.YGNodeLayoutGetWidth(root))
    assertEquals(52f, Yoga.YGNodeLayoutGetHeight(root))
    assertEquals(-10f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(72f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(52f, Yoga.YGNodeLayoutGetHeight(root_child0))
    assertEquals(10f, Yoga.YGNodeLayoutGetLeft(root_child0_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0_child0))
    assertEquals(52f, Yoga.YGNodeLayoutGetWidth(root_child0_child0))
    assertEquals(52f, Yoga.YGNodeLayoutGetHeight(root_child0_child0))
    Yoga.YGNodeFreeRecursive(root)
  }

  @Test
  fun align_items_center_child_without_margin_bigger_than_parent() {
    val config = Yoga.YGConfigNew()
    val root = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetJustifyContent(root, YGJustify.YGJustifyCenter)
    Yoga.YGNodeStyleSetAlignItems(root, YGAlign.YGAlignCenter)
    Yoga.YGNodeStyleSetWidth(root, 52f)
    Yoga.YGNodeStyleSetHeight(root, 52f)
    val root_child0 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetAlignItems(root_child0, YGAlign.YGAlignCenter)
    Yoga.YGNodeInsertChild(root, root_child0, 0)
    val root_child0_child0 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetWidth(root_child0_child0, 72f)
    Yoga.YGNodeStyleSetHeight(root_child0_child0, 72f)
    Yoga.YGNodeInsertChild(root_child0, root_child0_child0, 0)
    Yoga.YGNodeCalculateLayout(
      root,
      Yoga.YGUndefined,
      Yoga.YGUndefined,
      YGDirection.YGDirectionLTR,
    )
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root))
    assertEquals(52f, Yoga.YGNodeLayoutGetWidth(root))
    assertEquals(52f, Yoga.YGNodeLayoutGetHeight(root))
    assertEquals(-10f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(-10f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(72f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(72f, Yoga.YGNodeLayoutGetHeight(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child0_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0_child0))
    assertEquals(72f, Yoga.YGNodeLayoutGetWidth(root_child0_child0))
    assertEquals(72f, Yoga.YGNodeLayoutGetHeight(root_child0_child0))
    Yoga.YGNodeCalculateLayout(
      root,
      Yoga.YGUndefined,
      Yoga.YGUndefined,
      YGDirection.YGDirectionRTL,
    )
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root))
    assertEquals(52f, Yoga.YGNodeLayoutGetWidth(root))
    assertEquals(52f, Yoga.YGNodeLayoutGetHeight(root))
    assertEquals(-10f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(-10f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(72f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(72f, Yoga.YGNodeLayoutGetHeight(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child0_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0_child0))
    assertEquals(72f, Yoga.YGNodeLayoutGetWidth(root_child0_child0))
    assertEquals(72f, Yoga.YGNodeLayoutGetHeight(root_child0_child0))
    Yoga.YGNodeFreeRecursive(root)
  }

  @Test
  fun align_items_flex_end_child_without_margin_bigger_than_parent() {
    val config = Yoga.YGConfigNew()
    val root = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetJustifyContent(root, YGJustify.YGJustifyCenter)
    Yoga.YGNodeStyleSetAlignItems(root, YGAlign.YGAlignCenter)
    Yoga.YGNodeStyleSetWidth(root, 52f)
    Yoga.YGNodeStyleSetHeight(root, 52f)
    val root_child0 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetAlignItems(root_child0, YGAlign.YGAlignFlexEnd)
    Yoga.YGNodeInsertChild(root, root_child0, 0)
    val root_child0_child0 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetWidth(root_child0_child0, 72f)
    Yoga.YGNodeStyleSetHeight(root_child0_child0, 72f)
    Yoga.YGNodeInsertChild(root_child0, root_child0_child0, 0)
    Yoga.YGNodeCalculateLayout(
      root,
      Yoga.YGUndefined,
      Yoga.YGUndefined,
      YGDirection.YGDirectionLTR,
    )
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root))
    assertEquals(52f, Yoga.YGNodeLayoutGetWidth(root))
    assertEquals(52f, Yoga.YGNodeLayoutGetHeight(root))
    assertEquals(-10f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(-10f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(72f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(72f, Yoga.YGNodeLayoutGetHeight(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child0_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0_child0))
    assertEquals(72f, Yoga.YGNodeLayoutGetWidth(root_child0_child0))
    assertEquals(72f, Yoga.YGNodeLayoutGetHeight(root_child0_child0))
    Yoga.YGNodeCalculateLayout(
      root,
      Yoga.YGUndefined,
      Yoga.YGUndefined,
      YGDirection.YGDirectionRTL,
    )
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root))
    assertEquals(52f, Yoga.YGNodeLayoutGetWidth(root))
    assertEquals(52f, Yoga.YGNodeLayoutGetHeight(root))
    assertEquals(-10f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(-10f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(72f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(72f, Yoga.YGNodeLayoutGetHeight(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child0_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0_child0))
    assertEquals(72f, Yoga.YGNodeLayoutGetWidth(root_child0_child0))
    assertEquals(72f, Yoga.YGNodeLayoutGetHeight(root_child0_child0))
    Yoga.YGNodeFreeRecursive(root)
  }

  @Test
  fun align_center_should_size_based_on_content() {
    val config = Yoga.YGConfigNew()
    val root = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetAlignItems(root, YGAlign.YGAlignCenter)
    Yoga.YGNodeStyleSetMargin(root, YGEdge.YGEdgeTop, 20f)
    Yoga.YGNodeStyleSetWidth(root, 100f)
    Yoga.YGNodeStyleSetHeight(root, 100f)
    val root_child0 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetJustifyContent(root_child0, YGJustify.YGJustifyCenter)
    Yoga.YGNodeStyleSetFlexShrink(root_child0, 1f)
    Yoga.YGNodeInsertChild(root, root_child0, 0)
    val root_child0_child0 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetFlexGrow(root_child0_child0, 1f)
    Yoga.YGNodeStyleSetFlexShrink(root_child0_child0, 1f)
    Yoga.YGNodeInsertChild(root_child0, root_child0_child0, 0)
    val root_child0_child0_child0 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetWidth(root_child0_child0_child0, 20f)
    Yoga.YGNodeStyleSetHeight(root_child0_child0_child0, 20f)
    Yoga.YGNodeInsertChild(root_child0_child0, root_child0_child0_child0, 0)
    Yoga.YGNodeCalculateLayout(
      root,
      Yoga.YGUndefined,
      Yoga.YGUndefined,
      YGDirection.YGDirectionLTR,
    )
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root))
    assertEquals(20f, Yoga.YGNodeLayoutGetTop(root))
    assertEquals(100f, Yoga.YGNodeLayoutGetWidth(root))
    assertEquals(100f, Yoga.YGNodeLayoutGetHeight(root))
    assertEquals(40f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(20f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(20f, Yoga.YGNodeLayoutGetHeight(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child0_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0_child0))
    assertEquals(20f, Yoga.YGNodeLayoutGetWidth(root_child0_child0))
    assertEquals(20f, Yoga.YGNodeLayoutGetHeight(root_child0_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child0_child0_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0_child0_child0))
    assertEquals(20f, Yoga.YGNodeLayoutGetWidth(root_child0_child0_child0))
    assertEquals(20f, Yoga.YGNodeLayoutGetHeight(root_child0_child0_child0))
    Yoga.YGNodeCalculateLayout(
      root,
      Yoga.YGUndefined,
      Yoga.YGUndefined,
      YGDirection.YGDirectionRTL,
    )
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root))
    assertEquals(20f, Yoga.YGNodeLayoutGetTop(root))
    assertEquals(100f, Yoga.YGNodeLayoutGetWidth(root))
    assertEquals(100f, Yoga.YGNodeLayoutGetHeight(root))
    assertEquals(40f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(20f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(20f, Yoga.YGNodeLayoutGetHeight(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child0_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0_child0))
    assertEquals(20f, Yoga.YGNodeLayoutGetWidth(root_child0_child0))
    assertEquals(20f, Yoga.YGNodeLayoutGetHeight(root_child0_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child0_child0_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0_child0_child0))
    assertEquals(20f, Yoga.YGNodeLayoutGetWidth(root_child0_child0_child0))
    assertEquals(20f, Yoga.YGNodeLayoutGetHeight(root_child0_child0_child0))
    Yoga.YGNodeFreeRecursive(root)
  }

  @Test
  fun align_strech_should_size_based_on_parent() {
    val config = Yoga.YGConfigNew()
    val root = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetMargin(root, YGEdge.YGEdgeTop, 20f)
    Yoga.YGNodeStyleSetWidth(root, 100f)
    Yoga.YGNodeStyleSetHeight(root, 100f)
    val root_child0 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetJustifyContent(root_child0, YGJustify.YGJustifyCenter)
    Yoga.YGNodeStyleSetFlexShrink(root_child0, 1f)
    Yoga.YGNodeInsertChild(root, root_child0, 0)
    val root_child0_child0 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetFlexGrow(root_child0_child0, 1f)
    Yoga.YGNodeStyleSetFlexShrink(root_child0_child0, 1f)
    Yoga.YGNodeInsertChild(root_child0, root_child0_child0, 0)
    val root_child0_child0_child0 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetWidth(root_child0_child0_child0, 20f)
    Yoga.YGNodeStyleSetHeight(root_child0_child0_child0, 20f)
    Yoga.YGNodeInsertChild(root_child0_child0, root_child0_child0_child0, 0)
    Yoga.YGNodeCalculateLayout(
      root,
      Yoga.YGUndefined,
      Yoga.YGUndefined,
      YGDirection.YGDirectionLTR,
    )
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root))
    assertEquals(20f, Yoga.YGNodeLayoutGetTop(root))
    assertEquals(100f, Yoga.YGNodeLayoutGetWidth(root))
    assertEquals(100f, Yoga.YGNodeLayoutGetHeight(root))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(100f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(20f, Yoga.YGNodeLayoutGetHeight(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child0_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0_child0))
    assertEquals(100f, Yoga.YGNodeLayoutGetWidth(root_child0_child0))
    assertEquals(20f, Yoga.YGNodeLayoutGetHeight(root_child0_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child0_child0_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0_child0_child0))
    assertEquals(20f, Yoga.YGNodeLayoutGetWidth(root_child0_child0_child0))
    assertEquals(20f, Yoga.YGNodeLayoutGetHeight(root_child0_child0_child0))
    Yoga.YGNodeCalculateLayout(
      root,
      Yoga.YGUndefined,
      Yoga.YGUndefined,
      YGDirection.YGDirectionRTL,
    )
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root))
    assertEquals(20f, Yoga.YGNodeLayoutGetTop(root))
    assertEquals(100f, Yoga.YGNodeLayoutGetWidth(root))
    assertEquals(100f, Yoga.YGNodeLayoutGetHeight(root))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(100f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(20f, Yoga.YGNodeLayoutGetHeight(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child0_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0_child0))
    assertEquals(100f, Yoga.YGNodeLayoutGetWidth(root_child0_child0))
    assertEquals(20f, Yoga.YGNodeLayoutGetHeight(root_child0_child0))
    assertEquals(80f, Yoga.YGNodeLayoutGetLeft(root_child0_child0_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0_child0_child0))
    assertEquals(20f, Yoga.YGNodeLayoutGetWidth(root_child0_child0_child0))
    assertEquals(20f, Yoga.YGNodeLayoutGetHeight(root_child0_child0_child0))
    Yoga.YGNodeFreeRecursive(root)
  }

  @Test
  fun align_flex_start_with_shrinking_children() {
    val config = Yoga.YGConfigNew()
    val root = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetWidth(root, 500f)
    Yoga.YGNodeStyleSetHeight(root, 500f)
    val root_child0 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetAlignItems(root_child0, YGAlign.YGAlignFlexStart)
    Yoga.YGNodeInsertChild(root, root_child0, 0)
    val root_child0_child0 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetFlexGrow(root_child0_child0, 1f)
    Yoga.YGNodeStyleSetFlexShrink(root_child0_child0, 1f)
    Yoga.YGNodeInsertChild(root_child0, root_child0_child0, 0)
    val root_child0_child0_child0 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetFlexGrow(root_child0_child0_child0, 1f)
    Yoga.YGNodeStyleSetFlexShrink(root_child0_child0_child0, 1f)
    Yoga.YGNodeInsertChild(root_child0_child0, root_child0_child0_child0, 0)
    Yoga.YGNodeCalculateLayout(
      root,
      Yoga.YGUndefined,
      Yoga.YGUndefined,
      YGDirection.YGDirectionLTR,
    )
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root))
    assertEquals(500f, Yoga.YGNodeLayoutGetWidth(root))
    assertEquals(500f, Yoga.YGNodeLayoutGetHeight(root))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(500f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetHeight(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child0_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetWidth(root_child0_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetHeight(root_child0_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child0_child0_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0_child0_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetWidth(root_child0_child0_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetHeight(root_child0_child0_child0))
    Yoga.YGNodeCalculateLayout(
      root,
      Yoga.YGUndefined,
      Yoga.YGUndefined,
      YGDirection.YGDirectionRTL,
    )
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root))
    assertEquals(500f, Yoga.YGNodeLayoutGetWidth(root))
    assertEquals(500f, Yoga.YGNodeLayoutGetHeight(root))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(500f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetHeight(root_child0))
    assertEquals(500f, Yoga.YGNodeLayoutGetLeft(root_child0_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetWidth(root_child0_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetHeight(root_child0_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child0_child0_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0_child0_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetWidth(root_child0_child0_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetHeight(root_child0_child0_child0))
    Yoga.YGNodeFreeRecursive(root)
  }

  @Test
  fun align_flex_start_with_stretching_children() {
    val config = Yoga.YGConfigNew()
    val root = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetWidth(root, 500f)
    Yoga.YGNodeStyleSetHeight(root, 500f)
    val root_child0 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeInsertChild(root, root_child0, 0)
    val root_child0_child0 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetFlexGrow(root_child0_child0, 1f)
    Yoga.YGNodeStyleSetFlexShrink(root_child0_child0, 1f)
    Yoga.YGNodeInsertChild(root_child0, root_child0_child0, 0)
    val root_child0_child0_child0 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetFlexGrow(root_child0_child0_child0, 1f)
    Yoga.YGNodeStyleSetFlexShrink(root_child0_child0_child0, 1f)
    Yoga.YGNodeInsertChild(root_child0_child0, root_child0_child0_child0, 0)
    Yoga.YGNodeCalculateLayout(
      root,
      Yoga.YGUndefined,
      Yoga.YGUndefined,
      YGDirection.YGDirectionLTR,
    )
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root))
    assertEquals(500f, Yoga.YGNodeLayoutGetWidth(root))
    assertEquals(500f, Yoga.YGNodeLayoutGetHeight(root))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(500f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetHeight(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child0_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0_child0))
    assertEquals(500f, Yoga.YGNodeLayoutGetWidth(root_child0_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetHeight(root_child0_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child0_child0_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0_child0_child0))
    assertEquals(500f, Yoga.YGNodeLayoutGetWidth(root_child0_child0_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetHeight(root_child0_child0_child0))
    Yoga.YGNodeCalculateLayout(
      root,
      Yoga.YGUndefined,
      Yoga.YGUndefined,
      YGDirection.YGDirectionRTL,
    )
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root))
    assertEquals(500f, Yoga.YGNodeLayoutGetWidth(root))
    assertEquals(500f, Yoga.YGNodeLayoutGetHeight(root))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(500f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetHeight(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child0_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0_child0))
    assertEquals(500f, Yoga.YGNodeLayoutGetWidth(root_child0_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetHeight(root_child0_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child0_child0_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0_child0_child0))
    assertEquals(500f, Yoga.YGNodeLayoutGetWidth(root_child0_child0_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetHeight(root_child0_child0_child0))
    Yoga.YGNodeFreeRecursive(root)
  }

  @Test
  fun align_flex_start_with_shrinking_children_with_stretch() {
    val config = Yoga.YGConfigNew()
    val root = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetWidth(root, 500f)
    Yoga.YGNodeStyleSetHeight(root, 500f)
    val root_child0 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetAlignItems(root_child0, YGAlign.YGAlignFlexStart)
    Yoga.YGNodeInsertChild(root, root_child0, 0)
    val root_child0_child0 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetFlexGrow(root_child0_child0, 1f)
    Yoga.YGNodeStyleSetFlexShrink(root_child0_child0, 1f)
    Yoga.YGNodeInsertChild(root_child0, root_child0_child0, 0)
    val root_child0_child0_child0 = Yoga.YGNodeNewWithConfig(config)
    Yoga.YGNodeStyleSetFlexGrow(root_child0_child0_child0, 1f)
    Yoga.YGNodeStyleSetFlexShrink(root_child0_child0_child0, 1f)
    Yoga.YGNodeInsertChild(root_child0_child0, root_child0_child0_child0, 0)
    Yoga.YGNodeCalculateLayout(
      root,
      Yoga.YGUndefined,
      Yoga.YGUndefined,
      YGDirection.YGDirectionLTR,
    )
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root))
    assertEquals(500f, Yoga.YGNodeLayoutGetWidth(root))
    assertEquals(500f, Yoga.YGNodeLayoutGetHeight(root))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(500f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetHeight(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child0_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetWidth(root_child0_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetHeight(root_child0_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child0_child0_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0_child0_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetWidth(root_child0_child0_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetHeight(root_child0_child0_child0))
    Yoga.YGNodeCalculateLayout(
      root,
      Yoga.YGUndefined,
      Yoga.YGUndefined,
      YGDirection.YGDirectionRTL,
    )
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root))
    assertEquals(500f, Yoga.YGNodeLayoutGetWidth(root))
    assertEquals(500f, Yoga.YGNodeLayoutGetHeight(root))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0))
    assertEquals(500f, Yoga.YGNodeLayoutGetWidth(root_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetHeight(root_child0))
    assertEquals(500f, Yoga.YGNodeLayoutGetLeft(root_child0_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetWidth(root_child0_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetHeight(root_child0_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetLeft(root_child0_child0_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetTop(root_child0_child0_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetWidth(root_child0_child0_child0))
    assertEquals(0f, Yoga.YGNodeLayoutGetHeight(root_child0_child0_child0))
    Yoga.YGNodeFreeRecursive(root)
  }
}
