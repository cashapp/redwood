package app.cash.treehouse.schema.generator

import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.starProjectedType
import app.cash.treehouse.schema.Children as ChildrenAnnotation
import app.cash.treehouse.schema.Default as DefaultAnnotation
import app.cash.treehouse.schema.Node as NodeAnnotation
import app.cash.treehouse.schema.Property as PropertyAnnotation
import app.cash.treehouse.schema.Schema as SchemaAnnotation

fun parseSchema(schemaFqcn: String): Schema {
  val schemaType = Class.forName(schemaFqcn)

  val nodes = mutableListOf<Node>()
  val entities = schemaType.getAnnotation(SchemaAnnotation::class.java).entities
  for (entity in entities) {
    val nodeAnnotation = checkNotNull(entity.findAnnotation<NodeAnnotation>()) {
      "Schema entity $entity is not annotated with @Container or @Node"
    }

    // TODO must be data class
    // TODO no inheritance
    // TODO no computed properties
    // TODO no functions
    // TODO ensure tag values are unique inside a node
    val traits = entity.primaryConstructor!!.parameters.map {
      val property = it.findAnnotation<PropertyAnnotation>()
      val children = it.findAnnotation<ChildrenAnnotation>()
      val defaultExpression = it.findAnnotation<DefaultAnnotation>()?.expression

      if (property != null) {
        if (it.type.isSubtypeOf(Function::class.starProjectedType)) {
          Event(it.name!!, property.value, defaultExpression)
        } else {
          Property(it.name!!, property.value, it.type.asTypeName(), defaultExpression)
        }
      } else if (children != null) {
        Children(it.name!!, children.value)
      } else {
        throw IllegalStateException() // TODO message
      }
    }

    nodes += Node(nodeAnnotation.value, entity.asClassName(), traits)
  }

  // TODO ensure node values are unique inside a node

  return Schema(schemaType.simpleName, packageName(schemaType), nodes)
}
