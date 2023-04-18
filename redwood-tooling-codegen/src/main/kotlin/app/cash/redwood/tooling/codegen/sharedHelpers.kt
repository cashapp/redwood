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
package app.cash.redwood.tooling.codegen

import app.cash.redwood.tooling.schema.Deprecation
import app.cash.redwood.tooling.schema.Deprecation.Level.ERROR
import app.cash.redwood.tooling.schema.Deprecation.Level.WARNING
import app.cash.redwood.tooling.schema.FqType
import app.cash.redwood.tooling.schema.LayoutModifier
import app.cash.redwood.tooling.schema.ProtocolLayoutModifier
import app.cash.redwood.tooling.schema.ProtocolSchema
import app.cash.redwood.tooling.schema.ProtocolSchemaSet
import app.cash.redwood.tooling.schema.Schema
import app.cash.redwood.tooling.schema.Widget
import app.cash.redwood.tooling.schema.Widget.Event
import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.AnnotationSpec
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
import com.squareup.kotlinpoet.joinToCode

/**
 * Returns a single string that is likely to be unique within a schema, like `MapEntry` or
 * `NavigationBarButton`.
 */
internal val FqType.flatName: String
  get() = names.drop(1).joinToString("")

internal val Event.lambdaType: TypeName
  get() {
    parameterType?.let { parameterType ->
      return LambdaTypeName.get(null, parameterType.asTypeName(), returnType = UNIT)
        .copy(nullable = true)
    }
    return noArgumentEventLambda
  }

private val noArgumentEventLambda = LambdaTypeName.get(returnType = UNIT).copy(nullable = true)

internal fun Schema.composePackage(host: Schema? = null): String {
  return if (host == null) {
    val `package` = type.names[0]
    "$`package`.compose"
  } else {
    val hostPackage = host.type.names[0]
    "$hostPackage.compose.${type.flatName.lowercase()}"
  }
}

internal fun Schema.protocolBridgeType(): ClassName {
  return ClassName(composePackage(), "${type.flatName}ProtocolBridge")
}

internal fun Schema.protocolWidgetFactoryType(host: Schema): ClassName {
  return ClassName(composePackage(host), "Protocol${type.flatName}WidgetFactory")
}

internal fun Schema.protocolWidgetType(widget: Widget, host: Schema): ClassName {
  return ClassName(composePackage(host), "Protocol${widget.type.flatName}")
}

internal fun Schema.diffConsumingNodeFactoryType(): ClassName {
  return ClassName(widgetPackage(), "${type.flatName}DiffConsumingNodeFactory")
}

internal fun Schema.diffConsumingNodeType(widget: Widget, host: Schema): ClassName {
  return ClassName(widgetPackage(host), "DiffConsuming${widget.type.flatName}")
}

internal fun Schema.widgetType(widget: Widget): ClassName {
  return ClassName(widgetPackage(), widget.type.flatName)
}

internal fun Schema.getWidgetFactoryType(): ClassName {
  return ClassName(widgetPackage(), "${type.flatName}WidgetFactory")
}

internal fun Schema.getMutableWidgetFactoryType(): ClassName {
  return ClassName(widgetPackage(), "Mutable${type.flatName}WidgetFactory")
}

internal fun Schema.mutableWidgetType(widget: Widget): ClassName {
  return ClassName(widgetPackage(), "Mutable${widget.type.flatName}")
}

internal fun Schema.widgetValueType(widget: Widget): ClassName {
  return ClassName(widgetPackage(), "${widget.type.flatName}Value")
}

internal fun Schema.getWidgetFactoryProviderType(): ClassName {
  return ClassName(widgetPackage(), "${type.flatName}WidgetFactoryProvider")
}

internal fun Schema.getWidgetFactoriesType(): ClassName {
  return ClassName(widgetPackage(), "${type.flatName}WidgetFactories")
}

internal fun Schema.widgetPackage(host: Schema? = null): String {
  return if (host == null) {
    val `package` = type.names[0]
    "$`package`.widget"
  } else {
    val hostPackage = host.type.names[0]
    "$hostPackage.widget.${type.flatName.lowercase()}"
  }
}

internal fun Schema.layoutModifierType(layoutModifier: LayoutModifier): ClassName {
  return ClassName(type.names[0], layoutModifier.type.flatName)
}

internal fun Schema.layoutModifierSerializer(layoutModifier: LayoutModifier, host: Schema): ClassName {
  return ClassName(composePackage(host), layoutModifier.type.flatName + "Serializer")
}

internal fun Schema.layoutModifierImpl(layoutModifier: LayoutModifier): ClassName {
  return ClassName(composePackage(), layoutModifier.type.flatName + "Impl")
}

internal fun Schema.getTesterFunction(): MemberName {
  return MemberName(widgetPackage(), "${type.flatName}Tester")
}

internal val Schema.toLayoutModifier: MemberName get() =
  MemberName(widgetPackage(), "toLayoutModifier")

internal val Schema.layoutModifierToProtocol: MemberName get() =
  MemberName(composePackage(), "toProtocol")

internal fun ProtocolSchemaSet.allLayoutModifiers(): List<Pair<ProtocolSchema, ProtocolLayoutModifier>> {
  return all.flatMap { schema ->
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
        addStatement("return %L", layoutModifier.type.flatName.hashCode())
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
  val simpleName = layoutModifier.type.flatName
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

internal fun Deprecation.toAnnotationSpec(): AnnotationSpec {
  return AnnotationSpec.builder(Deprecated::class)
    .addMember("%S", message)
    .addMember("level = %M", level.toMemberName())
    .build()
}

private fun Deprecation.Level.toMemberName(): MemberName {
  return MemberName(
    DeprecationLevel::class.asClassName(),
    when (this) {
      WARNING -> DeprecationLevel.WARNING.name
      ERROR -> DeprecationLevel.ERROR.name
    },
  )
}
