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
package app.cash.redwood.schema.generator

import app.cash.redwood.schema.parser.Children
import app.cash.redwood.schema.parser.Event
import app.cash.redwood.schema.parser.Property
import app.cash.redwood.schema.parser.Schema
import app.cash.redwood.schema.parser.Widget
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.KModifier.OVERRIDE
import com.squareup.kotlinpoet.KModifier.PRIVATE
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName

/*
public class DiffConsumingSunspotWidgetFactory<T : Any>(
  private val delegate: SunspotWidgetFactory<T>,
  private val json: Json = Json.Default,
  private val mismatchHandler: ProtocolMismatchHandler = ProtocolMismatchHandler.Throwing,
) : DiffConsumingWidget.Factory<T> {
  public override fun create(kind: Int): DiffConsumingWidget<T>? = when (kind) {
    1 -> wrap(delegate.SunspotBox())
    2 -> wrap(delegate.SunspotText())
    3 -> wrap(delegate.SunspotButton())
    else -> {
      mismatchHandler.onUnknownWidget(kind)
      null
    }
  }

  public fun wrap(value: SunspotBox<T>): DiffConsumingWidget<T> {
    return ProtocolSunspotBox(delegate.SunspotBox(), json, mismatchHandler)
  }
  etc.
}
*/
internal fun generateDiffConsumingWidgetFactory(schema: Schema): FileSpec {
  val widgetFactory = schema.getWidgetFactoryType().parameterizedBy(typeVariableT)
  val type = schema.diffConsumingWidgetFactoryType()
  return FileSpec.builder(type.packageName, type.simpleName)
    .addType(
      TypeSpec.classBuilder(type)
        .addTypeVariable(typeVariableT)
        .addSuperinterface(DiffConsumingWidgetFactory.parameterizedBy(typeVariableT))
        .primaryConstructor(
          FunSpec.constructorBuilder()
            .addParameter("delegate", widgetFactory)
            .addParameter(
              ParameterSpec.builder("json", json)
                .defaultValue("%T", jsonCompanion)
                .build()
            )
            .addParameter(
              ParameterSpec.builder("mismatchHandler", WidgetProtocolMismatchHandler)
                .defaultValue("%T.Throwing", WidgetProtocolMismatchHandler)
                .build()
            )
            .build()
        )
        .addProperty(
          PropertySpec.builder("delegate", widgetFactory, PRIVATE)
            .initializer("delegate")
            .build()
        )
        .addProperty(
          PropertySpec.builder("json", json, PRIVATE)
            .initializer("json")
            .build()
        )
        .addProperty(
          PropertySpec.builder("mismatchHandler", WidgetProtocolMismatchHandler, PRIVATE)
            .initializer("mismatchHandler")
            .build()
        )
        .addFunction(
          FunSpec.builder("create")
            .addModifiers(OVERRIDE)
            .addParameter("kind", INT)
            .returns(DiffConsumingWidget.parameterizedBy(typeVariableT).copy(nullable = true))
            .beginControlFlow("return when (kind)")
            .apply {
              for (widget in schema.widgets.sortedBy { it.tag }) {
                addStatement("%L -> wrap(delegate.%N())", widget.tag, widget.flatName)
              }
            }
            .beginControlFlow("else ->")
            .addStatement("mismatchHandler.onUnknownWidget(kind)")
            .addStatement("null")
            .endControlFlow()
            .endControlFlow()
            .build()
        )
        .apply {
          for (widget in schema.widgets.sortedBy { it.flatName }) {
            addFunction(
              FunSpec.builder("wrap")
                .addParameter("value", schema.widgetType(widget).parameterizedBy(typeVariableT))
                .returns(DiffConsumingWidget.parameterizedBy(typeVariableT))
                .addStatement("return %T(value, json, mismatchHandler)", schema.diffConsumingWidgetType(widget))
                .build()
            )
          }
        }
        .build()
    )
    .build()
}

