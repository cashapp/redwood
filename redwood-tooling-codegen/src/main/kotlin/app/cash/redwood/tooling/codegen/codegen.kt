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

import app.cash.redwood.tooling.codegen.Type.Compose
import app.cash.redwood.tooling.codegen.Type.ComposeProtocol
import app.cash.redwood.tooling.codegen.Type.LayoutModifiers
import app.cash.redwood.tooling.codegen.Type.Widget
import app.cash.redwood.tooling.codegen.Type.WidgetProtocol
import app.cash.redwood.tooling.schema.ProtocolSchema
import java.nio.file.Path

public enum class Type {
  Compose,
  ComposeProtocol,
  LayoutModifiers,
  Widget,
  WidgetProtocol,
}

public fun ProtocolSchema.generate(type: Type, destination: Path) {
  when (type) {
    Compose -> {
      generateLayoutModifierImpls(this)?.writeTo(destination)
      for (scope in scopes) {
        generateScope(this, scope).writeTo(destination)
      }
      for (widget in widgets) {
        generateComposable(this, widget).writeTo(destination)
      }
    }
    ComposeProtocol -> {
      generateDiffProducingWidgetFactories(this).writeTo(destination)
      for (dependency in allSchemas) {
        generateDiffProducingWidgetFactory(dependency, host = this).writeTo(destination)
        generateDiffProducingLayoutModifiers(dependency, host = this).writeTo(destination)
        for (widget in dependency.widgets) {
          generateDiffProducingWidget(dependency, widget, host = this).writeTo(destination)
        }
      }
    }
    LayoutModifiers -> {
      for (layoutModifier in layoutModifiers) {
        generateLayoutModifierInterface(this, layoutModifier).writeTo(destination)
      }
    }
    Widget -> {
      generateWidgetFactories(this).writeTo(destination)
      generateWidgetFactory(this).writeTo(destination)
      for (widget in widgets) {
        generateWidget(this, widget).writeTo(destination)
      }
    }
    WidgetProtocol -> {
      generateDiffConsumingNodeFactory(this).writeTo(destination)
      for (dependency in allSchemas) {
        generateDiffConsumingLayoutModifiers(dependency, host = this).writeTo(destination)
        for (widget in dependency.widgets) {
          generateDiffConsumingWidget(dependency, widget, host = this).writeTo(destination)
        }
      }
    }
  }
}
