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
package app.cash.redwood.schema.parser

import kotlin.reflect.KClass
import kotlin.reflect.KType

public data class Schema(
  val name: String,
  val `package`: String,
  val scopes: List<KClass<*>>,
  val widgets: List<Widget>,
  val layoutModifiers: List<LayoutModifier>,
  val dependencies: List<Schema>,
)

public data class Widget(
  val tag: UInt,
  /** Either a 'data class' or 'object'. */
  val type: KClass<*>,
  /** Non-empty list for a 'data class' [type] or empty list for 'object' [type]. */
  val traits: List<Trait>,
) {
  public sealed interface Trait {
    public val tag: UInt
    public val name: String
    public val defaultExpression: String?
  }

  public data class Property(
    override val tag: UInt,
    override val name: String,
    val type: KType,
    override val defaultExpression: String?,
  ) : Trait

  public data class Event(
    override val tag: UInt,
    override val name: String,
    override val defaultExpression: String?,
    val parameterType: KType?,
  ) : Trait

  public data class Children(
    override val tag: UInt,
    override val name: String,
    override val defaultExpression: String?,
    val scope: KClass<*>? = null,
  ) : Trait
}

public data class LayoutModifier(
  val tag: UInt,
  val scopes: List<KClass<*>>,
  /** Either a 'data class' or 'object'. */
  val type: KClass<*>,
  /** Non-empty list for a 'data class' [type] or empty list for 'object' [type]. */
  val properties: List<Property>,
) {
  public data class Property(
    val name: String,
    val type: KType,
    val defaultExpression: String?,
  )
}
