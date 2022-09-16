/*
 * Copyright (C) 2022 Square, Inc.
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
package app.cash.redwood

import kotlin.jvm.JvmField

/**
 * Holds properties related to a single flex line.
 */
internal class Line {

  /**
   * The size of the flex line in pixels along the main axis of the flex container.
   */
  @JvmField var mainSize = 0

  /**
   * The size of the flex line in pixels along the cross axis of the flex container.
   */
  @JvmField var crossSize = 0

  /**
   * The count of the views contained in this flex line.
   */
  @JvmField var itemCount = 0

  /**
   * Holds the count of the views whose visibilities are gone.
   */
  @JvmField var invisibleItemCount = 0

  /**
   * The sum of the flexGrow properties of the children included in this flex line.
   */
  @JvmField var totalFlexGrow = 0f

  /**
   * The sum of the flexShrink properties of the children included in this flex line.
   */
  @JvmField var totalFlexShrink = 0f

  /**
   * The largest value of the individual child's baseline.
   */
  @JvmField var maxBaseline = 0

  /**
   * The sum of the cross size used before this flex line.
   */
  @JvmField var sumCrossSizeBefore = 0

  /**
   * Store the indices of the children views whose alignSelf property is stretch.
   * The stored indices are the absolute indices including all children in the Flexbox,
   * not the relative indices in this flex line.
   */
  @JvmField var indicesAlignSelfStretch = listOf<Int>()

  /**
   * The first view's index included in this flex line.
   */
  @JvmField var firstIndex = 0

  /**
   * The last view's index included in this flex line.
   */
  @JvmField var lastIndex = 0

  /**
   * Set to true if any [Node]s in this line have [Node.flexGrow] attributes set
   * (i.e. have a value other than [Node.DefaultFlexGrow])
   */
  @JvmField var anyItemsHaveFlexGrow = false

  /**
   * Set to true if any [Node]s in this line have [Node.flexShrink] attributes set
   * (i.e. have a value other than [Node.UndefinedFlexShrink])
   */
  @JvmField var anyItemsHaveFlexShrink = false
}
