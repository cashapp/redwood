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
}
