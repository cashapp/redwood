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
package app.cash.treehouse.schema.parser

import kotlin.reflect.KClass
import kotlin.reflect.KType

public data class Schema(
  val name: String,
  val `package`: String,
  val widgets: List<Widget>,
)

public data class Widget(
  val tag: Int,
  /** Either a 'data class' or 'object'. */
  val type: KClass<*>,
  /** Non-empty list for a 'data class' [type] or empty list for 'object' [type]. */
  val traits: List<Trait>,
)

public sealed class Trait {
  public abstract val tag: Int
  public abstract val name: String
  public abstract val defaultExpression: String?
}

public data class Property(
  override val tag: Int,
  override val name: String,
  val type: KType,
  override val defaultExpression: String?,
) : Trait()

public data class Event(
  override val tag: Int,
  override val name: String,
  // TODO parameter type list?
  override val defaultExpression: String?,
) : Trait()

public data class Children(
  override val tag: Int,
  override val name: String,
) : Trait() {
  override val defaultExpression: String? get() = null
}
