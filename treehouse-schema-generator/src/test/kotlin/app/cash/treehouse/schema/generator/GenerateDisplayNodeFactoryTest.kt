package app.cash.treehouse.schema.generator

import app.cash.treehouse.schema.Node
import app.cash.treehouse.schema.Property
import app.cash.treehouse.schema.Schema
import app.cash.treehouse.schema.parser.parseSchema
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class GenerateDisplayNodeFactoryTest {
  @Schema([
    NavigationBar.Button::class,
    Button::class,
  ])
  interface SimpleNameCollisionSchema
  interface NavigationBar {
    @Node(1)
    data class Button(@Property(1) val text: String)
  }
  @Node(3)
  data class Button(@Property(1) val text: String)

  @Test fun `simple names do not collide`() {
    val schema = parseSchema(SimpleNameCollisionSchema::class)

    val fileSpec = generateDisplayNodeFactory(schema)
    assertThat(fileSpec.toString()).contains("""
        |    1 -> GenerateDisplayNodeFactoryTestNavigationBarButton(parent)
        |    3 -> GenerateDisplayNodeFactoryTestButton(parent)
        |""".trimMargin())
  }

  @Schema([
    Node12::class,
    Node1::class,
    Node3::class,
    Node2::class,
  ])
  interface SortedByTagSchema
  @Node(1)
  data class Node1(@Property(1) val text: String)
  @Node(2)
  data class Node2(@Property(1) val text: String)
  @Node(3)
  data class Node3(@Property(1) val text: String)
  @Node(12)
  data class Node12(@Property(1) val text: String)

  @Test fun `names are sorted by their node tags`() {
    val schema = parseSchema(SortedByTagSchema::class)

    val fileSpec = generateDisplayNodeFactory(schema)
    assertThat(fileSpec.toString()).contains("""
        |    1 -> GenerateDisplayNodeFactoryTestNode1(parent)
        |    2 -> GenerateDisplayNodeFactoryTestNode2(parent)
        |    3 -> GenerateDisplayNodeFactoryTestNode3(parent)
        |    12 -> GenerateDisplayNodeFactoryTestNode12(parent)
        |""".trimMargin())
  }
}
