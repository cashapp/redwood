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
package app.cash.redwood.generator

import app.cash.redwood.schema.parser.LayoutModifier
import app.cash.redwood.schema.parser.Schema
import app.cash.redwood.schema.parser.Widget
import app.cash.redwood.schema.parser.Widget.Event
import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.UNIT
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.joinToCode

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

internal val Schema.composeTargetMarker get() = ClassName(composePackage, "${name}Composable")

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

internal fun Schema.layoutModifierType(layoutModifier: LayoutModifier): ClassName {
  return ClassName(`package`, layoutModifier.type.simpleName!!) // TODO flat name
}

internal fun Schema.layoutModifierSurrogate(layoutModifier: LayoutModifier): ClassName {
  return ClassName(composePackage, layoutModifier.type.simpleName!! + "Surrogate") // TODO flat name
}

internal fun layoutModifierEquals(schema: Schema, layoutModifier: LayoutModifier): FunSpec {
  val interfaceType = schema.layoutModifierType(layoutModifier)
  return FunSpec.builder("equals")
    .addModifiers(KModifier.OVERRIDE)
    .addParameter("other", ANY.copy(nullable = true))
    .returns(BOOLEAN)
    .apply {
      val conditions = mutableListOf(CodeBlock.of("other is %T", interfaceType))
      for (property in layoutModifier.properties) {
        conditions += CodeBlock.of("other.%1N == %1N", property.name)
      }
      addStatement("return %L", conditions.joinToCode("\n&& "))
    }
    .build()
}

internal fun layoutModifierHashCode(layoutModifier: LayoutModifier): FunSpec {
  return FunSpec.builder("hashCode")
    .addModifiers(KModifier.OVERRIDE)
    .returns(INT)
    .apply {
      if (layoutModifier.properties.isEmpty()) {
        addStatement("return %L", layoutModifier.type.simpleName!!.hashCode())
      } else {
        addStatement("var hash = 17")
        for (property in layoutModifier.properties) {
          addStatement("hash = 31 * hash + %N.hashCode()", property.name)
        }
        addStatement("return hash")
      }
    }
    .build()
}
