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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import app.cash.redwood.Modifier
import app.cash.redwood.layout.api.Constraint.Companion.Fill
import app.cash.redwood.layout.compose.Column
import app.cash.redwood.ui.dp
import com.example.redwood.testapp.compose.Text
import com.example.redwood.testapp.compose.backgroundColor
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay

@Composable
fun BrokenSizeUpdate(modifier: Modifier = Modifier) {
  var width by remember { mutableStateOf(20.dp) }
  var height by remember { mutableStateOf(20.dp) }
  LaunchedEffect(Unit) {
    while (true) {
      delay(1.seconds)
      width = 100.dp
      height = 100.dp

      delay(3.seconds)
      width = 20.dp
      height = 20.dp
    }
  }

  Column(
    width = Fill,
    height = Fill,
    modifier = modifier,
  ) {
    Text(
      "hello",
      modifier = Modifier
        .size(width, height)
        .backgroundColor(0xFFFF0000.toInt()),
    )
  }
}
