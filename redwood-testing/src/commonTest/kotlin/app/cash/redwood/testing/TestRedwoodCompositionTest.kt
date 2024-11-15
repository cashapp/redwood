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
package app.cash.redwood.testing

import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import app.cash.redwood.layout.compose.Column
import app.cash.redwood.layout.compose.Row
import app.cash.redwood.layout.testing.RedwoodLayoutTestingWidgetFactory
import app.cash.redwood.lazylayout.testing.RedwoodLazyLayoutTestingWidgetFactory
import app.cash.redwood.widget.MutableListChildren
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.example.redwood.testapp.compose.Text
import com.example.redwood.testapp.testing.TestSchemaTestingWidgetFactory
import com.example.redwood.testapp.widget.TestSchemaWidgetSystem
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class TestRedwoodCompositionTest {
  @Test fun awaitSnapshotCapturesMultipleChanges() = runTest {
    var count = 0
    val tester = TestRedwoodComposition(
      scope = backgroundScope,
      widgetSystem = TestSchemaWidgetSystem(
        TestSchema = TestSchemaTestingWidgetFactory(),
        RedwoodLayout = RedwoodLayoutTestingWidgetFactory(),
        RedwoodLazyLayout = RedwoodLazyLayoutTestingWidgetFactory(),
      ),
      container = MutableListChildren<WidgetValue>(),
      createSnapshot = { ++count },
    )

    // The content of a movableContentOf is applied to the node tree separately, resulting in
    // two calls to Applier.onEndChanges. If this signal is used to emit a snapshot, only a
    // partial view of the recomposition will be available.
    var isRow by mutableStateOf(true)
    tester.setContent {
      val movable = remember {
        movableContentOf {
          Text("one")
        }
      }
      if (isRow) {
        Row { movable() }
      } else {
        Column { movable() }
      }
    }

    assertThat(tester.awaitSnapshot()).isEqualTo(1)
    isRow = false
    assertThat(tester.awaitSnapshot()).isEqualTo(2)
  }
}
