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
package app.cash.redwood.tooling.codegen

import app.cash.redwood.schema.Modifier
import app.cash.redwood.schema.Schema
import app.cash.redwood.tooling.schema.ProtocolSchemaSet
import assertk.all
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import example.redwood.compose.TestScope
import kotlin.DeprecationLevel.ERROR
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import org.junit.Test

class ModifierGenerationTest {
  @Schema(
    [
      NavigationBar.ContentDescription::class,
      ContentDescription::class,
    ],
  )
  interface SimpleNameCollisionSchema

  object SimpleNameCollisionScope

  interface NavigationBar {
    @Modifier(1, SimpleNameCollisionScope::class)
    data class ContentDescription(val text: String)
  }

  @Modifier(2, SimpleNameCollisionScope::class)
  data class ContentDescription(val text: String)

  @Test fun `simple names do not collide`() {
    val schema = ProtocolSchemaSet.parse(SimpleNameCollisionSchema::class).schema

    val topType = schema.modifiers.single { it.type.flatName == "ModifierGenerationTestContentDescription" }
    val topTypeSpec = generateModifierInterface(schema, topType)
    assertThat(topTypeSpec.toString()).contains("interface ModifierGenerationTestContentDescription")

    val nestedType = schema.modifiers.single { it.type.flatName == "ModifierGenerationTestNavigationBarContentDescription" }
    val nestedTypeSpec = generateModifierInterface(schema, nestedType)
    assertThat(nestedTypeSpec.toString()).contains("interface ModifierGenerationTestNavigationBarContentDescription")
  }

  @Schema(
    [
      ScopedModifier::class,
    ],
  )
  interface ScopedModifierSchema

  object ModifierScope

  @Modifier(1, ModifierScope::class)
  object ScopedModifier

  @Test fun `layout modifier functions are stable`() {
    val schema = ProtocolSchemaSet.parse(ScopedModifierSchema::class).schema

    val modifier = schema.modifiers.single { it.type.names.last() == "ScopedModifier" }
    val scope = modifier.scopes.single { it.names.last() == "ModifierScope" }
    val scopeSpec = generateScope(schema, scope)
    assertThat(scopeSpec.toString()).contains(
      """
      |  @Stable
      |  public fun Modifier.modifierGenerationTestScopedModifier(): Modifier
      """.trimMargin(),
    )
  }

  @Test fun `layout modifier implements toString`() = with(object : TestScope {}) {
    var type = app.cash.redwood.Modifier.customType(20.seconds)
    assertThat(type.toString()).isEqualTo("CustomType(customType=20s)")

    type = app.cash.redwood.Modifier.customTypeStateless()
    assertThat(type.toString()).isEqualTo("CustomTypeStateless")

    type = app.cash.redwood.Modifier.customTypeWithDefault(40.minutes, "hello")
    assertThat(type.toString()).isEqualTo("CustomTypeWithDefault(customType=40m, string=hello)")
  }

  @Suppress("DEPRECATION")
  @Schema(
    [
      DeprecatedModifier::class,
    ],
  )
  interface DeprecatedSchema

  @Modifier(1, ModifierScope::class)
  @Deprecated("Hey")
  data class DeprecatedModifier(
    @Deprecated("Hello", level = ERROR)
    val a: String,
  )

  @Test fun deprecation() {
    val schema = ProtocolSchemaSet.parse(DeprecatedSchema::class).schema

    val modifier = schema.modifiers.single()
    val fileSpec = generateModifierInterface(schema, modifier)
    assertThat(fileSpec.toString()).all {
      contains(
        """
        |@Deprecated(
        |  "Hey",
        |  level = WARNING,
        |)
        |public interface ModifierGenerationTestDeprecatedModifier
        """.trimMargin(),
      )

      contains(
        """
        |  @Deprecated(
        |    "Hello",
        |    level = ERROR,
        |  )
        |  public val a:
        """.trimMargin(),
      )
    }
  }
}
