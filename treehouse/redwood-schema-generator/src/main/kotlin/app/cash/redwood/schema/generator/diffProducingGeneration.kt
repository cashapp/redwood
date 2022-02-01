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

import app.cash.exhaustive.Exhaustive
import app.cash.redwood.schema.parser.Children
import app.cash.redwood.schema.parser.Event
import app.cash.redwood.schema.parser.Property
import app.cash.redwood.schema.parser.Schema
import app.cash.redwood.schema.parser.Widget
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.INTERNAL
import com.squareup.kotlinpoet.KModifier.OVERRIDE
import com.squareup.kotlinpoet.KModifier.PRIVATE
import com.squareup.kotlinpoet.NOTHING
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName

/*
class DiffProducingSunspotWidgetFactory(
  private val serializerModule: SerializersModule = SerializersModule { },
) : SunspotWidgetFactory<Nothing>, DiffProducingWidget.Factory {
  override fun SunspotBox(): SunspotBox<Nothing> = ProtocolSunspotBox(serializerModule)
  override fun SunspotText(): SunspotText<Nothing> = ProtocolSunspotText(serializerModule)
  override fun SunspotButton(): SunspotButton<Nothing> = ProtocolSunspotButton(serializerModule)
}
*/
internal fun generateDiffProducingWidgetFactory(schema: Schema): FileSpec {
  val type = schema.diffProducingWidgetFactoryType()
  return FileSpec.builder(type.packageName, type.simpleName)
    .addType(
      TypeSpec.classBuilder(type)
        .addSuperinterface(schema.getWidgetFactoryType().parameterizedBy(NOTHING))
        .addSuperinterface(diffProducingWidgetFactory)
        .primaryConstructor(
          FunSpec.constructorBuilder()
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
          PropertySpec.builder("serializersModule", serializersModule, PRIVATE)
            .initializer("serializersModule")
            .build()
        )
        .apply {
          for (widget in schema.widgets) {
            addFunction(
              FunSpec.builder(widget.flatName)
                .addModifiers(OVERRIDE)
                .returns(schema.widgetType(widget).parameterizedBy(NOTHING))
                .addStatement(
                  "return %T(serializersModule)", schema.diffProducingWidgetType(widget)
                )
                .build()
            )
          }
        }
        .build()
    )
    .build()
}

/*
internal class DiffProducingSunspotButton(
  serializerModule: SerializersModule,
) : AbstractDiffProducingWidget(3), SunspotButton<Nothing> {
  private var onClick: (() -> Unit)? = null

  private val serializer_0: KSerializer<String?> = serializerModule.serializer()
  private val serializer_1: KSerializer<Boolean> = serializerModule.serializer()

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
internal fun generateDiffProducingWidget(schema: Schema, widget: Widget): FileSpec {
  val type = schema.diffProducingWidgetType(widget)
  val widgetName = schema.widgetType(widget)
  return FileSpec.builder(type.packageName, type.simpleName)
    .addType(
      TypeSpec.classBuilder(type)
        .addModifiers(INTERNAL)
        .superclass(abstractDiffProducingWidget)
        .addSuperclassConstructorParameter("%L", widget.tag)
        .addSuperinterface(widgetName.parameterizedBy(NOTHING))
        .primaryConstructor(
          FunSpec.constructorBuilder()
            .addParameter("serializersModule", serializersModule)
            .build()
        )
        .apply {
          var hasEvents = false
          var nextSerializerId = 0
          val serializerIds = mutableMapOf<TypeName, Int>()

          for (trait in widget.traits) {
            @Exhaustive when (trait) {
              is Property -> {
                val traitTypeName = trait.type.asTypeName()
                val serializerId = serializerIds.computeIfAbsent(traitTypeName) {
                  nextSerializerId++
                }

                addFunction(
                  FunSpec.builder(trait.name)
                    .addModifiers(OVERRIDE)
                    .addParameter(trait.name, traitTypeName)
                    .addStatement("appendDiff(%T(this.id, %L, %M(serializer_%L, %N)))", propertyDiff, trait.tag, encodeToJsonElement, serializerId, trait.name)
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
                    .addStatement("appendDiff(%T(this.id, %L, %M(%NSet)))", propertyDiff, trait.tag, jsonPrimitive, trait.name)
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
                    val parameterType = event.parameterType?.asTypeName()
                    if (parameterType != null) {
                      val serializerId = serializerIds.computeIfAbsent(parameterType) {
                        nextSerializerId++
                      }
                      addStatement(
                        "%L -> %N?.invoke(%M(serializer_%L, event.value))", event.tag, event.name, decodeFromJsonElement, serializerId,
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

          for ((typeName, id) in serializerIds) {
            addProperty(
              PropertySpec.builder("serializer_$id", kSerializer.parameterizedBy(typeName))
                .addModifiers(PRIVATE)
                .initializer("serializersModule.%M()", serializer)
                .build()
            )
          }
        }
        .build()
    )
    .build()
}
