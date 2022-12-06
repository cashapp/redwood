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
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.INTERNAL
import com.squareup.kotlinpoet.KModifier.OVERRIDE
import com.squareup.kotlinpoet.KModifier.PRIVATE
import com.squareup.kotlinpoet.KModifier.PUBLIC
import com.squareup.kotlinpoet.NOTHING
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName

/*
class DiffProducingSunspotWidgetFactory(
  public override val bridge: ProtocolBridge,
  private val json: Json = Json.Default,
  private val mismatchHandler: ProtocolMismatchHandler = ProtocolMismatchHandler.Throwing,
) : SunspotWidgetFactory<Nothing>, DiffProducingWidget.Factory {
  override val RedwoodLayout = DiffProducingRedwoodLayoutWidgetFactory(delegate.RedwoodLayout, bridge, json, mismatchHandler)
  override fun SunspotBox(): SunspotBox<Nothing> = ProtocolSunspotBox(bridge.nextId(), json, mismatchHandler)
  override fun SunspotText(): SunspotText<Nothing> = ProtocolSunspotText(bridge.nextId(), json, mismatchHandler)
  override fun SunspotButton(): SunspotButton<Nothing> = ProtocolSunspotButton(bridge.nextId(), json, mismatchHandler)
}
*/
internal fun generateDiffProducingWidgetFactory(schema: Schema, host: Schema = schema): FileSpec {
  val type = schema.diffProducingWidgetFactoryType(host)
  return FileSpec.builder(type.packageName, type.simpleName)
    .addType(
      TypeSpec.classBuilder(type)
        .addModifiers(if (schema === host) PUBLIC else INTERNAL)
        .addSuperinterface(schema.getWidgetFactoryType().parameterizedBy(NOTHING))
        .addSuperinterface(ComposeProtocol.DiffProducingWidgetFactory)
        .primaryConstructor(
          FunSpec.constructorBuilder()
            .addParameter("bridge", ComposeProtocol.ProtocolBridge)
            .addParameter(
              ParameterSpec.builder("json", KotlinxSerialization.Json)
                .defaultValue("%T", KotlinxSerialization.JsonDefault)
                .build(),
            )
            .addParameter(
              ParameterSpec.builder("mismatchHandler", ComposeProtocol.ProtocolMismatchHandler)
                .defaultValue("%T.Throwing", ComposeProtocol.ProtocolMismatchHandler)
                .build(),
            )
            .build(),
        )
        .addProperty(
          PropertySpec.builder("bridge", ComposeProtocol.ProtocolBridge, PUBLIC, OVERRIDE)
            .initializer("bridge")
            .build(),
        )
        .addProperty(
          PropertySpec.builder("json", KotlinxSerialization.Json, PRIVATE)
            .initializer("json")
            .build(),
        )
        .addProperty(
          PropertySpec.builder("mismatchHandler", ComposeProtocol.ProtocolMismatchHandler, PRIVATE)
            .initializer("mismatchHandler")
            .build(),
        )
        .apply {
          if (schema === host) {
            for (dependency in schema.dependencies) {
              addProperty(
                PropertySpec.builder(dependency.name, dependency.getWidgetFactoryType().parameterizedBy(NOTHING))
                  .addModifiers(OVERRIDE)
                  .initializer("%T(bridge, json, mismatchHandler)", dependency.diffProducingWidgetFactoryType(host))
                  .build(),
              )
            }
          }

          for (widget in schema.widgets) {
            addFunction(
              FunSpec.builder(widget.type.flatName)
                .addModifiers(OVERRIDE)
                .returns(schema.widgetType(widget).parameterizedBy(NOTHING))
                .addStatement(
                  "return %T(bridge, json, mismatchHandler)",
                  schema.diffProducingWidgetType(widget, host),
                )
                .build(),
            )
          }
        }
        .build(),
    )
    .build()
}

