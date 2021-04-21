/*
 * Copyright (C) 2021 Square, Inc.
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
package example.shared

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import app.cash.treehouse.compose.TreehouseScope
import example.counter.compose.CounterButton
import example.counter.compose.CounterText

@Composable
fun TreehouseScope.Counter(value: Int = 0) {
  var count by remember { mutableStateOf(value) }

  CounterButton("-1", onClick = { count-- })
  CounterText(count.toString())
  CounterButton("+1", onClick = { count++ })
}