/*
internal class DiffConsumingSunspotButton<T : Any>(
  private val delegate: SunspotButton<T>,
  private val json: Json,
  private val mismatchHandler: ProtocolMismatchHandler,
) : DiffConsumingWidget<T> {
  public override val value: T get() = delegate.value

  private val serializer_0: KSerializer<String?> = json.serializersModule.serializer()
  private val serializer_1: KSerializer<Boolean> = json.serializersModule.serializer()

  public override fun apply(diff: PropertyDiff, eventSink: EventSink): Unit {
    when (val tag = diff.tag) {
      1 -> delegate.text(json.decodeFromJsonElement(serializer_0, diff.value))
      2 -> delegate.enabled(json.decodeFromJsonElement(serializer_1, diff.value))
      3 -> {
        val onClick: (() -> Unit)? = if (diff.value.jsonPrimitive.boolean) {
          { eventSink.sendEvent(Event(diff.id, 3)) }
        } else {
          null
        }
        delegate.onClick(onClick)
      }
      else -> mismatchHandler.onUnknownProperty(12, tag)
    }
  }
}
*/
internal fun generateDiffConsumingWidget(schema: Schema, widget: Widget): FileSpec {
  val type = schema.diffConsumingWidgetType(widget)
  val widgetType = schema.widgetType(widget).parameterizedBy(typeVariableT)
  val protocolType = DiffConsumingWidget.parameterizedBy(typeVariableT)
  return FileSpec.builder(type.packageName, type.simpleName)
    .addType(
      TypeSpec.classBuilder(type)
        .addTypeVariable(typeVariableT)
        .addSuperinterface(protocolType)
        .primaryConstructor(
          FunSpec.constructorBuilder()
            .addParameter("delegate", widgetType)
            .addParameter("json", json)
            .addParameter("mismatchHandler", WidgetProtocolMismatchHandler)
            .build()
        )
        .addProperty(
          PropertySpec.builder("delegate", widgetType, PRIVATE)
            .initializer("delegate")
            .build()
        )
        .addProperty(
          PropertySpec.builder("json", json, PRIVATE)
            .initializer("json")
            .build()
        )
        .addProperty(
          PropertySpec.builder("mismatchHandler", WidgetProtocolMismatchHandler, PRIVATE)
            .initializer("mismatchHandler")
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
          var nextSerializerId = 0
          val serializerIds = mutableMapOf<TypeName, Int>()

          addFunction(
            FunSpec.builder("apply")
              .addModifiers(OVERRIDE)
              .addParameter("diff", propertyDiff)
              .addParameter("eventSink", eventSink)
              .beginControlFlow("when (val tag = diff.tag)")
              .apply {
                for (trait in properties) {
                  when (trait) {
                    is Property -> {
                      val propertyType = trait.type.asTypeName()
                      val serializerId = serializerIds.computeIfAbsent(propertyType) {
                        nextSerializerId++
                      }

                      addStatement(
                        "%L -> delegate.%N(json.decodeFromJsonElement(serializer_%L, diff.value))",
                        trait.tag,
                        trait.name,
                        serializerId,
                      )
                    }
                    is Event -> {
                      beginControlFlow("%L ->", trait.tag)
                      beginControlFlow(
                        "val %N: %T = if (diff.value.%M.%M)", trait.name, trait.lambdaType,
                        jsonElementToJsonPrimitive, jsonPrimitiveToBoolean
                      )
                      val parameterType = trait.parameterType?.asTypeName()
                      if (parameterType != null) {
                        val serializerId = serializerIds.computeIfAbsent(parameterType) {
                          nextSerializerId++
                        }
                        addStatement(
                          "{ eventSink.sendEvent(%T(diff.id, %L, json.encodeToJsonElement(serializer_%L, it))) }",
                          eventType,
                          trait.tag,
                          serializerId,
                        )
                      } else {
                        addStatement("{ eventSink.sendEvent(%T(diff.id, %L)) }", eventType, trait.tag,)
                      }
                      nextControlFlow("else")
                      addStatement("null")
                      endControlFlow()
                      addStatement("delegate.%1N(%1N)", trait.name)
                      endControlFlow()
                    }
                    is Children -> throw AssertionError()
                  }
                }

                for ((typeName, id) in serializerIds) {
                  addProperty(
                    PropertySpec.builder("serializer_$id", kSerializer.parameterizedBy(typeName))
                      .addModifiers(PRIVATE)
                      .initializer("json.serializersModule.%M()", serializer)
                      .build()
                  )
                }
              }
              .addStatement("else -> mismatchHandler.onUnknownProperty(%L, tag)", widget.tag)
              .endControlFlow()
              .build()
          )

          addFunction(
            FunSpec.builder("children")
              .addModifiers(OVERRIDE)
              .addParameter("tag", INT)
              .returns(childrenOfT.copy(nullable = true))
              .beginControlFlow("return when (tag)")
              .apply {
                for (children in childrens) {
                  addStatement("%L -> delegate.%N", children.tag, children.name)
                }
              }
              .beginControlFlow("else ->")
              .addStatement("mismatchHandler.onUnknownChildren(%L, tag)", widget.tag)
              .addStatement("null")
              .endControlFlow()
              .endControlFlow()
              .build()
          )
        }
        .build()
    )
    .build()
}
