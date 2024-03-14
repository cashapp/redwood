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
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.INTERNAL
import com.squareup.kotlinpoet.KModifier.OVERRIDE
import com.squareup.kotlinpoet.KModifier.PRIVATE
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.joinToCode

/*
@ObjCName("ExampleProtocolFactory", exact = true)
public class ExampleProtocolFactory<W : Any>(
  private val widgetSystem: ExampleWidgetSystem<W>,
  private val json: Json = Json.Default,
  private val mismatchHandler: ProtocolMismatchHandler = ProtocolMismatchHandler.Throwing,
) : GeneratedProtocolFactory<W> {
  override fun createNode(tag: WidgetTag): ProtocolNode<W>? = when (tag.value) {
    1 -> TextProtocolNode(delegate.Sunspot.Text(), json, mismatchHandler)
    2 -> ButtonProtocolNode(delegate.Sunspot.Button(), json, mismatchHandler)
    1_000_001 -> RedwoodLayoutRowProtocolNode(delegate.RedwoodLayout.Row(), json, mismatchHandler)
    1_000_002 -> RedwoodLayoutColumnProtocolNode(delegate.RedwoodLayout.Column(), json, mismatchHandler)
    else -> {
      mismatchHandler.onUnknownWidget(tag)
      null
    }
  }

  override fun createModifier(element: ModifierElement): Modifier {
    val serializer = when (element.tag) {
      1 -> AlignmentImpl.serializer()
      else -> {
        mismatchHandler.onUnknownModifier(element.tag)
        return Modifier
      }
    }
    return json.decodeFromJsonElement(serializer, element.value)
  }
}
*/
internal fun generateProtocolFactory(
  schemaSet: ProtocolSchemaSet,
): FileSpec {
  val schema = schemaSet.schema
  val widgetSystem = schema.getWidgetSystemType().parameterizedBy(typeVariableW)
  val type = schema.protocolFactoryType()
  return FileSpec.builder(type)
    .addAnnotation(suppressDeprecations)
    .addType(
      TypeSpec.classBuilder(type)
        .addTypeVariable(typeVariableW)
        .addSuperinterface(WidgetProtocol.GeneratedProtocolFactory.parameterizedBy(typeVariableW))
        .optIn(Stdlib.ExperimentalObjCName, Redwood.RedwoodCodegenApi)
        .addAnnotation(
          AnnotationSpec.builder(Stdlib.ObjCName)
            .addMember("%S", type.simpleName)
            .addMember("exact = true")
            .build(),
        )
        .primaryConstructor(
          FunSpec.constructorBuilder()
            .addParameter("widgetSystem", widgetSystem)
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
          PropertySpec.builder("widgetSystem", widgetSystem, PRIVATE)
            .initializer("widgetSystem")
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
          FunSpec.builder("createNode")
            .addModifiers(OVERRIDE)
            .addParameter("tag", Protocol.WidgetTag)
            .addAnnotation(Redwood.RedwoodCodegenApi)
            .returns(
              WidgetProtocol.ProtocolNode.parameterizedBy(typeVariableW)
                .copy(nullable = true),
            )
            .beginControlFlow("return when (tag.value)")
            .apply {
              for (dependency in schemaSet.all.sortedBy { it.widgets.firstOrNull()?.tag ?: 0 }) {
                for (widget in dependency.widgets.sortedBy { it.tag }) {
                  addStatement(
                    "%L -> %T(widgetSystem.%N.%N(), json, mismatchHandler)",
                    widget.tag,
                    dependency.protocolNodeType(widget, schema),
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
        .addFunction(
          FunSpec.builder("createModifier")
            .addModifiers(OVERRIDE)
            .addParameter("element", Protocol.ModifierElement)
            .returns(Redwood.Modifier)
            .apply {
              val modifiers = schemaSet.allModifiers()
              if (modifiers.isNotEmpty()) {
                beginControlFlow("val serializer = when (element.tag.value)")
                val host = schemaSet.schema
                for ((localSchema, modifier) in modifiers) {
                  val typeName = ClassName(localSchema.widgetPackage(host), modifier.type.flatName + "Impl")
                  if (modifier.properties.isEmpty()) {
                    addStatement("%L -> return %T", modifier.tag, typeName)
                  } else {
                    addStatement("%L -> %T.serializer()", modifier.tag, typeName)
                  }
                }
                beginControlFlow("else ->")
              }

              addStatement("mismatchHandler.onUnknownModifier(element.tag)")
              addStatement("return %T", Redwood.Modifier)

              if (modifiers.isNotEmpty()) {
                endControlFlow()
                endControlFlow()
                addStatement("return json.decodeFromJsonElement(serializer, element.value)")
              }
            }
            .build(),
        )
        .build(),
    )
    .build()
}

/*
internal class ProtocolButton<W : Any>(
  widget: Button<W>,
  private val json: Json,
  private val mismatchHandler: ProtocolMismatchHandler,
) : ProtocolNode<W>() {
  private var _widget: Button<W> = widget
  override val widget: Widget<W> get() = _widget

  private val serializer_0: KSerializer<String?> = json.serializersModule.serializer()
  private val serializer_1: KSerializer<Boolean> = json.serializersModule.serializer()

  public override fun apply(change: PropertyChange, eventSink: EventSink): Unit {
    when (change.tag.value) {
      1 -> _widget.text(json.decodeFromJsonElement(serializer_0, change.value))
      2 -> _widget.enabled(json.decodeFromJsonElement(serializer_1, change.value))
      3 -> {
        val onClick: (() -> Unit)? = if (change.value.jsonPrimitive.boolean) {
          { eventSink.sendEvent(Event(change.id, EventTag(3))) }
        } else {
          null
        }
        _widget.onClick(onClick)
      }
      else -> mismatchHandler.onUnknownProperty(WidgetTag(12), change.tag)
    }
  }

  public override fun children(tag: ChildrenTag): Widget.Children<W>? {
    mismatchHandler.onUnknownChildren(WidgetTag(2), tag)
    return null
  }
}
*/
internal fun generateProtocolNode(
  schema: ProtocolSchema,
  widget: ProtocolWidget,
  host: ProtocolSchema = schema,
): FileSpec {
  val type = schema.protocolNodeType(widget, host)
  val widgetType = schema.widgetType(widget).parameterizedBy(typeVariableW)
  val protocolType = WidgetProtocol.ProtocolNode.parameterizedBy(typeVariableW)
  val (childrens, properties) = widget.traits.partition { it is ProtocolChildren }
  return FileSpec.builder(type)
    .addAnnotation(suppressDeprecations)
    .addType(
      TypeSpec.classBuilder(type)
        .addModifiers(INTERNAL)
        .addTypeVariable(typeVariableW)
        .superclass(protocolType)
        .addAnnotation(Redwood.RedwoodCodegenApi)
        .primaryConstructor(
          FunSpec.constructorBuilder()
            .addParameter("widget", widgetType)
            .addParameter("json", KotlinxSerialization.Json)
            .addParameter("mismatchHandler", WidgetProtocol.ProtocolMismatchHandler)
            .build(),
        )
        .addProperty(
          PropertySpec.builder("_widget", widgetType, PRIVATE)
            .initializer("widget")
            .build(),
        )
        .addProperty(
          PropertySpec.builder("widget", RedwoodWidget.Widget.parameterizedBy(typeVariableW), OVERRIDE)
            .getter(
              FunSpec.getterBuilder()
                .addStatement("return _widget")
                .build(),
            )
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
          var nextSerializerId = 0
          val serializerIds = mutableMapOf<TypeName, Int>()

          addFunction(
            FunSpec.builder("apply")
              .addModifiers(OVERRIDE)
              .addParameter("change", Protocol.PropertyChange)
              .addParameter("eventSink", Protocol.EventSink)
              .beginControlFlow("when (change.tag.value)")
              .apply {
                for (trait in properties) {
                  when (trait) {
                    is ProtocolProperty -> {
                      val propertyType = trait.type.asTypeName()
                      val serializerId = serializerIds.computeIfAbsent(propertyType) {
                        nextSerializerId++
                      }

                      addStatement(
                        "%L -> _widget.%N(json.decodeFromJsonElement(serializer_%L, change.value))",
                        trait.tag,
                        trait.name,
                        serializerId,
                      )
                    }

                    is ProtocolEvent -> {
                      beginControlFlow("%L ->", trait.tag)
                      beginControlFlow(
                        "val %N: %T = if (change.value.%M.%M)",
                        trait.name,
                        trait.lambdaType,
                        KotlinxSerialization.jsonPrimitive,
                        KotlinxSerialization.jsonBoolean,
                      )
                      val arguments = mutableListOf<CodeBlock>()
                      for ((index, parameterFqType) in trait.parameterTypes.withIndex()) {
                        val parameterType = parameterFqType.asTypeName()
                        val serializerId = serializerIds.computeIfAbsent(parameterType) {
                          nextSerializerId++
                        }
                        arguments += CodeBlock.of(
                          "json.encodeToJsonElement(serializer_%L, arg%L)",
                          serializerId,
                          index,
                        )
                      }
                      if (trait.parameterTypes.isEmpty()) {
                        beginControlFlow("{")
                      } else {
                        beginControlFlow("{ %L ->", trait.parameterTypes.indices.map { CodeBlock.of("arg$it") }.joinToCode())
                      }
                      addStatement(
                        "eventSink.sendEvent(%T(change.id, %T(%L), listOf(%L)))",
                        Protocol.Event,
                        Protocol.EventTag,
                        trait.tag,
                        arguments.joinToCode(),
                      )
                      endControlFlow()

                      nextControlFlow("else")
                      if (trait.isNullable) {
                        addStatement("null")
                      } else {
                        addStatement("throw %T()", Stdlib.AssertionError)
                      }
                      endControlFlow()
                      addStatement("_widget.%1N(%1N)", trait.name)
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
                "else -> mismatchHandler.onUnknownProperty(%T(%L), change.tag)",
                Protocol.WidgetTag,
                widget.tag,
              )
              .endControlFlow()
              .build(),
          )

          for (children in childrens) {
            addProperty(
              PropertySpec.builder(children.name, WidgetProtocol.ProtocolChildren.parameterizedBy(typeVariableW))
                .addModifiers(PRIVATE)
                .initializer("%T(widget.%N)", WidgetProtocol.ProtocolChildren, children.name)
                .build(),
            )
          }
        }
        .addFunction(
          FunSpec.builder("children")
            .addModifiers(OVERRIDE)
            .addParameter("tag", Protocol.ChildrenTag)
            .returns(WidgetProtocol.ProtocolChildren.parameterizedBy(typeVariableW).copy(nullable = true))
            .apply {
              if (childrens.isNotEmpty()) {
                beginControlFlow("return when (tag.value)")
                for (children in childrens) {
                  addStatement("%L -> %N", children.tag, children.name)
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
        .build(),
    )
    .build()
}

internal fun generateProtocolModifierImpls(
  schema: ProtocolSchema,
  host: ProtocolSchema = schema,
): FileSpec? {
  if (schema.modifiers.isEmpty()) {
    return null
  }
  return FileSpec.builder(schema.widgetPackage(host), "modifierImpls")
    .addAnnotation(suppressDeprecations)
    .apply {
      for (modifier in schema.modifiers) {
        val typeName = ClassName(schema.widgetPackage(host), modifier.type.flatName + "Impl")
        val typeBuilder = if (modifier.properties.isEmpty()) {
          TypeSpec.objectBuilder(typeName)
        } else {
          TypeSpec.classBuilder(typeName)
            .addAnnotation(KotlinxSerialization.Serializable)
            .apply {
              val primaryConstructor = FunSpec.constructorBuilder()
              for (property in modifier.properties) {
                val propertyType = property.type.asTypeName()

                primaryConstructor.addParameter(
                  ParameterSpec.builder(property.name, propertyType)
                    .maybeDefaultValue(property.defaultExpression)
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
            .addSuperinterface(schema.modifierType(modifier))
            .addFunction(modifierEquals(schema, modifier))
            .addFunction(modifierHashCode(modifier))
            .addFunction(modifierToString(modifier))
            .build(),
        )
      }
    }
    .build()
}
