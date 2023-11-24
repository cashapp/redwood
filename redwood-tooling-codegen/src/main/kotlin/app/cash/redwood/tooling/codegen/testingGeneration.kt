/*
 * Copyright (C) 2023 Square, Inc.
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
import app.cash.redwood.tooling.schema.SchemaSet
import app.cash.redwood.tooling.schema.Widget
import app.cash.redwood.tooling.schema.Widget.Children
import app.cash.redwood.tooling.schema.Widget.Event
import app.cash.redwood.tooling.schema.Widget.Property
import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.INTERNAL
import com.squareup.kotlinpoet.KModifier.OVERRIDE
import com.squareup.kotlinpoet.KModifier.PRIVATE
import com.squareup.kotlinpoet.KModifier.SUSPEND
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.joinToCode

/*
suspend fun <R> ExampleTester(
  savedState: TestSavedState? = null,
  uiConfiguration: UiConfiguration = UiConfiguration(),
  body: suspend TestRedwoodComposition<List<WidgetValue>>.() -> R,
): R = coroutineScope {
  val factories = ExampleWidgetFactories(
    TestSchema = TestSchemaTestingWidgetFactory(),
    RedwoodLayout = RedwoodLayoutTestingWidgetFactory(),
  )
  val container = MutableListChildren<WidgetValue>()
  val tester = TestRedwoodComposition(this, factories, container, savedState, uiConfiguration) {
    container.map { it.value }
  }
  try {
    tester.body()
  } finally {
    tester.cancel()
  }
}
*/
internal fun generateTester(schemaSet: SchemaSet): FileSpec {
  val schema = schemaSet.schema
  val testerFunction = schema.getTesterFunction()
  val typeVarR = TypeVariableName("R")
  val bodyType = LambdaTypeName.get(
    receiver = RedwoodTesting.TestRedwoodComposition
      .parameterizedBy(LIST.parameterizedBy(RedwoodTesting.WidgetValue)),
    returnType = typeVarR,
  ).copy(suspending = true)
  return FileSpec.builder(testerFunction.packageName, testerFunction.simpleName)
    .addAnnotation(suppressDeprecations)
    .addFunction(
      FunSpec.builder(testerFunction)
        .optIn(Redwood.RedwoodCodegenApi)
        .addModifiers(SUSPEND)
        .addParameter(
          ParameterSpec.builder("onBackPressedDispatcher", Redwood.OnBackPressedDispatcher)
            .defaultValue("%T", RedwoodTesting.NoOpOnBackPressedDispatcher)
            .build(),
        )
        .addParameter(
          ParameterSpec.builder("savedState", RedwoodTesting.TestSavedState.copy(nullable = true))
            .defaultValue("null")
            .build(),
        )
        .addParameter(
          ParameterSpec.builder("uiConfiguration", Redwood.UiConfiguration)
            .defaultValue("%T()", Redwood.UiConfiguration)
            .build(),
        )
        .addParameter("body", bodyType)
        .addTypeVariable(typeVarR)
        .returns(typeVarR)
        .beginControlFlow("return %M", KotlinxCoroutines.coroutineScope)
        .addCode("val factories = %T(⇥\n", schema.getWidgetFactoriesType())
        .apply {
          for (dependency in schemaSet.all) {
            addCode("%N = %T(),\n", dependency.type.flatName, dependency.getTestingWidgetFactoryType())
          }
        }
        .addCode("⇤)\n")
        .addStatement("val container = %T<%T>()", RedwoodWidget.MutableListChildren, RedwoodTesting.WidgetValue)
        .beginControlFlow("val tester = %T(this, factories, container, onBackPressedDispatcher, savedState, uiConfiguration)", RedwoodTesting.TestRedwoodComposition)
        .addStatement("container.map { it.value }")
        .endControlFlow()
        .beginControlFlow("try")
        .addStatement("tester.body()")
        .nextControlFlow("finally")
        .addStatement("tester.cancel()")
        .endControlFlow()
        .endControlFlow()
        .build(),
    )
    .build()
}

/*
@RedwoodCodegenApi
public class EmojiSearchTestingWidgetFactory : EmojiSearchWidgetFactory<WidgetValue> {
  public override fun Text(): Text<WidgetValue> = MutableText()
  public override fun Button(): Button<WidgetValue> = MutableButton()
}
*/
internal fun generateMutableWidgetFactory(schema: Schema): FileSpec {
  val mutableWidgetFactoryType = schema.getTestingWidgetFactoryType()
  return FileSpec.builder(mutableWidgetFactoryType)
    .addAnnotation(suppressDeprecations)
    .addType(
      TypeSpec.classBuilder(mutableWidgetFactoryType)
        .addSuperinterface(schema.getWidgetFactoryType().parameterizedBy(RedwoodTesting.WidgetValue))
        .addAnnotation(Redwood.RedwoodCodegenApi)
        .apply {
          for (widget in schema.widgets) {
            addFunction(
              FunSpec.builder(widget.type.flatName)
                .addModifiers(OVERRIDE)
                .returns(schema.widgetType(widget).parameterizedBy(RedwoodTesting.WidgetValue))
                .addCode("return %T()", schema.mutableWidgetType(widget))
                .build(),
            )
          }
        }
        .build(),
    )
    .build()
}

