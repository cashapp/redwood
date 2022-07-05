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
package app.cash.redwood.generator

import app.cash.redwood.schema.parser.Schema
import app.cash.redwood.schema.parser.Widget
import app.cash.redwood.schema.parser.Widget.Children
import app.cash.redwood.schema.parser.Widget.Event
import app.cash.redwood.schema.parser.Widget.Property
import com.squareup.kotlinpoet.CodeBlock
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
  private val json: Json = Json.Default,
  private val mismatchHandler: ProtocolMismatchHandler = ProtocolMismatchHandler.Throwing,
) : SunspotWidgetFactory<Nothing>, DiffProducingWidget.Factory {
  override fun SunspotBox(): SunspotBox<Nothing> = ProtocolSunspotBox(json, mismatchHandler)
  override fun SunspotText(): SunspotText<Nothing> = ProtocolSunspotText(json, mismatchHandler)
  override fun SunspotButton(): SunspotButton<Nothing> = ProtocolSunspotButton(json, mismatchHandler)
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
              ParameterSpec.builder("json", Json)
                .defaultValue("%T", jsonCompanion)
                .build()
            )
            .addParameter(
              ParameterSpec.builder("mismatchHandler", ComposeProtocolMismatchHandler)
                .defaultValue("%T.Throwing", ComposeProtocolMismatchHandler)
                .build()
            )
            .build()
        )
        .addProperty(
          PropertySpec.builder("json", Json, PRIVATE)
            .initializer("json")
            .build()
        )
        .addProperty(
          PropertySpec.builder("mismatchHandler", ComposeProtocolMismatchHandler, PRIVATE)
            .initializer("mismatchHandler")
            .build()
        )
        .apply {
          for (widget in schema.widgets) {
            addFunction(
              FunSpec.builder(widget.flatName)
                .addModifiers(OVERRIDE)
                .returns(schema.widgetType(widget).parameterizedBy(NOTHING))
                .addStatement(
                  "return %T(json, mismatchHandler)", schema.diffProducingWidgetType(widget)
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
  private val json: Json,
  private val mismatchHandler: ProtocolMismatchHandler,
) : AbstractDiffProducingWidget(3), SunspotButton<Nothing> {
  private var onClick: (() -> Unit)? = null

  private val serializer_0: KSerializer<String?> = json.serializersModule.serializer()
  private val serializer_1: KSerializer<Boolean> = json.serializersModule.serializer()

  override var layoutModifiers: LayoutModifier
    get() = throw AssertionError()
    set(value) {
      val json = buildJsonArray {
        value.foldIn(Unit) { _, element -> add(element.toJsonElement(json))
      }
      appendDiff(LayoutModifiers(id, json))
    }

  override fun text(text: String?) {
    appendDiff(PropertyDiff(this.id, 1, json.encodeToJsonElement(serializer_0, text)))
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
      else -> mismatchHandler.onUnknownEvent(12, tag)
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
            .addParameter("json", Json)
            .addParameter("mismatchHandler", ComposeProtocolMismatchHandler)
            .build()
        )
        .addProperty(
          PropertySpec.builder("json", Json, PRIVATE)
            .initializer("json")
            .build()
        )
        .addProperty(
          PropertySpec.builder("mismatchHandler", ComposeProtocolMismatchHandler, PRIVATE)
            .initializer("mismatchHandler")
            .build()
        )
        .apply {
          var nextSerializerId = 0
          val serializerIds = mutableMapOf<TypeName, Int>()

          for (trait in widget.traits) {
            when (trait) {
              is Property -> {
                val traitTypeName = trait.type.asTypeName()
                val serializerId = serializerIds.computeIfAbsent(traitTypeName) {
                  nextSerializerId++
                }

                addFunction(
                  FunSpec.builder(trait.name)
                    .addModifiers(OVERRIDE)
                    .addParameter(trait.name, traitTypeName)
                    .addStatement("appendDiff(%T(this.id, %L, json.encodeToJsonElement(serializer_%L, %N)))", propertyDiff, trait.tag, serializerId, trait.name)
                    .build()
                )
              }
              is Event -> {
                addProperty(
                  PropertySpec.builder(trait.name, trait.lambdaType, PRIVATE)
                    .mutable()
                    .initializer("null")
                    .build()
                )
                addFunction(
                  FunSpec.builder(trait.name)
                    .addModifiers(OVERRIDE)
                    .addParameter(trait.name, trait.lambdaType)
                    .addStatement("val %1NSet = %1N != null", trait.name)
                    .beginControlFlow("if (%1NSet != (this.%1N != null))", trait.name)
                    .addStatement("appendDiff(%T(this.id, %L, %M(%NSet)))", propertyDiff, trait.tag, JsonPrimitive, trait.name)
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
                      "%L -> %N?.invoke(json.decodeFromJsonElement(serializer_%L, event.value))", event.tag, event.name, serializerId,
                    )
                  } else {
                    addStatement("%L -> %N?.invoke()", event.tag, event.name)
                  }
                }
              }
              .addStatement("else -> mismatchHandler.onUnknownEvent(%L, tag)", widget.tag)
              .endControlFlow()
              .build()
          )

          for ((typeName, id) in serializerIds) {
            addProperty(
              PropertySpec.builder("serializer_$id", KSerializer.parameterizedBy(typeName))
                .addModifiers(PRIVATE)
                .initializer("json.serializersModule.%M()", serializer)
                .build()
            )
          }
        }
        .addProperty(
          PropertySpec.builder("layoutModifiers", LayoutModifier, OVERRIDE)
            .mutable()
            .getter(
              FunSpec.getterBuilder()
                .addStatement("throw %T()", ae)
                .build()
            )
            .setter(
              FunSpec.setterBuilder()
                .addParameter("value", LayoutModifier)
                .beginControlFlow("val json = %M", buildJsonArray)
                .addStatement("value.foldIn(Unit) { _, element -> add(element.toJsonElement(json)) }")
                .endControlFlow()
                .addStatement("appendDiff(%T(id, json))", LayoutModifiers)
                .build()
            )
            .build()
        )
        .build()
    )
    .build()
}

internal fun generateDiffProducingLayoutModifier(schema: Schema): FileSpec {
  return FileSpec.builder(schema.composePackage, "layoutModifierSerialization")
    .addFunction(
      FunSpec.builder("toJsonElement")
        .addModifiers(INTERNAL)
        .receiver(LayoutModifierElement)
        .addParameter("json", Json)
        .returns(JsonElement)
        .beginControlFlow("return when (this)")
        .apply {
          for (layoutModifier in schema.layoutModifiers) {
            val modifierType = schema.layoutModifierType(layoutModifier)
            val surrogate = schema.layoutModifierSurrogate(layoutModifier)
            addStatement("is %T -> %T.encode(json, this)", modifierType, surrogate)
          }
        }
        .addStatement("else -> throw %T()", ae)
        .endControlFlow()
        .build()
    )
    .apply {
      for (layoutModifier in schema.layoutModifiers) {
        val surrogateName = schema.layoutModifierSurrogate(layoutModifier)
        val modifierType = schema.layoutModifierType(layoutModifier)

        addType(
          TypeSpec.classBuilder(surrogateName)
            .addAnnotation(Serializable)
            .addModifiers(PRIVATE)
            .addSuperinterface(modifierType)
            .apply {
              val primaryConstructor = FunSpec.constructorBuilder()

              for (property in layoutModifier.properties) {
                val propertyType = property.type.asTypeName()
                primaryConstructor.addParameter(property.name, propertyType)
                addProperty(
                  PropertySpec.builder(property.name, propertyType)
                    .addModifiers(OVERRIDE)
                    .addAnnotation(Contextual)
                    .initializer("%N", property.name)
                    .build()
                )
              }

              primaryConstructor(primaryConstructor.build())
            }
            .addFunction(
              FunSpec.constructorBuilder()
                .addParameter("delegate", modifierType)
                .callThisConstructor(layoutModifier.properties.map { CodeBlock.of("delegate.${it.name}") })
                .build()
            )
            .addType(
              TypeSpec.companionObjectBuilder()
                .addFunction(
                  FunSpec.builder("encode")
                    .addParameter("json", Json)
                    .addParameter("value", modifierType)
                    .returns(JsonElement)
                    .beginControlFlow("return %M", buildJsonArray)
                    .addStatement("add(%M(%L))", JsonPrimitive, layoutModifier.tag)
                    .addStatement("add(json.encodeToJsonElement(serializer(), %T(value)))", surrogateName)
                    .endControlFlow()
                    .build()
                )
                .build()
            )
            .build()
        )
      }
    }
    .build()
}
