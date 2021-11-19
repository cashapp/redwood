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
package app.cash.treehouse.schema.generator

import app.cash.treehouse.schema.Property
import app.cash.treehouse.schema.Schema
import app.cash.treehouse.schema.Widget
import app.cash.treehouse.schema.parser.parseSchema
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SchemaGenerationTest {
  @Schema(
    [
      DataClassWidget::class,
      ObjectWidget::class,
    ]
  )
  interface TestSchema
  @Widget(1)
  data class DataClassWidget(
    @Property(1) val label: String,
  )
  @Widget(2)
  object ObjectWidget

  @Test fun schemaInstanceCreation() {
    val schema = parseSchema(TestSchema::class)

    val dataClassWidget = generateSchemaWidget(schema, schema.widgets.single { it.type == DataClassWidget::class })
    assertThat(dataClassWidget.toString()).contains("get() = SchemaGenerationTest.DataClassWidget(\n")

    val objectWidget = generateSchemaWidget(schema, schema.widgets.single { it.type == ObjectWidget::class })
    assertThat(objectWidget.toString()).contains("get() = SchemaGenerationTest.ObjectWidget\n")
  }
}
