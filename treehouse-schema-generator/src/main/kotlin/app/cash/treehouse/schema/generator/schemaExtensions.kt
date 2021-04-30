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

import app.cash.treehouse.schema.parser.Schema
import app.cash.treehouse.schema.parser.Widget
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.asClassName

/**
 * Returns a single string that is likely to be unique within a schema, like `MapEntry` or
 * `NavigationBarButton`.
 */
internal val Widget.flatName: String
  get() = type.asClassName().simpleNames.joinToString(separator = "")

internal fun Schema.composeNodeType(widget: Widget): ClassName {
  return ClassName(composePackage, widget.flatName + "ComposeNode")
}

internal val Schema.composePackage get() = "$`package`.compose"

internal fun Schema.widgetType(widget: Widget): ClassName {
  return ClassName(displayPackage, widget.flatName)
}

internal fun Schema.getWidgetFactoryType(): ClassName {
  return ClassName(displayPackage, "${name}WidgetFactory")
}

internal val Schema.displayPackage get() = "$`package`.widget"

internal fun Schema.testType(widget: Widget): ClassName {
  return ClassName(testPackage, "Schema${widget.flatName}")
}

internal val Schema.testPackage get() = "$`package`.test"
