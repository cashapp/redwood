/*
 * Copyright (C) 2025 Square, Inc.
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

import app.cash.redwood.tooling.schema.ProtocolWidget.ProtocolTrait
import app.cash.redwood.tooling.schema.Schema
import app.cash.redwood.tooling.schema.Widget
import app.cash.redwood.tooling.schema.Widget.Children
import app.cash.redwood.tooling.schema.Widget.Event
import app.cash.redwood.tooling.schema.Widget.Property
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.DOUBLE
import com.squareup.kotlinpoet.FLOAT
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.KModifier.INTERNAL
import com.squareup.kotlinpoet.KModifier.OVERRIDE
import com.squareup.kotlinpoet.KModifier.PRIVATE
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.UNIT
import com.squareup.kotlinpoet.joinToCode

private val composeUiWidgetType = LambdaTypeName.get(
  receiver = null,
  ComposeUi.Modifier,
  returnType = UNIT,
).copy(
  annotations = listOf(
    AnnotationSpec.builder(ComposeRuntime.Composable).build(),
  ),
)

private val Widget.composeUiLambdaType: TypeName get() {
  val parameters = traits.mapNotNull { trait ->
    when (trait) {
      is Event -> ParameterSpec.builder(trait.name, trait.lambdaType).build()
      is Property -> ParameterSpec.builder(trait.name, trait.type.asTypeName()).build()
      is Children -> null
      is ProtocolTrait -> throw AssertionError()
    }
  }
  val modifierParameter = ParameterSpec.builder("modifier", ComposeUi.Modifier).build()
  return LambdaTypeName.get(
    receiver = null,
    parameters = parameters + modifierParameter,
    returnType = UNIT,
  ).copy(
    annotations = listOf(
      AnnotationSpec.builder(ComposeRuntime.Composable).build(),
    ),
  )
}

private fun Schema.composeUiWidgetType(widget: Widget): ClassName {
  return ClassName(composeUiPackage(), "ComposeUi" + widget.type.flatName)
}

internal fun generateComposeUiWidgetFactory(schema: Schema): FileSpec {
  val widgetFactoryType = schema.getWidgetFactoryType()
  val thisType = ClassName(schema.composeUiPackage(), "ComposeUi" + widgetFactoryType.simpleName)
  return buildFileSpec(thisType) {
    addAnnotation(suppressDeprecations)
    addType(
      buildClassSpec(thisType) {
        addSuperinterface(widgetFactoryType.parameterizedBy(composeUiWidgetType))

        val constructor = FunSpec.constructorBuilder()

        for (widget in schema.widgets) {
          val flatName = widget.type.flatName

          constructor.addParameter(flatName, widget.composeUiLambdaType)
          addProperty(
            PropertySpec.builder(flatName, widget.composeUiLambdaType)
              .addModifiers(PRIVATE)
              .initializer(flatName)
              .build(),
          )

          addFunction(
            FunSpec.builder(flatName)
              .returns(schema.widgetType(widget).parameterizedBy(composeUiWidgetType))
              .addModifiers(OVERRIDE)
              .addStatement("return %T(%N)", schema.composeUiWidgetType(widget), flatName)
              .build(),
          )
        }

        primaryConstructor(constructor.build())

        for (modifier in schema.unscopedModifiers) {
          addFunction(
            FunSpec.builder(modifier.type.flatName)
              .addModifiers(OVERRIDE)
              .addParameter("value", composeUiWidgetType)
              .addParameter("modifier", schema.modifierType(modifier))
              .build(),
          )
        }
      },
    )
  }
}

internal fun generateComposeUiBinding(schema: Schema, widget: Widget): FileSpec {
  val widgetType = schema.widgetType(widget)
  val thisType = schema.composeUiWidgetType(widget)
  return buildFileSpec(thisType) {
    addAnnotation(suppressDeprecations)
    addType(
      buildClassSpec(thisType) {
        addSuperinterface(widgetType.parameterizedBy(composeUiWidgetType))
        addModifiers(INTERNAL)

        primaryConstructor(
          FunSpec.constructorBuilder()
            .addParameter("delegate", widget.composeUiLambdaType)
            .build(),
        )
        addProperty(
          PropertySpec.builder("delegate", widget.composeUiLambdaType)
            .addModifiers(PRIVATE)
            .initializer("delegate")
            .build(),
        )

        addProperty(
          PropertySpec.builder("modifier", Redwood.Modifier)
            .addModifiers(OVERRIDE)
            .mutable(true)
            .initializer("%T", Redwood.Modifier)
            .build(),
        )

        val delegateArguments = mutableListOf<CodeBlock>()

        for (trait in widget.traits) {
          val traitType = when (trait) {
            is Property -> trait.type.asTypeName()
            is Event -> trait.lambdaType
            is Children -> continue
            is ProtocolTrait -> throw AssertionError()
          }

          val stateFactory: MemberName
          val stateDefault: CodeBlock
          val stateType: TypeName
          when (traitType) {
            INT -> {
              stateFactory = ComposeRuntime.mutableIntStateOf
              stateDefault = CodeBlock.of("0")
              stateType = traitType
            }
            DOUBLE -> {
              stateFactory = ComposeRuntime.mutableDoubleStateOf
              stateDefault = CodeBlock.of("0.0")
              stateType = traitType
            }
            FLOAT -> {
              stateFactory = ComposeRuntime.mutableFloatStateOf
              stateDefault = CodeBlock.of("0f")
              stateType = traitType
            }
            LONG -> {
              stateFactory = ComposeRuntime.mutableLongStateOf
              stateDefault = CodeBlock.of("0L")
              stateType = traitType
            }
            else -> {
              stateFactory = ComposeRuntime.mutableStateOf
              stateDefault = CodeBlock.of("null")
              // Always widen to nullable since we need to use it as an uninitialized value.
              stateType = traitType.copy(nullable = true)
            }
          }

          delegateArguments += if (stateType == traitType) {
            CodeBlock.of("%N", trait.name)
          } else {
            CodeBlock.of("%N as %T", trait.name, traitType)
          }

          // TODO Add addImport(MemberName) https://github.com/square/kotlinpoet/issues/2197
          addImport(ComposeRuntime.getValue.packageName, ComposeRuntime.getValue.simpleName)
          addImport(ComposeRuntime.setValue.packageName, ComposeRuntime.setValue.simpleName)

          addProperty(
            PropertySpec.builder(trait.name, stateType)
              .mutable(true)
              .addModifiers(PRIVATE)
              .delegate("%M(%L)", stateFactory, stateDefault)
              .build(),
          )
          addFunction(
            FunSpec.builder(trait.name)
              .addParameter(trait.name, traitType)
              .addModifiers(OVERRIDE)
              .addStatement("this.%1N = %1N", trait.name)
              .build(),
          )
        }

        addProperty(
          PropertySpec.builder("value", composeUiWidgetType)
            .addModifiers(OVERRIDE)
            .initializer(
              CodeBlock.builder()
                .add("{ modifier ->\n")
                .indent()
                .add("this.delegate.invoke(\n")
                .indent()
                .add(delegateArguments.joinToCode(",\n"))
                .add(",\nmodifier,\n")
                .unindent()
                .add(")\n")
                .unindent()
                .add("}\n")
                .build(),
            )
            .build(),
        )
      },
    )
  }
}
