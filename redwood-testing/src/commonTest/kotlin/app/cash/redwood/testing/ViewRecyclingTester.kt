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

import androidx.compose.runtime.Composable
import app.cash.redwood.RedwoodCodegenApi
import app.cash.redwood.layout.widget.RedwoodLayoutTestingWidgetFactory
import app.cash.redwood.lazylayout.widget.RedwoodLazyLayoutTestingWidgetFactory
import app.cash.redwood.protocol.ChildrenTag
import app.cash.redwood.protocol.WidgetTag
import app.cash.redwood.protocol.widget.ProtocolBridge
import app.cash.redwood.protocol.widget.ReuseId
import app.cash.redwood.widget.MutableListChildren
import app.cash.redwood.widget.Widget
import com.example.redwood.testing.compose.TestSchemaProtocolBridge
import com.example.redwood.testing.widget.TestSchemaProtocolFactory
import com.example.redwood.testing.widget.TestSchemaTester
import com.example.redwood.testing.widget.TestSchemaTestingWidgetFactory
import com.example.redwood.testing.widget.TestSchemaWidgetSystem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope

/**
 * Like [TestSchemaTester], but this also hooks up Redwood's protocol mechanism. That's necessary
 * because view recycling is only implemented as a part of the host-side protocol.
 */
@OptIn(RedwoodCodegenApi::class)
class ViewRecyclingTester(
  coroutineScope: CoroutineScope,
) {
  private val compositionProtocolBridge = TestSchemaProtocolBridge.create()

  internal val composition = TestRedwoodComposition(
    scope = coroutineScope,
    widgetSystem = compositionProtocolBridge.widgetSystem,
    container = compositionProtocolBridge.root,
    createSnapshot = { }, // The snapshot's value is a sentinel 'Unit'.
  )

  private val widgetProtocolFactory = TestSchemaProtocolFactory(
    widgetSystem = TestSchemaWidgetSystem(
      TestSchema = TestSchemaTestingWidgetFactory(),
      RedwoodLayout = RedwoodLayoutTestingWidgetFactory(),
      RedwoodLazyLayout = RedwoodLazyLayoutTestingWidgetFactory(),
    ),
  )

  private val widgetContainer = MutableListChildren<WidgetValue>()

  private val widgetBridge = ProtocolBridge(
    container = widgetContainer,
    factory = widgetProtocolFactory,
    eventSink = { throw AssertionError() },
    recycler = object : ProtocolBridge.Recycler {
      override fun reuseId(widgetTag: WidgetTag): ReuseId? {
        return when (widgetTag) {
          WidgetTag(1000004) -> ReuseId("box")
          else -> null
        }
      }

      override fun childrenTags(widgetTag: WidgetTag): List<ChildrenTag> {
        return when (widgetTag) {
          WidgetTag(1000004) -> listOf(ChildrenTag(1))
          else -> listOf()
        }
      }
    },
  )

  fun setContent(content: @Composable () -> Unit) {
    composition.setContent(content)
  }

  /** Returns the root widgets as mutable objects that have an identity. */
  val widgets: List<Widget<WidgetValue>>
    get() = widgetContainer.toList()

  /** Returns the a list of value objects. */
  suspend fun awaitSnapshot(): List<WidgetValue> {
    composition.awaitSnapshot()
    widgetBridge.sendChanges(compositionProtocolBridge.getChangesOrNull() ?: listOf())
    return widgetContainer.map { it.value }
  }
}

suspend fun <R> viewRecyclingTest(
  body: suspend ViewRecyclingTester.() -> R
): R = coroutineScope {
  val tester = ViewRecyclingTester(this)
  try {
    tester.body()
  } finally {
    tester.composition.cancel()
  }
}
