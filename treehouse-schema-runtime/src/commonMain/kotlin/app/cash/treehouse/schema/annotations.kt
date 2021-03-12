package app.cash.treehouse.schema

import kotlin.reflect.KClass

/**
 * Annotates an otherwise unused type with a set of the [Node]-annotated classes which are all part
 * of this schema.
 *
 * ```
 * @Schema(entities = [
 *   Box::class,
 *   Button::class,
 *   Text::class,
 * ])
 * interface Name
 * ```
 */
annotation class Schema(val entities: Array<KClass<*>>)

/**
 * Annotates a data class which represents a node in a UI tree. Each node in a [Schema] must have
 * a unique [value].
 *
 * All of the properties in the class must be annotated with either [Property] or [Children].
 *
 * ```
 * @Node(1)
 * data class Box(
 *   @Property(1) val orientation: Orientation,
 *   @Children val children: List<Any>,
 * )
 * ```
 */
annotation class Node(val value: Int)

/**
 * Annotates a [Node] property which represents a property on the associated UI node. Each property
 * in a [Node] class must have a unique [value].
 *
 * ```
 * @Node(1)
 * data class Text(
 *   @Property(1) val text: String,
 * )
 * ```
 */
annotation class Property(val value: Int)

/**
 * Annotates a [Node] property as representing child nodes which are contained within the enclosing
 * node. The type of the property must be `List<Any>`.
 *
 * ```
 * @Node(1)
 * data class Box(
 *   @Children val children: List<Any>,
 * )
 * ```
 */
annotation class Children

/**
 * Annotates a [Property] with an associated default expression. The [expression] is not
 * type-checked and will be used verbatim in the generated code.
 *
 * ```
 * @Node(1)
 * data class Button(
 *   @Property(1) val text: String,
 *   @Property(2) @Default("true") val enabled: Boolean,
 * )
 * ```
 */
annotation class Default(val expression: String)
