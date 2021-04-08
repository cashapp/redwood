package app.cash.treehouse.schema.generator

import app.cash.treehouse.schema.Node
import app.cash.treehouse.schema.Property
import app.cash.treehouse.schema.Schema
import app.cash.treehouse.schema.parser.parseSchema
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class GenerateComposeNodeTest {
  @Schema([
    IdPropertyNameCollisionNode::class,
  ])
  interface IdPropertyNameCollisionSchema
  @Node(1)
  data class IdPropertyNameCollisionNode(
    @Property(1) val label: String,
    @Property(2) val id: String,
  )

  @Test fun `id property does not collide`() {
    val schema = parseSchema(IdPropertyNameCollisionSchema::class)

    val fileSpec = generateComposeNode(schema, schema.nodes.single())
    assertThat(fileSpec.toString()).contains("""
        |        set(id) {
        |          appendDiff(PropertyDiff(this.id, 2, id))
        |        }
        |""".trimMargin())
  }
}
