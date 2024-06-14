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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import app.cash.redwood.RedwoodCodegenApi
import app.cash.redwood.layout.testing.RedwoodLayoutTestingWidgetFactory
import app.cash.redwood.lazylayout.testing.RedwoodLazyLayoutTestingWidgetFactory
import app.cash.redwood.protocol.guest.DefaultProtocolGuest
import app.cash.redwood.protocol.guest.guestRedwoodVersion
import app.cash.redwood.protocol.host.ProtocolHost
import app.cash.redwood.protocol.host.hostRedwoodVersion
import app.cash.redwood.widget.MutableListChildren
import app.cash.redwood.widget.Widget
import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import com.example.redwood.testapp.protocol.guest.TestSchemaProtocolWidgetSystemFactory
import com.example.redwood.testapp.protocol.host.TestSchemaProtocolFactory
import com.example.redwood.testapp.testing.TestSchemaTester
import com.example.redwood.testapp.testing.TestSchemaTestingWidgetFactory
import com.example.redwood.testapp.widget.TestSchemaWidgetSystem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope

/**
 * Like [TestSchemaTester], but this also hooks up Redwood's protocol mechanism. That's necessary
 * because view recycling is only implemented as a part of the host-side protocol.
 */
class ViewRecyclingTester(
  coroutineScope: CoroutineScope,
) {
  @OptIn(RedwoodCodegenApi::class)
  private val widgetProtocolFactory = TestSchemaProtocolFactory(
    widgetSystem = TestSchemaWidgetSystem(
      TestSchema = TestSchemaTestingWidgetFactory(),
      RedwoodLayout = RedwoodLayoutTestingWidgetFactory(),
      RedwoodLazyLayout = RedwoodLazyLayoutTestingWidgetFactory(),
    ),
  )

  private val widgetContainer = MutableListChildren<WidgetValue>()

  val host = ProtocolHost(
    guestVersion = guestRedwoodVersion,
    container = widgetContainer,
    factory = widgetProtocolFactory,
    eventSink = { throw AssertionError() },
  )

  private val guest = DefaultProtocolGuest(
    hostVersion = hostRedwoodVersion,
    widgetSystemFactory = TestSchemaProtocolWidgetSystemFactory,
  ).apply {
    initChangesSink(host)
  }

  internal val composition = TestRedwoodComposition(
    scope = coroutineScope,
    widgetSystem = guest.widgetSystem,
    container = guest.root,
    createSnapshot = { }, // The snapshot's value is a sentinel 'Unit'.
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
    guest.emitChanges()
    return widgetContainer.map { it.value }
  }
}

suspend fun <R> viewRecyclingTest(
  body: suspend ViewRecyclingTester.() -> R,
): R = coroutineScope {
  val tester = ViewRecyclingTester(this)
  try {
    tester.body()
  } finally {
    tester.composition.cancel()
  }
}

suspend fun assertReuse(
  content: @Composable (step: Int) -> Unit,
  assertFullSubtreesEqual: Boolean = true,
  step3Value: WidgetValue,
): Pair<List<Widget<WidgetValue>>, List<Widget<WidgetValue>>> {
  return viewRecyclingTest {
    var step by mutableIntStateOf(1)
    setContent {
      content(step)
    }

    // Step 1 draws the initial content, including the reusable element.
    awaitSnapshot()
    val step1Widgets = widgets.toList()
    val step1WidgetsFlattened = step1Widgets.flatten().toList()

    // Step 2 removes it, causing it to be pooled.
    step = 2
    awaitSnapshot()

    // Step 3 adds it back from the pool.
    step = 3
    awaitSnapshot()
    val step3Widgets = widgets
    val step3WidgetsFlattened = step3Widgets.flatten().toList()

    // Confirm the widgets are all the same.
    if (assertFullSubtreesEqual) {
      assertThat(step3WidgetsFlattened)
        .containsExactlyInAnyOrder(*step1WidgetsFlattened.toTypedArray())
    }
    assertThat(widgets.single().value).isEqualTo(step3Value)
    step1WidgetsFlattened to step3WidgetsFlattened
  }
}

suspend fun assertNoReuse(
  content: @Composable (step: Int) -> Unit,
  stepCount: Int = 3,
) {
  require(stepCount == 3 || stepCount == 4)

  viewRecyclingTest {
    var step by mutableIntStateOf(1)
    setContent {
      content(step)
    }

    // Step 1 draws the initial content, including the reusable element.
    awaitSnapshot()
    val allWidgetsBefore = widgets.flatten().toSet()

    // Step 2 removes it, causing it to be pooled.
    step = 2
    awaitSnapshot()

    // Queue up changes for step 3, but don't apply them. This is what happens in production when
    // the guest emits produces faster than the host consumes them. Use this to cause a node to be
    // added and removed in the same list of changes.
    step = 3
    if (stepCount == 4) {
      composition.awaitSnapshot()
      step = 4
    }
    awaitSnapshot()

    // Get widgets from the last step.
    val allWidgetsAfter = widgets.flatten().toSet()

    // Confirm none of the after widgets were reused.
    assertThat(allWidgetsBefore.intersect(allWidgetsAfter)).isEmpty()
  }
}

private fun List<Widget<WidgetValue>>.flatten(): Sequence<Widget<WidgetValue>> {
  return sequence {
    for (widget in this@flatten) {
      flattenRecursive(widget)
    }
  }
}

@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE") // To test implementation details!
private suspend fun SequenceScope<Widget<WidgetValue>>.flattenRecursive(
  widget: Widget<WidgetValue>,
) {
  yield(widget)

  val childrenLists = when (widget) {
    is app.cash.redwood.layout.testing.MutableBox -> listOf(widget.children)
    is app.cash.redwood.layout.testing.MutableColumn -> listOf(widget.children)
    is com.example.redwood.testapp.testing.MutableButton -> listOf()
    is com.example.redwood.testapp.testing.MutableSplit -> listOf(widget.left, widget.right)
    is com.example.redwood.testapp.testing.MutableText -> listOf()
    else -> error("unexpected widget: $widget")
  }

  for (children in childrenLists) {
    for (child in children) {
      flattenRecursive(child)
    }
  }
}
