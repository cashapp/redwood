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
package app.cash.treehouse.schema.generator

import app.cash.treehouse.schema.Property
import app.cash.treehouse.schema.Schema
import app.cash.treehouse.schema.Widget
import app.cash.treehouse.schema.parser.parseSchema
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.util.regex.Pattern
import java.util.regex.Pattern.MULTILINE

class GenerateProtocolWidgetFactoryTest {
  @Schema(
    [
      NavigationBar.Button::class,
      Button::class,
    ]
  )
  interface SimpleNameCollisionSchema
  interface NavigationBar {
    @Widget(1)
    data class Button(@Property(1) val text: String)
  }
  @Widget(3)
  data class Button(@Property(1) val text: String)

  @Test fun `simple names do not collide`() {
    val schema = parseSchema(SimpleNameCollisionSchema::class)

    val fileSpec = generateWidgetFactory(schema)
    assertThat(fileSpec.toString()).apply {
      contains("fun GenerateProtocolWidgetFactoryTestNavigationBarButton()")
      contains("fun GenerateProtocolWidgetFactoryTestButton()")
    }
  }

  @Schema(
    [
      Node12::class,
      Node1::class,
      Node3::class,
      Node2::class,
    ]
  )
  interface SortedByTagSchema
  @Widget(1)
  data class Node1(@Property(1) val text: String)
  @Widget(2)
  data class Node2(@Property(1) val text: String)
  @Widget(3)
  data class Node3(@Property(1) val text: String)
  @Widget(12)
  data class Node12(@Property(1) val text: String)

  @Test fun `names are sorted by their node tags`() {
    val schema = parseSchema(SortedByTagSchema::class)

    val fileSpec = generateDisplayProtocolWidgetFactory(schema)
    assertThat(fileSpec.toString()).containsMatch(
      Pattern.compile("1 ->[^2]+2 ->[^3]+3 ->[^1]+12 ->", MULTILINE)
    )
  }
}
