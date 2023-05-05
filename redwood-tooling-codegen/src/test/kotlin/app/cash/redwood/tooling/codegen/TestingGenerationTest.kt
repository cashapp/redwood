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
import app.cash.redwood.tooling.schema.ProtocolSchemaSet
import assertk.assertThat
import assertk.assertions.contains
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
    val schemaSet = ProtocolSchemaSet.parse(HappyPathSchema::class)
    val schema = schemaSet.schema

    val testerFileSpec = generateTester(schemaSet)
    assertThat(testerFileSpec.toString()).contains(
      """
      |@OptIn(RedwoodCodegenApi::class)
      |public fun TestingGenerationTestHappyPathSchemaTester(): RedwoodTester = RedwoodTester(
      |  provider = TestingGenerationTestHappyPathSchemaWidgetFactories(
      |    TestingGenerationTestHappyPathSchema =
      |        MutableTestingGenerationTestHappyPathSchemaWidgetFactory(),
      |  ),
      |)
      """.trimMargin(),
    )

    val mutableWidgetFactorySpec = generateMutableWidgetFactory(schema)
    assertThat(mutableWidgetFactorySpec.toString()).contains(
      """
      |@RedwoodCodegenApi
      |public class MutableTestingGenerationTestHappyPathSchemaWidgetFactory :
      |    TestingGenerationTestHappyPathSchemaWidgetFactory<MutableWidget> {
      |  public override fun TestingGenerationTestBasicWidget():
      |      TestingGenerationTestBasicWidget<MutableWidget> = MutableTestingGenerationTestBasicWidget()
      |}
      """.trimMargin(),
    )

    val mutableWidgetSpec = generateMutableWidget(schema, schema.widgets.single())
    assertThat(mutableWidgetSpec.toString()).contains(
      """
      |@RedwoodCodegenApi
      |internal class MutableTestingGenerationTestBasicWidget :
      |    TestingGenerationTestBasicWidget<MutableWidget>, MutableWidget {
      |  public override val `value`: MutableWidget
      |    get() = this
      |
      |  public override var layoutModifiers: LayoutModifier = LayoutModifier
      |
      |  private var trait: String? = null
      |
      |  private var onEvent: (() -> Unit)? = null
      |
      |  public override val block: MutableListChildren<MutableWidget> =
      |      MutableListChildren<MutableWidget>()
      |
      |  public override fun trait(trait: String): Unit {
      |    this.trait = trait
      |  }
      |
      |  public override fun onEvent(onEvent: () -> Unit): Unit {
      |    this.onEvent = onEvent
      |  }
      |
      |  public override fun snapshot(): TestingGenerationTestBasicWidgetValue =
      |      TestingGenerationTestBasicWidgetValue(
      |    layoutModifiers = layoutModifiers,
      |    trait = trait!!,
      |    onEvent = onEvent!!,
      |    block = block.map { it.`value`.snapshot() },
      |  )
      |}
      """.trimMargin(),
    )

    val widgetValueSpec = generateWidgetValue(schema, schema.widgets.single())
    assertThat(widgetValueSpec.toString()).contains(
      """
      |public class TestingGenerationTestBasicWidgetValue(
      |  public override val layoutModifiers: LayoutModifier = LayoutModifier,
      |  public val trait: String = "test",
      |  public val onEvent: () -> Unit = { error("test") },
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
      |
      |  public override fun addTo(
      |    parentId: Id,
      |    childrenTag: ChildrenTag,
      |    builder: ViewTree.Builder,
      |  ): Unit {
      |    val widgetId = Id(builder.nextId++)
      |    val widgetTag = WidgetTag(1)
      |    val childrenDiff = ChildrenDiff.Insert(
      |          parentId,
      |          childrenTag,
      |          widgetId,
      |          widgetTag,
      |          builder.childrenDiffs.size
      |        )
      |
      |    builder.childrenDiffs.add(childrenDiff)
      |    builder.propertyDiffs.add(PropertyDiff(widgetId, PropertyTag(1),
      |        builder.json.encodeToJsonElement(this.trait)))
      |    for (childrenList in childrenLists) {
      |      val nextChildrenTag = childrenTag.value + 1
      |      for (child in childrenList) {
      |        child.addTo(widgetId, ChildrenTag(nextChildrenTag), builder)
      |      }
      |    }
      |  }
      |}
      """.trimMargin(),
    )
  }
}