/*
internal class DiffProducingSunspotButton(
  private val bridge: ProtocolBridge,
  private val json: Json,
  private val mismatchHandler: ProtocolMismatchHandler,
) : DiffProducingWidget, SunspotButton<Nothing> {
  public override val id: Id = bridge.nextId()
  public override val tag: WidgetTag get() = WidgetTag(3)

  private var onClick: (() -> Unit)? = null

  private val serializer_0: KSerializer<String?> = json.serializersModule.serializer()
  private val serializer_1: KSerializer<Boolean> = json.serializersModule.serializer()

  override var layoutModifiers: LayoutModifier
    get() = throw AssertionError()
    set(value) {
      val json = buildJsonArray {
        value.forEach { element -> add(element.toJsonElement(json))
      }
      bridge.append(LayoutModifiers(id, json))
    }

  override fun text(text: String?) {
    bridge.append(PropertyDiff(this.id, PropertyTag(1), json.encodeToJsonElement(serializer_0, text)))
  }

  override fun onClick(onClick: (() -> Unit)?) {
    val onClickSet = onClick != null
    if (onClickSet != (this.onClick != null)) {
      bridge.append(PropertyDiff(this.id, PropertyTag(3), onClickSet))
    }
    this.onClick = onClick
  }

  override fun sendEvent(event: Event) {
    when (event.tag.value) {
      3 -> onClick?.invoke()
      else -> mismatchHandler.onUnknownEvent(12, event.tag)
    }
  }
}
*/
internal fun generateDiffProducingWidget(schema: Schema, widget: Widget, host: Schema = schema): FileSpec {
  val type = schema.diffProducingWidgetType(widget, host)
  val widgetName = schema.widgetType(widget)
  return FileSpec.builder(type.packageName, type.simpleName)
    .addType(
      TypeSpec.classBuilder(type)
        .addModifiers(INTERNAL)
        .addSuperinterface(ComposeProtocol.DiffProducingWidget)
        .addSuperinterface(widgetName.parameterizedBy(NOTHING))
        .primaryConstructor(
          FunSpec.constructorBuilder()
            .addParameter("bridge", ComposeProtocol.ProtocolBridge)
            .addParameter("json", KotlinxSerialization.Json)
            .addParameter("mismatchHandler", ComposeProtocol.ProtocolMismatchHandler)
            .build(),
        )
        .addProperty(
          PropertySpec.builder("bridge", ComposeProtocol.ProtocolBridge, PRIVATE)
            .initializer("bridge")
            .build(),
        )
        .addProperty(
          PropertySpec.builder("json", KotlinxSerialization.Json, PRIVATE)
            .initializer("json")
            .build(),
        )
        .addProperty(
          PropertySpec.builder("mismatchHandler", ComposeProtocol.ProtocolMismatchHandler, PRIVATE)
            .initializer("mismatchHandler")
            .build(),
        )
        .addProperty(
          PropertySpec.builder("id", Protocol.Id, PUBLIC, OVERRIDE)
            .initializer("bridge.nextId()")
            .build(),
        )
        .addProperty(
          PropertySpec.builder("tag", Protocol.WidgetTag, PUBLIC, OVERRIDE)
            .getter(
              FunSpec.getterBuilder()
                .addStatement("return %T(%L)", Protocol.WidgetTag, widget.tag)
                .build(),
            )
            .build(),
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
                    .addStatement(
                      "bridge.append(%T(this.id, %T(%L), json.encodeToJsonElement(serializer_%L, %N)))",
                      Protocol.PropertyDiff,
                      Protocol.PropertyTag,
                      trait.tag,
                      serializerId,
                      trait.name,
                    )
                    .build(),
                )
              }
              is Event -> {
                addProperty(
                  PropertySpec.builder(trait.name, trait.lambdaType, PRIVATE)
                    .mutable()
                    .initializer("null")
                    .build(),
                )
                addFunction(
                  FunSpec.builder(trait.name)
                    .addModifiers(OVERRIDE)
                    .addParameter(trait.name, trait.lambdaType)
                    .addStatement("val %1NSet = %1N != null", trait.name)
                    .beginControlFlow("if (%1NSet != (this.%1N != null))", trait.name)
                    .addStatement(
                      "bridge.append(%T(this.id, %T(%L), %M(%NSet)))",
                      Protocol.PropertyDiff,
                      Protocol.PropertyTag,
                      trait.tag,
                      KotlinxSerialization.JsonPrimitive,
                      trait.name,
                    )
                    .endControlFlow()
                    .addStatement("this.%1N = %1N", trait.name)
                    .build(),
                )
              }
              is Children -> {
                addProperty(
                  PropertySpec.builder(trait.name, RedwoodWidget.WidgetChildren.parameterizedBy(NOTHING))
                    .addModifiers(OVERRIDE)
                    .getter(
                      FunSpec.getterBuilder()
                        .addStatement(
                          "return bridge.widgetChildren(id, %T(%L))",
                          Protocol.ChildrenTag,
                          trait.tag,
                        )
                        .build(),
                    )
                    .build(),
                )
              }
            }
          }

          addFunction(
            FunSpec.builder("sendEvent")
              .addModifiers(OVERRIDE)
              .addParameter("event", Protocol.Event)
              .beginControlFlow("when (event.tag.value)")
              .apply {
                for (event in widget.traits.filterIsInstance<Event>()) {
                  val parameterType = event.parameterType?.asTypeName()
                  if (parameterType != null) {
                    val serializerId = serializerIds.computeIfAbsent(parameterType) {
                      nextSerializerId++
                    }
                    addStatement(
                      "%L -> %N?.invoke(json.decodeFromJsonElement(serializer_%L, event.value))",
                      event.tag,
                      event.name,
                      serializerId,
                    )
                  } else {
                    addStatement("%L -> %N?.invoke()", event.tag, event.name)
                  }
                }
              }
              .addStatement("else -> mismatchHandler.onUnknownEvent(tag, event.tag)")
              .endControlFlow()
              .build(),
          )

          for ((typeName, id) in serializerIds) {
            addProperty(
              PropertySpec.builder(
                "serializer_$id",
                KotlinxSerialization.KSerializer.parameterizedBy(typeName),
              )
                .addModifiers(PRIVATE)
                .initializer("json.serializersModule.%M()", KotlinxSerialization.serializer)
                .build(),
            )
          }
        }
        .addProperty(
          PropertySpec.builder("layoutModifiers", Redwood.LayoutModifier, OVERRIDE)
            .mutable()
            .getter(
              FunSpec.getterBuilder()
                .addStatement("throw %T()", Stdlib.AssertionError)
                .build(),
            )
            .setter(
              FunSpec.setterBuilder()
                .addParameter("value", Redwood.LayoutModifier)
                .addStatement("bridge.append(%T(id, value.%M(json)))", Protocol.LayoutModifiers, host.toJsonArray)
                .build(),
            )
            .build(),
        )
        .build(),
    )
    .build()
}

