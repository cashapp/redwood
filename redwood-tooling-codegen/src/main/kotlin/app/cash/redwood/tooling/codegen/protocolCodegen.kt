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
import com.squareup.kotlinpoet.FileSpec
import java.nio.file.Path

public enum class ProtocolCodegenType {
  Compose,
  Widget,
}

public fun ProtocolSchemaSet.generate(type: ProtocolCodegenType, destination: Path) {
  for (fileSpec in generateFileSpecs(type)) {
    fileSpec.writeTo(destination)
  }
}

internal fun ProtocolSchemaSet.generateFileSpecs(type: ProtocolCodegenType): List<FileSpec> {
  return buildList {
    when (type) {
      Compose -> {
        add(generateProtocolBridge(this@generateFileSpecs))
        add(generateComposeProtocolModifierSerialization(this@generateFileSpecs))
        for (dependency in all) {
          add(generateProtocolWidgetFactory(dependency, host = schema))
          generateProtocolModifierSerializers(dependency, host = schema)?.let { add(it) }
          for (widget in dependency.widgets) {
            add(generateProtocolWidget(dependency, widget, host = schema))
          }
        }
      }

      Widget -> {
        add(generateProtocolFactory(this@generateFileSpecs))
        for (dependency in all) {
          generateProtocolModifierImpls(dependency, host = schema)?.let { add(it) }
          for (widget in dependency.widgets) {
            add(generateProtocolNode(dependency, widget, host = schema))
          }
        }
      }
    }
  }
}
