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
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.INTERNAL
import com.squareup.kotlinpoet.KModifier.OVERRIDE
import com.squareup.kotlinpoet.KModifier.PRIVATE
import com.squareup.kotlinpoet.NOTHING
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName

/*
public fun ProtocolSunspotComposition(
  scope: CoroutineScope,
  onDiff: DiffSink = DiffSink {},
  onEvent: EventSink = EventSink {},
): TreehouseComposition {
  return TreehouseComposition(scope, ProtocolWidgetFactory(), onDiff, onEvent)
}

private class ProtocolWidgetFactory : SunspotWidgetFactory<Nothing> {
  override fun SunspotBox(): SunspotBox<Nothing> = ProtocolSunspotBox()
  override fun SunspotText(): SunspotText<Nothing> = ProtocolSunspotText()
  override fun SunspotButton(): SunspotButton<Nothing> = ProtocolSunspotButton()
}
*/
internal fun generateComposeProtocolWidgetFactory(schema: Schema): FileSpec {
  val factory = ClassName(schema.composePackage, "Protocol${schema.name}WidgetFactory")
  return FileSpec.builder(schema.composePackage, "composition")
    .addFunction(
      FunSpec.builder("Protocol${schema.name}Composition")
        .addParameter("scope", coroutineScope)
        .addParameter(
          ParameterSpec.builder("onDiff", diffSink)
            .defaultValue("%T {}", diffSink)
            .build()
        )
        .addParameter(
          ParameterSpec.builder("onEvent", eventSink)
            .defaultValue("%T {}", eventSink)
            .build()
        )
        .returns(treehouseComposition)
        .addStatement("return %T(scope, %T(), onDiff, onEvent)", treehouseComposition, factory)
        .build()
    )
    .addType(
      TypeSpec.classBuilder(factory)
        .addModifiers(PRIVATE)
        .addSuperinterface(schema.getWidgetFactoryType().parameterizedBy(NOTHING))
        .apply {
          for (widget in schema.widgets) {
            val protocolWidgetName = schema.composeProtocolWidgetType(widget)
            addFunction(
              FunSpec.builder(widget.flatName)
                .addModifiers(OVERRIDE)
                .returns(schema.widgetType(widget).parameterizedBy(NOTHING))
                .addStatement("return %T()", protocolWidgetName)
                .build()
            )
          }
        }
        .build()
    )
    .build()
}

/*
internal class ProtocolSunspotButton : ProtocolNode(3), SunspotButton<Nothing> {
  override val value: Nothing get() = throw AssertionError()

  private var onClick: (() -> Unit)? = null

  override fun text(text: String?) {
    appendDiff(PropertyDiff(this.id, 1, text))
  }

  override fun onClick(onClick: (() -> Unit)?) {
    val onClickSet = onClick != null
    if (onClickSet != (this.onClick != null)) {
      appendDiff(PropertyDiff(this.id, 3, onClickSet))
    }
    this.onClick = onClick
  }

  override fun sendEvent(event: Event) {
    when (val tag = event.tag) {
      3 -> onClick?.invoke()
      else -> throw IllegalArgumentException("Unknown tag $tag")
    }
  }
}
*/
internal fun generateComposeProtocolWidget(schema: Schema, widget: Widget): FileSpec {
  val protocolWidgetName = schema.composeProtocolWidgetType(widget)
  val widgetName = schema.widgetType(widget)
  return FileSpec.builder(protocolWidgetName.packageName, protocolWidgetName.simpleName)
    .addType(
      TypeSpec.classBuilder(protocolWidgetName)
        .addModifiers(INTERNAL)
        .superclass(protocolNode)
        .addSuperclassConstructorParameter("%L", widget.tag)
        .addSuperinterface(widgetName.parameterizedBy(NOTHING))
        .addProperty(
          PropertySpec.builder("value", NOTHING, OVERRIDE)
            .getter(
              FunSpec.getterBuilder()
                .addStatement("throw %T()", ae)
                .build()
            )
            .build()
        )
        .apply {
          var hasEvents = false
          for (trait in widget.traits) {
            @Exhaustive when (trait) {
              is Property -> {
                addFunction(
                  FunSpec.builder(trait.name)
                    .addModifiers(OVERRIDE)
                    .addParameter(trait.name, trait.type.asTypeName())
                    .addStatement("appendDiff(%T(this.id, %L, %N))", propertyDiff, trait.tag, trait.name)
                    .build()
                )
              }
              is Event -> {
                hasEvents = true

                addProperty(
                  PropertySpec.builder(trait.name, trait.lambdaType, PRIVATE)
                    .mutable(true)
                    .initializer("null")
                    .build()
                )
                addFunction(
                  FunSpec.builder(trait.name)
                    .addModifiers(OVERRIDE)
                    .addParameter(trait.name, trait.lambdaType)
                    .addStatement("val %1NSet = %1N != null", trait.name)
                    .beginControlFlow("if (%1NSet != (this.%1N != null))", trait.name)
                    .addStatement("appendDiff(%T(this.id, %L, %NSet))", propertyDiff, trait.tag, trait.name)
                    .endControlFlow()
                    .addStatement("this.%1N = %1N", trait.name)
                    .build()
                )
              }
              is Children -> {
                addProperty(
                  PropertySpec.builder(trait.name, NOTHING, OVERRIDE)
                    .getter(
                      FunSpec.getterBuilder()
                        .addStatement("throw %T()", ae)
                        .build()
                    )
                    .build()
                )
              }
            }
          }

          if (hasEvents) {
            addFunction(
              FunSpec.builder("sendEvent")
                .addModifiers(OVERRIDE)
                .addParameter("event", eventType)
                .beginControlFlow("when (val tag = event.tag)")
                .apply {
                  for (event in widget.traits.filterIsInstance<Event>()) {
                    if (event.parameterType != null) {
                      addStatement(
                        "%L -> %N?.invoke(event.value as %T)", event.tag, event.name,
                        event.parameterType!!.asTypeName()
                      )
                    } else {
                      addStatement("%L -> %N?.invoke()", event.tag, event.name)
                    }
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
