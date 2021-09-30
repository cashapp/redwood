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
package app.cash.treehouse.widget.zipline

import app.cash.treehouse.protocol.Diff
import app.cash.treehouse.widget.WidgetDisplay
import app.cash.treehouse.zipline.ZiplineComposition
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect

public suspend fun ZiplineComposition.setDisplay(display: WidgetDisplay<*>) {
  val diffsReference = diffs()
  val diffsFlow: Flow<Diff> = diffsReference.get()
  diffsFlow.collect { diff ->
    display.apply(diff) { event ->
      sendEvent(event)
    }
  }
}
