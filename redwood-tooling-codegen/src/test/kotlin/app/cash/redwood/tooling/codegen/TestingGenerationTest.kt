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
package app.cash.redwood.tooling.codegen

import app.cash.redwood.schema.Children
import app.cash.redwood.schema.Default
import app.cash.redwood.schema.Property
import app.cash.redwood.schema.Schema
import app.cash.redwood.schema.Widget
import app.cash.redwood.tooling.schema.parseSchema
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TestingGenerationTest {
  @Schema(
    [
      BasicWidget::class,
    ],
  )
  interface HappyPathSchema

  @Widget(1)
  data class BasicWidget(
    @Property(1)
    @Default("\"test\"")
    val trait: String,
    @Property(2)
    @Default("{ error(\"test\") }")
    val onEvent: () -> Unit,
    @Children(1)
    @Default("{}")
    val block: () -> Unit,
  )

  @Test fun `tester happy path`() {
    val schema = parseSchema(HappyPathSchema::class)

    val testerFileSpec = generateTester(schema)
    assertThat(testerFileSpec.toString()).contains(
      """
      |@OptIn(RedwoodCodegenApi::class)
      |public fun HappyPathSchemaTester(scope: CoroutineScope): RedwoodTester = RedwoodTester(
      |  scope = scope,
      |  provider = HappyPathSchemaWidgetFactories(
      |    HappyPathSchema = MutableHappyPathSchemaWidgetFactory(),
      |  ),
      |)
      """.trimMargin())

    val mutableWidgetFactorySpec = generateMutableWidgetFactory(schema)
    assertThat(mutableWidgetFactorySpec.toString()).contains(
      """
      |@RedwoodCodegenApi
      |public class MutableHappyPathSchemaWidgetFactory : HappyPathSchemaWidgetFactory<MutableWidget> {
      |  public override fun TestingGenerationTestBasicWidget():
      |      TestingGenerationTestBasicWidget<MutableWidget> = MutableTestingGenerationTestBasicWidget()
      |}
      """.trimMargin())

    val mutableWidgetSpec = generateMutableWidget(schema, schema.widgets.single())
    assertThat(mutableWidgetSpec.toString()).contains(
      """
      |@RedwoodCodegenApi
      |public class MutableTestingGenerationTestBasicWidget :
      |    TestingGenerationTestBasicWidget<MutableWidget>, MutableWidget {
      |  public override val `value`: MutableWidget
      |    get() = this
      |
      |  public override var layoutModifiers: LayoutModifier = LayoutModifier
      |
      |  private var trait: String = "test"
      |
      |  private var onEvent: (() -> Unit)? = { error("test") }
      |
      |  public override val block: MutableListChildren<MutableWidget> =
      |      MutableListChildren<MutableWidget>()
      |
      |  public override fun trait(trait: String): Unit {
      |    this.trait = trait
      |  }
      |
      |  public override fun onEvent(onEvent: (() -> Unit)?): Unit {
      |    this.onEvent = onEvent
      |  }
      |
      |  public override fun snapshot(): TestingGenerationTestBasicWidgetValue =
      |      TestingGenerationTestBasicWidgetValue(
      |    layoutModifiers = layoutModifiers,
      |    trait = trait,
      |    onEvent = onEvent,
      |    block = block.map { it.`value`.snapshot() },
      |  )
      |}
      """.trimMargin())

    val widgetValueSpec = generateWidgetValue(schema, schema.widgets.single())
    assertThat(widgetValueSpec.toString()).contains(
      """
      |public class TestingGenerationTestBasicWidgetValue(
      |  public override val layoutModifiers: LayoutModifier = LayoutModifier,
      |  public val trait: String = "test",
      |  public val onEvent: (() -> Unit)? = { error("test") },
      |  public val block: List<WidgetValue> = listOf(),
      |) : WidgetValue {
      |  public override val childrenLists: List<List<WidgetValue>>
      |    get() = listOf(
      |      block,
      |    )
      |
      |  public override fun equals(other: Any?): Boolean = other is
      |      TestingGenerationTestBasicWidgetValue &&
      |    other.layoutModifiers == layoutModifiers &&
      |    other.trait == trait &&
      |    other.block == block
      |
      |  public override fun hashCode(): Int = listOf(
      |    layoutModifiers,
      |    trait,
      |    block,
      |  ).hashCode()
      |
      |  public override fun toString(): String =
      |      "${'"'}"TestingGenerationTestBasicWidgetValue(layoutModifiers=${'$'}layoutModifiers, trait=${'$'}trait, block=${'$'}block)"${'"'}"
      |}
      """.trimMargin())
  }
}
