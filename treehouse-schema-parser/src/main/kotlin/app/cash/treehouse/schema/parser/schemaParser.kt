package app.cash.treehouse.schema.parser

import kotlin.reflect.KTypeProjection.Companion.invariant
import kotlin.reflect.full.createType
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.jvm.jvmName
import app.cash.treehouse.schema.Children as ChildrenAnnotation
import app.cash.treehouse.schema.Default as DefaultAnnotation
import app.cash.treehouse.schema.Node as NodeAnnotation
import app.cash.treehouse.schema.Property as PropertyAnnotation
import app.cash.treehouse.schema.Schema as SchemaAnnotation

private val LIST_OF_ANY_TYPE = List::class.createType(
  arguments = listOf(invariant(Any::class.createType()))
)

fun parseSchema(schemaType: Class<*>): Schema {
  val nodes = mutableListOf<Node>()

  val nodeTypes = requireNotNull(schemaType.getAnnotation(SchemaAnnotation::class.java)) {
    "Schema ${schemaType.name} missing @Schema annotation"
  }.nodes

  val duplicatedNodes = nodeTypes.groupBy { it }.filterValues { it.size > 1 }.keys
  if (duplicatedNodes.isNotEmpty()) {
    throw IllegalArgumentException(buildString {
      append("Schema contains repeated node")
      if (duplicatedNodes.size > 1) {
        append('s')
      }
      duplicatedNodes.joinTo(this, prefix = "\n\n- ", separator = "\n- ") { it.java.name.toString() }
    })
  }

  for (nodeType in nodeTypes) {
    val nodeAnnotation = requireNotNull(nodeType.findAnnotation<NodeAnnotation>()) {
      "${nodeType.java.name} missing @Node annotation"
    }
    require(nodeType.isData) {
      "@Node ${nodeType.java.name} must be 'data' class"
    }

    val traits = nodeType.primaryConstructor!!.parameters.map {
      val property = it.findAnnotation<PropertyAnnotation>()
      val children = it.findAnnotation<ChildrenAnnotation>()
      val defaultExpression = it.findAnnotation<DefaultAnnotation>()?.expression

      if (property != null) {
        if (it.type.isSubtypeOf(Function::class.starProjectedType)) {
          Event(it.name!!, property.value, defaultExpression)
        } else {
          Property(it.name!!, property.value, it.type, defaultExpression)
        }
      } else if (children != null) {
        require(it.type == LIST_OF_ANY_TYPE) {
          "@Children ${nodeType.java.name}#${it.name} must be of type 'List<Any>'"
        }
        Children(it.name!!, children.value)
      } else {
        throw IllegalArgumentException("Unannotated parameter \"${it.name}\" on ${nodeType.java.name}")
      }
    }

    val badChildren =
      traits.filterIsInstance<Children>().groupBy(Children::tag).filterValues { it.size > 1 }
    if (badChildren.isNotEmpty()) {
      throw IllegalArgumentException(buildString {
        appendLine("Node ${nodeType.java.name}'s @Children tags must be unique")
        for ((tag, children) in badChildren) {
          append("\n- @Children($tag): ")
          children.joinTo(this) { it.name }
        }
      })
    }

    val badProperties =
      traits.filterIsInstance<Property>().groupBy(Property::tag).filterValues { it.size > 1 }
    if (badProperties.isNotEmpty()) {
      throw IllegalArgumentException(buildString {
        appendLine("Node ${nodeType.java.name}'s @Property tags must be unique")
        for ((tag, property) in badProperties) {
          append("\n- @Property($tag): ")
          property.joinTo(this) { it.name }
        }
      })
    }

    nodes += Node(nodeAnnotation.value, nodeType, traits)
  }

  val badNodes = nodes.groupBy(Node::tag).filterValues { it.size > 1 }
  if (badNodes.isNotEmpty()) {
    throw IllegalArgumentException(buildString {
      appendLine("Schema @Node tags must be unique")
      for ((tag, node) in badNodes) {
        append("\n- @Node($tag): ")
        node.joinTo(this) { it.className.jvmName }
      }
    })
  }

  return Schema(schemaType.simpleName, packageName(schemaType), nodes)
}
