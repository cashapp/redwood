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
import app.cash.redwood.layout.api.Overflow
import app.cash.redwood.layout.compose.Box
import app.cash.redwood.layout.compose.Column
import app.cash.redwood.layout.compose.ColumnScope
import app.cash.redwood.layout.compose.Row
import app.cash.redwood.layout.compose.Spacer
import app.cash.redwood.ui.Margin
import app.cash.redwood.ui.dp
import com.example.redwood.testing.compose.Text
import com.example.redwood.testing.compose.backgroundColor

private val accentColor = 0xFFDDDDDDu
private val boxColor = 0xFFFFFF66u
private val backColor = 0x88FF0000u
private val middleColor = 0x8800FF00u
private val frontColor = 0x880000FFu

@Composable
fun BoxSandbox(modifier: Modifier = Modifier) {
  Column(
    width = Fill,
    height = Fill,
    overflow = Overflow.Scroll,
    horizontalAlignment = Stretch,
    modifier = modifier,
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
    modifier = Modifier.horizontalAlignment(Center).margin(Margin(bottom = 20.dp)),
  )
  Column(modifier = Modifier.backgroundColor(accentColor).margin(Margin(horizontal = 20.dp))) {
    Text(
      "Constraints [x y] | Alignments [x y]",
      modifier = Modifier.horizontalAlignment(Center).margin(Margin(vertical = 10.dp)),
    )
    Row(
      horizontalAlignment = MainAxisAlignment.Center,
      modifier = Modifier.horizontalAlignment(Stretch).margin(Margin(vertical = 10.dp)),
    ) {
      Spacer(modifier = Modifier.backgroundColor(boxColor).height(20.dp).width(20.dp))
      Text("Box", modifier = Modifier.margin(Margin(start = 4.dp, end = 12.dp)))
      Spacer(modifier = Modifier.backgroundColor(backColor).height(20.dp).width(20.dp))
      Text("Back", modifier = Modifier.margin(Margin(start = 4.dp, end = 12.dp)))
      Spacer(modifier = Modifier.backgroundColor(middleColor).height(20.dp).width(20.dp))
      Text("Middle", modifier = Modifier.margin(Margin(start = 4.dp, end = 12.dp)))
      Spacer(modifier = Modifier.backgroundColor(frontColor).height(20.dp).width(20.dp))
      Text("Front", modifier = Modifier.margin(Margin(start = 4.dp)))
    }
  }
}

@Composable
private fun ColumnScope.BoxRow(
  width: Constraint,
  height: Constraint,
  horizontalAlignment: CrossAxisAlignment,
  verticalAlignment: CrossAxisAlignment,
) {
  // Divider
  Spacer(
    modifier = Modifier
      .backgroundColor(accentColor)
      .height(1.dp)
      .horizontalAlignment(Stretch)
      .margin(Margin(top = 20.dp)),
  )
  Text(
    "$width $height | $horizontalAlignment $verticalAlignment",
    modifier = Modifier.horizontalAlignment(Center).margin(Margin(bottom = 8.dp)),
  )
  Row(
    margin = Margin(horizontal = 20.dp),
    modifier = Modifier.height(80.dp),
  ) {
    Box(
      width = width,
      height = height,
      horizontalAlignment = horizontalAlignment,
      verticalAlignment = verticalAlignment,
      modifier = Modifier.backgroundColor(boxColor),
    ) {
      Spacer(
        modifier = Modifier
          .backgroundColor(backColor)
          .width(100.dp)
          .height(40.dp)
          .margin(Margin(10.dp)),
      )
      Spacer(
        modifier = Modifier
          .backgroundColor(middleColor)
          .width(80.dp)
          .height(30.dp)
          .margin(Margin(10.dp)),
      )
      Spacer(
        modifier = Modifier
          .backgroundColor(frontColor)
          .width(60.dp)
          .height(20.dp)
          .margin(Margin(10.dp)),
      )
    }
  }
}
