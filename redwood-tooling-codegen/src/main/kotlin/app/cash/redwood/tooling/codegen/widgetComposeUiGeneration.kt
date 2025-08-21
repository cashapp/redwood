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
import com.squareup.kotlinpoet.KModifier.ABSTRACT
import com.squareup.kotlinpoet.KModifier.INTERNAL
import com.squareup.kotlinpoet.KModifier.OVERRIDE
import com.squareup.kotlinpoet.KModifier.PRIVATE
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.MemberName
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

private fun Schema.composeUiWidgetFactoryType(): ClassName {
  return ClassName(composeUiPackage(), "AbstractComposeUi" + getWidgetFactoryType().simpleName)
}

private fun Schema.composeUiWidgetType(widget: Widget): ClassName {
  return ClassName(composeUiPackage(), "ComposeUi" + widget.type.flatName)
}

private fun Widget.factoryFunction() = type.flatName + "Binding"

/*
abstract class AbstractComposeUiExampleWidgetFactory : ExampleWidgetFactory<@Composable (Modifier) -> Unit> {
  @Composable
  abstract fun TextBinding(text: String, modifier: Modifier)

  @Composable
  abstract fun ImageBinding(url: String, modifier: Modifier)

  override fun Text(): Text<@Composable (Modifier) -> Unit> = ComposeUiText(this)
  override fun Image(): Image<@Composable (Modifier) -> Unit> = ComposeUiImage(this)
}
*/
internal fun generateComposeUiWidgetFactory(schema: Schema): FileSpec {
  val widgetFactoryType = schema.getWidgetFactoryType()
  val thisType = schema.composeUiWidgetFactoryType()
  return buildFileSpec(thisType) {
    addAnnotation(suppressDeprecations)
    addType(
      buildClassSpec(thisType) {
        addModifiers(ABSTRACT)
        addSuperinterface(widgetFactoryType.parameterizedBy(composeUiWidgetType))

        for (widget in schema.widgets) {
          addFunction(
            FunSpec.builder(widget.factoryFunction())
              .addModifiers(ABSTRACT)
              .addAnnotation(ComposeRuntime.Composable)
              .apply {
                for (trait in widget.traits) {
                  val type = when (trait) {
                    is Property -> trait.type.asTypeName()
                    is Event -> trait.lambdaType
                    is Children -> RedwoodWidgetComposeUi.ComposeWidgetChildren
                    is ProtocolTrait -> throw AssertionError()
                  }
                  addParameter(trait.name, type)
                }
              }
              .addParameter("modifier", ComposeUi.Modifier)
              .build(),
          )
        }

        // Put the interface implementations at the bottom.
        for (widget in schema.widgets) {
          addFunction(
            FunSpec.builder(widget.type.flatName)
              .returns(schema.widgetType(widget).parameterizedBy(composeUiWidgetType))
              .addModifiers(OVERRIDE)
              .addStatement("return %T(this)", schema.composeUiWidgetType(widget))
              .build(),
          )
        }

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

/*
internal class ComposeUiText(
  private val factory: AbstractComposeUiExampleWidgetFactory,
) : Text<@Composable (UiModifier) -> Unit> {
  override var modifier: RedwoodModifier = RedwoodModifier

  private var text: String? by mutableStateOf(null)

  override val value: @Composable (UiModifier) -> Unit = { modifier ->
    factory.TextBinding(
      text as String,
      modifier,
    )
  }

  override fun text(text: String) {
    this.text = text
  }
}
*/
internal fun generateComposeUiBinding(schema: Schema, widget: Widget): FileSpec {
  val widgetType = schema.widgetType(widget)
  val widgetFactoryType = schema.composeUiWidgetFactoryType()
  val thisType = schema.composeUiWidgetType(widget)
  return buildFileSpec(thisType) {
    addAnnotation(suppressDeprecations)
    addType(
      buildClassSpec(thisType) {
        addSuperinterface(widgetType.parameterizedBy(composeUiWidgetType))
        addModifiers(INTERNAL)

        primaryConstructor(
          FunSpec.constructorBuilder()
            .addParameter("factory", widgetFactoryType)
            .build(),
        )
        addProperty(
          PropertySpec.builder("factory", widgetFactoryType)
            .addModifiers(PRIVATE)
            .initializer("factory")
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
          if (trait is Children) {
            val backingPropertyName = "_" + trait.name
            delegateArguments += CodeBlock.of("%N", backingPropertyName)

            addProperty(
              PropertySpec.builder(backingPropertyName, RedwoodWidgetComposeUi.ComposeWidgetChildren)
                .addModifiers(PRIVATE)
                .initializer("%T()", RedwoodWidgetComposeUi.ComposeWidgetChildren)
                .build(),
            )
            addProperty(
              PropertySpec.builder(trait.name, RedwoodWidget.WidgetChildren.parameterizedBy(composeUiWidgetType))
                .addModifiers(OVERRIDE)
                .getter(
                  FunSpec.getterBuilder()
                    .addStatement("return %N", backingPropertyName)
                    .build(),
                )
                .build(),
            )
          } else {
            val traitType = when (trait) {
              is Property -> trait.type.asTypeName()
              is Event -> trait.lambdaType
              is Children -> throw AssertionError()
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
        }

        addProperty(
          PropertySpec.builder("value", composeUiWidgetType)
            .addModifiers(OVERRIDE)
            .initializer(
              CodeBlock.builder()
                .add("{ modifier ->\n")
                .indent()
                .add("this.factory.%N(\n", widget.factoryFunction())
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
