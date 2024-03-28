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

import app.cash.redwood.schema.Property
import app.cash.redwood.schema.Schema
import app.cash.redwood.schema.Widget
import app.cash.redwood.tooling.schema.ProtocolSchemaSet
import assertk.all
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.containsMatch
import com.example.redwood.testing.TestSchema
import kotlin.text.RegexOption.MULTILINE
import org.junit.Test

class WidgetProtocolGenerationTest {
  @Schema(
    [
      Node12::class,
      Node1::class,
      Node3::class,
      Node2::class,
    ],
  )
  interface SortedByTagSchema

  @Widget(1)
  data class Node1(@Property(1) val text: String)

  @Widget(2)
  data class Node2(@Property(1) val text: String)

  @Widget(3)
  data class Node3(@Property(1) val text: String)

  @Widget(12)
  data class Node12(@Property(1) val text: String)

  @Test fun `names are sorted by their node tags`() {
    val schema = ProtocolSchemaSet.parse(SortedByTagSchema::class)

    val fileSpec = generateProtocolFactory(schema)
    assertThat(fileSpec.toString())
      .containsMatch(Regex("1 ->[^2]+2 ->[^3]+3 ->[^1]+12 ->", MULTILINE))
  }

  @Test fun `dependency layout modifier are included in serialization`() {
    val schema = ProtocolSchemaSet.parse(TestSchema::class)

    val fileSpec = generateProtocolFactory(schema)
    assertThat(fileSpec.toString()).all {
      contains("1 -> TestRowVerticalAlignmentImpl.serializer()")
      contains("1_000_001 -> GrowImpl.serializer()")
    }
  }
}
