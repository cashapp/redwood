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
import com.squareup.kotlinpoet.KModifier.DATA
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
              ParameterSpec.builder("json", Json)
                .defaultValue("%T", jsonCompanion)
                .build(),
            )
            .addParameter(
              ParameterSpec.builder("mismatchHandler", WidgetProtocolMismatchHandler)
                .defaultValue("%T.Throwing", WidgetProtocolMismatchHandler)
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
          PropertySpec.builder("json", Json, PRIVATE)
            .initializer("json")
            .build(),
        )
        .addProperty(
          PropertySpec.builder("mismatchHandler", WidgetProtocolMismatchHandler, PRIVATE)
            .initializer("mismatchHandler")
            .build(),
        )
        .apply {
          if (schema === host) {
            // Only conform to entrypoint interface if we are the host. If we are not the host,
            // the host will handle the entire transitive dependency set of tags itself.
            addSuperinterface(DiffConsumingWidgetFactory.parameterizedBy(typeVariableT))
            addFunction(
              FunSpec.builder("create")
                .addModifiers(OVERRIDE)
                .addParameter("kind", INT)
                .returns(DiffConsumingWidget.parameterizedBy(typeVariableT).copy(nullable = true))
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
  val protocolType = DiffConsumingWidget.parameterizedBy(typeVariableT)
  return FileSpec.builder(type.packageName, type.simpleName)
    .addType(
      TypeSpec.classBuilder(type)
        .addModifiers(if (schema === host) PUBLIC else INTERNAL)
        .addTypeVariable(typeVariableT)
        .addSuperinterface(protocolType)
        .primaryConstructor(
          FunSpec.constructorBuilder()
            .addParameter("delegate", widgetType)
            .addParameter("json", Json)
            .addParameter("mismatchHandler", WidgetProtocolMismatchHandler)
            .build(),
        )
        .addProperty(
          PropertySpec.builder("delegate", widgetType, PRIVATE)
            .initializer("delegate")
            .build(),
        )
        .addProperty(
          PropertySpec.builder("json", Json, PRIVATE)
            .initializer("json")
            .build(),
        )
        .addProperty(
          PropertySpec.builder("mismatchHandler", WidgetProtocolMismatchHandler, PRIVATE)
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
          PropertySpec.builder("layoutModifiers", LayoutModifier, OVERRIDE)
            .mutable()
            .getter(
              FunSpec.getterBuilder()
                .addStatement("return delegate.layoutModifiers")
                .build(),
            )
            .setter(
              FunSpec.setterBuilder()
                .addParameter("value", LayoutModifier)
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
                        jsonElementToJsonPrimitive,
                        jsonPrimitiveToBoolean,
                      )
                      val parameterType = trait.parameterType?.asTypeName()
                      if (parameterType != null) {
                        val serializerId = serializerIds.computeIfAbsent(parameterType) {
                          nextSerializerId++
                        }
                        addStatement(
                          "{ eventSink.sendEvent(%T(diff.id, %LU, json.encodeToJsonElement(serializer_%L, it))) }",
                          eventType,
                          trait.tag,
                          serializerId,
                        )
                      } else {
                        addStatement("{ eventSink.sendEvent(%T(diff.id, %LU)) }", eventType, trait.tag)
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
                    PropertySpec.builder("serializer_$id", KSerializer.parameterizedBy(typeName))
                      .addModifiers(PRIVATE)
                      .initializer("json.serializersModule.%M()", serializer)
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
              .returns(childrenOfT.copy(nullable = true))
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
            .addParameter("value", JsonArray)
            .addStatement(
              """
              |layoutModifiers = value.fold<%1T, %2T>(%2T) { modifier, element ->
              |  modifier then element.%3M(json, mismatchHandler)
              |}
              """.trimMargin(),
              JsonElement,
              LayoutModifier,
              host.toLayoutModifier,
            )
            .build(),
        )
        .build(),
    )
    .build()
}

internal fun generateDiffConsumingLayoutModifier(schema: Schema, host: Schema = schema): FileSpec {
  return FileSpec.builder(schema.widgetPackage(host), "layoutModifierSerialization")
    .apply {
      if (schema === host) {
        addFunction(
          FunSpec.builder("toLayoutModifier")
            .addModifiers(INTERNAL)
            .receiver(JsonElement)
            .addParameter("json", Json)
            .addParameter("mismatchHandler", WidgetProtocolMismatchHandler)
            .returns(LayoutModifier)
            .addStatement("val array = %M", jsonArray)
            .addStatement("require(array.size == 2) { \"Layout modifier JSON array length != 2: \${array.size}\" }")
            .addStatement("")
            .beginControlFlow("val serializer = when (val tag = array[0].%M.%M)", jsonPrimitive, jsonInt)
            .apply {
              if (schema.layoutModifiers.isEmpty()) {
                addAnnotation(
                  AnnotationSpec.builder(Suppress::class)
                    .addMember("%S, %S, %S, %S", "UNUSED_PARAMETER", "UNUSED_EXPRESSION", "UNUSED_VARIABLE", "UNREACHABLE_CODE")
                    .build(),
                )
              }
              for (layoutModifier in schema.layoutModifiers) {
                val typeName = ClassName(schema.widgetPackage(host), layoutModifier.type.simpleName!! + "Impl")
                if (layoutModifier.properties.isEmpty()) {
                  addStatement("%L -> return %T", layoutModifier.tag, typeName)
                } else {
                  addStatement("%L -> %T.serializer()", layoutModifier.tag, typeName)
                }
              }
            }
            .beginControlFlow("else ->")
            .addStatement("mismatchHandler.onUnknownLayoutModifier(tag)")
            .addStatement("return %T", LayoutModifier)
            .endControlFlow()
            .endControlFlow()
            .addStatement("")
            .addStatement("val value = array[1].%M", jsonObject)
            .addStatement("return json.decodeFromJsonElement(serializer, value)")
            .build(),
        )
      }

      for (layoutModifier in schema.layoutModifiers) {
        val typeName =
          ClassName(schema.widgetPackage(host), layoutModifier.type.simpleName!! + "Impl")
        val typeBuilder = if (layoutModifier.properties.isEmpty()) {
          TypeSpec.objectBuilder(typeName)
        } else {
          TypeSpec.classBuilder(typeName)
            .addModifiers(DATA)
            .addAnnotation(Serializable)
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
                    .addAnnotation(Contextual)
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
