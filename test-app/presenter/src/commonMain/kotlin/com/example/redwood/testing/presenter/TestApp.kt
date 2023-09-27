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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import app.cash.redwood.Modifier
import app.cash.redwood.compose.BackHandler
import app.cash.redwood.layout.api.Constraint.Companion.Fill
import app.cash.redwood.layout.api.CrossAxisAlignment.Companion.Stretch
import app.cash.redwood.layout.api.Overflow
import app.cash.redwood.layout.compose.Column
import app.cash.redwood.ui.Margin
import app.cash.redwood.ui.dp
import com.example.redwood.testing.compose.Button
import com.example.redwood.testing.compose.Text

@Composable
fun TestApp(httpClient: HttpClient) {
  val screen = remember { mutableStateOf<Screen?>(null) }
  val activeScreen = screen.value
  if (activeScreen == null) {
    HomeScreen(screen)
  } else {
    val onBack = { screen.value = null }
    BackHandler(onBack = onBack)
    Column(width = Fill, height = Fill) {
      Button("Back", onClick = onBack)

      // TODO This should be a Box.
      Column(
        width = Fill,
        horizontalAlignment = Stretch,
        modifier = Modifier.grow(1.0).horizontalAlignment(Stretch),
      ) {
        activeScreen.Show(httpClient)
      }
    }
  }
}

@Suppress("unused") // Used via reflection.
enum class Screen {
  RepoSearch {
    @Composable
    override fun Show(httpClient: HttpClient) {
      RepoSearch(httpClient)
    }
  },
  UiConfiguration {
    @Composable
    override fun Show(httpClient: HttpClient) {
      UiConfigurationValues()
    }
  },
  ;

  @Composable
  abstract fun Show(httpClient: HttpClient)
}

@Composable
private fun HomeScreen(screen: MutableState<Screen?>) {
  Column(
    width = Fill,
    height = Fill,
    overflow = Overflow.Scroll,
    horizontalAlignment = Stretch,
  ) {
    Text("Test App Screens:", modifier = Modifier.margin(Margin(8.dp)))
    Screen.entries.forEach {
      Button(it.name, onClick = {
        screen.value = it
      })
    }
  }
}
