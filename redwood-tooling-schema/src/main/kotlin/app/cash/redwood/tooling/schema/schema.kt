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

/** A [Schema] and its dependencies. */
public interface SchemaSet {
  public val schema: Schema
  public val dependencies: Map<FqType, Schema>

  public val all: List<Schema> get() = buildList {
    add(schema)
    addAll(dependencies.values)
  }
}

public interface Schema {
  public val type: FqType
  public val scopes: List<FqType>
  public val widgets: List<Widget>
  public val layoutModifiers: List<LayoutModifier>
  public val dependencies: List<FqType>

  /**
   * Convert this schema to JSON which can be embedded inside the schema artifact.
   * This JSON will be read when the schema is used as a dependency.
   */
  public fun toEmbeddedSchema(): EmbeddedSchema
}

public data class EmbeddedSchema(
  val path: String,
  val json: String,
)

public interface Deprecation {
  public val level: Level
  public val message: String

  public enum class Level {
    WARNING,
    ERROR,
  }
}

public interface Widget {
  /** Either a 'data class' or 'object'. */
  public val type: FqType

  /** Non-null if this widget is deprecated. */
  public val deprecation: Deprecation?

  /** Non-empty list for a 'data class' [type] or empty list for 'object' [type]. */
  public val traits: List<Trait>

  public sealed interface Trait {
    public val name: String
    public val defaultExpression: String?

    /** Non-null if this trait is deprecated. */
    public val deprecation: Deprecation?
  }

  public interface Property : Trait {
    public val type: FqType
  }

  public interface Event : Trait {
    public val parameterType: FqType?
    public val isNullable: Boolean
  }

  public interface Children : Trait {
    public val scope: FqType?
  }
}

public interface LayoutModifier {
  public val scopes: List<FqType>

  /** Either a 'data class' or 'object'. */
  public val type: FqType

  /** Non-null if this layout modifier is deprecated. */
  public val deprecation: Deprecation?

  /** Non-empty list for a 'data class' [type] or empty list for 'object' [type]. */
  public val properties: List<Property>

  public interface Property {
    public val name: String
    public val type: FqType
    public val isSerializable: Boolean
    public val defaultExpression: String?

    /** Non-null if this property is deprecated. */
    public val deprecation: Deprecation?
  }
}

/** A [ProtocolSchema] and its dependencies. */
public interface ProtocolSchemaSet : SchemaSet {
  override val schema: ProtocolSchema
  override val dependencies: Map<FqType, ProtocolSchema>

  override val all: List<ProtocolSchema> get() = buildList {
    add(schema)
    addAll(dependencies.values)
  }
}

public interface ProtocolSchema : Schema {
  override val widgets: List<ProtocolWidget>
  override val layoutModifiers: List<ProtocolLayoutModifier>
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
