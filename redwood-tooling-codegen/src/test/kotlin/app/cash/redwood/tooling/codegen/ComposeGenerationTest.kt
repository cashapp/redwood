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

import androidx.compose.runtime.Composable
import app.cash.redwood.schema.Children
import app.cash.redwood.schema.Property
import app.cash.redwood.schema.Schema
import app.cash.redwood.schema.Widget
import app.cash.redwood.tooling.schema.FqType
import app.cash.redwood.tooling.schema.parseTestSchema
import assertk.all
import assertk.assertThat
import assertk.assertions.contains
import org.junit.Test

object RowScope

class ComposeGenerationTest {
  @Schema(
    [
      Row::class,
    ],
  )
  interface ScopedAndUnscopedSchema

  @Widget(1)
  data class Row(
    @Children(1) val scoped: RowScope.() -> Unit,
    @Children(2) val unscoped: () -> Unit,
  )

  @Test fun `scoped and unscoped children`() {
    val schema = parseTestSchema(ScopedAndUnscopedSchema::class).schema

    val fileSpec = generateComposable(schema, schema.widgets.single())
    assertThat(fileSpec.toString()).all {
      contains("scoped: @Composable RowScope.() -> Unit")
      contains("unscoped: @Composable () -> Unit")
    }
  }

  @Test fun `scope is annotated with layout scope marker`() {
    val schema = parseTestSchema(ScopedAndUnscopedSchema::class).schema

    val fileSpec = generateModifierScope(schema, FqType(listOf("example", "RowScope")))
    assertThat(fileSpec.toString()).contains(
      """
      |@LayoutScopeMarker
      |public interface RowScope
      """.trimMargin(),
    )
  }

  @Schema(
    [
      DefaultTestWidget::class,
    ],
  )
  interface DefaultSchema

  @Widget(1)
  data class DefaultTestWidget(
    @Property(1)
    val trait: String = "test",
    @Property(2)
    val onEvent: () -> Unit = { error("test") },
    @Children(1)
    val block: () -> Unit = {},
  )

  @Test fun `default is supported for all property types`() {
    val schema = parseTestSchema(DefaultSchema::class).schema

    val fileSpec = generateComposable(schema, schema.widgets.single())
    assertThat(fileSpec.toString()).all {
      contains("trait: String = \"test\"")
      contains("onEvent: () -> Unit = { error(\"test\") }")
      contains("block: @Composable () -> Unit = {}")
    }
  }

  @Schema(
    [
      MultipleChildWidget::class,
    ],
  )
  interface MultipleChildSchema

  @Widget(1)
  data class MultipleChildWidget(
    @Children(1) val top: @Composable () -> Unit,
    @Children(2) val bottom: @Composable () -> Unit,
  )

  @Test fun `layout modifier is the last non child parameter`() {
    val schema = parseTestSchema(MultipleChildSchema::class).schema

    val fileSpec = generateComposable(schema, schema.widgets.single())
    assertThat(fileSpec.toString()).contains(
      """
      |  modifier: Modifier = Modifier,
      |  top: @Composable () -> Unit,
      |  bottom: @Composable () -> Unit,
      """.trimMargin(),
    )
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
  object DeprecatedWidget

  @Test fun deprecation() {
    // NOTE: There's no way to deprecate a parameter to a function.

    val schema = parseTestSchema(DeprecatedSchema::class).schema

    val fileSpec = generateComposable(schema, schema.widgets.single())
    assertThat(fileSpec.toString()).contains(
      """
      |@Deprecated(
      |  "Hey",
      |  level = WARNING,
      |)
      |public fun ComposeGenerationTestDeprecatedWidget
      """.trimMargin(),
    )
  }
}
