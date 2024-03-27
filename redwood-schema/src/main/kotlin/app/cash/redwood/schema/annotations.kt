/*
 * Copyright (C) 2021 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package app.cash.redwood.schema

import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.PROPERTY
import kotlin.reflect.KClass

/**
 * Annotates an otherwise unused type with a set of [Widget]-annotated or [Modifier]-annotated
 * classes which are all part of this schema.
 *
 * ```kotlin
 * @Schema([
 *   Row::class,
 *   RowAlignment::class,
 *   Button::class,
 *   Text::class,
 * ])
 * interface Name
 * ```
 *
 * @see Widget
 * @see Modifier
 */
@Retention(RUNTIME)
@Target(CLASS)
public annotation class Schema(
  val members: Array<KClass<*>>,
  val dependencies: Array<Dependency> = [],
  /**
   * Widget tags which are reserved. These cannot be used by a widget in [members].
   * This is useful for ensuring tags from old, retired widgets are not accidentally reused.
   *
   * ```kotlin
   * @Schema(
   *   members = [
   *     Row::class,
   *     Button::class,
   *     Text::class,
   *   ],
   *   reservedWidgets = [
   *     4, // Retired Column widget.
   *   ],
   * )
   * interface MySchema
   * ```
   */
  val reservedWidgets: IntArray = [],
  /**
   * Modifier tags which are reserved. These cannot be used by a modifier in [members].
   * This is useful for ensuring tags from old, retired modifiers are not accidentally reused.
   *
   * ```kotlin
   * @Schema(
   *   members = [
   *     Row::class,
   *     Button::class,
   *     Text::class,
   *   ],
   *   reservedModifiers = [
   *     3, // Retired RowAlignment modifier.
   *   ],
   * )
   * interface MySchema
   * ```
   */
  val reservedModifiers: IntArray = [],
) {
  @Retention(RUNTIME)
  @Target // None, use only within @Schema.
  public annotation class Dependency(
    val tag: Int,
    val schema: KClass<*>,
  )
}

/**
 * Annotates a data class which represents a widget in a UI tree. Each widget in a [Schema] must
 * have a unique [tag] among all [@Widget][Widget] annotations in the [Schema].
 *
 * All the properties in the class must be annotated with either [Property] or [Children].
 *
 * ```
 * @Widget(1)
 * data class Box(
 *   @Property(1) val orientation: Orientation,
 *   @Children val children: () -> Unit,
 * )
 * ```
 */
@Retention(RUNTIME)
@Target(CLASS)
public annotation class Widget(
  val tag: Int,
  /**
   * Property tags which are reserved. These cannot be used by a [@Property][Property] annotation.
   * This is useful for ensuring tags from old, retired properties are not accidentally reused.
   *
   * ```kotlin
   * @Widget(
   *   tag = 12,
   *   reservedProperties = [
   *     3, // Retired double-click event.
   *   ],
   * )
   * data class MyButton(…)
   * ```
   */
  val reservedProperties: IntArray = [],
  /**
   * Children tags which are reserved. These cannot be used by a [@Children][Children] annotation.
   * This is useful for ensuring tags from old, retired children are not accidentally reused.
   *
   * ```kotlin
   * @Widget(
   *   tag = 12,
   *   reservedChildren = [
   *     2, // Retired action item slot.
   *   ],
   * )
   * data class Toolbar(…)
   * ```
   */
  val reservedChildren: IntArray = [],
)

/**
 * Annotates a [Widget] property which represents a property on the associated UI widget. Properties
 * in a [Widget] class must have a unique [tag] among all [@Property][Property] annotations in
 * the class.
 *
 * ```
 * @Widget(1)
 * data class Text(
 *   @Property(1) val text: String,
 * )
 * ```
 */
@Retention(RUNTIME)
@Target(PROPERTY)
public annotation class Property(val tag: Int)

/**
 * Annotates a [Widget] property as representing child widgets which are contained within the
 * enclosing widget. Children in a [Widget] class must have a unique [tag] among all
 * [@Children][Children] annotations in the class. The type of the property must be `() -> Unit`.
 *
 * ```
 * @Widget(1)
 * data class Box(
 *   @Children(1) val children: () -> Unit,
 * )
 * ```
 *
 * A class may be used as the lambda receiver to denote a scope. The receiver class will be
 * propagated to the generated code and must be included in the [schema][Schema].
 *
 * ```
 * @Widget(1)
 * data class Box(
 *   @Children(1) val children: BoxScope.() -> Unit,
 * )
 * ```
 */
@Retention(RUNTIME)
@Target(PROPERTY)
public annotation class Children(
  val tag: Int,
)

/**
 * Annotates a [Property] with an associated default expression. The [expression] is not
 * type-checked and will be used verbatim in the generated code.
 *
 * ```
 * @Widget(1)
 * data class Button(
 *   @Property(1) val text: String,
 *   @Property(2) @Default("true") val enabled: Boolean,
 * )
 * ```
 */
@Retention(RUNTIME)
@Target(PROPERTY)
public annotation class Default(val expression: String)

/**
 * Annotates a data class which represents a modifier for a [Widget].
 *
 * Each modifier in a [Schema] must have a unique [tag] among all [Modifier] annotations in the
 * [Schema].
 *
 * ```
 * @Modifier(1)
 * data class BackgroundColor(
 *   val value: Color,
 * )
 * ```
 *
 * To create a modifier that applies a specific parent widget, supply one or more scope types.
 *
 * ```
 * @Modifier(1, RowScope::class)
 * data class RowAlignment(
 *   val value: VerticalAlignment,
 * )
 * ```
 *
 * When defining the [Widget], use the same scope type as a receiver on [Children] property.
 *
 * ```
 * @Widget(1)
 * data class Row(
 *   @Children(1) val children: RowScope.() -> Unit,
 * )
 * ```
 *
 * When a modifier is applied to a widget it will be processed in one of two ways:
 * 1. If scoped, the parent widget which provides the scope will query the value. For example,
 *    each implementation of a `Row` will handle `HorizontalAlignment` set on its children.
 * 2. If unscoped, the widget factory interface will expose callback functions that are invoked
 *    when a value is used. For example, a `BackgroundColor(value: V, modifier: BackgroundColor)`
 *    function will be generated on the widget factory for each implementation to provide. The
 *    implementation should be idempotent, as it will be invoked each time a new modifier chain is
 *    set, even if the specific unscoped modifier has not changed.
 */
@Retention(RUNTIME)
@Target(CLASS)
public annotation class Modifier(
  val tag: Int,
  vararg val scopes: KClass<*>,
)
