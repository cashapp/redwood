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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import app.cash.redwood.Modifier
import app.cash.redwood.layout.api.Constraint.Companion.Fill
import app.cash.redwood.layout.api.CrossAxisAlignment.Companion.Stretch
import app.cash.redwood.layout.compose.Column
import app.cash.redwood.layout.compose.Row
import com.example.redwood.testing.compose.Button
import com.example.redwood.testing.compose.backgroundColor
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay

private const val ROWS = 5
private const val COLS = 3

@Composable
@OptIn(ExperimentalStdlibApi::class)
fun UnscopedModifiers(modifier: Modifier = Modifier) {
  val colors = remember {
    // https://issuetracker.google.com/issues/330350695
    List(ROWS * COLS) { Random.nextInt() }.toMutableStateList()
  }
  LaunchedEffect(Unit) {
    while (true) {
      delay(1.seconds)

      val randomIndex = Random.nextInt(colors.size)
      colors[randomIndex] = Random.nextInt()
    }
  }

  Column(width = Fill, horizontalAlignment = Stretch, modifier = modifier) {
    for (row in 0 until ROWS) {
      Row {
        for (col in 0 until COLS) {
          val value = colors[row * COLS + col]
          Button(
            text = "#" + value.toHexString(),
            modifier = Modifier
              .backgroundColor(value)
              .flex(1.0),
            onClick = { colors.fill(value) },
          )
        }
      }
    }
  }
}
