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
package app.cash.redwood.treehouse

import app.cash.redwood.testing.toChangeList
import assertk.assertThat
import assertk.assertions.isEqualTo
import example.redwood.compose.ExampleSchemaProtocolBridge
import example.redwood.compose.Row
import example.redwood.compose.Text
import example.redwood.widget.ExampleSchemaTester
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import platform.UIKit.UILabel
import platform.UIKit.UIStackView
import platform.UIKit.UIView

class ChangeListRendererTest {
  @Test fun createSnapshotChangeListAndRenderIt() = runTest {
    ExampleSchemaTester {
      setContent {
        Text("hello")
      }
      val snapshot = awaitSnapshot()
      val snapshotChangeList = snapshot.toChangeList(ExampleSchemaProtocolBridge)

      val treehouseUIKitView = TreehouseUIKitView(ExampleSchemaWidgetSystem())

      val renderer = ChangeListRenderer<UIView>(Json)
      renderer.render(treehouseUIKitView, snapshotChangeList)

      val uiView = treehouseUIKitView.view
      assertThat((uiView.subviews.firstOrNull() as? UILabel)?.text).isEqualTo("hello")
    }
  }

  @Test fun renderSnapshotChangeListWithSubviews() = runTest {
    ExampleSchemaTester {
      setContent {
        Row {
          Text("red")
          Text("orange")
          Text("yellow")
        }
      }
      val snapshot = awaitSnapshot()
      val snapshotChangeList = snapshot.toChangeList(ExampleSchemaProtocolBridge)

      val treehouseUIKitView = TreehouseUIKitView(ExampleSchemaWidgetSystem())

      val renderer = ChangeListRenderer<UIView>(Json)
      renderer.render(treehouseUIKitView, snapshotChangeList)

      val uiView = treehouseUIKitView.view
      val row = uiView.subviews.firstOrNull() as UIStackView
      assertThat((row.subviews[0] as? UILabel)?.text).isEqualTo("red")
      assertThat((row.subviews[1] as? UILabel)?.text).isEqualTo("orange")
      assertThat((row.subviews[2] as? UILabel)?.text).isEqualTo("yellow")
    }
  }
}
