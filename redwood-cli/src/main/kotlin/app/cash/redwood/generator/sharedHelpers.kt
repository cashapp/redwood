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
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.UNIT
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.joinToCode
import kotlin.reflect.KClass

/**
 * Returns a single string that is likely to be unique within a schema, like `MapEntry` or
 * `NavigationBarButton`.
 */
internal val KClass<*>.flatName: String
  get() = asClassName().simpleNames.joinToString(separator = "")

internal val Event.lambdaType: TypeName
  get() {
    parameterType?.let { parameterType ->
      return LambdaTypeName.get(null, parameterType.asTypeName(), returnType = UNIT)
        .copy(nullable = true)
    }
    return noArgumentEventLambda
  }

private val noArgumentEventLambda = LambdaTypeName.get(returnType = UNIT).copy(nullable = true)

internal fun Schema.composePackage(host: Schema = this): String {
  return if (this === host) {
    "$`package`.compose"
  } else {
    "${host.`package`}.compose.${name.lowercase()}"
  }
}

internal val Schema.composeTargetMarker get() = ClassName(composePackage(this), "${name}Composable")

internal fun Schema.diffProducingWidgetFactoryType(host: Schema): ClassName {
  return ClassName(composePackage(host), "DiffProducing${name}WidgetFactory")
}

internal fun Schema.diffProducingWidgetType(widget: Widget, host: Schema): ClassName {
  return ClassName(composePackage(host), "DiffProducing${widget.type.flatName}")
}

internal fun Schema.diffConsumingWidgetFactoryType(host: Schema): ClassName {
  return ClassName(widgetPackage(host), "DiffConsuming${name}WidgetFactory")
}

internal fun Schema.diffConsumingWidgetType(widget: Widget, host: Schema): ClassName {
  return ClassName(widgetPackage(host), "DiffConsuming${widget.type.flatName}")
}

internal fun Schema.widgetType(widget: Widget): ClassName {
  return ClassName(widgetPackage(this), widget.type.flatName)
}

internal fun Schema.getWidgetFactoryType(): ClassName {
  return ClassName(widgetPackage(this), "${name}WidgetFactory")
}

internal fun Schema.widgetPackage(host: Schema = this): String {
  return if (this === host) {
    "$`package`.widget"
  } else {
    "${host.`package`}.widget.${name.lowercase()}"
  }
}

internal fun Schema.layoutModifierType(layoutModifier: LayoutModifier): ClassName {
  return ClassName(`package`, layoutModifier.type.flatName)
}

internal fun Schema.layoutModifierSurrogate(layoutModifier: LayoutModifier, host: Schema): ClassName {
  return ClassName(composePackage(host), layoutModifier.type.flatName + "Surrogate")
}

internal fun Schema.layoutModifierImpl(layoutModifier: LayoutModifier): ClassName {
  return ClassName(composePackage(), layoutModifier.type.simpleName!! + "Impl")
}

internal val Schema.toLayoutModifier: MemberName get() =
  MemberName(widgetPackage(this), "toLayoutModifier")

internal val Schema.toProtocol: MemberName get() =
  MemberName(composePackage(this), "toProtocol")

internal fun Schema.allLayoutModifiers(): List<Pair<Schema, LayoutModifier>> {
  return (listOf(this) + dependencies).flatMap { schema ->
    schema.layoutModifiers.map { schema to it }
  }
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

internal fun layoutModifierToString(layoutModifier: LayoutModifier): FunSpec {
  val simpleName = layoutModifier.type.simpleName!!
  return FunSpec.builder("toString")
    .addModifiers(KModifier.OVERRIDE)
    .returns(STRING)
    .apply {
      if (layoutModifier.properties.isEmpty()) {
        addStatement("return %S", simpleName)
      } else {
        val statement = StringBuilder().append(simpleName).append("(")
        val lastIndex = layoutModifier.properties.lastIndex
        for ((index, property) in layoutModifier.properties.withIndex()) {
          val suffix = if (index != lastIndex) ", " else ")"
          statement.append(property.name).append("=$").append(property.name).append(suffix)
        }
        addStatement("return %P", statement.toString())
      }
    }
    .build()
}
