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
package app.cash.redwood.compose

import app.cash.redwood.ui.Margin
import app.cash.redwood.ui.dp
import assertk.assertThat
import assertk.assertions.containsExactly
import com.example.redwood.testapp.compose.Text
import com.example.redwood.testapp.testing.TestSchemaTester
import com.example.redwood.testapp.testing.TextValue
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class LocalViewInsetsTest {
  @Test
  fun localInsetsUpdated() = runTest {
    TestSchemaTester {
      uiConfigurations.value = uiConfigurations.value.copy(
        viewInsets = Margin(top = 30.dp),
      )

      setContent {
        Text("available=${LocalViewInsets.current}")
      }

      assertThat(awaitSnapshot()).containsExactly(
        TextValue(text = "available=${Margin(top = 30.0.dp)}"),
      )

      uiConfigurations.value = uiConfigurations.value.copy(
        viewInsets = Margin(top = 20.dp),
      )

      assertThat(awaitSnapshot()).containsExactly(
        TextValue(text = "available=${Margin(top = 20.0.dp)}"),
      )
    }
  }

  @Test
  fun consumeInsets() = runTest {
    TestSchemaTester {
      uiConfigurations.value = uiConfigurations.value.copy(
        viewInsets = Margin(top = 30.dp),
      )

      setContent {
        Text("before available=${LocalViewInsets.current}")

        ConsumeInsets { consumed ->
          Text("child consumed=$consumed")
          Text("child available=${LocalViewInsets.current}")
        }

        Text("after available=${LocalViewInsets.current}")
      }

      assertThat(awaitSnapshot()).containsExactly(
        TextValue(text = "before available=${Margin(top = 30.0.dp)}"),
        TextValue(text = "child consumed=${Margin(top = 30.0.dp)}"),
        TextValue(text = "child available=${Margin(top = 0.0.dp)}"),
        TextValue(text = "after available=${Margin(top = 30.0.dp)}"),
      )
    }
  }

  @Test
  fun consumeInsetsWithMax() = runTest {
    TestSchemaTester {
      uiConfigurations.value = uiConfigurations.value.copy(
        viewInsets = Margin(top = 30.dp),
      )

      setContent {
        Text("before available=${LocalViewInsets.current}")

        ConsumeInsets(maximumValue = Margin(top = 10.0.dp)) { consumed ->
          Text("child consumed=$consumed")
          Text("child available=${LocalViewInsets.current}")
        }

        Text("after available=${LocalViewInsets.current}")
      }

      assertThat(awaitSnapshot()).containsExactly(
        TextValue(text = "before available=${Margin(top = 30.0.dp)}"),
        TextValue(text = "child consumed=${Margin(top = 10.0.dp)}"),
        TextValue(text = "child available=${Margin(top = 20.0.dp)}"),
        TextValue(text = "after available=${Margin(top = 30.0.dp)}"),
      )
    }
  }

  @Test
  fun consumeAllInsetsWithMaxAndUpdates() = runTest {
    TestSchemaTester {
      uiConfigurations.value = uiConfigurations.value.copy(
        viewInsets = Margin(10.0.dp, 20.0.dp, 30.0.dp, 40.0.dp),
      )

      setContent {
        ConsumeInsets(maximumValue = Margin(all = 15.dp)) { consumed ->
          Text("consumed=$consumed")
          Text("available=${LocalViewInsets.current}")
        }
      }

      assertThat(awaitSnapshot()).containsExactly(
        TextValue(text = "consumed=${Margin(10.0.dp, 15.0.dp, 15.0.dp, 15.0.dp)}"),
        TextValue(text = "available=${Margin(0.0.dp, 5.0.dp, 15.0.dp, 25.0.dp)}"),
      )

      uiConfigurations.value = uiConfigurations.value.copy(
        viewInsets = Margin(11.0.dp, 21.0.dp, 31.0.dp, 41.0.dp),
      )

      assertThat(awaitSnapshot()).containsExactly(
        TextValue(text = "consumed=${Margin(11.0.dp, 15.0.dp, 15.0.dp, 15.0.dp)}"),
        TextValue(text = "available=${Margin(0.0.dp, 6.0.dp, 16.0.dp, 26.0.dp)}"),
      )
    }
  }
}
