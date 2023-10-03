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
import app.cash.redwood.layout.api.CrossAxisAlignment
import app.cash.redwood.layout.api.MainAxisAlignment
import app.cash.redwood.layout.api.Overflow
import app.cash.redwood.layout.compose.Box
import app.cash.redwood.layout.compose.Column
import app.cash.redwood.layout.compose.Row
import app.cash.redwood.ui.Margin
import app.cash.redwood.ui.dp
import com.example.redwood.testing.compose.Button
import com.example.redwood.testing.compose.Text

@Composable
fun BoxSandbox(modifier: Modifier = Modifier) {
  Column(
    width = Constraint.Fill,
    height = Constraint.Fill,
    overflow = Overflow.Scroll,
    horizontalAlignment = CrossAxisAlignment.Stretch,
    verticalAlignment = MainAxisAlignment.Start,
  ) {
    val crossAxisAlignments = listOf<CrossAxisAlignment>(
      CrossAxisAlignment.Start,
      CrossAxisAlignment.Center,
      CrossAxisAlignment.Stretch,
      CrossAxisAlignment.End,
    )

    val constraints = listOf<Constraint>(
      Constraint.Fill,
      Constraint.Wrap,
    )

    // Iterate over all permutations
    constraints.forEach {
      val widthConstraint = it
      constraints.forEach {
        val heightConstraint = it
        crossAxisAlignments.forEach {
          val horizontalAlignment = it
          crossAxisAlignments.forEach {
            val verticalAlignment = it
            Text("$widthConstraint $heightConstraint $horizontalAlignment $verticalAlignment")
            BoxRow(
              width = widthConstraint,
              height = heightConstraint,
              horizontalAlignment = horizontalAlignment,
              verticalAlignment = verticalAlignment,
              modifier = Modifier.height(120.dp),
            )
          }
        }
      }
    }
  }
}

@Composable
private fun BoxRow(
  width: Constraint,
  height: Constraint,
  horizontalAlignment: CrossAxisAlignment,
  verticalAlignment: CrossAxisAlignment,
  modifier: Modifier = Modifier,
) {
  Column(
    width = Constraint.Fill,
    height = Constraint.Fill,
    horizontalAlignment = CrossAxisAlignment.Start,
    margin = Margin(horizontal = 20.dp, vertical = 0.dp),
    modifier = modifier,
  ) {
    Row(
      width = Constraint.Fill,
      height = Constraint.Fill,
    ) {
      Box(
        width = width,
        height = height,
        horizontalAlignment = horizontalAlignment,
        verticalAlignment = verticalAlignment,
      ) {
        Button("BACK - BACK - BACK\nBACK - BACK - BACK\nBACK - BACK - BACK", onClick = null)
        Button("MIDDLE - MIDDLE\nMIDDLE - MIDDLE", onClick = null)
        Button("FRONT", onClick = null)
      }
    }
  }
}
