/*
 * Copyright (C) 2025 Square, Inc.
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import app.cash.redwood.Modifier
import app.cash.redwood.layout.api.Constraint.Companion.Fill
import app.cash.redwood.layout.api.CrossAxisAlignment
import app.cash.redwood.layout.compose.Column
import app.cash.redwood.ui.basic.compose.Text
import com.example.redwood.testapp.compose.Button

@Composable
fun AnimatedTransitions(modifier: Modifier = Modifier) {
  var slideState by remember { mutableIntStateOf(0) }
  var fadeState by remember { mutableIntStateOf(0) }

  Column(
    width = Fill,
    height = Fill,
    horizontalAlignment = CrossAxisAlignment.Stretch
  ) {
    Button(
      text = "Slide",
      modifier = Modifier,
      onClick = {
        slideState = (slideState + 1)
      },
    )
    Text(
      modifier = run {
        if (slideState % 2 == 0) {
          Modifier.horizontalAlignment(CrossAxisAlignment.Start)
        } else {
          Modifier.horizontalAlignment(CrossAxisAlignment.End)
        }
      },
      text = "$slideState"
    )
    Button(
      text = "Fade",
      modifier = Modifier,
      onClick = {
        fadeState = (fadeState + 1)
      },
    )
    if (fadeState % 2 == 0) {
      Text(
        modifier = Modifier.horizontalAlignment(CrossAxisAlignment.Center),
        text = "$fadeState"
      )
    }
  }
}
