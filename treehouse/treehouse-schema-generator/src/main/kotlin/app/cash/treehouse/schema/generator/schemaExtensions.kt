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

import app.cash.treehouse.schema.parser.Event
import app.cash.treehouse.schema.parser.Schema
import app.cash.treehouse.schema.parser.Widget
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.UNIT
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName

/**
 * Returns a single string that is likely to be unique within a schema, like `MapEntry` or
 * `NavigationBarButton`.
 */
internal val Widget.flatName: String
  get() = type.asClassName().simpleNames.joinToString(separator = "")

internal val Event.lambdaType: TypeName
  get() {
    parameterType?.let { parameterType ->
      return LambdaTypeName.get(null, parameterType.asTypeName(), returnType = UNIT)
        .copy(nullable = true)
    }
    return noArgumentEventLambda
  }

private val noArgumentEventLambda = LambdaTypeName.get(returnType = UNIT).copy(nullable = true)

internal val Schema.composePackage get() = "$`package`.compose"

internal fun Schema.diffProducingWidgetFactoryType(): ClassName {
  return ClassName(composePackage, "DiffProducing${name}WidgetFactory")
}

internal fun Schema.diffProducingWidgetType(widget: Widget): ClassName {
  return ClassName(composePackage, "DiffProducing${widget.flatName}")
}

internal fun Schema.diffConsumingWidgetFactoryType(): ClassName {
  return ClassName(widgetPackage, "DiffConsuming${name}WidgetFactory")
}

internal fun Schema.diffConsumingWidgetType(widget: Widget): ClassName {
  return ClassName(widgetPackage, "DiffConsuming${widget.flatName}")
}

internal fun Schema.widgetType(widget: Widget): ClassName {
  return ClassName(widgetPackage, widget.flatName)
}

internal fun Schema.getWidgetFactoryType(): ClassName {
  return ClassName(widgetPackage, "${name}WidgetFactory")
}

internal val Schema.widgetPackage get() = "$`package`.widget"

internal fun Schema.testType(widget: Widget): ClassName {
  return ClassName(testPackage, "Schema${widget.flatName}")
}

internal val Schema.testPackage get() = "$`package`.test"
