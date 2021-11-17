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

import app.cash.exhaustive.Exhaustive
import app.cash.treehouse.schema.parser.Children
import app.cash.treehouse.schema.parser.Event
import app.cash.treehouse.schema.parser.Property
import app.cash.treehouse.schema.parser.Schema
import app.cash.treehouse.schema.parser.Widget
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.KModifier.OVERRIDE
import com.squareup.kotlinpoet.KModifier.PRIVATE
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName

/*
public class ProtocolWidgetFactory<T : Any>(
  private val delegate: SunspotWidgetFactory<T>
) : ProtocolWidget.Factory<T> {
  public override fun create(kind: Int): ProtocolWidget<T> = when (kind) {
    1 -> ProtocolSunspotBox(delegate.SunspotBox())
    2 -> ProtocolSunspotText(delegate.SunspotText())
    3 -> ProtocolSunspotButton(delegate.SunspotButton())
    else -> throw IllegalArgumentException("Unknown kind $kind")
  }
}
*/
internal fun generateDisplayProtocolWidgetFactory(schema: Schema): FileSpec {
  val widgetFactory = schema.getWidgetFactoryType().parameterizedBy(typeVariableT)
  return FileSpec.builder(schema.displayPackage, "ProtocolWidgetFactory")
    .addType(
      TypeSpec.classBuilder("ProtocolWidgetFactory")
        .addTypeVariable(typeVariableT)
        .addSuperinterface(protocolWidgetFactory.parameterizedBy(typeVariableT))
        .primaryConstructor(
          FunSpec.constructorBuilder()
            .addParameter("delegate", widgetFactory)
            .build()
        )
        .addProperty(
          PropertySpec.builder("delegate", widgetFactory, PRIVATE)
            .initializer("delegate")
            .build()
        )
        .addFunction(
          FunSpec.builder("create")
            .addModifiers(OVERRIDE)
            .addParameter("kind", INT)
            .returns(protocolWidget.parameterizedBy(typeVariableT))
            .beginControlFlow("return when (kind)")
            .apply {
              for (widget in schema.widgets.sortedBy { it.tag }) {
                val protocolType = schema.displayProtocolWidgetType(widget)
                addStatement("%L -> %T(delegate.%N())", widget.tag, protocolType, widget.flatName)
              }
            }
            .addStatement("else -> throw %T(\"Unknown kind \$kind\")", iae)
            .endControlFlow()
            .build()
        )
        .build()
    )
    .build()
}

/*
public class ProtocolSunspotButton<T : Any>(
  private val delegate: SunspotButton<T>
) : ProtocolWidget<T> {
  public override val value: T
    get() = delegate.value

  public override fun apply(diff: PropertyDiff, eventSink: EventSink): Unit {
    when (val tag = diff.tag) {
      1 -> delegate.text(diff.value as String?)
      2 -> delegate.enabled(diff.value as Boolean)
      3 -> {
        val onClick: (() -> Unit)? = if (diff.value as Boolean) {
          { eventSink.sendEvent(Event(diff.id, 3, null)) }
        } else {
          null
        }
        delegate.onClick(onClick)
      }
      else -> throw IllegalArgumentException("Unknown tag $tag")
    }
  }
}
*/
internal fun generateDisplayProtocolWidget(schema: Schema, widget: Widget): FileSpec {
  val type = schema.displayProtocolWidgetType(widget)
  val widgetType = schema.widgetType(widget).parameterizedBy(typeVariableT)
  val protocolType = protocolWidget.parameterizedBy(typeVariableT)
  return FileSpec.builder(type.packageName, type.simpleName)
    .addType(
      TypeSpec.classBuilder(type)
        .addTypeVariable(typeVariableT)
        .addSuperinterface(protocolType)
        .primaryConstructor(
          FunSpec.constructorBuilder()
            .addParameter("delegate", widgetType)
            .build()
        )
        .addProperty(
          PropertySpec.builder("delegate", widgetType, PRIVATE)
            .initializer("delegate")
            .build()
        )
        .addProperty(
          PropertySpec.builder("value", typeVariableT, OVERRIDE)
            .getter(
              FunSpec.getterBuilder()
                .addStatement("return delegate.value")
                .build()
            )
            .build()
        )
        .apply {
          val (childrens, properties) = widget.traits.partition { it is Children }

          if (properties.isNotEmpty()) {
            addFunction(
              FunSpec.builder("apply")
                .addModifiers(OVERRIDE)
                .addParameter("diff", propertyDiff)
                .addParameter("eventSink", eventSink)
                .beginControlFlow("when (val tag = diff.tag)")
                .apply {
                  for (trait in widget.traits) {
                    @Exhaustive when (trait) {
                      is Property -> {
                        addStatement(
                          "%L -> delegate.%N(diff.value as %T)", trait.tag, trait.name,
                          trait.type.asTypeName()
                        )
                      }
                      is Event -> {
                        beginControlFlow("%L ->", trait.tag)
                        beginControlFlow(
                          "val %N: %T = if (diff.value as %T)", trait.name, trait.lambdaType,
                          BOOLEAN
                        )
                        addStatement(
                          "{ eventSink.sendEvent(%T(diff.id, %L, %L)) }", eventType, trait.tag,
                          if (trait.parameterType != null) "it" else "null"
                        )
                        nextControlFlow("else")
                        addStatement("null")
                        endControlFlow()
                        addStatement("delegate.%1N(%1N)", trait.name)
                        endControlFlow()
                      }
                      is Children -> throw AssertionError()
                    }
                  }
                }
                .addStatement("else -> throw %T(\"Unknown tag \$tag\")", iae)
                .endControlFlow()
                .build()
            )
          }

          if (childrens.isNotEmpty()) {
            addFunction(
              FunSpec.builder("children")
                .addModifiers(OVERRIDE)
                .addParameter("tag", INT)
                .returns(childrenOfT)
                .beginControlFlow("return when (tag)")
                .apply {
                  for (children in childrens) {
                    addStatement("%L -> delegate.%N", children.tag, children.name)
                  }
                }
                .addStatement("else -> throw %T(\"Unknown tag \$tag\")", iae)
                .endControlFlow()
                .build()
            )
          }
        }
        .build()
    )
    .build()
}