internal fun generateDiffProducingLayoutModifiers(schema: Schema, host: Schema = schema): FileSpec {
  return FileSpec.builder(schema.composePackage(host), "layoutModifierSerialization")
    .apply {
      if (schema === host) {
        addFunction(generateToJsonArray(schema))
        addFunction(generateToJsonElement(schema))
      }

      for (layoutModifier in schema.layoutModifiers) {
        val surrogateName = schema.layoutModifierSurrogate(layoutModifier, host)
        val modifierType = schema.layoutModifierType(layoutModifier)

        addType(
          if (layoutModifier.properties.isEmpty()) {
            TypeSpec.objectBuilder(surrogateName)
              .addModifiers(PRIVATE)
              .addFunction(
                FunSpec.builder("encode")
                  .returns(KotlinxSerialization.JsonElement)
                  .beginControlFlow("return %M", KotlinxSerialization.buildJsonArray)
                  .addStatement(
                    "add(%M(%L))",
                    KotlinxSerialization.JsonPrimitive,
                    layoutModifier.tag,
                  )
                  .addStatement("add(%M {})", KotlinxSerialization.buildJsonObject)
                  .endControlFlow()
                  .build(),
              )
              .build()
          } else {
            TypeSpec.classBuilder(surrogateName)
              .addAnnotation(KotlinxSerialization.Serializable)
              .addModifiers(INTERNAL)
              .addSuperinterface(modifierType)
              .apply {
                val primaryConstructor = FunSpec.constructorBuilder()

                for (property in layoutModifier.properties) {
                  val propertyType = property.type.asTypeName()

                  primaryConstructor.addParameter(
                    ParameterSpec.builder(property.name, propertyType)
                      .apply {
                        property.defaultExpression?.let { defaultValue(it) }
                      }
                      .build(),
                  )

                  addProperty(
                    PropertySpec.builder(property.name, propertyType)
                      .addModifiers(OVERRIDE)
                      .addAnnotation(KotlinxSerialization.Contextual)
                      .initializer("%N", property.name)
                      .build(),
                  )
                }

                primaryConstructor(primaryConstructor.build())
              }
              .addFunction(
                FunSpec.constructorBuilder()
                  .addParameter("delegate", modifierType)
                  .callThisConstructor(layoutModifier.properties.map { CodeBlock.of("delegate.${it.name}") })
                  .build(),
              )
              .addType(
                TypeSpec.companionObjectBuilder()
                  .addFunction(
                    FunSpec.builder("encode")
                      .addParameter("json", KotlinxSerialization.Json)
                      .addParameter("value", modifierType)
                      .returns(KotlinxSerialization.JsonElement)
                      .beginControlFlow("return %M", KotlinxSerialization.buildJsonArray)
                      .addStatement(
                        "add(%M(%L))",
                        KotlinxSerialization.JsonPrimitive,
                        layoutModifier.tag,
                      )
                      .addStatement("add(json.encodeToJsonElement(serializer(), %T(value)))", surrogateName)
                      .endControlFlow()
                      .build(),
                  )
                  .build(),
              )
              .build()
          },
        )
      }
    }
    .build()
}

