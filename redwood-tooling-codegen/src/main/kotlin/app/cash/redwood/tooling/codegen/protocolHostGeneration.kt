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

import app.cash.redwood.tooling.codegen.Protocol.Id
import app.cash.redwood.tooling.codegen.Protocol.WidgetTag
import app.cash.redwood.tooling.schema.ProtocolSchema
import app.cash.redwood.tooling.schema.ProtocolSchemaSet
import app.cash.redwood.tooling.schema.ProtocolWidget
import app.cash.redwood.tooling.schema.ProtocolWidget.ProtocolChildren
import app.cash.redwood.tooling.schema.ProtocolWidget.ProtocolEvent
import app.cash.redwood.tooling.schema.ProtocolWidget.ProtocolProperty
import app.cash.redwood.tooling.schema.Schema
import app.cash.redwood.tooling.schema.Widget
import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.INT_ARRAY
import com.squareup.kotlinpoet.KModifier.INTERNAL
import com.squareup.kotlinpoet.KModifier.OVERRIDE
import com.squareup.kotlinpoet.KModifier.PRIVATE
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.buildCodeBlock
import com.squareup.kotlinpoet.joinToCode

/*
@ObjCName("ExampleProtocolFactory", exact = true)
public class ExampleProtocolFactory<W : Any>(
  override val widgetSystem: ExampleWidgetSystem<W>,
  private val json: Json = Json.Default,
  private val mismatchHandler: ProtocolMismatchHandler = ProtocolMismatchHandler.Throwing,
) : GeneratedProtocolFactory<W> {
  private val childrenTags: IntObjectMap<IntArray> = MutableIntObjectMap(4).apply {
        put(1, intArrayOf(1))
        put(3, intArrayOf(1))
        put(1_000_001, intArrayOf(1, 2))
        put(1_000_002, intArrayOf(1))
      }

  override fun widgetChildren(tag: WidgetTag): IntArray? {
    return childrenTags[tag.value]
  }

  override fun createNode(id: Id, tag: WidgetTag): ProtocolNode<W>? = when (tag.value) {
    1 -> TextProtocolNode(id, delegate.Sunspot.Text(), json, mismatchHandler)
    2 -> ButtonProtocolNode(id, delegate.Sunspot.Button(), json, mismatchHandler)
    1_000_001 -> RedwoodLayoutRowProtocolNode(id, delegate.RedwoodLayout.Row(), json, mismatchHandler)
    1_000_002 -> RedwoodLayoutColumnProtocolNode(id, delegate.RedwoodLayout.Column(), json, mismatchHandler)
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
  val type = ClassName(schema.hostProtocolPackage(), "${schema.type.flatName}ProtocolFactory")
  return buildFileSpec(type) {
    addAnnotation(suppressDeprecations)
    addType(
      TypeSpec.classBuilder(type)
        .addTypeVariable(typeVariableW)
        .addSuperinterface(ProtocolHost.GeneratedProtocolFactory.parameterizedBy(typeVariableW))
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
              ParameterSpec.builder("mismatchHandler", ProtocolHost.ProtocolMismatchHandler)
                .defaultValue("%T.Throwing", ProtocolHost.ProtocolMismatchHandler)
                .build(),
            )
            .build(),
        )
        .addProperty(
          PropertySpec.builder("widgetSystem", widgetSystem, OVERRIDE)
            .initializer("widgetSystem")
            .build(),
        )
        .addProperty(
          PropertySpec.builder("json", KotlinxSerialization.Json, PRIVATE)
            .initializer("json")
            .build(),
        )
        .addProperty(
          PropertySpec.builder("mismatchHandler", ProtocolHost.ProtocolMismatchHandler, PRIVATE)
            .initializer("mismatchHandler")
            .build(),
        )
        .addProperty(
          PropertySpec.builder(
            "childrenTags",
            AndroidxCollection.IntObjectMap.parameterizedBy(INT_ARRAY),
            PRIVATE,
          )
            .initializer(
              buildCodeBlock {
                val allParentWidgets = schemaSet.all
                  .flatMap { it.widgets }
                  .filter { it.traits.any { it is ProtocolChildren } }
                  .sortedBy { it.tag }

                beginControlFlow(
                  "%T<%T>(%L).apply",
                  AndroidxCollection.MutableIntObjectMap,
                  INT_ARRAY,
                  allParentWidgets.size,
                )
                for (parentWidget in allParentWidgets) {
                  addStatement(
                    "put(%L, %M(%L))",
                    parentWidget.tag,
                    MemberName("kotlin", "intArrayOf"),
                    parentWidget.traits
                      .filterIsInstance<ProtocolChildren>()
                      .sortedBy { it.tag }
                      .joinToCode { CodeBlock.of("%L", it.tag) },
                  )
                }
                endControlFlow()
              },
            )
            .build(),
        )
        .addFunction(
          FunSpec.builder("widgetChildren")
            .addModifiers(OVERRIDE)
            .addParameter("tag", WidgetTag)
            .returns(INT_ARRAY.copy(nullable = true))
            .addStatement("return childrenTags[tag.value]")
            .build(),
        )
        .addFunction(
          FunSpec.builder("createNode")
            .addModifiers(OVERRIDE)
            .addParameter("id", Id)
            .addParameter("tag", WidgetTag)
            .returns(
              ProtocolHost.ProtocolNode.parameterizedBy(typeVariableW)
                .copy(nullable = true),
            )
            .beginControlFlow("return when (tag.value)")
            .apply {
              for (dependency in schemaSet.all.sortedBy { it.widgets.firstOrNull()?.tag ?: 0 }) {
                for (widget in dependency.widgets.sortedBy { it.tag }) {
                  addStatement(
                    "%L -> %T(id, widgetSystem.%N.%N(), json, mismatchHandler)",
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
                  val typeName = ClassName(localSchema.hostProtocolPackage(host), modifier.type.flatName + "Impl")
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
  }
}

/*
internal class ProtocolButton<W : Any>(
  id: Id,
  widget: Button<W>,
  private val json: Json,
  private val mismatchHandler: ProtocolMismatchHandler,
) : ProtocolNode<W>(id) {
  override val widgetTag: WidgetTag get() = WidgetTag(4)

  private var _widget: Button<W>? = widget
  override val widget: Widget<W> get() = _widget ?: error("detached")

  private val serializer_0: KSerializer<String?> = json.serializersModule.serializer()
  private val serializer_1: KSerializer<Boolean> = json.serializersModule.serializer()

  public override fun apply(change: PropertyChange, eventSink: UiEventSink): Unit {
    val widget = _widget ?: error("detached")
    when (change.tag.value) {
      1 -> widget.text(json.decodeFromJsonElement(serializer_0, change.value))
      2 -> widget.enabled(json.decodeFromJsonElement(serializer_1, change.value))
      3 -> {
        val onClick: (() -> Unit)? = if (change.value.jsonPrimitive.boolean) {
          OnClick(json, id, eventSink)
        } else {
          null
        }
        widget.onClick(onClick)
      }
      else -> mismatchHandler.onUnknownProperty(WidgetTag(12), change.tag)
    }
  }

  public override fun children(tag: ChildrenTag): Widget.Children<W>? {
    mismatchHandler.onUnknownChildren(WidgetTag(2), tag)
    return null
  }

  public override fun visitIds(block: (Id) -> Unit) {
    block(id)
  }

  public override fun detach() {
    _widget = null
  }

  public override fun toString() = "ProtocolButton(id=$id, tag=12)
}
*/
internal fun generateProtocolNode(
  schema: ProtocolSchema,
  widget: ProtocolWidget,
  host: ProtocolSchema = schema,
): FileSpec {
  val type = schema.protocolNodeType(widget, host)
  val widgetType = schema.widgetType(widget).parameterizedBy(typeVariableW)
  val protocolType = ProtocolHost.ProtocolNode.parameterizedBy(typeVariableW)
  val (childrens, properties) = widget.traits.partition { it is ProtocolChildren }
  return buildFileSpec(type) {
    addAnnotation(suppressDeprecations)
    addType(
      TypeSpec.classBuilder(type)
        .addModifiers(INTERNAL)
        .addTypeVariable(typeVariableW)
        .superclass(protocolType)
        .addAnnotation(Redwood.RedwoodCodegenApi)
        .primaryConstructor(
          FunSpec.constructorBuilder()
            .addParameter("id", Id)
            .addParameter("widget", widgetType)
            .addParameter("json", KotlinxSerialization.Json)
            .addParameter("mismatchHandler", ProtocolHost.ProtocolMismatchHandler)
            .build(),
        )
        .addSuperclassConstructorParameter("id")
        .addProperty(
          PropertySpec.builder("widgetTag", WidgetTag, OVERRIDE)
            .getter(
              FunSpec.getterBuilder()
                .addStatement("return %T(%L)", WidgetTag, widget.tag)
                .build(),
            )
            .build(),
        )
        .addProperty(
          PropertySpec.builder("widgetName", STRING, OVERRIDE)
            .getter(
              FunSpec.getterBuilder()
                .addStatement("return %S", widget.type.flatName)
                .build(),
            )
            .build(),
        )
        .addProperty(
          PropertySpec.builder("_widget", widgetType.copy(nullable = true), PRIVATE)
            .mutable(true)
            .initializer("widget")
            .build(),
        )
        .addProperty(
          PropertySpec.builder("widget", RedwoodWidget.Widget.parameterizedBy(typeVariableW), OVERRIDE)
            .getter(
              FunSpec.getterBuilder()
                .addStatement("return _widget ?: error(%S)", "detached")
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
          PropertySpec.builder("mismatchHandler", ProtocolHost.ProtocolMismatchHandler, PRIVATE)
            .initializer("mismatchHandler")
            .build(),
        )
        .apply {
          var nextSerializerId = 0
          val serializerIds = mutableMapOf<TypeName, Int>()

          for (trait in properties) {
            if (trait is ProtocolEvent) {
              addType(generateEventHandler(trait))
            }
          }

          addFunction(
            FunSpec.builder("apply")
              .addModifiers(OVERRIDE)
              .addParameter("change", Protocol.PropertyChange)
              .addParameter("eventSink", ProtocolHost.UiEventSink)
              .apply {
                if (properties.isNotEmpty()) {
                  addStatement("val widget = _widget ?: error(%S)", "detached")
                }
                beginControlFlow("when (change.tag.value)")
                for (trait in properties) {
                  when (trait) {
                    is ProtocolProperty -> {
                      val propertyType = trait.type.asTypeName()
                      val serializerId = serializerIds.computeIfAbsent(propertyType) {
                        nextSerializerId++
                      }

                      addStatement(
                        "%L -> widget.%N(json.decodeFromJsonElement(serializer_%L, change.value))",
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
                      if (trait.parameters.isEmpty()) {
                        addStatement(
                          "%L(id, eventSink)::invoke",
                          trait.eventHandlerName,
                        )
                      } else {
                        val arguments = mutableListOf<CodeBlock>()
                        for (parameter in trait.parameters) {
                          val parameterType = parameter.type.asTypeName()
                          val serializerId = serializerIds.computeIfAbsent(parameterType) {
                            nextSerializerId++
                          }
                          arguments += CodeBlock.of("serializer_%L", serializerId)
                        }
                        addStatement(
                          "%L(id, eventSink, %L)::invoke",
                          trait.eventHandlerName,
                          arguments.joinToCode(),
                        )
                      }

                      nextControlFlow("else")
                      if (trait.isNullable) {
                        addStatement("null")
                      } else {
                        addStatement("throw %T()", Stdlib.AssertionError)
                      }
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
                "else -> mismatchHandler.onUnknownProperty(%T(%L), change.tag)",
                Protocol.WidgetTag,
                widget.tag,
              )
              .endControlFlow()
              .build(),
          )

          for (children in childrens) {
            addProperty(
              PropertySpec.builder(children.name, ProtocolHost.ProtocolChildren.parameterizedBy(typeVariableW))
                .addModifiers(PRIVATE)
                .initializer("%T(widget.%N)", ProtocolHost.ProtocolChildren, children.name)
                .build(),
            )
          }
        }
        .addFunction(
          FunSpec.builder("children")
            .addModifiers(OVERRIDE)
            .addParameter("tag", Protocol.ChildrenTag)
            .returns(ProtocolHost.ProtocolChildren.parameterizedBy(typeVariableW).copy(nullable = true))
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
        .apply {
          if (childrens.isNotEmpty()) {
            addFunction(
              FunSpec.builder("visitIds")
                .addModifiers(OVERRIDE)
                .addParameter("visitor", ProtocolHost.IdVisitor)
                .addStatement("visitor.visit(id)")
                .apply {
                  for (trait in widget.traits) {
                    if (trait is ProtocolChildren) {
                      addStatement("%N.visitIds(visitor)", trait.name)
                    }
                  }
                }
                .build(),
            )
          }
        }
        .addFunction(
          FunSpec.builder("detach")
            .addModifiers(OVERRIDE)
            .apply {
              for (trait in widget.traits) {
                if (trait is ProtocolChildren) {
                  addStatement("%N.detach()", trait.name)
                }
              }
            }
            .addStatement("_widget = null")
            .build(),
        )
        .build(),
    )
  }
}

/** Returns a class name like "OnClick". */
private val ProtocolEvent.eventHandlerName: String
  get() = name.replaceFirstChar { it.uppercase() }

/**
 * Generates a named event handler class. We do this instead of using a lambda to be explicit in
 * which variables are captured by the event handler. (This avoids problems when mixing
 * garbage-collected Kotlin objects with reference-counted Swift objects.)
 */
/*
private class OnClick(
  private val id: Id,
  private val eventSink: UiEventSink,
  private val serializer_0: KSerializer<Int>,
  private val serializer_1: KSerializer<String>,
) : (Int, String) -> Unit {
  override fun invoke(arg0: Int, arg1: String) {
    eventSink.sendEvent(
      UiEvent(
        id,
        EventTag(3),
        arrayOf(
          arg0,
          arg1,
        ),
        arrayOf(
          serializer_0,
          serializer_1,
        ),
      )
    )
  }
}
*/
private fun generateEventHandler(
  trait: ProtocolEvent,
): TypeSpec {
  val constructor = FunSpec.constructorBuilder()
  val invoke = FunSpec.builder("invoke")
    .addAnnotation(
      AnnotationSpec.builder(Suppress::class)
        .addMember("%S", "UNCHECKED_CAST")
        .build(),
    )

  val classBuilder = TypeSpec.classBuilder(trait.eventHandlerName)
    .addModifiers(PRIVATE)

  addConstructorParameterAndProperty(classBuilder, constructor, "id", Protocol.Id)
  addConstructorParameterAndProperty(classBuilder, constructor, "eventSink", ProtocolHost.UiEventSink)

  val arguments = mutableListOf<CodeBlock>()
  val serializers = mutableListOf<CodeBlock>()
  for ((index, parameter) in trait.parameters.withIndex()) {
    val parameterType = parameter.type.asTypeName()
    val serializerType = KotlinxSerialization.KSerializer.parameterizedBy(parameterType)
    val serializerId = "serializer_$index"
    val parameterName = "arg$index"

    addConstructorParameterAndProperty(classBuilder, constructor, serializerId, serializerType)
    invoke.addParameter(ParameterSpec(parameterName, parameterType))

    arguments += CodeBlock.of("%L", parameterName)
    serializers += CodeBlock.of(
      "%L as %T",
      serializerId,
      KotlinxSerialization.KSerializer.parameterizedBy(ANY.copy(nullable = true)),
    )
  }

  if (serializers.isEmpty()) {
    invoke.addCode(
      "eventSink.sendEvent(%T(id, %T(%L), null, null))",
      ProtocolHost.UiEvent,
      Protocol.EventTag,
      trait.tag,
    )
  } else {
    invoke.addCode(
      "eventSink.sendEvent(⇥\n%T(⇥\nid,\n%T(%L),\narrayOf(⇥\n%L,\n⇤),\narrayOf(⇥\n%L,\n⇤),\n⇤),\n⇤)",
      ProtocolHost.UiEvent,
      Protocol.EventTag,
      trait.tag,
      arguments.joinToCode(separator = ",\n"),
      serializers.joinToCode(separator = ",\n"),
    )
  }

  classBuilder.primaryConstructor(constructor.build())
  classBuilder.addFunction(invoke.build())
  return classBuilder.build()
}

/** Adds a constructor parameter and property with the same name. */
private fun addConstructorParameterAndProperty(
  classBuilder: TypeSpec.Builder,
  constructorBuilder: FunSpec.Builder,
  name: String,
  type: TypeName,
) {
  constructorBuilder.addParameter(
    ParameterSpec(
      name = name,
      type = type,
    ),
  )

  classBuilder.addProperty(
    PropertySpec.builder(
      name = name,
      type = type,
      modifiers = listOf(PRIVATE),
    ).initializer(name)
      .build(),
  )
}

internal fun generateProtocolModifierImpls(
  schema: ProtocolSchema,
  host: ProtocolSchema = schema,
): FileSpec? {
  if (schema.modifiers.isEmpty()) {
    return null
  }
  return buildFileSpec(schema.hostProtocolPackage(host), "modifierImpls") {
    addAnnotation(suppressDeprecations)

    for (modifier in schema.modifiers) {
      val typeName = ClassName(schema.hostProtocolPackage(host), modifier.type.flatName + "Impl")
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
}

private fun Schema.protocolNodeType(widget: Widget, host: Schema): ClassName {
  return ClassName(hostProtocolPackage(host), "Protocol${widget.type.flatName}")
}
