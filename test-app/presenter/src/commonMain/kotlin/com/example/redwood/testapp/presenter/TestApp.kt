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
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import app.cash.redwood.Modifier
import app.cash.redwood.compose.BackHandler
import app.cash.redwood.layout.api.Constraint.Companion.Fill
import app.cash.redwood.layout.api.CrossAxisAlignment.Companion.Stretch
import app.cash.redwood.layout.api.Overflow
import app.cash.redwood.layout.compose.Column
import app.cash.redwood.ui.Margin
import app.cash.redwood.ui.dp
import com.example.redwood.testapp.compose.Button
import com.example.redwood.testapp.compose.Text

private val screens = buildMap<String, @Composable TestContext.() -> Unit> {
  put("Repo Search") { RepoSearch(httpClient) }
  put("UI Configuration") { UiConfigurationValues() }
  put("Box Sandbox") { BoxSandbox() }
  put("Unscoped Modifiers") { UnscopedModifiers() }
  put("Broken Row/Column") { BrokenRowColumn() }
  put("Broken Size Update") { BrokenSizeUpdate() }
  put("Movable Content") { MovableContent() }
}

@Stable
class TestContext(
  val httpClient: HttpClient,
)

@Composable
fun TestApp(
  context: TestContext,
  modifier: Modifier = Modifier,
) {
  var screenKey by rememberSaveable { mutableStateOf<String?>(null) }
  if (screenKey == null) {
    ScreenList(onScreenChange = { screenKey = it })
  } else {
    val onBack = { screenKey = null }
    BackHandler(onBack = onBack)

    Column(width = Fill, height = Fill, modifier = modifier) {
      Button("Back", onClick = onBack)

      val content = screens[screenKey]
      if (content == null) {
        Text("No screen found with key '$screenKey'!")
      } else {
        // TODO This should be a Box.
        Column(
          width = Fill,
          horizontalAlignment = Stretch,
          modifier = Modifier.grow(1.0).horizontalAlignment(Stretch),
        ) {
          with(context) {
            content()
          }
        }
      }
    }
  }
}

@Composable
private fun ScreenList(onScreenChange: (screenKey: String) -> Unit) {
  Column(
    width = Fill,
    height = Fill,
    overflow = Overflow.Scroll,
    horizontalAlignment = Stretch,
  ) {
    Text("Test App Screens:", modifier = Modifier.margin(Margin(8.dp)))
    for (key in screens.keys) {
      Button(key, onClick = {
        onScreenChange(key)
      })
    }
  }
}
