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
package app.cash.redwood.tooling.schema

import app.cash.redwood.schema.Schema
import app.cash.redwood.schema.Widget
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import org.junit.Test

class SchemaJsonTest {
  @Schema(
    members = [
      MyWidget::class,
    ],
  )
  interface MySchema

  @Widget(1)
  object MyWidget

  @Test fun pathReflectsFqcn() {
    val embeddedSchema = ProtocolSchemaSet.parse(MySchema::class).schema.toEmbeddedSchema()
    assertThat(embeddedSchema.path).isEqualTo("app/cash/redwood/tooling/schema/SchemaJsonTest.MySchema.json")
  }

  @Test fun versioned() {
    val embeddedSchema = ProtocolSchemaSet.parse(MySchema::class).schema.toEmbeddedSchema()
    assertThat(embeddedSchema.json).contains(""""version": 1,""")
  }
}
