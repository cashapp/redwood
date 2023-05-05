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

import app.cash.redwood.tooling.codegen.Protocol.ChildrenTag
import app.cash.redwood.tooling.codegen.Protocol.Id
import app.cash.redwood.tooling.codegen.Protocol.ViewTreeBuilder
import app.cash.redwood.tooling.schema.ProtocolWidget
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
import com.squareup.kotlinpoet.KModifier.PUBLIC
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec

/*
@OptIn(RedwoodCodegenApi::class)
public fun SunspotTester(scope: CoroutineScope): RedwoodTester {
  return RedwoodTester(
    scope = scope,
    provider = SunspotWidgetFactories(
      Sunspot = MutableSunspotWidgetFactory(),
      RedwoodLayout = MutableRedwoodLayoutWidgetFactory(),
    ),
  )
}
*/
internal fun generateTester(schemaSet: SchemaSet): FileSpec {
  val schema = schemaSet.schema
  val testerFunction = schema.getTesterFunction()
  return FileSpec.builder(testerFunction.packageName, testerFunction.simpleName)
    .addFunction(
      FunSpec.builder(testerFunction.simpleName)
        .addAnnotation(Redwood.OptInToRedwoodCodegenApi)
        .addParameter("scope", KotlinxCoroutines.CoroutineScope)
        .returns(RedwoodTesting.RedwoodTester)
        .addCode("return %T(⇥\n", RedwoodTesting.RedwoodTester)
        .addCode("scope = scope,\n")
        .addCode("provider = %T(⇥\n", schema.getWidgetFactoriesType())
        .apply {
          for (dependency in schemaSet.all) {
            addCode("%N = %T(),\n", dependency.type.flatName, dependency.getMutableWidgetFactoryType())
          }
        }
        .addCode("⇤),\n")
        .addCode("⇤)\n", RedwoodTesting.RedwoodTester)
        .build(),
    )
    .build()
}