private fun generateToJsonArray(schema: Schema): FunSpec {
  return FunSpec.builder("toJsonArray")
    .addModifiers(INTERNAL)
    .receiver(Redwood.LayoutModifier)
    .addParameter("json", KotlinxSerialization.Json)
    .beginControlFlow("return %M", KotlinxSerialization.buildJsonArray)
    .addStatement("forEach { element -> add(element.%M(json)) }", schema.toJsonElement)
    .endControlFlow()
    .returns(KotlinxSerialization.JsonArray)
    .build()
}

private fun generateToJsonElement(schema: Schema): FunSpec {
  return FunSpec.builder("toJsonElement")
    .addModifiers(PRIVATE)
    .receiver(Redwood.LayoutModifierElement)
    .addParameter("json", KotlinxSerialization.Json)
    .returns(KotlinxSerialization.JsonElement)
    .beginControlFlow("return when (this)")
    .apply {
      val layoutModifiers = schema.allLayoutModifiers()
      if (layoutModifiers.isEmpty()) {
        addAnnotation(
          AnnotationSpec.builder(Suppress::class)
            .addMember("%S, %S", "UNUSED_PARAMETER", "UNUSED_EXPRESSION")
            .build(),
        )
      } else {
        for ((localSchema, layoutModifier) in layoutModifiers) {
          val modifierType = localSchema.layoutModifierType(layoutModifier)
          val surrogate = localSchema.layoutModifierSurrogate(layoutModifier, schema)
          if (layoutModifier.properties.isEmpty()) {
            addStatement("is %T -> %T.encode()", modifierType, surrogate)
          } else {
            addStatement("is %T -> %T.encode(json, this)", modifierType, surrogate)
          }
        }
      }
    }
    .addStatement("else -> throw %T()", Stdlib.AssertionError)
    .endControlFlow()
    .build()
}
