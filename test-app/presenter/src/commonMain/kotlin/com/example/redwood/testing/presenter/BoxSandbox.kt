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
package com.example.redwood.testing.presenter

import androidx.compose.runtime.Composable
import app.cash.redwood.Modifier
import app.cash.redwood.layout.api.Constraint
import app.cash.redwood.layout.api.Constraint.Companion.Fill
import app.cash.redwood.layout.api.Constraint.Companion.Wrap
import app.cash.redwood.layout.api.CrossAxisAlignment
import app.cash.redwood.layout.api.CrossAxisAlignment.Companion.Center
import app.cash.redwood.layout.api.CrossAxisAlignment.Companion.End
import app.cash.redwood.layout.api.CrossAxisAlignment.Companion.Start
import app.cash.redwood.layout.api.CrossAxisAlignment.Companion.Stretch
import app.cash.redwood.layout.api.MainAxisAlignment
import app.cash.redwood.layout.api.MainAxisAlignment.Companion.SpaceBetween
import app.cash.redwood.layout.api.Overflow
import app.cash.redwood.layout.compose.Box
import app.cash.redwood.layout.compose.Column
import app.cash.redwood.layout.compose.ColumnScope
import app.cash.redwood.layout.compose.Row
import app.cash.redwood.layout.compose.Spacer
import app.cash.redwood.ui.Margin
import app.cash.redwood.ui.dp
import com.example.redwood.testing.compose.Rectangle
import com.example.redwood.testing.compose.Text

private val accentColor = 0xFFDDDDDDu
private val rowColor = 0xFFDDDDDDu
private val boxColor = 0xFFFFFF66u
private val backColor = 0x88FF0000u
private val middleColor = 0x8800FF00u
private val frontColor = 0x880000FFu

private val rowHeight = 80.dp

@Composable
fun BoxSandbox() {
  Column(
    width = Fill,
    height = Fill,
    overflow = Overflow.Scroll,
    horizontalAlignment = Stretch,
    verticalAlignment = MainAxisAlignment.Start,
  ) {
    val crossAxisAlignments = listOf(
      Start,
      Center,
      Stretch,
      End,
    )

    val constraints = listOf(
      Fill,
      Wrap,
    )

    Legend()

// Uncomment to debug a specific permutation.
//    BoxRow(
//      width = Wrap,
//      height = Wrap,
//      horizontalAlignment = Start,
//      verticalAlignment = Stretch,
//      modifier = Modifier.height(140.dp),
//    )

    // Iterate over all permutations
    constraints.forEach { widthConstraint ->
      constraints.forEach { heightConstraint ->
        crossAxisAlignments.forEach { horizontalAlignment ->
          crossAxisAlignments.forEach { verticalAlignment ->
            BoxRow(
              width = widthConstraint,
              height = heightConstraint,
              horizontalAlignment = horizontalAlignment,
              verticalAlignment = verticalAlignment,
              modifier = Modifier.height(rowHeight),
            )
          }
        }
      }
    }
  }
}

@Composable
private fun ColumnScope.Legend() {
  Text(
    "Legend",
    modifier = Modifier.horizontalAlignment(Center).height(40.dp),
  )
  Box(
    width = Fill,
    height = Wrap,
    horizontalAlignment = Stretch,
    verticalAlignment = Start,
    modifier = Modifier.height(80.dp).margin(Margin(horizontal = 20.dp)),
  ) {
    Rectangle(
      backgroundColor = accentColor,
      cornerRadius = 12f,
      modifier = Modifier.horizontalAlignment(Stretch).verticalAlignment(Stretch),
    )
    Column(
      width = Fill,
      horizontalAlignment = Center,
    ) {
      Text(
        "Constraints [x y] | Alignments [x y]",
        modifier = Modifier.horizontalAlignment(Center).height(40.dp),
      )
      Row(
        horizontalAlignment = SpaceBetween,
        verticalAlignment = Center,
      ) {
        Rectangle(
          backgroundColor = boxColor,
          cornerRadius = 4f,
          modifier = Modifier.width(20.dp).height(20.dp),
        )
        Text(" Box")
        Spacer(width = 12.dp)
        Rectangle(
          backgroundColor = backColor,
          cornerRadius = 4f,
          modifier = Modifier.width(20.dp).height(20.dp),
        )
        Text(" Back")
        Spacer(width = 12.dp)
        Rectangle(
          backgroundColor = middleColor,
          cornerRadius = 4f,
          modifier = Modifier.width(20.dp).height(20.dp),
        )
        Text(" Middle")
        Spacer(width = 12.dp)
        Rectangle(
          backgroundColor = frontColor,
          cornerRadius = 4f,
          modifier = Modifier.width(20.dp).height(20.dp),
        )
        Text(" Front")
      }
    }
  }
}

@Composable
private fun ColumnScope.BoxRow(
  width: Constraint,
  height: Constraint,
  horizontalAlignment: CrossAxisAlignment,
  verticalAlignment: CrossAxisAlignment,
  modifier: Modifier = Modifier,
) {
  // Divider
  Rectangle(accentColor, modifier = Modifier.height(1.dp).horizontalAlignment(Stretch).margin(Margin(top = 20.dp)))
  Text(
    "$width $height | $horizontalAlignment $verticalAlignment",
    modifier = Modifier.horizontalAlignment(Center).height(40.dp),
  )
  Column(
    width = Fill,
    height = Fill,
    horizontalAlignment = Start,
    margin = Margin(horizontal = 20.dp, vertical = 0.dp),
    modifier = modifier,
  ) {
    Box(
      width = Fill,
      height = Fill,
      horizontalAlignment = Stretch,
      verticalAlignment = Stretch,
    ) {
      Rectangle(backgroundColor = rowColor)
      Row(
        width = Fill,
        height = Fill,
      ) {
        // This is the box we're measuring.
        Box(
          width = width,
          height = height,
          horizontalAlignment = horizontalAlignment,
          verticalAlignment = verticalAlignment,
          // TODO: Add backgroundColor = boxColor once the modifiers are available.
        ) {
          Rectangle(
            backgroundColor = backColor,
            cornerRadius = 12f,
            modifier = Modifier.width((rowHeight.value * 1.4).dp)
              .height((rowHeight.value * 0.8).dp)
              .margin(Margin(5.dp)),
          )
          Rectangle(
            backgroundColor = middleColor,
            cornerRadius = 12f,
            modifier = Modifier.width((rowHeight.value * 1.2).dp)
              .height((rowHeight.value * 0.6).dp)
              .margin(Margin(10.dp)),
          )
          Rectangle(
            backgroundColor = frontColor,
            cornerRadius = 12f,
            modifier = Modifier.width((rowHeight.value).dp)
              .height((rowHeight.value * 0.4).dp)
              .margin(Margin(15.dp)),
          )
        }
      }
    }
  }
}
