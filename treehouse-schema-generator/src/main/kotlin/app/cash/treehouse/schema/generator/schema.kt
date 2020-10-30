package app.cash.treehouse.schema.generator

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName

data class Schema(
  val name: String,
  private val `package`: String,
  val containers: List<Container>,
  val nodes: List<Node>,
) {
  val clientPackage = "$`package`.client"
  val serverPackage = "$`package`.server"

  internal val bridgeType = ClassName(clientPackage, "${name}Bridge")
  internal val baseNodeType = ClassName(clientPackage, "${name}Node")
  internal val nodeFactoryType = ClassName(clientPackage, "${name}NodeFactory")

  fun clientContainerType(container: Container): ClassName {
    return ClassName(clientPackage, container.name)
  }

  fun serverContainerType(container: Container): ClassName {
    return ClassName(serverPackage, container.name)
  }

  fun clientNodeType(node: Node): ClassName {
    return ClassName(clientPackage, node.name)
  }

  fun serverNodeType(node: Node): ClassName {
    return ClassName(serverPackage, node.name + "Node")
  }
}

data class Container(
  val name: String
)

data class Node(
  val tag: Int,
  val name: String,
  val traits: List<Trait>,
)

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
