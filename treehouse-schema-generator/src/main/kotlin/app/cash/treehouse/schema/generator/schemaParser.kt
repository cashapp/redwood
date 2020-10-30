package app.cash.treehouse.schema.generator

import app.cash.treehouse.schema.Default
import app.cash.treehouse.schema.Tag
import com.squareup.kotlinpoet.asTypeName
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.starProjectedType
import app.cash.treehouse.schema.Container as ContainerAnnotation
import app.cash.treehouse.schema.Node as NodeAnnotation
import app.cash.treehouse.schema.Schema as SchemaAnnotation

fun parseSchema(schemaFqcn: String): Schema {
  val schemaType = Class.forName(schemaFqcn)

  val containers = mutableListOf<Container>()
  val nodes = mutableListOf<Node>()
  val entities = schemaType.getAnnotation(SchemaAnnotation::class.java).entities
  for (entity in entities) {
    val containerAnnotation = entity.findAnnotation<ContainerAnnotation>()
    val nodeAnnotation = entity.findAnnotation<NodeAnnotation>()

    val entityClassName = entity.simpleName!!
    if (containerAnnotation != null) {
      // TODO must be interface
      // TODO no inheritance
      // TODO no properties
      // TODO no functions
      containers += Container(entityClassName)
    } else if (nodeAnnotation != null) {
      // TODO must be data class
      // TODO no inheritance
      // TODO no computed properties
      // TODO no functions
      // TODO ensure tag values are unique inside a node
      val traits = entity.primaryConstructor!!.parameters.map {
        val tag = it.findAnnotation<Tag>() ?: throw IllegalStateException() // TODO message
        val defaultExpression = it.findAnnotation<Default>()?.expression
        if (it.type.isSubtypeOf(Function::class.starProjectedType)) {
          Event(it.name!!, tag.value, defaultExpression)
        } else {
          Property(it.name!!, tag.value, it.type.asTypeName(), defaultExpression)
        }
      }
      nodes += Node(nodeAnnotation.value, entityClassName, traits)
    } else {
      throw IllegalStateException("Schema entity $entity is not annotated with @Container or @Node")
    }
  }

  // TODO ensure node values are unique inside a node

  return Schema(schemaType.simpleName, schemaType.packageName, containers, nodes)
}
