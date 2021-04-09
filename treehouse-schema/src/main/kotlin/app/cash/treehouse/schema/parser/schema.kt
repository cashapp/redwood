package app.cash.treehouse.schema.parser

import kotlin.reflect.KClass
import kotlin.reflect.KType

data class Schema(
  val name: String,
  val `package`: String,
  val widgets: List<Widget>,
)

data class Widget(
  val tag: Int,
  val className: KClass<*>,
  val traits: List<Trait>,
)

sealed class Trait {
  abstract val tag: Int
  abstract val name: String
  abstract val defaultExpression: String?
}

data class Property(
  override val tag: Int,
  override val name: String,
  val type: KType,
  override val defaultExpression: String?,
) : Trait()

data class Event(
  override val tag: Int,
  override val name: String,
  // TODO parameter type list?
  override val defaultExpression: String?,
) : Trait()

data class Children(
  override val tag: Int,
  override val name: String,
) : Trait() {
  override val defaultExpression: String? get() = null
}