/*
@RedwoodCodegenApi
public class MutableSunspotWidgetFactory : SunspotWidgetFactory<MutableWidget> {
  public override fun SunspotText(): SunspotText<MutableWidget> = MutableSunspotText()

  public override fun SunspotButton(): SunspotButton<MutableWidget> = MutableSunspotButton()
}
*/
internal fun generateMutableWidgetFactory(schema: Schema): FileSpec {
  val mutableWidgetFactoryType = schema.getMutableWidgetFactoryType()
  return FileSpec.builder(mutableWidgetFactoryType)
    .addType(
      TypeSpec.classBuilder(mutableWidgetFactoryType)
        .addSuperinterface(schema.getWidgetFactoryType().parameterizedBy(RedwoodTesting.MutableWidget))
        .addAnnotation(Redwood.RedwoodCodegenApi)
        .apply {
          for (widget in schema.widgets) {
            addFunction(
              FunSpec.builder(widget.type.flatName)
                .addModifiers(PUBLIC, OVERRIDE)
                .returns(schema.widgetType(widget).parameterizedBy(RedwoodTesting.MutableWidget))
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
@RedwoodCodegenApi
internal class MutableSunspotButton : SunspotButton<MutableWidget>, MutableWidget {
  public override val value: MutableWidget
    get() = this

  public override var layoutModifiers: LayoutModifier = LayoutModifier

  private var text: String? = null
  private var enabled: Boolean? = null
  private var maxLength: Int? = null

  public override fun text(text: String?) {
    this.text = text
  }

  public override fun enabled(enabled: Boolean) {
    this.enabled = enabled
  }

  public override fun snapshot() : SunspotButtonValue {
    return SunspotButtonValue(layoutModifiers, text, enabled!!, maxLength!!)
  }
}
*/
internal fun generateMutableWidget(schema: Schema, widget: Widget): FileSpec {
  val mutableWidgetType = schema.mutableWidgetType(widget)
  val widgetValueType = schema.widgetValueType(widget)
  return FileSpec.builder(mutableWidgetType)
    .addType(
      TypeSpec.classBuilder(mutableWidgetType)
        .addModifiers(INTERNAL)
        .addSuperinterface(schema.widgetType(widget).parameterizedBy(RedwoodTesting.MutableWidget))
        .addSuperinterface(RedwoodTesting.MutableWidget)
        .addAnnotation(Redwood.RedwoodCodegenApi)
        .addProperty(
          PropertySpec.builder("value", RedwoodTesting.MutableWidget)
            .addModifiers(PUBLIC, OVERRIDE)
            .getter(
              FunSpec.getterBuilder()
                .addCode("return this")
                .build(),
            )
            .build(),
        )
        .addProperty(
          PropertySpec.builder("layoutModifiers", Redwood.LayoutModifier)
            .addModifiers(PUBLIC, OVERRIDE)
            .mutable(true)
            .initializer("%T", Redwood.LayoutModifier)
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
                    .addModifiers(PUBLIC, OVERRIDE)
                    .addParameter(trait.name, type)
                    .addCode("this.%N = %N", trait.name, trait.name)
                    .build(),
                )
              }
              is Children -> {
                val mutableChildrenOfMutableWidget = RedwoodWidget.MutableListChildren
                  .parameterizedBy(RedwoodTesting.MutableWidget)
                addProperty(
                  PropertySpec.builder(trait.name, mutableChildrenOfMutableWidget)
                    .addModifiers(PUBLIC, OVERRIDE)
                    .initializer("%T()", mutableChildrenOfMutableWidget)
                    .build(),
                )
              }
              is ProtocolTrait -> throw AssertionError()
            }
          }
        }
        .addFunction(
          FunSpec.builder("snapshot")
            .addModifiers(PUBLIC, OVERRIDE)
            .returns(widgetValueType)
            .addCode("return %T(⇥\n", widgetValueType)
            .addCode("layoutModifiers = layoutModifiers,\n")
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
                      addCode("%N = %N,\n", trait.name, trait.name)
                    } else {
                      addCode("%N = %N!!,\n", trait.name, trait.name)
                    }
                  }
                  is Children -> addCode("%N = %N.map { it.`value`.snapshot() },\n", trait.name, trait.name)
                  is ProtocolTrait -> throw AssertionError()
                }
              }
            }
            .addCode("⇤)\n")
            .build(),
        )
        .build(),
    )
    .build()
}

