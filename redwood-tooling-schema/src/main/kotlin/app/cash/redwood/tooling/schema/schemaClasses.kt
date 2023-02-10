/*
 * Copyright (C) 2022 Square, Inc.
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

import app.cash.redwood.tooling.schema.LayoutModifier.Property
import app.cash.redwood.tooling.schema.ProtocolWidget.ProtocolChildren
import app.cash.redwood.tooling.schema.ProtocolWidget.ProtocolEvent
import app.cash.redwood.tooling.schema.ProtocolWidget.ProtocolProperty
import app.cash.redwood.tooling.schema.ProtocolWidget.ProtocolTrait

internal data class ParsedProtocolSchemaSet(
  override val schema: ProtocolSchema,
  override val dependencies: Map<FqType, ProtocolSchema>,
) : ProtocolSchemaSet

internal data class ParsedProtocolSchema(
  override val type: FqType,
  override val scopes: List<FqType>,
  override val widgets: List<ProtocolWidget>,
  override val layoutModifiers: List<ProtocolLayoutModifier>,
  override val dependencies: List<FqType>,
) : ProtocolSchema

internal data class ParsedProtocolWidget(
  override val tag: Int,
  override val type: FqType,
  override val traits: List<ProtocolTrait>,
) : ProtocolWidget

internal data class ParsedProtocolProperty(
  override val tag: Int,
  override val name: String,
  override val type: FqType,
  override val defaultExpression: String?,
) : ProtocolProperty

internal data class ParsedProtocolEvent(
  override val tag: Int,
  override val name: String,
  override val defaultExpression: String?,
  override val parameterType: FqType?,
) : ProtocolEvent

internal data class ParsedProtocolChildren(
  override val tag: Int,
  override val name: String,
  override val defaultExpression: String?,
  override val scope: FqType?,
) : ProtocolChildren

internal data class ParsedProtocolLayoutModifier(
  override val tag: Int,
  override val scopes: List<FqType>,
  override val type: FqType,
  override val properties: List<Property>,
) : ProtocolLayoutModifier

internal data class ParsedProtocolLayoutModifierProperty(
  override val name: String,
  override val type: FqType,
  override val isSerializable: Boolean,
  override val defaultExpression: String?,
) : Property
