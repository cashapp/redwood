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

import app.cash.redwood.yoga.enums.YogaAlign
import app.cash.redwood.yoga.enums.YogaFlexDirection
import app.cash.redwood.yoga.interfaces.YogaBaselineFunction
import kotlin.test.Test
import kotlin.test.assertEquals

class YGAlignBaselineTest {
    private val baselineFunc: YogaBaselineFunction
        get() = YogaBaselineFunction { node: YogaNode?, width: Float, height: Float -> height / 2 }

    @Test
    fun test_align_baseline_parent_using_child_in_column_as_reference() {
        val config = YogaConfigFactory.create()
        val root = createYGNode(config, YogaFlexDirection.ROW, 1000f, 1000f, true)
        val root_child0 = createYGNode(config, YogaFlexDirection.COLUMN, 500f, 600f, false)
        root.addChildAt(root_child0, 0)
        val root_child1 = createYGNode(config, YogaFlexDirection.COLUMN, 500f, 800f, false)
        root.addChildAt(root_child1, 1)
        val root_child1_child0 = createYGNode(config, YogaFlexDirection.COLUMN, 500f, 300f, false)
        root_child1.addChildAt(root_child1_child0, 0)
        val root_child1_child1 = createYGNode(config, YogaFlexDirection.COLUMN, 500f, 400f, false)
        root_child1_child1.setBaselineFunction(baselineFunc)
        root_child1_child1.setIsReferenceBaseline(true)
        root_child1.addChildAt(root_child1_child1, 1)
        root.calculateLayout(YogaConstants.UNDEFINED, YogaConstants.UNDEFINED)
        assertEquals(0f, root_child0.getLayoutX(), 0.0f)
        assertEquals(0f, root_child0.getLayoutY(), 0.0f)
        assertEquals(500f, root_child1.getLayoutX(), 0.0f)
        assertEquals(100f, root_child1.getLayoutY(), 0.0f)
        assertEquals(0f, root_child1_child0.getLayoutX(), 0.0f)
        assertEquals(0f, root_child1_child0.getLayoutY(), 0.0f)
        assertEquals(0f, root_child1_child1.getLayoutX(), 0.0f)
        assertEquals(300f, root_child1_child1.getLayoutY(), 0.0f)
    }

    @Test
    fun test_align_baseline_parent_using_child_in_row_as_reference() {
        val config = YogaConfigFactory.create()
        val root = createYGNode(config, YogaFlexDirection.ROW, 1000f, 1000f, true)
        val root_child0 = createYGNode(config, YogaFlexDirection.COLUMN, 500f, 600f, false)
        root.addChildAt(root_child0, 0)
        val root_child1 = createYGNode(config, YogaFlexDirection.ROW, 500f, 800f, true)
        root.addChildAt(root_child1, 1)
        val root_child1_child0 = createYGNode(config, YogaFlexDirection.COLUMN, 500f, 500f, false)
        root_child1.addChildAt(root_child1_child0, 0)
        val root_child1_child1 = createYGNode(config, YogaFlexDirection.COLUMN, 500f, 400f, false)
        root_child1_child1.setBaselineFunction(baselineFunc)
        root_child1_child1.setIsReferenceBaseline(true)
        root_child1.addChildAt(root_child1_child1, 1)
        root.calculateLayout(YogaConstants.UNDEFINED, YogaConstants.UNDEFINED)
        assertEquals(0f, root_child0.getLayoutX(), 0.0f)
        assertEquals(0f, root_child0.getLayoutY(), 0.0f)
        assertEquals(500f, root_child1.getLayoutX(), 0.0f)
        assertEquals(100f, root_child1.getLayoutY(), 0.0f)
        assertEquals(0f, root_child1_child0.getLayoutX(), 0.0f)
        assertEquals(0f, root_child1_child0.getLayoutY(), 0.0f)
        assertEquals(500f, root_child1_child1.getLayoutX(), 0.0f)
        assertEquals(300f, root_child1_child1.getLayoutY(), 0.0f)
    }

    private fun createYGNode(
      config: YogaConfig,
      flexDirection: YogaFlexDirection,
      width: Float,
      height: Float,
      alignBaseline: Boolean
    ): YogaNode {
        val node = YogaNodeFactory.create(config)
        node.setFlexDirection(flexDirection)
        node.setWidth(width)
        node.setHeight(height)
        if (alignBaseline) {
            node.setAlignItems(YogaAlign.BASELINE)
        }
        return node
    }
}
