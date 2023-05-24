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
 * ```
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
public annotation class Widget(val tag: Int)

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
 * Annotates a data class which represents a layout modifier for a [Widget]. Each layout modifier
 * in a [Schema] must have a unique [tag] among all [Modifier] annotations in the [Schema].
 * Additionally, each layout modifier can be associated with one or more scopes.
 *
 * ```
 * @Modifier(1, RowScope::class)
 * data class RowAlignment(
 *   val value: VerticalAlignment,
 * )
 * ```
 */
@Retention(RUNTIME)
@Target(CLASS)
public annotation class Modifier(
  val tag: Int,
  vararg val scopes: KClass<*>,
)
