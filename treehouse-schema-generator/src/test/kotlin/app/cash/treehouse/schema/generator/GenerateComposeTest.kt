package app.cash.treehouse.schema.generator

import app.cash.treehouse.schema.Property
import app.cash.treehouse.schema.Schema
import app.cash.treehouse.schema.Widget
import app.cash.treehouse.schema.parser.parseSchema
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class GenerateComposeTest {
  @Schema(
    [
      IdPropertyNameCollisionNode::class,
    ]
  )
  interface IdPropertyNameCollisionSchema
  @Widget(1)
  data class IdPropertyNameCollisionNode(
    @Property(1) val label: String,
    @Property(2) val id: String,
  )

  @Test fun `id property does not collide`() {
    val schema = parseSchema(IdPropertyNameCollisionSchema::class)

    val fileSpec = generateComposeNode(schema, schema.widgets.single())
    assertThat(fileSpec.toString()).contains(
      """
      |        set(id) {
      |          appendDiff(PropertyDiff(this.id, 2, id))
      |        }
      |""".trimMargin()
    )
  }
}
