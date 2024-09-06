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
package app.cash.redwood.tooling.codegen

import app.cash.redwood.schema.Property
import app.cash.redwood.schema.Schema
import app.cash.redwood.schema.Widget
import app.cash.redwood.tooling.schema.parseTestSchema
import assertk.all
import assertk.assertThat
import assertk.assertions.contains
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
    val schema = parseTestSchema(SimpleNameCollisionSchema::class).schema

    val fileSpec = generateWidgetFactory(schema)
    assertThat(fileSpec.toString()).all {
      contains("fun WidgetGenerationTestNavigationBarButton()")
      contains("fun WidgetGenerationTestButton()")
    }
  }

  @Test fun tagInWidgetFactoryKDoc() {
    val schema = parseTestSchema(SimpleNameCollisionSchema::class).schema

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
    val schema = parseTestSchema(SimpleNameCollisionSchema::class).schema
    val button = schema.widgets.single { it.type.flatName == "WidgetGenerationTestButton" }

    val fileSpec = generateWidget(schema, button)
    assertThat(fileSpec.toString()).all {
      contains(
        """
        | * {tag=3}
        | */
        |@OptIn(ExperimentalObjCName::class)
        |@ObjCName(
        |  "WidgetGenerationTestButton",
        |  exact = true,
        |)
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

  @Schema(
    [
      EventParameterNameWidget::class,
    ],
  )
  interface EventParameterNameSchema

  @Widget(1)
  data class EventParameterNameWidget(
    @Property(1) val none: () -> Unit,
    @Property(2) val one: (s: String) -> Unit,
    @Property(3) val mixed: (i: Int, Long) -> Unit,
  )

  @Test fun eventParameterNames() {
    val schema = parseTestSchema(EventParameterNameSchema::class).schema

    val fileSpec = generateWidget(schema, schema.widgets.single())
    assertThat(fileSpec.toString()).all {
      contains("fun none(none: () -> Unit)")
      contains("fun one(one: (s: String) -> Unit)")
      contains("fun mixed(mixed: (i: Int, Long) -> Unit)")
    }
  }
}
