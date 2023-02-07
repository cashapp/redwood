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
package app.cash.redwood.tooling.schema

import app.cash.redwood.tooling.schema.Widget.Children
import app.cash.redwood.tooling.schema.Widget.Event
import app.cash.redwood.tooling.schema.Widget.Property
import app.cash.redwood.tooling.schema.Widget.Trait

public interface Schema {
  public val type: FqType
  public val scopes: List<FqType>
  public val widgets: List<Widget>
  public val layoutModifiers: List<LayoutModifier>
  public val dependencies: List<Schema>

  /** This schema and all its [dependencies]. */
  public val allSchemas: List<Schema> get() = buildList {
    add(this@Schema)
    addAll(dependencies)
  }
}

public interface Widget {
  /** Either a 'data class' or 'object'. */
  public val type: FqType

  /** Non-empty list for a 'data class' [type] or empty list for 'object' [type]. */
  public val traits: List<Trait>

  public sealed interface Trait {
    public val name: String
    public val defaultExpression: String?
  }

  public interface Property : Trait {
    public val type: FqType
  }

  public interface Event : Trait {
    public val parameterType: FqType?
  }

  public interface Children : Trait {
    public val scope: FqType?
  }
}

public interface LayoutModifier {
  public val scopes: List<FqType>

  /** Either a 'data class' or 'object'. */
  public val type: FqType

  /** Non-empty list for a 'data class' [type] or empty list for 'object' [type]. */
  public val properties: List<Property>

  public data class Property(
    val name: String,
    val type: FqType,
    val isSerializable: Boolean,
    val defaultExpression: String?,
  )
}

public interface ProtocolSchema : Schema {
  override val widgets: List<ProtocolWidget>
  override val layoutModifiers: List<ProtocolLayoutModifier>
  override val dependencies: List<ProtocolSchema>

  override val allSchemas: List<ProtocolSchema> get() = buildList {
    add(this@ProtocolSchema)
    addAll(dependencies)
  }
}

public interface ProtocolWidget : Widget {
  public val tag: Int
  override val traits: List<ProtocolTrait>

  public sealed interface ProtocolTrait : Trait {
    public val tag: Int
  }
  public interface ProtocolProperty : Property, ProtocolTrait
  public interface ProtocolEvent : Event, ProtocolTrait
  public interface ProtocolChildren : Children, ProtocolTrait
}

public interface ProtocolLayoutModifier : LayoutModifier {
  public val tag: Int
}
