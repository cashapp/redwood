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
import app.cash.redwood.tooling.codegen.CodegenType.LayoutModifiers
import app.cash.redwood.tooling.codegen.CodegenType.Testing
import app.cash.redwood.tooling.codegen.CodegenType.Widget
import app.cash.redwood.tooling.schema.SchemaSet
import java.nio.file.Path

public enum class CodegenType {
  Compose,
  LayoutModifiers,
  Testing,
  Widget,
}

public fun SchemaSet.generate(type: CodegenType, destination: Path) {
  when (type) {
    Compose -> {
      generateLayoutModifierImpls(schema)?.writeTo(destination)
      for (scope in schema.scopes) {
        generateScope(schema, scope).writeTo(destination)
      }
      for (widget in schema.widgets) {
        generateComposable(schema, widget).writeTo(destination)
      }
    }
    LayoutModifiers -> {
      for (layoutModifier in schema.layoutModifiers) {
        generateLayoutModifierInterface(schema, layoutModifier).writeTo(destination)
      }
    }
    Testing -> {
      generateTester(this).writeTo(destination)
      generateMutableWidgetFactory(schema).writeTo(destination)
      for (widget in schema.widgets) {
        generateMutableWidget(schema, widget).writeTo(destination)
        generateWidgetValue(schema, widget).writeTo(destination)
      }
    }
    Widget -> {
      generateWidgetFactories(this).writeTo(destination)
      generateWidgetFactory(schema).writeTo(destination)
      for (widget in schema.widgets) {
        generateWidget(schema, widget).writeTo(destination)
      }
    }
  }
}
