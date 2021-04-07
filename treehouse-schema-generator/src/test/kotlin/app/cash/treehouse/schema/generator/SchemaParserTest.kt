package app.cash.treehouse.schema.generator

import app.cash.treehouse.schema.Children
import app.cash.treehouse.schema.Node
import app.cash.treehouse.schema.Property
import app.cash.treehouse.schema.Schema
import org.junit.Test

class SchemaParserTest {
  interface NonAnnotationSchema

  @Test fun nonAnnotatedSchemaThrows() {
    assertThrows<IllegalArgumentException> {
      parseSchema(NonAnnotationSchema::class.java)
    }.hasMessageThat().isEqualTo(
      "Schema app.cash.treehouse.schema.generator.SchemaParserTest\$NonAnnotationSchema missing @Schema annotation")
  }

  @Schema([
    NonAnnotatedNode::class,
  ])
  interface NonAnnotatedNodeSchema
  data class NonAnnotatedNode(
    @Property(1) val name: String,
  )

  @Test fun nonAnnotatedNodeThrows() {
    assertThrows<IllegalArgumentException> {
      parseSchema(NonAnnotatedNodeSchema::class.java)
    }.hasMessageThat().isEqualTo(
      "app.cash.treehouse.schema.generator.SchemaParserTest\$NonAnnotatedNode missing @Node annotation")
  }

  @Schema([
    DuplicateNodeTagA::class,
    NonDuplicateNodeTag::class,
    DuplicateNodeTagB::class,
  ])
  interface DuplicateNodeTagSchema
  @Node(1)
  data class DuplicateNodeTagA(
    @Property(1) val name: String,
  )
  @Node(2)
  data class NonDuplicateNodeTag(
    @Property(1) val name: String,
  )
  @Node(1)
  data class DuplicateNodeTagB(
    @Property(1) val name: String,
  )

  @Test fun duplicateNodeTagThrows() {
    assertThrows<IllegalArgumentException> {
      parseSchema(DuplicateNodeTagSchema::class.java)
    }.hasMessageThat().isEqualTo(
      """
      |Schema @Node tags must be unique
      |
      |- @Node(1): app.cash.treehouse.schema.generator.SchemaParserTest${'$'}DuplicateNodeTagA, app.cash.treehouse.schema.generator.SchemaParserTest${'$'}DuplicateNodeTagB
      """.trimMargin())
  }

  @Schema([
    RepeatedNode::class,
    RepeatedNode::class,
  ])
  interface RepeatedNodeTypeSchema
  @Node(1)
  data class RepeatedNode(
    @Property(1) val name: String,
  )

  @Test fun repeatedNodeTypeThrows() {
    assertThrows<IllegalArgumentException> {
      parseSchema(RepeatedNodeTypeSchema::class.java)
    }.hasMessageThat().isEqualTo(
      """
      |Schema contains repeated node
      |
      |- app.cash.treehouse.schema.generator.SchemaParserTest${'$'}RepeatedNode
      """.trimMargin())
  }

  @Schema([
    DuplicatePropertyTagNode::class,
  ])
  interface DuplicatePropertyTagSchema

  @Node(1)
  data class DuplicatePropertyTagNode(
    @Property(1) val name: String,
    @Property(2) val age: Int,
    @Property(1) val nickname: String,
  )

  @Test fun duplicatePropertyTagThrows() {
    assertThrows<IllegalArgumentException> {
      parseSchema(DuplicatePropertyTagSchema::class.java)
    }.hasMessageThat().isEqualTo(
      """
      |Node app.cash.treehouse.schema.generator.SchemaParserTest${'$'}DuplicatePropertyTagNode's @Property tags must be unique
      |
      |- @Property(1): name, nickname
      """.trimMargin())
  }

  @Schema([
    DuplicateChildrenTagNode::class,
  ])
  interface DuplicateChildrenTagSchema
  @Node(1)
  data class DuplicateChildrenTagNode(
    @Children(1) val childrenA: List<Any>,
    @Property(1) val name: String,
    @Children(1) val childrenB: List<Any>,
  )

  @Test fun duplicateChildrenTagThrows() {
    assertThrows<IllegalArgumentException> {
      parseSchema(DuplicateChildrenTagSchema::class.java)
    }.hasMessageThat().isEqualTo(
      """
      |Node app.cash.treehouse.schema.generator.SchemaParserTest${'$'}DuplicateChildrenTagNode's @Children tags must be unique
      |
      |- @Children(1): childrenA, childrenB
      """.trimMargin())
  }

  @Schema([
    UnannotatedPrimaryParameterNode::class,
  ])
  interface UnannotatedPrimaryParameterSchema
  @Node(1)
  data class UnannotatedPrimaryParameterNode(
    @Property(1) val name: String,
    @Children(1) val children: List<Any>,
    val unannotated: String,
  )

  @Test fun unannotatedPrimaryParameterThrows() {
    assertThrows<IllegalArgumentException> {
      parseSchema(UnannotatedPrimaryParameterSchema::class.java)
    }.hasMessageThat().isEqualTo(
      "Unannotated parameter \"unannotated\" on app.cash.treehouse.schema.generator.SchemaParserTest\$UnannotatedPrimaryParameterNode")
  }

  @Schema([
    NonDataClassNode::class,
  ])
  interface NonDataClassSchema
  @Node(1)
  class NonDataClassNode(
    @Property(1) val name: String,
  )

  @Test fun nonDataClassThrows() {
    assertThrows<IllegalArgumentException> {
      parseSchema(NonDataClassSchema::class.java)
    }.hasMessageThat().isEqualTo(
      "@Node app.cash.treehouse.schema.generator.SchemaParserTest\$NonDataClassNode must be 'data' class")
  }

  @Schema([
    InvalidChildrenTypeNode::class,
  ])
  interface InvalidChildrenTypeSchema
  @Node(1)
  data class InvalidChildrenTypeNode(
    @Children(1) val children: List<String>,
  )

  @Test fun invalidChildrenTypeThrows() {
    assertThrows<IllegalArgumentException> {
      parseSchema(InvalidChildrenTypeSchema::class.java)
    }.hasMessageThat().isEqualTo(
      "@Children app.cash.treehouse.schema.generator.SchemaParserTest\$InvalidChildrenTypeNode#children must be of type 'List<Any>'")
  }
}
