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

import app.cash.redwood.tooling.codegen.ProtocolCodegenType.Compose
import app.cash.redwood.tooling.codegen.ProtocolCodegenType.Widget
import app.cash.redwood.tooling.schema.ProtocolSchemaSet
import java.nio.file.Path

public enum class ProtocolCodegenType {
  Compose,
  Widget,
}

public fun ProtocolSchemaSet.generate(type: ProtocolCodegenType, destination: Path) {
  when (type) {
    Compose -> {
      generateProtocolBridge(this).writeTo(destination)
      generateComposeProtocolModifierSerialization(this).writeTo(destination)
      for (dependency in all) {
        generateProtocolWidgetFactory(dependency, host = schema).writeTo(destination)
        generateProtocolModifierSerializers(dependency, host = schema)?.writeTo(destination)
        for (widget in dependency.widgets) {
          generateProtocolWidget(dependency, widget, host = schema).writeTo(destination)
        }
      }
    }
    Widget -> {
      generateProtocolNodeFactory(this).writeTo(destination)
      generateWidgetProtocolModifierSerialization(this).writeTo(destination)
      for (dependency in all) {
        generateProtocolModifierImpls(dependency, host = schema)?.writeTo(destination)
        for (widget in dependency.widgets) {
          generateProtocolNode(dependency, widget, host = schema).writeTo(destination)
        }
      }
    }
  }
}
