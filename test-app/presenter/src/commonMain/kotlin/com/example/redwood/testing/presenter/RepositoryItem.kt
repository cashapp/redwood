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
import app.cash.redwood.layout.compose.Column
import app.cash.redwood.ui.Margin
import app.cash.redwood.ui.dp
import com.example.redwood.testing.compose.Text

@Composable
fun RepositoryItem(
  repository: Repository,
) {
  Column(margin = Margin(16.dp)) {
    Text(text = repository.fullName)
  }
}
