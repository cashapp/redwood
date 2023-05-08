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

import app.cash.redwood.schema.Children
import app.cash.redwood.schema.Property
import app.cash.redwood.schema.Schema
import app.cash.redwood.schema.Widget
import app.cash.redwood.tooling.schema.ProtocolSchemaSet
import assertk.all
import assertk.assertThat
import assertk.assertions.contains
import kotlin.DeprecationLevel.ERROR
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
    val schema = ProtocolSchemaSet.parse(SimpleNameCollisionSchema::class).schema

    val fileSpec = generateWidgetFactory(schema)
    assertThat(fileSpec.toString()).all {
      contains("fun WidgetGenerationTestNavigationBarButton()")
      contains("fun WidgetGenerationTestButton()")
    }
  }

  @Test fun tagInWidgetFactoryKDoc() {
    val schema = ProtocolSchemaSet.parse(SimpleNameCollisionSchema::class).schema

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
    val schema = ProtocolSchemaSet.parse(SimpleNameCollisionSchema::class).schema
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

  @Suppress("DEPRECATION")
  @Schema(
    [
      DeprecatedWidget::class,
    ],
  )
  interface DeprecatedSchema

  @Widget(1)
  @Deprecated("Hey")
  data class DeprecatedWidget(
    @Property(1)
    @Deprecated("Property", level = ERROR)
    val prop: String,
    @Property(2)
    @Deprecated("Event", level = ERROR)
    val event: () -> Unit,
    @Children(1)
    @Deprecated("Children", level = ERROR)
    val children: () -> Unit,
  )

  @Test fun deprecation() {
    val schema = ProtocolSchemaSet.parse(DeprecatedSchema::class).schema

    val widget = schema.widgets.single()
    val fileSpec = generateWidget(schema, widget)
    assertThat(fileSpec.toString()).all {
      contains(
        """
        |@Deprecated(
        |  "Hey",
        |  level = WARNING,
        |)
        |public interface WidgetGenerationTestDeprecatedWidget
        """.trimMargin(),
      )

      contains(
        """
        |  @Deprecated(
        |    "Property",
        |    level = ERROR,
        |  )
        |  public fun prop(
        """.trimMargin(),
      )

      contains(
        """
        |  @Deprecated(
        |    "Event",
        |    level = ERROR,
        |  )
        |  public fun event(
        """.trimMargin(),
      )

      contains(
        """
        |  @Deprecated(
        |    "Children",
        |    level = ERROR,
        |  )
        |  public val children:
        """.trimMargin(),
      )
    }
  }
}
