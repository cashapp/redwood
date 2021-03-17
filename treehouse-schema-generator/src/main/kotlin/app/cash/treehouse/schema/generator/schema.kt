package app.cash.treehouse.schema.generator

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName

data class Schema(
  val name: String,
  private val `package`: String,
  val nodes: List<Node>,
) {
  val composePackage = "$`package`.compose"
  val displayPackage = "$`package`.display"

  internal val nodeFactoryType = ClassName(displayPackage, "${name}NodeFactory")

  fun displayNodeType(node: Node): ClassName {
    return ClassName(displayPackage, node.flatName)
  }

  fun composeNodeType(node: Node): ClassName {
    return ClassName(composePackage, node.flatName + "Node")
  }
}

data class Node(
  val tag: Int,
  private val className: ClassName,
  val traits: List<Trait>,
) {
  /**
   * Returns a single string that is likely to be unique within a schema, like `MapEntry` or
   * `NavigationBarButton`.
   */
  val flatName: String
    get() = className.simpleNames.joinToString(separator = "")
}

sealed class Trait {
  abstract val name: String
  abstract val tag: Int
  abstract val defaultExpression: String?
}

data class Property(
  override val name: String,
  override val tag: Int,
  val type: TypeName,
  override val defaultExpression: String?,
) : Trait()

data class Event(
  override val name: String,
  override val tag: Int,
  // TODO parameter type list?
  override val defaultExpression: String?,
) : Trait()

data class Children(
  override val name: String
) : Trait() {
  override val tag: Int get() = 0
  override val defaultExpression: String? get() = null
}