/*
internal class MutableButton : Button<WidgetValue> {
  public override val value: WidgetValue
    get() = ButtonValue(modifier, text, enabled!!, maxLength!!)

  public override var modifier: Modifier = Modifier

  private var text: String? = null
  private var enabled: Boolean? = null
  private var maxLength: Int? = null

  public override fun text(text: String?) {
    this.text = text
  }

  public override fun enabled(enabled: Boolean) {
    this.enabled = enabled
  }
}
*/
internal fun generateMutableWidget(schema: Schema, widget: Widget): FileSpec {
  val mutableWidgetType = schema.mutableWidgetType(widget)
  val widgetValueType = schema.widgetValueType(widget)
  return FileSpec.builder(mutableWidgetType)
    .addAnnotation(suppressDeprecations)
    .addType(
      TypeSpec.classBuilder(mutableWidgetType)
        .addModifiers(INTERNAL)
        .addSuperinterface(schema.widgetType(widget).parameterizedBy(RedwoodTesting.WidgetValue))
        .addProperty(
          PropertySpec.builder("value", RedwoodTesting.WidgetValue)
            .addModifiers(OVERRIDE)
            .getter(
              FunSpec.getterBuilder()
                .addCode("return %T(⇥\n", widgetValueType)
                .addCode("modifier = modifier,\n")
                .apply {
                  for (trait in widget.traits) {
                    when (trait) {
                      is Event, is Property -> {
                        val nullable = when (trait) {
                          is Property -> trait.type.nullable
                          is Event -> trait.lambdaType.isNullable
                          else -> false
                        }
                        if (nullable) {
                          addCode("%1N = %1N,\n", trait.name)
                        } else {
                          addCode("%1N = %1N!!,\n", trait.name)
                        }
                      }
                      is Children -> addCode("%1N = %1N.map { it.`value` },\n", trait.name)
                      is ProtocolTrait -> throw AssertionError()
                    }
                  }
                }
                .addCode("⇤)\n")
                .build(),
            )
            .build(),
        )
        .addProperty(
          PropertySpec.builder("modifier", Redwood.Modifier)
            .addModifiers(OVERRIDE)
            .mutable(true)
            .initializer("%T", Redwood.Modifier)
            .build(),
        )
        .apply {
          for (trait in widget.traits) {
            when (trait) {
              is Property, is Event -> {
                val type = when (trait) {
                  is Property -> trait.type.asTypeName()
                  is Event -> trait.lambdaType
                  else -> throw AssertionError()
                }
                addProperty(
                  PropertySpec.builder(trait.name, type.copy(nullable = true))
                    .addModifiers(PRIVATE)
                    .initializer("null")
                    .mutable(true)
                    .build(),
                )
                addFunction(
                  FunSpec.builder(trait.name)
                    .addModifiers(OVERRIDE)
                    .addParameter(trait.name, type)
                    .addCode("this.%N = %N", trait.name, trait.name)
                    .build(),
                )
              }
              is Children -> {
                val mutableChildrenOfMutableWidget = RedwoodWidget.MutableListChildren
                  .parameterizedBy(RedwoodTesting.WidgetValue)
                addProperty(
                  PropertySpec.builder(trait.name, mutableChildrenOfMutableWidget)
                    .addModifiers(OVERRIDE)
                    .initializer("%T()", RedwoodWidget.MutableListChildren)
                    .build(),
                )
              }
              is ProtocolTrait -> throw AssertionError()
            }
          }
        }
        .build(),
    )
    .build()
}

