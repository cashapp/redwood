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
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.KModifier.INTERNAL
import com.squareup.kotlinpoet.KModifier.OVERRIDE
import com.squareup.kotlinpoet.KModifier.PRIVATE
import com.squareup.kotlinpoet.KModifier.PUBLIC
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.U_INT
import com.squareup.kotlinpoet.asTypeName

/*
public class DiffConsumingSunspotWidgetFactory<T : Any>(
  private val delegate: SunspotWidgetFactory<T>,
  private val json: Json = Json.Default,
  private val mismatchHandler: ProtocolMismatchHandler = ProtocolMismatchHandler.Throwing,
) : DiffConsumingWidget.Factory<T> {
  override val RedwoodLayout = DiffConsumingRedwoodLayoutWidgetFactory(delegate.RedwoodLayout, json, mismatchHandler)

  override fun create(kind: Int): DiffConsumingWidget<T>? = when (kind) {
    1 -> SunspotBox()
    2 -> SunspotText()
    3 -> SunspotButton()
    1_000_001 -> RedwoodLayout.Row()
    1_000_002 -> RedwoodLayout.Column()
    else -> {
      mismatchHandler.onUnknownWidget(kind)
      null
    }
  }

  private fun SunspotBox(): ProtocolSunspotBox<T> {
    return ProtocolSunspotBox(delegate.SunspotBox(), json, mismatchHandler)
  }
  etc.
}
*/
internal fun generateDiffConsumingWidgetFactory(schema: Schema, host: Schema = schema): FileSpec {
  val widgetFactory = schema.getWidgetFactoryType().parameterizedBy(typeVariableT)
  val type = schema.diffConsumingWidgetFactoryType(host)
  return FileSpec.builder(type.packageName, type.simpleName)
    .addType(
      TypeSpec.classBuilder(type)
        .addModifiers(if (schema === host) PUBLIC else INTERNAL)
        .addTypeVariable(typeVariableT)
        .primaryConstructor(
          FunSpec.constructorBuilder()
            .addParameter("delegate", widgetFactory)
            .addParameter(
              ParameterSpec.builder("json", KotlinxSerialization.Json)
                .defaultValue("%T", KotlinxSerialization.JsonDefault)
                .build(),
            )
            .addParameter(
              ParameterSpec.builder("mismatchHandler", WidgetProtocol.ProtocolMismatchHandler)
                .defaultValue("%T.Throwing", WidgetProtocol.ProtocolMismatchHandler)
                .build(),
            )
            .build(),
        )
        .addProperty(
          PropertySpec.builder("delegate", widgetFactory, PRIVATE)
            .initializer("delegate")
            .build(),
        )
        .addProperty(
          PropertySpec.builder("json", KotlinxSerialization.Json, PRIVATE)
            .initializer("json")
            .build(),
        )
        .addProperty(
          PropertySpec.builder("mismatchHandler", WidgetProtocol.ProtocolMismatchHandler, PRIVATE)
            .initializer("mismatchHandler")
            .build(),
        )
        .apply {
          if (schema === host) {
            // Only conform to entrypoint interface if we are the host. If we are not the host,
            // the host will handle the entire transitive dependency set of tags itself.
            addSuperinterface(
              WidgetProtocol.DiffConsumingWidgetFactory.parameterizedBy(typeVariableT),
            )
            addFunction(
              FunSpec.builder("create")
                .addModifiers(OVERRIDE)
                .addParameter("kind", INT)
                .returns(
                  WidgetProtocol.DiffConsumingWidget.parameterizedBy(typeVariableT)
                    .copy(nullable = true),
                )
                .beginControlFlow("return when (kind)")
                .apply {
                  for (widget in schema.widgets.sortedBy { it.tag }) {
                    addStatement("%L -> %N()", widget.tag, widget.type.flatName)
                  }
                  for (dependency in schema.dependencies.sortedBy { it.widgets.firstOrNull()?.tag ?: 0U }) {
                    for (widget in dependency.widgets.sortedBy { it.tag }) {
                      addStatement("%L -> %N.%N()", widget.tag, dependency.name, widget.type.flatName)
                    }
                  }
                }
                .beginControlFlow("else ->")
                .addStatement("mismatchHandler.onUnknownWidget(kind)")
                .addStatement("null")
                .endControlFlow()
                .endControlFlow()
                .build(),
            )

            for (dependency in schema.dependencies.sortedBy { it.name }) {
              val dependencyType = dependency.diffConsumingWidgetFactoryType(host)
              addProperty(
                PropertySpec.builder(dependency.name, dependencyType.parameterizedBy(typeVariableT))
                  .addModifiers(PRIVATE)
                  .initializer(
                    "%T(delegate.%N, json, mismatchHandler)",
                    dependencyType,
                    dependency.name,
                  )
                  .build(),
              )
            }
          }

          for (widget in schema.widgets.sortedBy { it.type.flatName }) {
            val diffConsumingWidgetType = schema.diffConsumingWidgetType(widget, host)
            addFunction(
              FunSpec.builder(widget.type.flatName)
                .addModifiers(if (schema === host) PRIVATE else INTERNAL)
                .returns(diffConsumingWidgetType.parameterizedBy(typeVariableT))
                .addStatement(
                  "return %T(delegate.%N(), json, mismatchHandler)",
                  diffConsumingWidgetType,
                  widget.type.flatName,
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
internal class DiffConsumingSunspotButton<T : Any>(
  private val delegate: SunspotButton<T>,
  private val json: Json,
  private val mismatchHandler: ProtocolMismatchHandler,
) : DiffConsumingWidget<T> {
  public override val value: T get() = delegate.value

  public override val layoutModifiers: LayoutModifier
    get() = delegate.layoutModifiers
    set(value) { delegate.layoutModifiers = value }

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
internal fun generateDiffConsumingWidget(schema: Schema, widget: Widget, host: Schema = schema): FileSpec {
  val type = schema.diffConsumingWidgetType(widget, host)
  val widgetType = schema.widgetType(widget).parameterizedBy(typeVariableT)
  val protocolType = WidgetProtocol.DiffConsumingWidget.parameterizedBy(typeVariableT)
  return FileSpec.builder(type.packageName, type.simpleName)
    .addType(
      TypeSpec.classBuilder(type)
        .addModifiers(INTERNAL)
        .addTypeVariable(typeVariableT)
        .addSuperinterface(protocolType)
        .primaryConstructor(
          FunSpec.constructorBuilder()
            .addParameter("delegate", widgetType)
            .addParameter("json", KotlinxSerialization.Json)
            .addParameter("mismatchHandler", WidgetProtocol.ProtocolMismatchHandler)
            .build(),
        )
        .addProperty(
          PropertySpec.builder("delegate", widgetType, PRIVATE)
            .initializer("delegate")
            .build(),
        )
        .addProperty(
          PropertySpec.builder("json", KotlinxSerialization.Json, PRIVATE)
            .initializer("json")
            .build(),
        )
        .addProperty(
          PropertySpec.builder("mismatchHandler", WidgetProtocol.ProtocolMismatchHandler, PRIVATE)
            .initializer("mismatchHandler")
            .build(),
        )
        .addProperty(
          PropertySpec.builder("value", typeVariableT, OVERRIDE)
            .getter(
              FunSpec.getterBuilder()
                .addStatement("return delegate.value")
                .build(),
            )
            .build(),
        )
        .addProperty(
          PropertySpec.builder("layoutModifiers", Redwood.LayoutModifier, OVERRIDE)
            .mutable()
            .getter(
              FunSpec.getterBuilder()
                .addStatement("return delegate.layoutModifiers")
                .build(),
            )
            .setter(
              FunSpec.setterBuilder()
                .addParameter("value", Redwood.LayoutModifier)
                .addStatement("delegate.layoutModifiers = value")
                .build(),
            )
            .build(),
        )
        .apply {
          val (childrens, properties) = widget.traits.partition { it is Children }
          var nextSerializerId = 0
          val serializerIds = mutableMapOf<TypeName, Int>()

          addFunction(
            FunSpec.builder("apply")
              .addModifiers(OVERRIDE)
              .addParameter("diff", Protocol.PropertyDiff)
              .addParameter("eventSink", Protocol.EventSink)
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
                        "%LU -> delegate.%N(json.decodeFromJsonElement(serializer_%L, diff.value))",
                        trait.tag,
                        trait.name,
                        serializerId,
                      )
                    }

                    is Event -> {
                      beginControlFlow("%LU ->", trait.tag)
                      beginControlFlow(
                        "val %N: %T = if (diff.value.%M.%M)",
                        trait.name,
                        trait.lambdaType,
                        KotlinxSerialization.jsonPrimitive,
                        KotlinxSerialization.jsonBoolean,
                      )
                      val parameterType = trait.parameterType?.asTypeName()
                      if (parameterType != null) {
                        val serializerId = serializerIds.computeIfAbsent(parameterType) {
                          nextSerializerId++
                        }
                        addStatement(
                          "{ eventSink.sendEvent(%T(diff.id, %LU, json.encodeToJsonElement(serializer_%L, it))) }",
                          Protocol.Event,
                          trait.tag,
                          serializerId,
                        )
                      } else {
                        addStatement(
                          "{ eventSink.sendEvent(%T(diff.id, %LU)) }",
                          Protocol.Event,
                          trait.tag,
                        )
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
              .addStatement("else -> mismatchHandler.onUnknownProperty(%L, tag)", widget.tag)
              .endControlFlow()
              .build(),
          )

          addFunction(
            FunSpec.builder("children")
              .addModifiers(OVERRIDE)
              .addParameter("tag", U_INT)
              .returns(RedwoodWidget.WidgetChildrenOfT.copy(nullable = true))
              .apply {
                if (childrens.isNotEmpty()) {
                  beginControlFlow("return when (tag)")
                  for (children in childrens) {
                    addStatement("%LU -> delegate.%N", children.tag, children.name)
                  }
                  beginControlFlow("else ->")
                  addStatement("mismatchHandler.onUnknownChildren(%L, tag)", widget.tag)
                  addStatement("null")
                  endControlFlow()
                  endControlFlow()
                } else {
                  addStatement("mismatchHandler.onUnknownChildren(%L, tag)", widget.tag)
                  addStatement("return null")
                }
              }
              .build(),
          )
        }
        .addFunction(
          FunSpec.builder("updateLayoutModifier")
            .addModifiers(OVERRIDE)
            .addParameter("value", KotlinxSerialization.JsonArray)
            .addStatement("layoutModifiers = value.%M(json, mismatchHandler)", host.toLayoutModifier)
            .build(),
        )
        .build(),
    )
    .build()
}

internal fun generateDiffConsumingLayoutModifiers(schema: Schema, host: Schema = schema): FileSpec {
  return FileSpec.builder(schema.widgetPackage(host), "layoutModifierSerialization")
    .apply {
      if (schema === host) {
        addFunction(generateJsonArrayToLayoutModifier(schema))
        addFunction(generateJsonElementToLayoutModifier(schema))
      }

      for (layoutModifier in schema.layoutModifiers) {
        val typeName = ClassName(schema.widgetPackage(host), layoutModifier.type.simpleName!! + "Impl")
        val typeBuilder = if (layoutModifier.properties.isEmpty()) {
          TypeSpec.objectBuilder(typeName)
        } else {
          TypeSpec.classBuilder(typeName)
            .addAnnotation(KotlinxSerialization.Serializable)
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
        }
        addType(
          typeBuilder
            .addModifiers(if (schema === host) PRIVATE else INTERNAL)
            .addSuperinterface(schema.layoutModifierType(layoutModifier))
            .addFunction(layoutModifierEquals(schema, layoutModifier))
            .addFunction(layoutModifierHashCode(layoutModifier))
            .addFunction(layoutModifierToString(layoutModifier))
            .build(),
        )
      }
    }
    .build()
}

private fun generateJsonArrayToLayoutModifier(schema: Schema): FunSpec {
  return FunSpec.builder("toLayoutModifier")
    .addModifiers(INTERNAL)
    .receiver(KotlinxSerialization.JsonArray)
    .addParameter("json", KotlinxSerialization.Json)
    .addParameter("mismatchHandler", WidgetProtocol.ProtocolMismatchHandler)
    .addStatement(
      """
      |return fold<%1T, %2T>(%2T) { modifier, element ->
      |  modifier then element.%3M(json, mismatchHandler)
      |}
      """.trimMargin(),
      KotlinxSerialization.JsonElement,
      Redwood.LayoutModifier,
      schema.toLayoutModifier,
    )
    .returns(Redwood.LayoutModifier)
    .build()
}

private fun generateJsonElementToLayoutModifier(schema: Schema): FunSpec {
  return FunSpec.builder("toLayoutModifier")
    .addModifiers(PRIVATE)
    .receiver(KotlinxSerialization.JsonElement)
    .addParameter("json", KotlinxSerialization.Json)
    .addParameter("mismatchHandler", WidgetProtocol.ProtocolMismatchHandler)
    .returns(Redwood.LayoutModifier)
    .addStatement("val array = %M", KotlinxSerialization.jsonArray)
    .addStatement("require(array.size == 2) { \"Layout modifier JSON array length != 2: \${array.size}\" }")
    .addStatement("")
    .beginControlFlow(
      "val serializer = when (val tag = array[0].%M.%M)",
      KotlinxSerialization.jsonPrimitive,
      KotlinxSerialization.jsonInt,
    )
    .apply {
      val layoutModifiers = schema.allLayoutModifiers()
      if (layoutModifiers.isEmpty()) {
        addAnnotation(
          AnnotationSpec.builder(Suppress::class)
            .addMember("%S, %S, %S, %S", "UNUSED_PARAMETER", "UNUSED_EXPRESSION", "UNUSED_VARIABLE", "UNREACHABLE_CODE")
            .build(),
        )
      } else {
        for ((layoutModifierSchema, layoutModifier) in layoutModifiers) {
          val typeName = ClassName(layoutModifierSchema.widgetPackage(schema), layoutModifier.type.simpleName!! + "Impl")
          if (layoutModifier.properties.isEmpty()) {
            addStatement("%L -> return %T", layoutModifier.tag, typeName)
          } else {
            addStatement("%L -> %T.serializer()", layoutModifier.tag, typeName)
          }
        }
      }
    }
    .beginControlFlow("else ->")
    .addStatement("mismatchHandler.onUnknownLayoutModifier(tag)")
    .addStatement("return %T", Redwood.LayoutModifier)
    .endControlFlow()
    .endControlFlow()
    .addStatement("")
    .addStatement("val value = array[1].%M", KotlinxSerialization.jsonObject)
    .addStatement("return json.decodeFromJsonElement(serializer, value)")
    .build()
}