/*
public class SunspotButtonValue(
  public override val layoutModifiers: LayoutModifier = LayoutModifier,
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
    """SunspotButtonValue(text=$text, enabled=$enabled)"""
}
*/
internal fun generateWidgetValue(schema: Schema, widget: Widget): FileSpec {
  val widgetValueType = schema.widgetValueType(widget)

  val classBuilder = TypeSpec.classBuilder(widgetValueType)
    .addModifiers(PUBLIC)
    .addSuperinterface(RedwoodTesting.WidgetValue)
    .addProperty(
      PropertySpec.builder("layoutModifiers", Redwood.LayoutModifier)
        .addModifiers(PUBLIC, OVERRIDE)
        .initializer("layoutModifiers")
        .build(),
    )

  val constructorBuilder = FunSpec.constructorBuilder()
    .addParameter(
      ParameterSpec.builder("layoutModifiers", Redwood.LayoutModifier)
        .defaultValue("%T", Redwood.LayoutModifier)
        .build(),
    )

  val childrenListsBuilder = CodeBlock.builder()
    .add("return %M(⇥\n", Stdlib.listOf)

  val equalsBuilder = CodeBlock.builder()
    .add("return other is %T·&&⇥\n", widgetValueType)
    .add("other.layoutModifiers == layoutModifiers", widgetValueType)
  val hashCodeBuilder = CodeBlock.builder()
    .add("return %M(⇥\n", Stdlib.listOf)
    .add("layoutModifiers,\n")
  val toStringBuilder = StringBuilder()
    .append("${widgetValueType.simpleName}(layoutModifiers=${'$'}layoutModifiers")
  val addToBuilder = CodeBlock.builder()
    .addStatement("val widgetId = %T(builder.nextId++)", Protocol.Id)
    .addStatement("val widgetTag = %T(%L)", Protocol.WidgetTag, (widget as ProtocolWidget).tag)
    .addStatement(
      """
      |val childrenDiff = %T(
      |  parentId,
      |  childrenTag,
      |  widgetId,
      |  widgetTag,
      |  builder.childrenDiffs.size
      |)
      |
      """.trimMargin(),
      Protocol.ChildrenDiff.nestedClass("Insert"),
    )
    .addStatement("builder.childrenDiffs.add(childrenDiff)")

  for (trait in widget.traits) {
    val type = when (trait) {
      is Property -> trait.type.asTypeName()
      is Event -> trait.lambdaType
      is Children -> Stdlib.List.parameterizedBy(RedwoodTesting.WidgetValue)
      else -> throw AssertionError()
    }

    val defaultExpression = when (trait) {
      is Property -> trait.defaultExpression?.let { CodeBlock.of(it) }
      is Event -> trait.defaultExpression?.let { CodeBlock.of(it) }
      is Children -> CodeBlock.of("%M()", Stdlib.listOf)
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

    if (trait is ProtocolWidget.ProtocolProperty) {
      addToBuilder.addStatement(
        "builder.propertyDiffs.add(%T(widgetId, %T(%L), builder.json.%M(this.%N)))",
        Protocol.PropertyDiff,
        Protocol.PropertyTag,
        trait.tag,
        KotlinxSerialization.encodeToJsonElement,
        trait.name,
      )
    }

    when (trait) {
      is Property, is Children -> {
        if (trait is Children) {
          childrenListsBuilder.add("%N,\n", trait.name)
        }

        equalsBuilder.add("·&&\n")
        equalsBuilder.add("other.%N == %N", trait.name, trait.name)

        hashCodeBuilder.add("%N,\n", trait.name)

        toStringBuilder.append(", ${trait.name}=${'$'}${trait.name}")
      }

      is Event -> Unit // Events are omitted from equals/hashCode/toString.

      else -> throw AssertionError()
    }
  }

  childrenListsBuilder.add("⇤)\n")

  equalsBuilder.add("⇤\n")

  hashCodeBuilder.add("⇤).hashCode()\n")

  toStringBuilder.append(")")

  addToBuilder
    .beginControlFlow("for (childrenList in childrenLists)")
    .addStatement("val nextChildrenTag = childrenTag.value + 1")
    .beginControlFlow("for (child in childrenList)")
    .addStatement("child.addTo(widgetId, %T(nextChildrenTag), builder)", Protocol.ChildrenTag)
    .endControlFlow()
    .endControlFlow()

  return FileSpec.builder(widgetValueType)
    .addType(
      classBuilder
        .primaryConstructor(constructorBuilder.build())
        .addProperty(
          PropertySpec.builder(
            "childrenLists",
            LIST.parameterizedBy(LIST.parameterizedBy(RedwoodTesting.WidgetValue)),
          )
            .addModifiers(PUBLIC, OVERRIDE)
            .getter(
              FunSpec.getterBuilder()
                .addCode(childrenListsBuilder.build())
                .build(),
            )
            .build(),
        )
        .addFunction(
          FunSpec.builder("equals")
            .addModifiers(PUBLIC, OVERRIDE)
            .addParameter("other", ANY.copy(nullable = true))
            .returns(BOOLEAN)
            .addCode(equalsBuilder.build())
            .build(),
        )
        .addFunction(
          FunSpec.builder("hashCode")
            .addModifiers(PUBLIC, OVERRIDE)
            .returns(Int::class)
            .addCode(hashCodeBuilder.build())
            .build(),
        )
        .addFunction(
          FunSpec.builder("toString")
            .addModifiers(PUBLIC, OVERRIDE)
            .returns(String::class)
            .addCode("return %P", toStringBuilder.toString())
            .build(),
        )
        .addFunction(
          FunSpec.builder("addTo")
            .addModifiers(PUBLIC, OVERRIDE)
            .addParameter("parentId", Id)
            .addParameter("childrenTag", ChildrenTag)
            .addParameter("builder", ViewTreeBuilder)
            .addCode(addToBuilder.build())
            .build(),
        )
        .build(),
    )
    .build()
}
