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
package app.cash.redwood.generator

import app.cash.redwood.schema.Property
import app.cash.redwood.schema.Schema
import app.cash.redwood.schema.Widget
import app.cash.redwood.schema.parser.parseSchema
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class WidgetGenerationTest {
  @Schema(
    [
      NavigationBar.Button::class,
      Button::class,
    ],
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
      contains("fun WidgetGenerationTestNavigationBarButton()")
      contains("fun WidgetGenerationTestButton()")
    }
  }

  @Test fun tagInWidgetFactoryKDoc() {
    val schema = parseSchema(SimpleNameCollisionSchema::class)

    val fileSpec = generateWidgetFactory(schema)
    assertThat(fileSpec.toString()).contains(
      """
      |   * {tag=3}
      |   */
      |  public fun WidgetGenerationTestButton
      """.trimMargin(),
    )
  }

  @Test fun tagInWidgetKdoc() {
    val schema = parseSchema(SimpleNameCollisionSchema::class)
    val button = schema.widgets.single { it.flatName == "WidgetGenerationTestButton" }

    val fileSpec = generateWidget(schema, button)
    assertThat(fileSpec.toString()).apply {
      contains(
        """
        | * {tag=3}
        | */
        |public interface WidgetGenerationTestButton
        """.trimMargin(),
      )
      contains(
        """
        |   * {tag=1}
        |   */
        |  public fun text
        """.trimMargin(),
      )
    }
  }
}
