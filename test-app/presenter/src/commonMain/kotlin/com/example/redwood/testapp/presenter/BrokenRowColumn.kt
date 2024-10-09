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
package com.example.redwood.testapp.presenter

import androidx.compose.runtime.Composable
import app.cash.redwood.Modifier
import app.cash.redwood.layout.api.Constraint
import app.cash.redwood.layout.api.CrossAxisAlignment
import app.cash.redwood.layout.api.MainAxisAlignment
import app.cash.redwood.layout.compose.Column
import app.cash.redwood.layout.compose.Row
import app.cash.redwood.layout.compose.Spacer
import app.cash.redwood.ui.Margin
import app.cash.redwood.ui.dp
import com.example.redwood.testapp.compose.backgroundColor

@Composable
fun BrokenRowColumn(modifier: Modifier = Modifier) {
  Column(
    width = Constraint.Fill,
    height = Constraint.Fill,
    // Uncomment this to fix(?) behavior.
    // horizontalAlignment = CrossAxisAlignment.Stretch,
    margin = Margin(top = 24.dp),
    modifier = modifier,
  ) {
    Row(
      width = Constraint.Fill,
      horizontalAlignment = MainAxisAlignment.Center,
    ) {
      Column(
        width = Constraint.Fill,
        height = Constraint.Fill,
        horizontalAlignment = CrossAxisAlignment.Stretch,
        modifier = Modifier
          .margin(Margin(start = 24.dp, end = 24.dp))
          .flex(1.0),
      ) {
        Spacer(48.dp, 48.dp, Modifier.backgroundColor(0xFFFF0000.toInt()))
      }
    }
  }
}
