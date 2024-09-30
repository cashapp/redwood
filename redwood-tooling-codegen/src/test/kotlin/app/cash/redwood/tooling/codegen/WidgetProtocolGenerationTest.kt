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

import app.cash.redwood.tooling.schema.ProtocolSchemaSet
import assertk.all
import assertk.assertThat
import assertk.assertions.contains
import com.example.redwood.testapp.TestSchema
import org.junit.Test

class WidgetProtocolGenerationTest {
  @Test fun `dependency layout modifier are included in serialization`() {
    val schema = ProtocolSchemaSet.load(TestSchema::class)

    val fileSpec = generateProtocolFactory(schema)
    assertThat(fileSpec.toString()).all {
      contains("1 -> TestRowVerticalAlignmentImpl.serializer()")
      contains("1_000_001 -> GrowImpl.serializer()")
    }
  }
}
