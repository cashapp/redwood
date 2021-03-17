package app.cash.treehouse.schema.generator

import com.google.common.truth.Truth.assertThat
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.asTypeName
import org.junit.Test

class GenerateComposeNodeTest {
  @Test
  internal fun `happy path`() {
    val node = Node(
      tag = 1,
      className = ClassName("com.example", "Button"),
      traits = listOf(
        Property("label", tag = 2, type = String::class.asTypeName(), defaultExpression = null),
        Property("color", tag = 3, type = String::class.asTypeName(), defaultExpression = null)
      )
    )

    val schema = Schema("TestSchema", "com.example", nodes = listOf(node))

    val fileSpec = generateComposeNode(schema, node)
    assertThat(fileSpec.toString()).isEqualTo("""
        |package com.example.compose
        |
        |import androidx.compose.runtime.Applier
        |import androidx.compose.runtime.Composable
        |import androidx.compose.runtime.ComposeNode
        |import app.cash.treehouse.compose.Node
        |import app.cash.treehouse.compose.TreehouseScope
        |import app.cash.treehouse.protocol.PropertyDiff
        |import kotlin.String
        |import kotlin.Unit
        |
        |@Composable
        |public fun TreehouseScope.Button(label: String, color: String): Unit {
        |  ComposeNode<Node, Applier<Node>>(
        |      factory = {
        |        Node(nextId(), 1)
        |      },
        |      update = {
        |        set(label) {
        |          appendDiff(PropertyDiff(this.id, 2, label))
        |        }
        |        set(color) {
        |          appendDiff(PropertyDiff(this.id, 3, color))
        |        }
        |      },
        |      )
        |}
        |""".trimMargin())
  }

  @Test
  internal fun `id property does not collide`() {
    val node = Node(
      tag = 1,
      className = ClassName("com.example", "Button"),
      traits = listOf(
        Property("label", tag = 2, type = String::class.asTypeName(), defaultExpression = null),
        Property("id", tag = 3, type = String::class.asTypeName(), defaultExpression = null)
      )
    )

    val schema = Schema("TestSchema", "com.example", nodes = listOf(node))

    val fileSpec = generateComposeNode(schema, node)
    assertThat(fileSpec.toString()).contains("""
        |        set(id) {
        |          appendDiff(PropertyDiff(this.id, 3, id))
        |        }
        |""".trimMargin())
  }
}
