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

import app.cash.redwood.tooling.codegen.CodegenType.Compose
import app.cash.redwood.tooling.codegen.CodegenType.Modifiers
import app.cash.redwood.tooling.codegen.CodegenType.Testing
import app.cash.redwood.tooling.codegen.CodegenType.Widget
import app.cash.redwood.tooling.schema.SchemaSet
import com.squareup.kotlinpoet.FileSpec
import java.nio.file.Path

public enum class CodegenType {
  Compose,
  Modifiers,
  Testing,
  Widget,
}

public fun SchemaSet.generate(type: CodegenType, destination: Path) {
  for (fileSpec in generateFileSpecs(type)) {
    fileSpec.writeTo(destination)
  }
}

internal fun SchemaSet.generateFileSpecs(type: CodegenType): List<FileSpec> {
  return buildList {
    when (type) {
      Compose -> {
        generateModifierImpls(schema)?.let { add(it) }
        generateUnscopedModifiers(schema)?.let { add(it) }
        for (scope in schema.scopes) {
          add(generateModifierScope(schema, scope))
        }
        for (widget in schema.widgets) {
          add(generateComposable(schema, widget))
        }
      }

      Modifiers -> {
        for (modifier in schema.modifiers) {
          add(generateModifierInterface(schema, modifier))
        }
      }

      Testing -> {
        add(generateTester(this@generateFileSpecs))
        add(generateMutableWidgetFactory(schema))
        for (widget in schema.widgets) {
          add(generateMutableWidget(schema, widget))
          add(generateWidgetValue(schema, widget))
        }
      }

      Widget -> {
        add(generateWidgetSystem(this@generateFileSpecs))
        add(generateWidgetFactory(schema))
        for (widget in schema.widgets) {
          add(generateWidget(schema, widget))
        }
      }
    }
  }
}