/*
public class ButtonValue(
  public override val modifier: Modifier = Modifier,
  public val text: String? = null,
  public val enabled: Boolean = false,
) : WidgetValue {
  public override val childrenLists: List<List<WidgetValue>>
    get() = listOf()

  public override fun equals(other: Any?): Boolean = other is SunspotButtonValue &&
    other.text == text &&
    other.enabled == enabled

  public override fun hashCode(): Int = listOf(
    text,
    enabled,
  ).hashCode()

  public override fun toString(): String =
    """ButtonValue(text=$text, enabled=$enabled)"""
}
*/
internal fun generateWidgetValue(schema: Schema, widget: Widget): FileSpec {
  val widgetValueType = schema.widgetValueType(widget)

  val classBuilder = TypeSpec.classBuilder(widgetValueType)
    .addSuperinterface(RedwoodTesting.WidgetValue)
    .addProperty(
      PropertySpec.builder("modifier", Redwood.Modifier)
        .addModifiers(OVERRIDE)
        .initializer("modifier")
        .build(),
    )

  val constructorBuilder = FunSpec.constructorBuilder()
    .addParameter(
      ParameterSpec.builder("modifier", Redwood.Modifier)
        .defaultValue("%T", Redwood.Modifier)
        .build(),
    )

  val childrenLists = mutableListOf<CodeBlock>()
  val equalsComparisons = mutableListOf(
    CodeBlock.of("other is %T", widgetValueType),
    CodeBlock.of("other.modifier == modifier"),
  )
  val hashCodeProperties = mutableListOf(CodeBlock.of("modifier"))
  val toStringProperties = mutableListOf("modifier=\$modifier")

  fun addEqualsHashCodeToString(trait: Widget.Trait) {
    equalsComparisons += CodeBlock.of("other.%1N == %1N", trait.name)
    hashCodeProperties += CodeBlock.of("%N", trait.name)
    toStringProperties += "${trait.name}=\$${trait.name}"
  }

  val toWidgetChildrenBuilder = CodeBlock.builder()
  val toWidgetPropertiesBuilder = CodeBlock.builder()

  for (trait in widget.traits) {
    val type: TypeName
    val defaultExpression: CodeBlock?
    when (trait) {
      is Property -> {
        type = trait.type.asTypeName()
        defaultExpression = trait.defaultExpression?.let { CodeBlock.of(it) }
        addEqualsHashCodeToString(trait)

        toWidgetPropertiesBuilder.addStatement("instance.%1N(%1N)", trait.name)
      }
      is Children -> {
        type = Stdlib.List.parameterizedBy(RedwoodTesting.WidgetValue)
        defaultExpression = CodeBlock.of("%M()", Stdlib.listOf)
        addEqualsHashCodeToString(trait)

        childrenLists += CodeBlock.of("%N", trait.name)

        toWidgetChildrenBuilder.beginControlFlow("for ((index, child) in %N.withIndex())", trait.name)
          .addStatement("instance.%N.insert(index, child.toWidget(provider))", trait.name)
          .endControlFlow()
      }
      is Event -> {
        type = trait.lambdaType
        defaultExpression = trait.defaultExpression?.let { CodeBlock.of(it) }
        // Events are omitted from equals/hashCode/toString.
      }

      else -> throw AssertionError()
    }

    constructorBuilder.addParameter(
      ParameterSpec.builder(trait.name, type)
        .defaultValue(defaultExpression)
        .build(),
    )

    classBuilder.addProperty(
      PropertySpec.builder(trait.name, type)
        .initializer("%N", trait.name)
        .build(),
    )
  }

  return FileSpec.builder(widgetValueType)
    .addAnnotation(suppressDeprecations)
    .addType(
      classBuilder
        .primaryConstructor(constructorBuilder.build())
        .addProperty(
          PropertySpec.builder(
            "childrenLists",
            LIST.parameterizedBy(LIST.parameterizedBy(RedwoodTesting.WidgetValue)),
          )
            .addModifiers(OVERRIDE)
            .getter(
              FunSpec.getterBuilder()
                .addStatement("return %M(%L)", Stdlib.listOf, childrenLists.joinToCode())
                .build(),
            )
            .build(),
        )
        .addFunction(
          FunSpec.builder("equals")
            .addModifiers(OVERRIDE)
            .addParameter("other", ANY.copy(nullable = true))
            .returns(BOOLEAN)
            .addStatement("return %L", equalsComparisons.joinToCode(" &&\n"))
            .build(),
        )
        .addFunction(
          FunSpec.builder("hashCode")
            .addModifiers(OVERRIDE)
            .returns(Int::class)
            .addStatement("return %M(%L).hashCode()", Stdlib.listOf, hashCodeProperties.joinToCode())
            .build(),
        )
        .addFunction(
          FunSpec.builder("toString")
            .addModifiers(OVERRIDE)
            .returns(String::class)
            .addStatement(
              "return %P",
              toStringProperties.joinToString(
                prefix = "${widgetValueType.simpleName}(",
                postfix = ")",
              ),
            )
            .build(),
        )
        .addFunction(
          FunSpec.builder("toWidget")
            .addModifiers(OVERRIDE)
            .addTypeVariable(typeVariableW)
            .addParameter("provider", RedwoodWidget.WidgetProvider.parameterizedBy(typeVariableW))
            .returns(RedwoodWidget.Widget.parameterizedBy(typeVariableW))
            .addStatement("val factory = provider as %T", schema.getWidgetFactoryProviderType().parameterizedBy(typeVariableW))
            .addStatement("val instance = factory.%L.%L()", schema.type.flatName, widget.type.flatName)
            .addStatement("")
            .addStatement("instance.modifier = modifier")
            .addCode(toWidgetPropertiesBuilder.build())
            .addStatement("")
            .addCode(toWidgetChildrenBuilder.build())
            .addStatement("")
            .addStatement("return instance")
            .build(),
        )
        .build(),
    )
    .build()
}
