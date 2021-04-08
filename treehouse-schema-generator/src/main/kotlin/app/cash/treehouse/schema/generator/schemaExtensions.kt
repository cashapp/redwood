package app.cash.treehouse.schema.generator

import app.cash.treehouse.schema.parser.Node
import app.cash.treehouse.schema.parser.Schema
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.asClassName

/**
 * Returns a single string that is likely to be unique within a schema, like `MapEntry` or
 * `NavigationBarButton`.
 */
internal val Node.flatName: String
  get() = className.asClassName().simpleNames.joinToString(separator = "")

internal fun Schema.composeNodeType(node: Node): ClassName {
  return ClassName(composePackage, node.flatName + "Node")
}

internal val Schema.composePackage get() = "$`package`.compose"

internal fun Schema.displayNodeType(node: Node): ClassName {
  return ClassName(displayPackage, node.flatName)
}

internal fun Schema.getNodeFactoryType(): ClassName {
  return ClassName(displayPackage, "${name}NodeFactory")
}

internal val Schema.displayPackage get() = "$`package`.display"
