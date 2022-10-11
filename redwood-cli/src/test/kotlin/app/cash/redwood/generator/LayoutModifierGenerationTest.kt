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
package app.cash.redwood.generator

import app.cash.redwood.schema.LayoutModifier
import app.cash.redwood.schema.Schema
import app.cash.redwood.schema.parser.parseSchema
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class LayoutModifierGenerationTest {
  @Schema(
    [
      NavigationBar.ContentDescription::class,
      ContentDescription::class,
    ],
  )
  interface SimpleNameCollisionSchema
  interface NavigationBar {
    @LayoutModifier(1)
    data class ContentDescription(val text: String)
  }

  @LayoutModifier(3)
  data class ContentDescription(val text: String)

  @Test fun `simple names do not collide`() {
    val schema = parseSchema(SimpleNameCollisionSchema::class)

    val topType = schema.layoutModifiers.single { it.type == ContentDescription::class }
    val topTypeSpec = generateLayoutModifierInterface(schema, topType)
    assertThat(topTypeSpec.toString()).contains("interface LayoutModifierGenerationTestContentDescription")

    val nestedType = schema.layoutModifiers.single { it.type == NavigationBar.ContentDescription::class }
    val nestedTypeSpec = generateLayoutModifierInterface(schema, nestedType)
    assertThat(nestedTypeSpec.toString()).contains("interface LayoutModifierGenerationTestNavigationBarContentDescription")
  }
}
