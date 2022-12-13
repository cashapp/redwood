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
package app.cash.redwood.tooling.codegen

import app.cash.redwood.schema.LayoutModifier
import app.cash.redwood.schema.Property
import app.cash.redwood.schema.Schema

@Schema(
  members = [
    PrimaryModifier::class,
  ],
  dependencies = [
    Schema.Dependency(1, SecondarySchema::class),
  ],
)
interface PrimarySchema

object PrimaryScope

@LayoutModifier(1, PrimaryScope::class)
data class PrimaryModifier(@Property(1) val text: String)

@Schema(
  members = [
    SecondaryModifier::class,
  ],
)
interface SecondarySchema

object SecondaryScope

@LayoutModifier(1, SecondaryScope::class)
data class SecondaryModifier(@Property(1) val valid: Boolean)
