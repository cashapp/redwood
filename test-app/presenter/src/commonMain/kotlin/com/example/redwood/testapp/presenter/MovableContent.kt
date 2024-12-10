/*
 * Copyright (C) 2024 Square, Inc.
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
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import app.cash.redwood.Modifier
import app.cash.redwood.layout.api.Constraint
import app.cash.redwood.layout.compose.Column
import app.cash.redwood.layout.compose.Row
import app.cash.redwood.ui.Margin
import app.cash.redwood.ui.dp
import com.example.redwood.testapp.compose.Button
import com.example.redwood.testapp.compose.Text
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay

@Composable
fun MovableContent(modifier: Modifier = Modifier) {
  var isColumn by remember { mutableStateOf(true) }

  Column(
    width = Constraint.Fill,
    modifier = modifier,
  ) {
    Row(
      width = Constraint.Fill,
    ) {
      Button("Column", onClick = { isColumn = true })
      Button("Row", onClick = { isColumn = false })
    }

    val numbers = remember { mutableStateListOf(1L, 1L) }
    val content = remember {
      movableContentOf {
        var a by remember { mutableLongStateOf(1) }
        var b by remember { mutableLongStateOf(1) }
        LaunchedEffect(Unit) {
          while (true) {
            delay(1.seconds)
            val c = a + b
            a = b
            b = c
            numbers += c
          }
        }

        Text("a = $a", modifier = Modifier.margin(Margin(8.dp)))
        Text("b = $b", modifier = Modifier.margin(Margin(8.dp)))
        Text("a + b = ${a + b}", modifier = Modifier.margin(Margin(8.dp)))
      }
    }
    if (isColumn) {
      Column { content() }
    } else {
      Row { content() }
    }

    Text(numbers.joinToString(), modifier = Modifier.margin(Margin(8.dp)))
  }
}
