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
package app.cash.redwood.tooling.codegen

import app.cash.redwood.tooling.schema.ProtocolSchema
import app.cash.redwood.tooling.schema.ProtocolSchemaSet
import app.cash.redwood.tooling.schema.ProtocolWidget
import app.cash.redwood.tooling.schema.ProtocolWidget.ProtocolChildren
import app.cash.redwood.tooling.schema.ProtocolWidget.ProtocolEvent
import app.cash.redwood.tooling.schema.ProtocolWidget.ProtocolProperty
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.INTERNAL
import com.squareup.kotlinpoet.KModifier.OVERRIDE
import com.squareup.kotlinpoet.KModifier.PRIVATE
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec

/*
public class SunspotDiffConsumingNodeFactory<W : Any>(
  private val provider: SunspotWidgetFactoryProvider<W>,
  private val json: Json = Json.Default,
  private val mismatchHandler: ProtocolMismatchHandler = ProtocolMismatchHandler.Throwing,
) : DiffConsumingNode.Factory<W> {
  override fun create(
    parentId: Id,
    parentChildren: Widget.Children<W>,
    tag: WidgetTag,
  ): DiffConsumingNode<W>? = when (tag.value) {
    1 -> DiffConsumingTextNode(parentId, parentChildren, delegate.Sunspot.Text(), json, mismatchHandler)
    2 -> DiffConsumingButtonNode(parentId, parentChildren, delegate.Sunspot.Button(), json, mismatchHandler)
    1_000_001 -> DiffConsumingRedwoodLayoutRowNode(parentId, parentChildren, delegate.RedwoodLayout.Row(), json, mismatchHandler)
    1_000_002 -> DiffConsumingRedwoodLayoutColumnNode(parentId, parentChildren, delegate.RedwoodLayout.Column(), json, mismatchHandler)
    else -> {
      mismatchHandler.onUnknownWidget(tag)
      null
    }
  }
}
*/
internal fun generateDiffConsumingNodeFactory(
  schemaSet: ProtocolSchemaSet,
): FileSpec {
  val schema = schemaSet.schema
  val provider = schema.getWidgetFactoryProviderType().parameterizedBy(typeVariableW)
  val type = schema.diffConsumingNodeFactoryType()
  return FileSpec.builder(type)
    .addType(
      TypeSpec.classBuilder(type)
        .addTypeVariable(typeVariableW)
        .addSuperinterface(WidgetProtocol.DiffConsumingNodeFactory.parameterizedBy(typeVariableW))
        .primaryConstructor(
          FunSpec.constructorBuilder()
            .addParameter("provider", provider)
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
          PropertySpec.builder("provider", provider, PRIVATE)
            .initializer("provider")
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
        .addFunction(
          FunSpec.builder("create")
            .addModifiers(OVERRIDE)
            .addParameter("parentId", Protocol.Id)
            .addParameter("parentChildren", RedwoodWidget.WidgetChildrenOfW)
            .addParameter("tag", Protocol.WidgetTag)
            .returns(
              WidgetProtocol.DiffConsumingNode.parameterizedBy(typeVariableW)
                .copy(nullable = true),
            )
            .beginControlFlow("return when (tag.value)")
            .apply {
              for (dependency in schemaSet.all.sortedBy { it.widgets.firstOrNull()?.tag ?: 0 }) {
                for (widget in dependency.widgets.sortedBy { it.tag }) {
                  addStatement(
                    "%L -> %T(parentId, parentChildren, provider.%N.%N(), json, mismatchHandler)",
                    widget.tag,
                    dependency.diffConsumingNodeType(widget, schema),
                    dependency.type.flatName,
                    widget.type.flatName,
                  )
                }
              }
            }
            .beginControlFlow("else ->")
            .addStatement("mismatchHandler.onUnknownWidget(tag)")
            .addStatement("null")
            .endControlFlow()
            .endControlFlow()
            .build(),
        )
        .build(),
    )
    .build()
}

/*
internal class DiffConsumingSunspotButton<W : Any>(
  private val delegate: SunspotButton<W>,
  private val json: Json,
  private val mismatchHandler: ProtocolMismatchHandler,
) : DiffConsumingWidget<W> {
  public override val value: W get() = delegate.value

  public override val layoutModifiers: LayoutModifier
    get() = delegate.layoutModifiers
    set(value) { delegate.layoutModifiers = value }

  private val serializer_0: KSerializer<String?> = json.serializersModule.serializer()
  private val serializer_1: KSerializer<Boolean> = json.serializersModule.serializer()

  public override fun apply(diff: PropertyDiff, eventSink: EventSink): Unit {
    when (diff.tag.value) {
      1 -> delegate.text(json.decodeFromJsonElement(serializer_0, diff.value))
      2 -> delegate.enabled(json.decodeFromJsonElement(serializer_1, diff.value))
      3 -> {
        val onClick: (() -> Unit)? = if (diff.value.jsonPrimitive.boolean) {
          { eventSink.sendEvent(Event(diff.id, EventTag(3))) }
        } else {
          null
        }
        delegate.onClick(onClick)
      }
      else -> mismatchHandler.onUnknownProperty(WidgetTag(12), diff.tag)
    }
  }
}
*/
internal fun generateDiffConsumingWidget(
  schema: ProtocolSchema,
  widget: ProtocolWidget,
  host: ProtocolSchema = schema,
): FileSpec {
  val type = schema.diffConsumingNodeType(widget, host)
  val widgetType = schema.widgetType(widget).parameterizedBy(typeVariableW)
  val protocolType = WidgetProtocol.DiffConsumingNode.parameterizedBy(typeVariableW)
  return FileSpec.builder(type)
    .addType(
      TypeSpec.classBuilder(type)
        .addModifiers(INTERNAL)
        .addTypeVariable(typeVariableW)
        .superclass(protocolType)
        .primaryConstructor(
          FunSpec.constructorBuilder()
            .addParameter("parentId", Protocol.Id)
            .addParameter("parentChildren", RedwoodWidget.WidgetChildrenOfW)
            .addParameter("widget", widgetType)
            .addParameter("json", KotlinxSerialization.Json)
            .addParameter("mismatchHandler", WidgetProtocol.ProtocolMismatchHandler)
            .build(),
        )
        .addSuperclassConstructorParameter("parentId")
        .addSuperclassConstructorParameter("parentChildren")
        .addProperty(
          PropertySpec.builder("widget", widgetType, OVERRIDE)
            .initializer("widget")
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
          val (childrens, properties) = widget.traits.partition { it is ProtocolChildren }
          var nextSerializerId = 0
          val serializerIds = mutableMapOf<TypeName, Int>()

          addFunction(
            FunSpec.builder("apply")
              .addModifiers(OVERRIDE)
              .addParameter("diff", Protocol.PropertyDiff)
              .addParameter("eventSink", Protocol.EventSink)
              .beginControlFlow("when (diff.tag.value)")
              .apply {
                for (trait in properties) {
                  when (trait) {
                    is ProtocolProperty -> {
                      val propertyType = trait.type.asTypeName()
                      val serializerId = serializerIds.computeIfAbsent(propertyType) {
                        nextSerializerId++
                      }

                      addStatement(
                        "%L -> widget.%N(json.decodeFromJsonElement(serializer_%L, diff.value))",
                        trait.tag,
                        trait.name,
                        serializerId,
                      )
                    }

                    is ProtocolEvent -> {
                      beginControlFlow("%L ->", trait.tag)
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
                          "{ eventSink.sendEvent(%T(diff.id, %T(%L), json.encodeToJsonElement(serializer_%L, it))) }",
                          Protocol.Event,
                          Protocol.EventTag,
                          trait.tag,
                          serializerId,
                        )
                      } else {
                        addStatement(
                          "{ eventSink.sendEvent(%T(diff.id, %T(%L))) }",
                          Protocol.Event,
                          Protocol.EventTag,
                          trait.tag,
                        )
                      }
                      nextControlFlow("else")
                      addStatement("null")
                      endControlFlow()
                      addStatement("widget.%1N(%1N)", trait.name)
                      endControlFlow()
                    }

                    is ProtocolChildren -> throw AssertionError()
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
              .addStatement(
                "else -> mismatchHandler.onUnknownProperty(%T(%L), diff.tag)",
                Protocol.WidgetTag,
                widget.tag,
              )
              .endControlFlow()
              .build(),
          )

          addFunction(
            FunSpec.builder("children")
              .addModifiers(OVERRIDE)
              .addParameter("tag", Protocol.ChildrenTag)
              .returns(RedwoodWidget.WidgetChildrenOfW.copy(nullable = true))
              .apply {
                if (childrens.isNotEmpty()) {
                  beginControlFlow("return when (tag.value)")
                  for (children in childrens) {
                    addStatement("%L -> widget.%N", children.tag, children.name)
                  }
                  beginControlFlow("else ->")
                  addStatement(
                    "mismatchHandler.onUnknownChildren(%T(%L), tag)",
                    Protocol.WidgetTag,
                    widget.tag,
                  )
                  addStatement("null")
                  endControlFlow()
                  endControlFlow()
                } else {
                  addStatement(
                    "mismatchHandler.onUnknownChildren(%T(%L), tag)",
                    Protocol.WidgetTag,
                    widget.tag,
                  )
                  addStatement("return null")
                }
              }
              .build(),
          )
        }
        .addFunction(
          FunSpec.builder("updateLayoutModifier")
            .addModifiers(OVERRIDE)
            .addParameter("elements", LIST.parameterizedBy(Protocol.LayoutModifierElement))
            .addStatement("widget.layoutModifiers = elements.%M(json, mismatchHandler)", host.toLayoutModifier)
            .build(),
        )
        .build(),
    )
    .build()
}

internal fun generateDiffConsumingLayoutModifierSerialization(
  schemaSet: ProtocolSchemaSet,
): FileSpec {
  return FileSpec.builder(schemaSet.schema.widgetPackage(), "layoutModifierSerialization")
    .addFunction(generateJsonArrayToLayoutModifier(schemaSet.schema))
    .addFunction(generateJsonElementToLayoutModifier(schemaSet))
    .build()
}

internal fun generateDiffConsumingLayoutModifierImpls(
  schema: ProtocolSchema,
  host: ProtocolSchema = schema,
): FileSpec {
  return FileSpec.builder(schema.widgetPackage(host), "layoutModifierImpls")
    .apply {
      for (layoutModifier in schema.layoutModifiers) {
        val typeName = ClassName(schema.widgetPackage(host), layoutModifier.type.flatName + "Impl")
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
            .addModifiers(INTERNAL)
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

private fun generateJsonArrayToLayoutModifier(schema: ProtocolSchema): FunSpec {
  return FunSpec.builder("toLayoutModifier")
    .addModifiers(INTERNAL)
    .receiver(LIST.parameterizedBy(Protocol.LayoutModifierElement))
    .addParameter("json", KotlinxSerialization.Json)
    .addParameter("mismatchHandler", WidgetProtocol.ProtocolMismatchHandler)
    .addStatement(
      """
      |return fold<%1T, %2T>(%2T) { modifier, element ->
      |  modifier then element.%3M(json, mismatchHandler)
      |}
      """.trimMargin(),
      Protocol.LayoutModifierElement,
      Redwood.LayoutModifier,
      schema.toLayoutModifier,
    )
    .returns(Redwood.LayoutModifier)
    .build()
}

private fun generateJsonElementToLayoutModifier(schemaSet: ProtocolSchemaSet): FunSpec {
  return FunSpec.builder("toLayoutModifier")
    .addModifiers(PRIVATE)
    .receiver(Protocol.LayoutModifierElement)
    .addParameter("json", KotlinxSerialization.Json)
    .addParameter("mismatchHandler", WidgetProtocol.ProtocolMismatchHandler)
    .returns(Redwood.LayoutModifier)
    .beginControlFlow("val serializer = when (tag.value)")
    .apply {
      val layoutModifiers = schemaSet.allLayoutModifiers()
      if (layoutModifiers.isEmpty()) {
        addAnnotation(
          AnnotationSpec.builder(Suppress::class)
            .addMember("%S, %S, %S, %S", "UNUSED_PARAMETER", "UNUSED_EXPRESSION", "UNUSED_VARIABLE", "UNREACHABLE_CODE")
            .build(),
        )
      } else {
        val host = schemaSet.schema
        for ((localSchema, layoutModifier) in layoutModifiers) {
          val typeName = ClassName(localSchema.widgetPackage(host), layoutModifier.type.flatName + "Impl")
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
    .addStatement("return json.decodeFromJsonElement(serializer, value)")
    .build()
}
