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
import app.cash.redwood.tooling.schema.ProtocolModifier
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
import com.squareup.kotlinpoet.BOOLEAN
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
import com.squareup.kotlinpoet.jvm.jvmField

/*
@ObjCName("ExampleProtocolFactory", exact = true)
public class ExampleProtocolFactory(
  override val json: Json = Json.Default,
  override val mismatchHandler: ProtocolMismatchHandler = ProtocolMismatchHandler.Throwing,
) : GeneratedHostProtocol {
  private val widgets: IntObjectMap<WidgetHostProtocol> =
      MutableIntObjectMap(4).apply {
        put(1, ButtonHostProtocol(json, mismatchHandler))
        put(3, TextHostProtocol(json, mismatchHandler))
        put(1_000_001, RowHostProtocol(json, mismatchHandler))
        put(1_000_002, ColumnHostProtocol(json, mismatchHandler))
      }

  override fun widget(tag: WidgetTag): WidgetHostProtocol? {
    widgets[tag.value]?.let { return it }
    mismatchHandler.onUnknownWidget(tag)
    return null
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
internal fun generateHostProtocol(
  schemaSet: ProtocolSchemaSet,
): FileSpec {
  val schema = schemaSet.schema
  val type = ClassName(schema.hostProtocolPackage(), "${schema.type.flatName}HostProtocol")
  return buildFileSpec(type) {
    addAnnotation(suppressDeprecations)
    addType(
      TypeSpec.classBuilder(type)
        .addSuperinterface(ProtocolHost.GeneratedHostProtocol)
        .optIn(Stdlib.ExperimentalObjCName, Redwood.RedwoodCodegenApi)
        .addAnnotation(
          AnnotationSpec.builder(Stdlib.ObjCName)
            .addMember("%S", type.simpleName)
            .addMember("exact = true")
            .build(),
        )
        .primaryConstructor(
          FunSpec.constructorBuilder()
            .addModifiers(PRIVATE)
            .addParameter("json", KotlinxSerialization.Json)
            .addParameter("mismatchHandler", ProtocolHost.ProtocolMismatchHandler)
            .build(),
        )
        .addProperty(
          PropertySpec.builder("json", KotlinxSerialization.Json, OVERRIDE)
            .initializer("json")
            .build(),
        )
        .addProperty(
          PropertySpec.builder("mismatchHandler", ProtocolHost.ProtocolMismatchHandler, OVERRIDE)
            .initializer("mismatchHandler")
            .build(),
        )
        .addProperty(
          PropertySpec.builder(
            "widgets",
            AndroidxCollection.IntObjectMap
              .parameterizedBy(ProtocolHost.WidgetHostProtocol),
            PRIVATE,
          )
            .initializer(
              buildCodeBlock {
                val allWidgets = schemaSet.all
                  .flatMap { schema -> schema.widgets.map { it to schema } }
                  .sortedBy { it.first.tag }

                beginControlFlow(
                  "%T<%T>(%L).apply",
                  AndroidxCollection.MutableIntObjectMap,
                  ProtocolHost.WidgetHostProtocol,
                  allWidgets.size,
                )
                for ((widget, widgetSchema) in allWidgets) {
                  addStatement(
                    "put(%L, %T(json, mismatchHandler))",
                    widget.tag,
                    schema.widgetHostProtocolType(widget, widgetSchema),
                  )
                }
                endControlFlow()
              },
            )
            .build(),
        )
        .addFunction(
          FunSpec.builder("widget")
            .addModifiers(OVERRIDE)
            .addParameter("tag", WidgetTag)
            .returns(ProtocolHost.WidgetHostProtocol.copy(nullable = true))
            .addStatement("widgets[tag.value]?.let { return it }")
            .addStatement("mismatchHandler.onUnknownWidget(tag)")
            .addStatement("return null")
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
                for ((modifierSchema, modifier) in modifiers) {
                  val typeName = schema.modifierImplType(modifier, modifierSchema)
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
        .addType(
          TypeSpec.companionObjectBuilder("Factory")
            .addSuperinterface(ProtocolHost.HostProtocolFactory)
            .addAnnotation(
              AnnotationSpec.builder(Stdlib.ObjCName)
                .addMember("%S", type.simpleName + "Factory")
                .addMember("exact = true")
                .build(),
            )
            .addFunction(
              FunSpec.builder("create")
                .addModifiers(OVERRIDE)
                .addParameter("json", KotlinxSerialization.Json)
                .addParameter("mismatchHandler", ProtocolHost.ProtocolMismatchHandler)
                .returns(type)
                .addStatement("return %T(json, mismatchHandler)", type)
                .build(),
            )
            .build(),
        )
        .build(),
    )
  }
}

/*
internal class ButtonHostProtocol<W : Any>(
  val json: Json,
  val mismatchHandler: ProtocolMismatchHandler,
) : WidgetHostProtocol {
  override val childrenTags: IntArray?
    get() = null

  val serializer_0: KSerializer<String?> = json.serializersModule.serializer()
  val serializer_1: KSerializer<Boolean> = json.serializersModule.serializer()

  override fun createNode(id: Id, widgetSystem: WidgetSystem<W>): ProtocolNode<W> {
    val schemaOwner = widgetSystem as RedwoodLayoutWidgetFactoryOwner<W>
    val widget = schemaOwner.RedwoodLayout.Box()
    return BoxProtocolNode(id, widget, this)
  }
}

private class ButtonProtocolNode<W : Any>(
  id: Id,
  widget: Button<W>,
  private val protocol: ButtonHostProtocol<W>,
) : ProtocolNode<W>(id) {
  override val widgetTag: WidgetTag get() = WidgetTag(4)

  private var _widget: Button<W>? = widget
  override val widget: Widget<W> get() = _widget ?: error("detached")

  public override fun apply(change: PropertyChange, eventSink: UiEventSink): Unit {
    val widget = _widget ?: error("detached")
    when (change.propertyTag.value) {
      1 -> widget.text(json.decodeFromJsonElement(protocol.serializer_0, change.value))
      2 -> widget.enabled(json.decodeFromJsonElement(protocol.serializer_1, change.value))
      3 -> {
        val onClick: (() -> Unit)? = if (change.value.jsonPrimitive.boolean) {
          OnClick(json, id, eventSink)
        } else {
          null
        }
        widget.onClick(onClick)
      }
      else -> mismatchHandler.onUnknownProperty(WidgetTag(12), change.propertyTag)
    }
  }

  public override fun children(tag: ChildrenTag): Widget.Children<W>? {
    mismatchHandler.onUnknownChildren(WidgetTag(2), tag)
    return null
  }

  public override fun detach() {
    _widget = null
  }
}
*/
internal fun generateProtocolNode(
  generatingSchema: ProtocolSchema,
  widgetSchema: ProtocolSchema,
  widget: ProtocolWidget,
): FileSpec {
  val widgetType = widgetSchema.widgetType(widget).parameterizedBy(typeVariableW)
  val widgetSystem = generatingSchema.getWidgetSystemType().parameterizedBy(typeVariableW)
  val widgetProtocolType = generatingSchema.widgetHostProtocolType(widget, widgetSchema)
  val widgetNodeType = widgetProtocolType.peerClass("${widget.type.flatName}ProtocolNode")

  var nextSerializerId = 0
  val serializerIds = mutableMapOf<TypeName, Int>()
  for (trait in widget.traits) {
    when (trait) {
      is ProtocolProperty -> {
        serializerIds.computeIfAbsent(trait.type.asTypeName()) {
          nextSerializerId++
        }
      }
      is ProtocolEvent -> {
        serializerIds.computeIfAbsent(BOOLEAN) {
          nextSerializerId++
        }
        for (parameter in trait.parameters) {
          serializerIds.computeIfAbsent(parameter.type.asTypeName()) {
            nextSerializerId++
          }
        }
      }
      is ProtocolChildren -> {
        // Nothing to do!
      }
    }
  }

  val (childrens, properties) = widget.traits.partition { it is ProtocolChildren }

  return buildFileSpec(widgetProtocolType) {
    addAnnotation(suppressDeprecations)
    addType(
      TypeSpec.classBuilder(widgetProtocolType)
        .addModifiers(INTERNAL)
        .addSuperinterface(ProtocolHost.WidgetHostProtocol)
        .addAnnotation(Redwood.RedwoodCodegenApi)
        .primaryConstructor(
          FunSpec.constructorBuilder()
            .addParameter("json", KotlinxSerialization.Json)
            .addParameter("mismatchHandler", ProtocolHost.ProtocolMismatchHandler)
            .build(),
        )
        .addProperty(
          PropertySpec.builder("json", KotlinxSerialization.Json)
            .initializer("json")
            .jvmField()
            .build(),
        )
        .addProperty(
          PropertySpec.builder("mismatchHandler", ProtocolHost.ProtocolMismatchHandler)
            .initializer("mismatchHandler")
            .jvmField()
            .build(),
        )
        .addProperty(
          PropertySpec.builder("childrenTags", INT_ARRAY.copy(nullable = true), OVERRIDE)
            .getter(
              FunSpec.getterBuilder()
                .apply {
                  val childrens = widget.traits.filterIsInstance<ProtocolChildren>()
                  if (childrens.isEmpty()) {
                    addStatement("return null")
                  } else {
                    addStatement(
                      "return %M(%L)",
                      MemberName("kotlin", "intArrayOf"),
                      childrens.joinToCode { CodeBlock.of("%L", it.tag) },
                    )
                  }
                }
                .build(),
            )
            .build(),
        )
        .apply {
          for ((typeName, id) in serializerIds) {
            addProperty(
              PropertySpec.builder(
                "serializer_$id",
                KotlinxSerialization.KSerializer.parameterizedBy(typeName),
              )
                .jvmField()
                .initializer("json.serializersModule.%M()", KotlinxSerialization.serializer)
                .build(),
            )
          }
        }
        .addFunction(
          FunSpec.builder("createNode")
            .addModifiers(OVERRIDE)
            .addTypeVariable(typeVariableW)
            .addParameter("id", Protocol.Id)
            .addParameter("widgetSystem", RedwoodWidget.WidgetSystem.parameterizedBy(typeVariableW))
            .returns(ProtocolHost.ProtocolNode.parameterizedBy(typeVariableW))
            .addStatement("val schemaWidgetSystem = widgetSystem as %T", widgetSystem)
            .addStatement("val widget = schemaWidgetSystem.%L.%L()", widgetSchema.type.flatName, widget.type.flatName)
            .addStatement("return %T(id, widget, this)", widgetNodeType)
            .build(),
        )
        .addFunction(
          FunSpec.builder("propertyDeserializer")
            .addModifiers(OVERRIDE)
            .addParameter("tag", Protocol.PropertyTag)
            .returns(KotlinxSerialization.DeserializationStrategy.parameterizedBy(ANY.copy(nullable = true)).copy(nullable = true))
            .beginControlFlow("return when (tag.value)")
            .apply {
              for (trait in widget.traits) {
                val traitType = when (trait) {
                  is ProtocolProperty -> trait.type.asTypeName()
                  is ProtocolEvent -> BOOLEAN
                  is ProtocolChildren -> continue
                }
                val serializerId = serializerIds.getValue(traitType)
                addStatement("%L -> serializer_%L", trait.tag, serializerId)
              }
            }
            .beginControlFlow("else ->")
            .addStatement(
              "mismatchHandler.onUnknownProperty(%T(%L), tag)",
              Protocol.WidgetTag,
              widget.tag,
            )
            .addStatement("null")
            .endControlFlow()
            .endControlFlow()
            .build(),
        )
        .build(),
    )
    addType(
      TypeSpec.classBuilder(widgetNodeType)
        .addModifiers(PRIVATE)
        .addTypeVariable(typeVariableW)
        .superclass(ProtocolHost.ProtocolNode.parameterizedBy(typeVariableW))
        .addAnnotation(Redwood.RedwoodCodegenApi)
        .primaryConstructor(
          FunSpec.constructorBuilder()
            .addParameter("id", Id)
            .addParameter("widget", widgetType)
            .addParameter("protocol", widgetProtocolType)
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
          PropertySpec.builder("protocol", widgetProtocolType, PRIVATE)
            .initializer("protocol")
            .build(),
        )
        .apply {
          for (trait in properties) {
            if (trait is ProtocolEvent) {
              addType(generateEventHandler(trait, widgetProtocolType, serializerIds))
            }
          }

          addFunction(
            FunSpec.builder("apply")
              .addModifiers(OVERRIDE)
              .addParameter("change", ProtocolHost.UiPropertyChange)
              .addParameter("eventSink", ProtocolHost.UiEventSink)
              .apply {
                if (properties.isNotEmpty()) {
                  addStatement("val widget = _widget ?: error(%S)", "detached")
                }
                beginControlFlow("when (change.tag.value)")
                for (trait in properties) {
                  when (trait) {
                    is ProtocolProperty -> {
                      addStatement(
                        "%L -> widget.%N(change.value as %T)",
                        trait.tag,
                        trait.name,
                        trait.type.asTypeName(),
                      )
                    }

                    is ProtocolEvent -> {
                      beginControlFlow("%L ->", trait.tag)
                      beginControlFlow(
                        "val %N: %T = if (change.value as %T)",
                        trait.name,
                        trait.lambdaType,
                        BOOLEAN,
                      )
                      if (trait.parameters.isEmpty()) {
                        addStatement(
                          "%L(id, eventSink)::invoke",
                          trait.eventHandlerName,
                        )
                      } else {
                        addStatement(
                          "%L(id, eventSink, protocol)::invoke",
                          trait.eventHandlerName,
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
              }
              .addStatement(
                "else -> protocol.mismatchHandler.onUnknownProperty(%T(%L), change.tag)",
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
                  "protocol.mismatchHandler.onUnknownChildren(%T(%L), tag)",
                  Protocol.WidgetTag,
                  widget.tag,
                )
                addStatement("null")
                endControlFlow()
                endControlFlow()
              } else {
                addStatement(
                  "protocol.mismatchHandler.onUnknownChildren(%T(%L), tag)",
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
  private val protocol: TextInputHostProtocol<*>,
) : (Int, String) -> Unit {
  override fun invoke(arg0: Int, arg1: String) {
    eventSink.sendEvent(
      GeneratedUiEvent(
        id,
        EventTag(3),
        protocol.json,
        arrayOf(
          arg0,
          arg1,
        ),
        arrayOf(
          protocol.serializer_2,
          protocol.serializer_5,
        ),
      )
    )
  }
}
*/
private fun generateEventHandler(
  trait: ProtocolEvent,
  widgetProtocolType: ClassName,
  serializerIds: Map<TypeName, Int>,
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
  if (trait.parameters.isNotEmpty()) {
    addConstructorParameterAndProperty(classBuilder, constructor, "protocol", widgetProtocolType)
  }

  val arguments = mutableListOf<CodeBlock>()
  val serializers = mutableListOf<CodeBlock>()
  for ((index, parameter) in trait.parameters.withIndex()) {
    val parameterType = parameter.type.asTypeName()
    val parameterName = parameter.name ?: "arg$index"

    invoke.addParameter(ParameterSpec(parameterName, parameterType))

    arguments += CodeBlock.of("%L", parameterName)

    val serializerId = serializerIds.getValue(parameterType)
    serializers += CodeBlock.of(
      "protocol.serializer_%L as %T",
      serializerId,
      KotlinxSerialization.SerializationStrategy.parameterizedBy(ANY.copy(nullable = true)),
    )
  }

  if (serializers.isEmpty()) {
    invoke.addCode(
      "eventSink.sendEvent(%T(id, %T(%L), null, null, null))",
      ProtocolHost.GeneratedUiEvent,
      Protocol.EventTag,
      trait.tag,
    )
  } else {
    invoke.addCode(
      "eventSink.sendEvent(⇥\n%T(⇥\nid,\n%T(%L),\nprotocol.json,\narrayOf(⇥\n%L,\n⇤),\narrayOf(⇥\n%L,\n⇤),\n⇤),\n⇤)",
      ProtocolHost.GeneratedUiEvent,
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
  generatingSchema: ProtocolSchema,
  modifierSchema: ProtocolSchema,
): FileSpec? {
  if (modifierSchema.modifiers.isEmpty()) {
    return null
  }
  val targetPackage = generatingSchema.hostProtocolPackage(modifierSchema)
  return buildFileSpec(targetPackage, "modifierImpls") {
    addAnnotation(suppressDeprecations)

    for (modifier in modifierSchema.modifiers) {
      val typeName = generatingSchema.modifierImplType(modifier, modifierSchema)
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
          .addSuperinterface(modifierSchema.modifierType(modifier))
          .addFunction(modifierEquals(modifierSchema, modifier))
          .addFunction(modifierHashCode(modifier))
          .addFunction(modifierToString(modifier))
          .build(),
      )
    }
  }
}

private fun Schema.widgetHostProtocolType(widget: Widget, widgetSchema: ProtocolSchema): ClassName {
  return ClassName(hostProtocolPackage(widgetSchema), "${widget.type.flatName}HostProtocol")
}

private fun Schema.modifierImplType(modifier: ProtocolModifier, modifierSchema: ProtocolSchema): ClassName {
  return ClassName(hostProtocolPackage(modifierSchema), "${modifier.type.flatName}Impl")
}
