/*
 * Copyright 2016 Google Inc. All rights reserved.
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
package app.cash.redwood.flexbox

import app.cash.redwood.flexbox.FlexNode.Companion.DefaultFlexGrow
import app.cash.redwood.flexbox.FlexNode.Companion.UndefinedFlexShrink

/**
 * Holds properties related to a single flex line.
 */
internal class FlexLine {
  /**
   * The size of the flex line along the main axis of the flex container.
   */
  var mainSize = 0

  /**
   * The size of the flex line along the cross axis of the flex container.
   */
  var crossSize = 0

  /**
   * The number of nodes contained in this flex line.
   */
  var itemCount = 0

  /**
   * The number of nodes who are invisible (i.e. aren't included in measure or layout).
   */
  var invisibleItemCount = 0

  /**
   * The sum of the flexGrow properties of the children included in this flex line.
   */
  var totalFlexGrow = 0f

  /**
   * The sum of the flexShrink properties of the children included in this flex line.
   */
  var totalFlexShrink = 0f

  /**
   * The largest value of the individual child's baseline.
   */
  var maxBaseline = 0

  /**
   * The sum of the cross size used before this flex line.
   */
  var sumCrossSizeBefore = 0

  /**
   * The index of the first child included in this flex line (inclusive).
   */
  var firstIndex = 0

  /**
   * The index of the last child included in this flex line (inclusive).
   */
  var lastIndex = 0

  /**
   * True if any [FlexNode]s in this line have [FlexNode.flexGrow] attributes set
   * (i.e. have a value other than [DefaultFlexGrow]).
   */
  var anyItemsHaveFlexGrow = false

  /**
   * True if any [FlexNode]s in this line have [FlexNode.flexShrink] attributes set
   * (i.e. have a value other than [UndefinedFlexShrink]).
   */
  var anyItemsHaveFlexShrink = false
}
