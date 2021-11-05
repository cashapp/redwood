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
public class ProtocolDisplayWidgetFactory<T : Any>(
  private val delegate: SunspotWidgetFactory<T>,
  private val serializersModule: SerializersModule = SerializersModule { }
) : ProtocolWidget.Factory<T> {
  public override fun create(kind: Int): ProtocolWidget<T> = when (kind) {
    1 -> wrap(delegate.SunspotBox())
    2 -> wrap(delegate.SunspotText())
    3 -> wrap(delegate.SunspotButton())
    else -> throw IllegalArgumentException("Unknown kind $kind")
  }

  public fun wrap(value: SunspotBox): ProtocolSunspotBox {
    return ProtocolSunspotBox(delegate.SunspotBox(), serializersModule)
  }
  etc.
}
*/
internal fun generateDisplayProtocolWidgetFactory(schema: Schema): FileSpec {
  val widgetFactory = schema.getWidgetFactoryType().parameterizedBy(typeVariableT)
  return FileSpec.builder(schema.displayPackage, "ProtocolDisplayWidgetFactory")
    .addType(
      TypeSpec.classBuilder("ProtocolDisplayWidgetFactory")
        .addTypeVariable(typeVariableT)
        .addSuperinterface(protocolWidgetFactory.parameterizedBy(typeVariableT))
        .primaryConstructor(
          FunSpec.constructorBuilder()
            .addParameter("delegate", widgetFactory)
            .addParameter(
              ParameterSpec.builder("serializersModule", serializersModule)
                // TODO Use EmptySerializersModule once stable
                //  https://github.com/Kotlin/kotlinx.serialization/issues/1765
                .defaultValue("%T { }", serializersModule)
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
          PropertySpec.builder("serializersModule", serializersModule, PRIVATE)
            .initializer("serializersModule")
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
                addStatement("%L -> wrap(delegate.%N())", widget.tag, widget.flatName)
              }
            }
            .addStatement("else -> throw %T(\"Unknown kind \$kind\")", iae)
            .endControlFlow()
            .build()
        )
        .apply {
          for (widget in schema.widgets.sortedBy { it.flatName }) {
            val protocolWidgetType = schema.displayProtocolWidgetType(widget)
            addFunction(
              FunSpec.builder("wrap")
                .addParameter("value", schema.widgetType(widget).parameterizedBy(typeVariableT))
                .returns(protocolWidgetType.parameterizedBy(typeVariableT))
                .addStatement("return %T(value, serializersModule)", protocolWidgetType)
                .build()
            )
          }
        }
        .build()
    )
    .build()
}

/*
public class ProtocolSunspotButton<T : Any>(
  private val delegate: SunspotButton<T>,
  serializersModule: SerializersModule,
) : ProtocolWidget<T> {
  public override val value: T get() = delegate.value

  private val serializer_0: KSerializer<String?> = serializersModule.serializer()
  private val serializer_1: KSerializer<Boolean> = serializersModule.serializer()

  public override fun apply(diff: PropertyDiff, eventSink: EventSink): Unit {
    when (val tag = diff.tag) {
      1 -> delegate.text(Json.decodeFromJsonElement(serializer_0, diff.value))
      2 -> delegate.enabled(Json.decodeFromJsonElement(serializer_1, diff.value))
      3 -> {
        val onClick: (() -> Unit)? = if (diff.value.jsonPrimitive.boolean) {
          { eventSink.sendEvent(Event(diff.id, 3)) }
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
            .addParameter("serializersModule", serializersModule)
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
          var nextSerializerId = 0
          val serializerIds = mutableMapOf<TypeName, Int>()

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
                        val propertyType = trait.type.asTypeName()
                        val serializerId = serializerIds.computeIfAbsent(propertyType) {
                          nextSerializerId++
                        }

                        addStatement(
                          "%L -> delegate.%N(%M(serializer_%L, diff.value))", trait.tag, trait.name,
                          decodeFromJsonElement, serializerId
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
                          addStatement("{ eventSink.sendEvent(%T(diff.id, %L, %M(serializer_%L, it))) }", eventType, trait.tag, encodeToJsonElement, serializerId)
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
                        .initializer("serializersModule.%M()", serializer)
                        .build()
                    )
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
