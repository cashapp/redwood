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

import app.cash.redwood.schema.LayoutModifier
import app.cash.redwood.schema.Schema
import app.cash.redwood.tooling.schema.ProtocolSchemaSet
import com.google.common.truth.Truth.assertThat
import example.redwood.compose.TestScope
import kotlin.DeprecationLevel.ERROR
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import org.junit.Test

class LayoutModifierGenerationTest {
  @Schema(
    [
      NavigationBar.ContentDescription::class,
      ContentDescription::class,
    ],
  )
  interface SimpleNameCollisionSchema

  object SimpleNameCollisionScope

  interface NavigationBar {
    @LayoutModifier(1, SimpleNameCollisionScope::class)
    data class ContentDescription(val text: String)
  }

  @LayoutModifier(2, SimpleNameCollisionScope::class)
  data class ContentDescription(val text: String)

  @Test fun `simple names do not collide`() {
    val schema = ProtocolSchemaSet.parse(SimpleNameCollisionSchema::class).schema

    val topType = schema.layoutModifiers.single { it.type.flatName == "LayoutModifierGenerationTestContentDescription" }
    val topTypeSpec = generateLayoutModifierInterface(schema, topType)
    assertThat(topTypeSpec.toString()).contains("interface LayoutModifierGenerationTestContentDescription")

    val nestedType = schema.layoutModifiers.single { it.type.flatName == "LayoutModifierGenerationTestNavigationBarContentDescription" }
    val nestedTypeSpec = generateLayoutModifierInterface(schema, nestedType)
    assertThat(nestedTypeSpec.toString()).contains("interface LayoutModifierGenerationTestNavigationBarContentDescription")
  }

  @Schema(
    [
      ScopedLayoutModifier::class,
    ],
  )
  interface ScopedModifierSchema

  object LayoutModifierScope

  @LayoutModifier(1, LayoutModifierScope::class)
  object ScopedLayoutModifier

  @Test fun `layout modifier functions are stable`() {
    val schema = ProtocolSchemaSet.parse(ScopedModifierSchema::class).schema

    val modifier = schema.layoutModifiers.single { it.type.names.last() == "ScopedLayoutModifier" }
    val scope = modifier.scopes.single { it.names.last() == "LayoutModifierScope" }
    val scopeSpec = generateScope(schema, scope)
    assertThat(scopeSpec.toString()).contains(
      """
      |  @Stable
      |  public fun LayoutModifier.layoutModifierGenerationTestScopedLayoutModifier(): LayoutModifier
      """.trimMargin(),
    )
  }

  @Test fun `layout modifier implements toString`() = with(object : TestScope {}) {
    var type = app.cash.redwood.LayoutModifier.customType(20.seconds)
    assertThat(type.toString()).isEqualTo("CustomType(customType=20s)")

    type = app.cash.redwood.LayoutModifier.customTypeStateless()
    assertThat(type.toString()).isEqualTo("CustomTypeStateless")

    type = app.cash.redwood.LayoutModifier.customTypeWithDefault(40.minutes, "hello")
    assertThat(type.toString()).isEqualTo("CustomTypeWithDefault(customType=40m, string=hello)")
  }

  @Suppress("DEPRECATION")
  @Schema(
    [
      DeprecatedLayoutModifier::class,
    ],
  )
  interface DeprecatedSchema

  @LayoutModifier(1, LayoutModifierScope::class)
  @Deprecated("Hey")
  data class DeprecatedLayoutModifier(
    @Deprecated("Hello", level = ERROR)
    val a: String,
  )

  @Test fun deprecation() {
    val schema = ProtocolSchemaSet.parse(DeprecatedSchema::class).schema

    val modifier = schema.layoutModifiers.single()
    val fileSpec = generateLayoutModifierInterface(schema, modifier)
    assertThat(fileSpec.toString()).apply {
      contains(
        """
        |@Deprecated(
        |  "Hey",
        |  level = WARNING,
        |)
        |public interface LayoutModifierGenerationTestDeprecatedLayoutModifier
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
