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
import app.cash.redwood.tooling.schema.Modifier
import app.cash.redwood.tooling.schema.ProtocolModifier
import app.cash.redwood.tooling.schema.ProtocolSchema
import app.cash.redwood.tooling.schema.ProtocolSchemaSet
import app.cash.redwood.tooling.schema.Schema
import app.cash.redwood.tooling.schema.Widget
import app.cash.redwood.tooling.schema.Widget.Event
import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.Annotatable
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.Documentable
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterSpec
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
  get() = LambdaTypeName.get(
    null,
    parameters.map {
      if (it.name != null) {
        ParameterSpec.builder(it.name!!, it.type.asTypeName()).build()
      } else {
        ParameterSpec.unnamed(it.type.asTypeName())
      }
    },
    returnType = UNIT,
  ).copy(nullable = isNullable)

internal val Schema.unscopedModifiers get() = modifiers.filter { it.scopes.isEmpty() }

internal fun Schema.composePackage() = type.names[0] + ".compose"
internal fun Schema.modifierPackage() = type.names[0] + ".modifier"
internal fun Schema.testingPackage() = type.names[0] + ".testing"
internal fun Schema.widgetPackage() = type.names[0] + ".widget"

internal fun Schema.guestProtocolPackage(otherSchema: Schema? = null) = protocolPackage("guest", otherSchema)
internal fun Schema.hostProtocolPackage(otherSchema: Schema? = null) = protocolPackage("host", otherSchema)
private fun Schema.protocolPackage(name: String, otherSchema: Schema?): String {
  val base = type.names[0] + ".protocol." + name
  return if (otherSchema != null) "$base.${otherSchema.type.flatName.lowercase()}" else base
}

internal fun Schema.widgetType(widget: Widget): ClassName {
  return ClassName(widgetPackage(), widget.type.flatName)
}

internal fun Schema.getWidgetFactoryType(): ClassName {
  return ClassName(widgetPackage(), "${type.flatName}WidgetFactory")
}

internal fun Schema.getWidgetFactoryOwnerType(): ClassName {
  return ClassName(widgetPackage(), "${type.flatName}WidgetFactoryOwner")
}

internal fun Schema.getWidgetSystemType(): ClassName {
  return ClassName(widgetPackage(), "${type.flatName}WidgetSystem")
}

internal fun Schema.modifierType(modifier: Modifier): ClassName {
  return ClassName(modifierPackage(), modifier.type.flatName)
}

internal fun ProtocolSchemaSet.allModifiers(): List<Pair<ProtocolSchema, ProtocolModifier>> {
  return all.flatMap { schema ->
    schema.modifiers.map { schema to it }
  }
}

internal fun modifierEquals(schema: Schema, modifier: Modifier): FunSpec {
  val interfaceType = schema.modifierType(modifier)
  return FunSpec.builder("equals")
    .addModifiers(KModifier.OVERRIDE)
    .addParameter("other", ANY.copy(nullable = true))
    .returns(BOOLEAN)
    .apply {
      val conditions = mutableListOf(CodeBlock.of("other is %T", interfaceType))
      for (property in modifier.properties) {
        conditions += CodeBlock.of("other.%1N == %1N", property.name)
      }
      addStatement("return %L", conditions.joinToCode("\n&& "))
    }
    .build()
}

internal fun modifierHashCode(modifier: Modifier): FunSpec {
  return FunSpec.builder("hashCode")
    .addModifiers(KModifier.OVERRIDE)
    .returns(INT)
    .apply {
      if (modifier.properties.isEmpty()) {
        addStatement("return %L", modifier.type.flatName.hashCode())
      } else {
        addStatement("var hash = 17")
        for (property in modifier.properties) {
          addStatement("hash = 31 * hash + %N.hashCode()", property.name)
        }
        addStatement("return hash")
      }
    }
    .build()
}

internal fun modifierToString(modifier: Modifier): FunSpec {
  val simpleName = modifier.type.flatName
  return FunSpec.builder("toString")
    .addModifiers(KModifier.OVERRIDE)
    .returns(STRING)
    .apply {
      if (modifier.properties.isEmpty()) {
        addStatement("return %S", simpleName)
      } else {
        val statement = StringBuilder().append(simpleName).append("(")
        val lastIndex = modifier.properties.lastIndex
        for ((index, property) in modifier.properties.withIndex()) {
          val suffix = if (index != lastIndex) ", " else ")"
          statement.append(property.name).append("=$").append(property.name).append(suffix)
        }
        addStatement("return %P", statement.toString())
      }
    }
    .build()
}

internal val suppressDeprecations = AnnotationSpec.builder(Suppress::class)
  .useSiteTarget(AnnotationSpec.UseSiteTarget.FILE)
  .addMember("%S, %S", "DEPRECATION", "OVERRIDE_DEPRECATION")
  .build()

/** Add a `@Deprecated` annotation corresponding to [deprecation], if it is not null. */
internal fun <T : Annotatable.Builder<T>> T.maybeAddDeprecation(deprecation: Deprecation?) = apply {
  if (deprecation != null) {
    addAnnotation(
      AnnotationSpec.builder(Deprecated::class)
        .addMember("%S", deprecation.message)
        .addMember(
          "level = %M",
          MemberName(
            DeprecationLevel::class.asClassName(),
            when (deprecation.level) {
              WARNING -> DeprecationLevel.WARNING.name
              ERROR -> DeprecationLevel.ERROR.name
            },
          ),
        )
        .build(),
    )
  }
}

/** Calls [Documentable.Builder.addKdoc] if [string] is not null. */
internal fun <T : Documentable.Builder<T>> T.maybeAddKDoc(string: String?) = apply {
  if (string != null) {
    addKdoc(string)
  }
}

/** Calls [ParameterSpec.Builder.defaultValue] if [value] is not null. */
internal fun ParameterSpec.Builder.maybeDefaultValue(value: String?) = apply {
  if (value != null) {
    defaultValue(value)
  }
}

internal fun <T : Annotatable.Builder<T>> T.optIn(vararg names: ClassName): T = apply {
  addAnnotation(
    AnnotationSpec.builder(ClassName("kotlin", "OptIn"))
      .apply {
        for (name in names) {
          addMember("%T::class", name)
        }
      }
      .build(),
  )
}
