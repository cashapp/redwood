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
package app.cash.redwood.compose

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import app.cash.redwood.Modifier
import app.cash.redwood.RedwoodCodegenApi
import app.cash.redwood.layout.widget.RedwoodLayoutTestingWidgetFactory
import app.cash.redwood.protocol.widget.ProtocolBridge
import app.cash.redwood.testing.TestRedwoodComposition
import app.cash.redwood.testing.WidgetValue
import app.cash.redwood.widget.MutableListChildren
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import example.redwood.compose.Button
import example.redwood.compose.ExampleSchemaProtocolBridge
import example.redwood.compose.Row
import example.redwood.compose.ScopedRow
import example.redwood.compose.TestScope
import example.redwood.compose.Text
import example.redwood.widget.ExampleSchemaProtocolNodeFactory
import example.redwood.widget.ExampleSchemaTestingWidgetFactory
import example.redwood.widget.ExampleSchemaWidgetFactories
import example.redwood.widget.ExampleSchemaWidgetFactory
import kotlin.test.Test
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest

class DirectChangeListenerTest : AbstractChangeListenerTest() {
  override fun <T> CoroutineScope.launchComposition(
    factories: ExampleSchemaWidgetFactories<WidgetValue>,
    snapshot: () -> T,
  ) = TestRedwoodComposition(this, factories, MutableListChildren(), snapshot)
}

class ProtocolChangeListenerTest : AbstractChangeListenerTest() {
  override fun <T> CoroutineScope.launchComposition(
    factories: ExampleSchemaWidgetFactories<WidgetValue>,
    snapshot: () -> T,
  ): TestRedwoodComposition<T> {
    val composeBridge = ExampleSchemaProtocolBridge.create()
    val widgetBridge = ProtocolBridge(MutableListChildren(), ExampleSchemaProtocolNodeFactory(factories)) {
      throw AssertionError()
    }
    return TestRedwoodComposition(this, composeBridge.provider, composeBridge.root) {
      composeBridge.getChangesOrNull()?.let { changes ->
        widgetBridge.sendChanges(changes)
      }
      snapshot()
    }
  }
}

@OptIn(RedwoodCodegenApi::class)
abstract class AbstractChangeListenerTest {
  // There is no test parameter injector in multiplatform, so we fake it with subtypes.
  abstract fun <T> CoroutineScope.launchComposition(
    factories: ExampleSchemaWidgetFactories<WidgetValue>,
    snapshot: () -> T,
  ): TestRedwoodComposition<T>

  @Test fun propertyChangeNotifiesWidget() = runTest {
    val button = ListeningButton()
    val factories = ExampleSchemaWidgetFactories(
      ExampleSchema = object : ExampleSchemaWidgetFactory<WidgetValue> by ExampleSchemaTestingWidgetFactory() {
        override fun Button() = button
      },
      RedwoodLayout = RedwoodLayoutTestingWidgetFactory(),
    )
    val c = backgroundScope.launchComposition(factories, button::changes)

    var text by mutableStateOf("hi")
    c.setContent {
      Button(text, onClick = null)
    }
    assertThat(c.awaitSnapshot()).containsExactly("modifier Modifier", "text hi", "onClick false", "onEndChanges")

    text = "hello"
    assertThat(c.awaitSnapshot()).containsExactly("text hello", "onEndChanges")
  }

  @Test fun unrelatedPropertyChangeDoesNotNotifyWidget() = runTest {
    val button = ListeningButton()
    val factories = ExampleSchemaWidgetFactories(
      ExampleSchema = object : ExampleSchemaWidgetFactory<WidgetValue> by ExampleSchemaTestingWidgetFactory() {
        override fun Button() = button
      },
      RedwoodLayout = RedwoodLayoutTestingWidgetFactory(),
    )
    val c = backgroundScope.launchComposition(factories, button::changes)

    var text by mutableStateOf("hi")
    c.setContent {
      Button("hi", onClick = null)
      Text(text)
    }
    assertThat(c.awaitSnapshot()).containsExactly("modifier Modifier", "text hi", "onClick false", "onEndChanges")

    text = "hello"
    assertThat(c.awaitSnapshot()).isEmpty()
  }

