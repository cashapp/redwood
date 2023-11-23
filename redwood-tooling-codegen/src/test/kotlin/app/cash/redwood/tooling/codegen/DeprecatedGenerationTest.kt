/*
 * Copyright (C) 2023 Square, Inc.
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

import app.cash.redwood.schema.Children
import app.cash.redwood.schema.Modifier
import app.cash.redwood.schema.Property
import app.cash.redwood.schema.Schema
import app.cash.redwood.schema.Widget
import app.cash.redwood.tooling.schema.ProtocolSchemaSet
import assertk.all
import assertk.assertAll
import assertk.assertThat
import assertk.assertions.contains
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import kotlin.DeprecationLevel.ERROR
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(TestParameterInjector::class)
class DeprecatedGenerationTest {
  @Suppress("DEPRECATION")
  @Schema(
    [
      DeprecatedModifier::class,
      DeprecatedWidget::class,
    ],
  )
  interface DeprecatedSchema

  @Widget(1)
  @Deprecated("Hey")
  data class DeprecatedWidget(
    @Property(1)
    @Deprecated("Property", level = ERROR)
    val prop: String,
    @Property(2)
    @Deprecated("Event", level = ERROR)
    val event: () -> Unit,
    @Children(1)
    @Deprecated("Children", level = ERROR)
    val children: () -> Unit,
  )

  @Modifier(1, ModifierGenerationTest.ModifierScope::class)
  @Deprecated("Hey")
  data class DeprecatedModifier(
    @Deprecated("Hello", level = ERROR)
    val a: String,
  )

  @Test fun modifierInterface() {
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
        |public interface DeprecatedGenerationTestDeprecatedModifier
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

  @Test fun widget() {
    val schema = ProtocolSchemaSet.parse(DeprecatedSchema::class).schema

    val widget = schema.widgets.single()
    val fileSpec = generateWidget(schema, widget)
    assertThat(fileSpec.toString()).all {
      contains(
        """
        |@Deprecated(
        |  "Hey",
        |  level = WARNING,
        |)
        |public interface DeprecatedGenerationTestDeprecatedWidget
        """.trimMargin(),
      )

      contains(
        """
        |  @Deprecated(
        |    "Property",
        |    level = ERROR,
        |  )
        |  public fun prop(
        """.trimMargin(),
      )

      contains(
        """
        |  @Deprecated(
        |    "Event",
        |    level = ERROR,
        |  )
        |  public fun event(
        """.trimMargin(),
      )

      contains(
        """
        |  @Deprecated(
        |    "Children",
        |    level = ERROR,
        |  )
        |  public val children:
        """.trimMargin(),
      )
    }
  }

  @Test
  fun protocolCodegen(
    @TestParameter type: ProtocolCodegenType,
  ) {
    val schema = ProtocolSchemaSet.parse(DeprecatedSchema::class)
    assertAll {
      for (fileSpec in schema.generateFileSpecs(type)) {
        assertThat(fileSpec.toString()).contains("""@file:Suppress("DEPRECATION")""")
      }
    }
  }

  @Test fun codegen(
    @TestParameter type: CodegenType,
  ) {
    val schema = ProtocolSchemaSet.parse(DeprecatedSchema::class)
    assertAll {
      for (fileSpec in schema.generateFileSpecs(type)) {
        assertThat(fileSpec.toString()).contains("""@file:Suppress("DEPRECATION")""")
      }
    }
  }
}
