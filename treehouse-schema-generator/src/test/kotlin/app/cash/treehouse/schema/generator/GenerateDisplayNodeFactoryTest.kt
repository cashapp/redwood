package app.cash.treehouse.schema.generator

import com.google.common.truth.Truth.assertThat
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.asTypeName
import org.junit.Test

class GenerateDisplayNodeFactoryTest {
  @Test
  internal fun `simple names do not collide`() {
    val navigationBarButton = Node(
      tag = 1,
      className = ClassName("com.example.sunspot", "NavigationBar", "Button"),
      traits = listOf(
        Property("label", tag = 2, type = String::class.asTypeName(), defaultExpression = null)
      )
    )
    val mooncakeButton = Node(
      tag = 3,
      className = ClassName("com.example.sunspot", "Button"),
      traits = listOf(
        Property("label", tag = 4, type = String::class.asTypeName(), defaultExpression = null)
      )
    )

    val schema = Schema(
      name = "TestSchema",
      `package` = "com.example",
      nodes = listOf(
        navigationBarButton, mooncakeButton
      )
    )

    val fileSpec = generateDisplayNodeFactory(schema)
    assertThat(fileSpec.toString()).contains("""
        |    1 -> NavigationBarButton(parent)
        |    3 -> Button(parent)
        |""".trimMargin())
  }

  @Test
  internal fun `names are sorted by their node tags`() {
    fun node(tag: Int) = Node(
      tag = tag,
      className = ClassName("com.example.sunspot", "Node$tag"),
      traits = emptyList()
    )

    val schema = Schema(
      name = "TestSchema",
      `package` = "com.example",
      nodes = listOf(
        node(3), node(1), node(2), node(12)
      )
    )

    val fileSpec = generateDisplayNodeFactory(schema)
    assertThat(fileSpec.toString()).contains("""
        |    1 -> Node1(parent)
        |    2 -> Node2(parent)
        |    3 -> Node3(parent)
        |    12 -> Node12(parent)
        |""".trimMargin())
  }
}