  @Test fun modifierChangeNotifiesWidget() = runTest {
    val button = ListeningButton()
    val factories = ExampleSchemaWidgetFactories(
      ExampleSchema = object : ExampleSchemaWidgetFactory<WidgetValue> by ExampleSchemaTestingWidgetFactory() {
        override fun Button() = button
      },
      RedwoodLayout = RedwoodLayoutTestingWidgetFactory(),
    )
    val c = backgroundScope.launchComposition(factories, button::changes)

    var modifier by mutableStateOf<Modifier>(Modifier)
    c.setContent {
      Button("hi", onClick = null, modifier = modifier)
    }
    assertThat(c.awaitSnapshot()).containsExactly("modifier Modifier", "text hi", "onClick false", "onEndChanges")

    modifier = with(object : TestScope {}) {
      Modifier.accessibilityDescription("hey")
    }
    assertThat(c.awaitSnapshot()).containsExactly("modifier AccessibilityDescription(value=hey)", "onEndChanges")
  }

  @Test fun multipleChangesNotifyWidgetOnce() = runTest {
    val button = ListeningButton()
    val factories = ExampleSchemaWidgetFactories(
      ExampleSchema = object : ExampleSchemaWidgetFactory<WidgetValue> by ExampleSchemaTestingWidgetFactory() {
        override fun Button() = button
      },
      RedwoodLayout = RedwoodLayoutTestingWidgetFactory(),
    )
    val c = backgroundScope.launchComposition(factories, button::changes)

    var text by mutableStateOf("hi")
    var modifier by mutableStateOf<Modifier>(Modifier)
    c.setContent {
      Button(text, onClick = null, modifier = modifier)
    }
    assertThat(c.awaitSnapshot()).containsExactly("modifier Modifier", "text hi", "onClick false", "onEndChanges")

    text = "hello"
    modifier = with(object : TestScope {}) {
      Modifier.accessibilityDescription("hey")
    }
    assertThat(c.awaitSnapshot()).containsExactly("modifier AccessibilityDescription(value=hey)", "text hello", "onEndChanges")
  }

  @Test fun childrenChangeNotifiesWidget() = runTest {
    val row = ListeningRow()
    val factories = ExampleSchemaWidgetFactories(
      ExampleSchema = object : ExampleSchemaWidgetFactory<WidgetValue> by ExampleSchemaTestingWidgetFactory() {
        override fun Row() = row
      },
      RedwoodLayout = RedwoodLayoutTestingWidgetFactory(),
    )
    val c = backgroundScope.launchComposition(factories, row::changes)

    var two by mutableStateOf(false)
    c.setContent {
      Row {
        Button("one", onClick = null)
        if (two) {
          Button("two", onClick = null)
        }
        Button("three", onClick = null)
      }
    }
    assertThat(c.awaitSnapshot()).containsExactly("modifier Modifier", "children insert", "children insert", "onEndChanges")

    two = true
    assertThat(c.awaitSnapshot()).containsExactly("children insert", "onEndChanges")
  }

  @Test fun childrenDescendantChangeDoesNotNotifyWidget() = runTest {
    val row = ListeningRow()
    val factories = ExampleSchemaWidgetFactories(
      ExampleSchema = object : ExampleSchemaWidgetFactory<WidgetValue> by ExampleSchemaTestingWidgetFactory() {
        override fun Row() = row
      },
      RedwoodLayout = RedwoodLayoutTestingWidgetFactory(),
    )
    val c = backgroundScope.launchComposition(factories, row::changes)

    var two by mutableStateOf(false)
    c.setContent {
      Row {
        ScopedRow {
          Button("one", onClick = null)
          if (two) {
            Button("two", onClick = null)
          }
          Button("three", onClick = null)
        }
      }
    }
    assertThat(c.awaitSnapshot()).containsExactly("modifier Modifier", "children insert", "onEndChanges")

    two = true
    assertThat(c.awaitSnapshot()).isEmpty()
  }
}
