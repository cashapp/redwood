package app.cash.treehouse.schema.parser

import kotlin.reflect.KClass
import kotlin.reflect.KType

data class Schema(
  val name: String,
  val `package`: String,
  val nodes: List<Node>,
)

data class Node(
  val tag: Int,
  val className: KClass<*>,
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
  val type: KType,
  override val defaultExpression: String?,
) : Trait()

data class Event(
  override val name: String,
  override val tag: Int,
  // TODO parameter type list?
  override val defaultExpression: String?,
) : Trait()

data class Children(
  override val name: String,
  override val tag: Int,
) : Trait() {
  override val defaultExpression: String? get() = null
}
