package app.cash.treehouse.schema.generator

import app.cash.treehouse.schema.Property
import app.cash.treehouse.schema.Schema
import app.cash.treehouse.schema.Widget
import app.cash.treehouse.schema.parser.parseSchema
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class GenerateWidgetFactoryTest {
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
    assertThat(fileSpec.toString()).contains(
      """
      |    1 -> GenerateWidgetFactoryTestNavigationBarButton()
      |    3 -> GenerateWidgetFactoryTestButton()
      |""".trimMargin()
    )
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

    val fileSpec = generateWidgetFactory(schema)
    assertThat(fileSpec.toString()).contains(
      """
      |    1 -> GenerateWidgetFactoryTestNode1()
      |    2 -> GenerateWidgetFactoryTestNode2()
      |    3 -> GenerateWidgetFactoryTestNode3()
      |    12 -> GenerateWidgetFactoryTestNode12()
      |""".trimMargin()
    )
  }
}
