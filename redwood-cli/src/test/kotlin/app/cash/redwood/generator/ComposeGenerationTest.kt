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

import androidx.compose.runtime.Composable
import app.cash.redwood.schema.Children
import app.cash.redwood.schema.Default
import app.cash.redwood.schema.Property
import app.cash.redwood.schema.Schema
import app.cash.redwood.schema.Widget
import app.cash.redwood.tooling.schema.parseSchema
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ComposeGenerationTest {
  @Schema(
    [
      Row::class,
    ],
  )
  interface ScopedAndUnscopedSchema
  object RowScope

  @Widget(1)
  data class Row(
    @Children(1) val scoped: RowScope.() -> Unit,
    @Children(2) val unscoped: () -> Unit,
  )

  @Test fun `scoped and unscoped children`() {
    val schema = parseSchema(ScopedAndUnscopedSchema::class)

    val fileSpec = generateComposable(schema, schema.widgets.single())
    assertThat(fileSpec.toString()).apply {
      contains("scoped: @Composable RowScope.() -> Unit")
      contains("unscoped: @Composable () -> Unit")
    }
  }

  @Test fun `scope is annotated with layout scope marker`() {
    val schema = parseSchema(ScopedAndUnscopedSchema::class)

    val fileSpec = generateScope(schema, RowScope::class)
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
    @Property(1) @Default("\"test\"")
    val trait: String,
    @Property(2) @Default("{ error(\"test\") }")
    val onEvent: () -> Unit,
    @Children(1) @Default("{}")
    val block: () -> Unit,
  )

  @Test fun `default is supported for all property types`() {
    val schema = parseSchema(DefaultSchema::class)

    val fileSpec = generateComposable(schema, schema.widgets.single())
    assertThat(fileSpec.toString()).apply {
      contains("trait: String = \"test\"")
      contains("onEvent: (() -> Unit)? = { error(\"test\") }")
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
    val schema = parseSchema(MultipleChildSchema::class)

    val fileSpec = generateComposable(schema, schema.widgets.single())
    assertThat(fileSpec.toString()).contains(
      """
      |  layoutModifier: LayoutModifier = LayoutModifier,
      |  top: @Composable () -> Unit,
      |  bottom: @Composable () -> Unit,
      """.trimMargin(),
    )
  }
}
