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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import app.cash.redwood.Modifier
import app.cash.redwood.RedwoodCodegenApi
import app.cash.redwood.layout.compose.Box
import app.cash.redwood.protocol.ChildrenTag
import app.cash.redwood.protocol.Id
import app.cash.redwood.protocol.host.ProtocolHost
import app.cash.redwood.protocol.host.ProtocolNode
import assertk.assertThat
import assertk.assertions.hasMessage
import assertk.assertions.isNotNull
import assertk.assertions.isSameInstanceAs
import com.example.redwood.testapp.compose.TestRow
import com.example.redwood.testapp.compose.Text
import com.example.redwood.testapp.compose.reuse
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlinx.coroutines.test.runTest

@OptIn(RedwoodCodegenApi::class)
class WidgetDetachingTest {
  @Test
  fun widgetIsDetachedWhenItIsRemovedFromComposition() = runTest {
    viewRecyclingTest {
      var step by mutableIntStateOf(1)

      setContent {
        TestRow {
          if (step == 1) {
            Text("hello")
          }
        }
      }

      awaitSnapshot()
      val protocolText = host.extractProtocolText()
      assertThat(protocolText.widget).isNotNull()

      step++
      awaitSnapshot()
      val thrown = assertFailsWith<IllegalStateException> {
        protocolText.widget
      }
      assertThat(thrown).hasMessage("detached")
    }
  }

  @Test
  fun widgetIsDetachedWhenItsParentIsRemovedFromComposition() = runTest {
    viewRecyclingTest {
      var step by mutableIntStateOf(1)

      setContent {
        if (step == 1) {
          TestRow {
            Text("hello")
          }
        }
      }

      awaitSnapshot()
      val protocolText = host.extractProtocolText()
      assertThat(protocolText.widget).isNotNull()

      step++
      awaitSnapshot()
      val thrown = assertFailsWith<IllegalStateException> {
        protocolText.widget
      }
      assertThat(thrown).hasMessage("detached")
    }
  }

  @Test
  fun widgetIsDetachedWhenProtocolHostIsClosed() = runTest {
    viewRecyclingTest {
      setContent {
        TestRow {
          Text("hello")
        }
      }

      awaitSnapshot()
      val protocolText = host.extractProtocolText()
      assertThat(protocolText.widget).isNotNull()

      host.close()
      val thrown = assertFailsWith<IllegalStateException> {
        protocolText.widget
      }
      assertThat(thrown).hasMessage("detached")
    }
  }

  @Test
  fun widgetIsNotDetachedWhenWidgetIsPooled() = runTest {
    viewRecyclingTest {
      var step by mutableIntStateOf(1)

      setContent {
        TestRow {
          if (step == 1 || step == 3) {
            Text(
              modifier = Modifier.reuse(),
              text = "hello",
            )
          }
        }
      }

      awaitSnapshot()
      val protocolTextStep1 = host.extractProtocolText()
      assertThat(protocolTextStep1.widget).isNotNull()

      step++
      awaitSnapshot()
      assertThat(protocolTextStep1.widget).isNotNull()

      step++
      awaitSnapshot()
      assertThat(host.extractProtocolText()).isSameInstanceAs(protocolTextStep1)
    }
  }

  @Test
  @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE") // To test implementation details!
  fun widgetIsDetachedWhenWidgetIsEvictedFromPool() = runTest {
    val poolSize = app.cash.redwood.protocol.host.POOL_SIZE

    viewRecyclingTest {
      var step by mutableIntStateOf(1)

      setContent {
        TestRow {
          if (step == 1) {
            Text(
              modifier = Modifier.reuse(),
              text = "hello",
            )
          } else if (step == 2) {
            // These elements won't be pooled until step 3...
            for (i in 0 until poolSize) {
              Box(
                modifier = Modifier.reuse(),
              ) {
              }
            }
          }
        }
      }

      awaitSnapshot()
      val protocolText = host.extractProtocolText()
      assertThat(protocolText.widget).isNotNull()

      step++
      awaitSnapshot()
      assertThat(protocolText.widget).isNotNull()

      step++
      awaitSnapshot()
      val thrown = assertFailsWith<IllegalStateException> {
        protocolText.widget
      }
      assertThat(thrown).hasMessage("detached")
    }
  }

  @Test
  @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE") // To test implementation details!
  fun widgetIsDetachedWhenParentWidgetIsEvictedFromPool() = runTest {
    val poolSize = app.cash.redwood.protocol.host.POOL_SIZE

    viewRecyclingTest {
      var step by mutableIntStateOf(1)

      setContent {
        if (step == 1) {
          TestRow(
            modifier = Modifier.reuse(),
          ) {
            Text("hello")
          }
        } else if (step == 2) {
          // These elements won't be pooled until step 3...
          for (i in 0 until poolSize) {
            Box(
              modifier = Modifier.reuse(),
            ) {
            }
          }
        }
      }

      awaitSnapshot()
      val protocolText = host.extractProtocolText()
      assertThat(protocolText.widget).isNotNull()

      // protocolText is not detached when its parent is pooled.
      step++
      awaitSnapshot()
      assertThat(protocolText.widget).isNotNull()

      // protocolText is detached when its parent is evicted from the pool.
      step++
      awaitSnapshot()
      val thrown = assertFailsWith<IllegalStateException> {
        protocolText.widget
      }
      assertThat(thrown).hasMessage("detached")
    }
  }

  @Test
  fun pooledWidgetIsDetachedWhenProtocolHostIsClosed() = runTest {
    viewRecyclingTest {
      var step by mutableIntStateOf(1)

      setContent {
        TestRow {
          if (step == 1) {
            Text(
              modifier = Modifier.reuse(),
              text = "hello",
            )
          }
        }
      }

      awaitSnapshot()
      val protocolText = host.extractProtocolText()
      assertThat(protocolText.widget).isNotNull()

      step++
      awaitSnapshot()
      assertThat(protocolText.widget).isNotNull()

      host.close()
      val thrown = assertFailsWith<IllegalStateException> {
        protocolText.widget
      }
      assertThat(thrown).hasMessage("detached")
    }
  }

  @Test
  fun childOfPooledWidgetIsDetachedWhenProtocolHostIsClosed() = runTest {
    viewRecyclingTest {
      var step by mutableIntStateOf(1)

      setContent {
        if (step == 1) {
          TestRow(
            modifier = Modifier.reuse(),
          ) {
            Text("hello")
          }
        }
      }

      awaitSnapshot()
      val protocolText = host.extractProtocolText()
      assertThat(protocolText.widget).isNotNull()

      step++
      awaitSnapshot()
      assertThat(protocolText.widget).isNotNull()

      host.close()
      val thrown = assertFailsWith<IllegalStateException> {
        protocolText.widget
      }
      assertThat(thrown).hasMessage("detached")
    }
  }

  @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE") // To test implementation details!
  private fun ProtocolHost<WidgetValue>.extractProtocolText(): ProtocolNode<WidgetValue> {
    val root = node(Id.Root)
    val testRow = root.children(ChildrenTag.Root)!!.nodes.single()
    return testRow.children(ChildrenTag(1))!!.nodes.single()
  }
}
