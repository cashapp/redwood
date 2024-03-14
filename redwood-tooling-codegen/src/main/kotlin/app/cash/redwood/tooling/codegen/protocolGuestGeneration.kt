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
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.BYTE
import com.squareup.kotlinpoet.CHAR
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.DOUBLE
import com.squareup.kotlinpoet.FLOAT
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.KModifier.INTERNAL
import com.squareup.kotlinpoet.KModifier.OVERRIDE
import com.squareup.kotlinpoet.KModifier.PRIVATE
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.NOTHING
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.SHORT
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.UNIT
import com.squareup.kotlinpoet.joinToCode

/*
class ExampleProtocolBridge private constructor(
  private val state: ProtocolState,
  private val mismatchHandler: ProtocolMismatchHandler,
  override val root: Widget.Children<Nothing>,
  override val widgetSystem: ExampleWidgetSystem<Nothing>,
) : ProtocolBridge {
  override fun getChangesOrNull(): List<Change>? = state.getChangesOrNull()
  override fun sendEvent(event: Event) {
    val node = state.getWidget(event.id)
    if (node != null) {
      node.sendEvent(event)
    } else {
      mismatchHandler.onUnknownEventNode(event.id, event.tag)
    }
  }

  companion object : ProtocolBridge.Factory {
    override fun create(
      json: Json,
      mismatchHandler: ProtocolMismatchHandler,
    ): ExampleProtocolBridge {
      val bridge = ProtocolBridge()
      val root = bridge.widgetChildren(Id.Root, ChildrenTag.Root)
      val widgetSystem = ExampleWidgetSystem(
          Example = ProtocolExampleWidgetFactory(bridge, json, mismatchHandler),
          RedwoodLayout = ProtocolRedwoodLayoutWidgetFactory(bridge, json, mismatchHandler),
      )
      return ExampleProtocolBridge(bridge, mismatchHandler, root, widgetSystem)
    }
  }
}
*/
internal fun generateProtocolBridge(
  schemaSet: ProtocolSchemaSet,
): FileSpec {
  val schema = schemaSet.schema
  val type = schema.protocolBridgeType()
  val widgetSystemType = schema.getWidgetSystemType().parameterizedBy(NOTHING)
  return FileSpec.builder(type)
    .addAnnotation(suppressDeprecations)
    .addType(
      TypeSpec.classBuilder(type)
        .addSuperinterface(ProtocolGuest.ProtocolBridge)
        .primaryConstructor(
          FunSpec.constructorBuilder()
            .addModifiers(PRIVATE)
            .addParameter("state", ProtocolGuest.ProtocolState)
            .addParameter("mismatchHandler", ProtocolGuest.ProtocolMismatchHandler)
            .addParameter("root", RedwoodWidget.WidgetChildren.parameterizedBy(NOTHING))
            .addParameter("widgetSystem", widgetSystemType)
            .build(),
        )
        .addProperty(
          PropertySpec.builder("state", ProtocolGuest.ProtocolState, PRIVATE)
            .initializer("state")
            .build(),
        )
        .addProperty(
          PropertySpec.builder("mismatchHandler", ProtocolGuest.ProtocolMismatchHandler, PRIVATE)
            .initializer("mismatchHandler")
            .build(),
        )
        .addProperty(
          PropertySpec.builder("root", RedwoodWidget.WidgetChildren.parameterizedBy(NOTHING), OVERRIDE)
            .initializer("root")
            .build(),
        )
        .addProperty(
          PropertySpec.builder("widgetSystem", widgetSystemType, OVERRIDE)
            .initializer("widgetSystem")
            .build(),
        )
        .addFunction(
          FunSpec.builder("getChangesOrNull")
            .addModifiers(OVERRIDE)
            .returns(LIST.parameterizedBy(Protocol.Change).copy(nullable = true))
            .addStatement("return state.getChangesOrNull()")
            .build(),
        )
        .addFunction(
          FunSpec.builder("sendEvent")
            .addModifiers(OVERRIDE)
            .returns(UNIT)
            .addParameter("event", Protocol.Event)
            .addStatement("val node = state.getWidget(event.id)")
            .beginControlFlow("if (node != null)")
            .addStatement("node.sendEvent(event)")
            .nextControlFlow("else")
            .addStatement("mismatchHandler.onUnknownEventNode(event.id, event.tag)")
            .endControlFlow()
            .build(),
        )
        .addType(
          TypeSpec.companionObjectBuilder()
            .addSuperinterface(ProtocolGuest.ProtocolBridgeFactory)
            .addFunction(
              FunSpec.builder("create")
                .addModifiers(OVERRIDE)
                .addParameter("json", KotlinxSerialization.Json)
                .addParameter("mismatchHandler", ProtocolGuest.ProtocolMismatchHandler)
                .returns(type)
                .addStatement("val state = %T()", ProtocolGuest.ProtocolState)
                .addStatement("val root = state.widgetChildren(%T.Root, %T.Root)", Protocol.Id, Protocol.ChildrenTag)
                .apply {
                  val arguments = buildList {
                    for (dependency in schemaSet.all) {
                      add(CodeBlock.of("%N = %T(state, json, mismatchHandler)", dependency.type.flatName, dependency.protocolWidgetFactoryType(schema)))
                    }
                  }
                  addStatement("val widgetSystem = %T(\n%L)", schema.getWidgetSystemType(), arguments.joinToCode(separator = ",\n"))
                }
                .addStatement("return %T(state, mismatchHandler, root, widgetSystem)", type)
                .build(),
            )
            .build(),
        )
        .build(),
    )
    .build()
}

/*
internal class ProtocolExampleWidgetFactory(
  private val state: ProtocolState,
  private val json: Json,
  private val mismatchHandler: ProtocolMismatchHandler,
) : ExampleWidgetFactory<Nothing> {
  override fun Box(): Box<Nothing> {
    val widget = ProtocolExampleBox(state, json, mismatchHandler)
    state.append(Create(widget.id, widget.tag))
    return widget
  }
  override fun Text(): Text<Nothing> {
    return ProtocolExampleText(state, json, mismatchHandler)
    state.append(Create(widget.id, widget.tag))
    return widget
  }
  override fun Button(): Button<Nothing> {
    val widget = ProtocolExampleButton(state, json, mismatchHandler)
    state.append(Create(widget.id, widget.tag))
    return widget
  }
}
*/
internal fun generateProtocolWidgetFactory(
  schema: ProtocolSchema,
  host: ProtocolSchema = schema,
): FileSpec {
  val type = schema.protocolWidgetFactoryType(host)
  return FileSpec.builder(type)
    .addAnnotation(suppressDeprecations)
    .addType(
      TypeSpec.classBuilder(type)
        .addModifiers(INTERNAL)
        .addSuperinterface(schema.getWidgetFactoryType().parameterizedBy(NOTHING))
        .primaryConstructor(
          FunSpec.constructorBuilder()
            .addParameter("state", ProtocolGuest.ProtocolState)
            .addParameter("json", KotlinxSerialization.Json)
            .addParameter("mismatchHandler", ProtocolGuest.ProtocolMismatchHandler)
            .build(),
        )
        .addProperty(
          PropertySpec.builder("state", ProtocolGuest.ProtocolState, PRIVATE)
            .initializer("state")
            .build(),
        )
        .addProperty(
          PropertySpec.builder("json", KotlinxSerialization.Json, PRIVATE)
            .initializer("json")
            .build(),
        )
        .addProperty(
          PropertySpec.builder("mismatchHandler", ProtocolGuest.ProtocolMismatchHandler, PRIVATE)
            .initializer("mismatchHandler")
            .build(),
        )
        .apply {
          for (widget in schema.widgets) {
            addFunction(
              FunSpec.builder(widget.type.flatName)
                .addModifiers(OVERRIDE)
                .returns(schema.widgetType(widget).parameterizedBy(NOTHING))
                .addStatement(
                  "val widget = %T(state, json, mismatchHandler)",
                  schema.protocolWidgetType(widget, host),
                )
                .addStatement("state.append(%T(widget.id, widget.tag))", Protocol.Create)
                .addStatement("return widget")
                .build(),
            )
          }
        }
        .build(),
    )
    .build()
}

/*
internal class ProtocolButton(
  private val state: ProtocolState,
  private val json: Json,
  private val mismatchHandler: ProtocolMismatchHandler,
) : ProtocolWidget, Button<Nothing> {
  public override val id: Id = state.nextId()
  public override val tag: WidgetTag get() = WidgetTag(3)

  private var onClick: (() -> Unit)? = null

  private val serializer_0: KSerializer<String?> = json.serializersModule.serializer()
  private val serializer_1: KSerializer<Boolean> = json.serializersModule.serializer()

  override var modifier: Modifier
    get() = throw AssertionError()
    set(value) {
      val json = buildJsonArray {
        value.forEach { element -> add(element.toJsonElement(json))
      }
      state.append(ModifierChange(id, json))
    }

  override fun text(text: String?) {
    state.append(PropertyChange(this.id, PropertyTag(1), json.encodeToJsonElement(serializer_0, text)))
  }

  override fun onClick(onClick: (() -> Unit)?) {
    val onClickSet = onClick != null
    if (onClickSet != (this.onClick != null)) {
      state.append(PropertyChange(this.id, PropertyTag(3), onClickSet))
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
internal fun generateProtocolWidget(
  schema: ProtocolSchema,
  widget: ProtocolWidget,
  host: ProtocolSchema = schema,
): FileSpec {
  val type = schema.protocolWidgetType(widget, host)
  val widgetName = schema.widgetType(widget)
  return FileSpec.builder(type)
    .addAnnotation(suppressDeprecations)
    .addType(
      TypeSpec.classBuilder(type)
        .addModifiers(INTERNAL)
        .addSuperinterface(ProtocolGuest.ProtocolWidget)
        .addSuperinterface(widgetName.parameterizedBy(NOTHING))
        .primaryConstructor(
          FunSpec.constructorBuilder()
            .addParameter("state", ProtocolGuest.ProtocolState)
            .addParameter("json", KotlinxSerialization.Json)
            .addParameter("mismatchHandler", ProtocolGuest.ProtocolMismatchHandler)
            .build(),
        )
        .addProperty(
          PropertySpec.builder("state", ProtocolGuest.ProtocolState, PRIVATE)
            .initializer("state")
            .build(),
        )
        .addProperty(
          PropertySpec.builder("json", KotlinxSerialization.Json, PRIVATE)
            .initializer("json")
            .build(),
        )
        .addProperty(
          PropertySpec.builder("mismatchHandler", ProtocolGuest.ProtocolMismatchHandler, PRIVATE)
            .initializer("mismatchHandler")
            .build(),
        )
        .addProperty(
          PropertySpec.builder("id", Protocol.Id, OVERRIDE)
            .initializer("state.nextId()")
            .build(),
        )
        .addProperty(
          PropertySpec.builder("tag", Protocol.WidgetTag, OVERRIDE)
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
              is ProtocolProperty -> {
                val traitTypeName = trait.type.asTypeName()
                val serializerId = serializerIds.computeIfAbsent(traitTypeName) {
                  nextSerializerId++
                }

                addFunction(
                  FunSpec.builder(trait.name)
                    .addModifiers(OVERRIDE)
                    .addParameter(trait.name, traitTypeName)
                    .addStatement(
                      "this.state.append(%T(this.id, %T(%L), json.encodeToJsonElement(serializer_%L, %N)))",
                      Protocol.PropertyChange,
                      Protocol.PropertyTag,
                      trait.tag,
                      serializerId,
                      trait.name,
                    )
                    .build(),
                )
              }

              is ProtocolEvent -> {
                addProperty(
                  PropertySpec.builder(trait.name, trait.lambdaType.copy(nullable = true), PRIVATE)
                    .mutable()
                    .initializer("null")
                    .build(),
                )
                if (trait.isNullable) {
                  addProperty(
                    PropertySpec.builder(trait.name + "_firstSet", BOOLEAN, PRIVATE)
                      .mutable()
                      .initializer("true")
                      .build(),
                  )
                }
                addFunction(
                  FunSpec.builder(trait.name)
                    .addModifiers(OVERRIDE)
                    .addParameter(trait.name, trait.lambdaType)
                    .apply {
                      val newValue = if (trait.isNullable) {
                        addStatement("val %1NSet = %1N != null", trait.name)
                        beginControlFlow("if (%1NSet != (this.%1N != null) || %1N_firstSet)", trait.name)
                        addStatement("%N_firstSet = false", trait.name)
                        trait.name + "Set"
                      } else {
                        beginControlFlow("if (this.%1N == null)", trait.name)
                        "true"
                      }
                      addStatement(
                        "this.state.append(%T(this.id, %T(%L), %M(%L)))",
                        Protocol.PropertyChange,
                        Protocol.PropertyTag,
                        trait.tag,
                        KotlinxSerialization.JsonPrimitive,
                        newValue,
                      )
                    }
                    .endControlFlow()
                    .addStatement("this.%1N = %1N", trait.name)
                    .build(),
                )
              }

              is ProtocolChildren -> {
                addProperty(
                  PropertySpec.builder(trait.name, RedwoodWidget.WidgetChildren.parameterizedBy(NOTHING))
                    .addModifiers(OVERRIDE)
                    .initializer("state.widgetChildren(id, %T(%L))", Protocol.ChildrenTag, trait.tag)
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
                for (event in widget.traits.filterIsInstance<ProtocolEvent>()) {
                  val arguments = mutableListOf<CodeBlock>()
                  for ((index, parameterFqType) in event.parameterTypes.withIndex()) {
                    val parameterType = parameterFqType.asTypeName()
                    val serializerId = serializerIds.computeIfAbsent(parameterType) {
                      nextSerializerId++
                    }
                    arguments += CodeBlock.of(
                      "json.decodeFromJsonElement(serializer_%L, event.args[%L])",
                      serializerId,
                      index,
                    )
                  }
                  addStatement(
                    "%L -> %N?.invoke(%L)",
                    event.tag,
                    event.name,
                    arguments.joinToCode(),
                  )
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
          PropertySpec.builder("modifier", Redwood.Modifier, OVERRIDE)
            .mutable()
            .getter(
              FunSpec.getterBuilder()
                .addStatement("throw %T()", Stdlib.AssertionError)
                .build(),
            )
            .setter(
              FunSpec.setterBuilder()
                .addParameter("value", Redwood.Modifier)
                .addStatement("state.append(%T(id, value.%M(json)))", Protocol.ModifierChange, host.modifierToProtocol)
                .build(),
            )
            .build(),
        )
        .build(),
    )
    .build()
}

/*
internal object GrowSerializer : KSerializer<Grow> {
  override val descriptor =
    buildClassSerialDescriptor("app.cash.redwood.layout.Grow") {
      element<Double>("value")
    }

  override fun serialize(encoder: Encoder, value: Grow) {
    encoder.encodeStructure(descriptor) {
      encodeDoubleElement(descriptor, 0, value.value)
    }
  }

  override fun deserialize(decoder: Decoder): Grow {
    throw AssertionError()
  }

  fun encode(json: Json, value: Grow): ModifierElement {
    val element = json.encodeToJsonElement(this, value)
    return ModifierElement(ModifierTag(3), element)
  }
}
*/
internal fun generateProtocolModifierSerializers(
  schema: ProtocolSchema,
  host: ProtocolSchema,
): FileSpec? {
  val serializableModifiers = schema.modifiers.filter { it.properties.isNotEmpty() }
  if (serializableModifiers.isEmpty()) {
    return null
  }
  return FileSpec.builder(schema.composePackage(host), "modifierSerializers")
    .addAnnotation(suppressDeprecations)
    .apply {
      for (modifier in serializableModifiers) {
        val serializerType = schema.modifierSerializer(modifier, host)
        val modifierType = schema.modifierType(modifier)

        var nextSerializerId = 0
        val serializerIds = mutableMapOf<TypeName, Int>()
        val serializables = mutableSetOf<TypeName>()

        val descriptorBody = CodeBlock.builder()
        val serializerBody = CodeBlock.builder()
        for ((index, property) in modifier.properties.withIndex()) {
          val propertyType = property.type.asTypeName()
          descriptorBody.addStatement("%M<%T>(%S)", KotlinxSerialization.element, propertyType, property.name)

          if (property.defaultExpression != null) {
            serializerBody.beginControlFlow(
              "if (composite.shouldEncodeElementDefault(descriptor, %L) || value.%N != %L)",
              index,
              property.name,
              property.defaultExpression,
            )
          }
          when (propertyType) {
            BOOLEAN -> serializerBody.addStatement(
              "composite.encodeBooleanElement(descriptor, %L, value.%N)",
              index,
              property.name,
            )

            BYTE -> serializerBody.addStatement(
              "composite.encodeByteElement(descriptor, %L, value.%N)",
              index,
              property.name,
            )

            CHAR -> serializerBody.addStatement(
              "composite.encodeCharElement(descriptor, %L, value.%N)",
              index,
              property.name,
            )

            SHORT -> serializerBody.addStatement(
              "composite.encodeShortElement(descriptor, %L, value.%N)",
              index,
              property.name,
            )

            INT -> serializerBody.addStatement(
              "composite.encodeIntElement(descriptor, %L, value.%N)",
              index,
              property.name,
            )

            LONG -> serializerBody.addStatement(
              "composite.encodeLongElement(descriptor, %L, value.%N)",
              index,
              property.name,
            )

            FLOAT -> serializerBody.addStatement(
              "composite.encodeFloatElement(descriptor, %L, value.%N)",
              index,
              property.name,
            )

            DOUBLE -> serializerBody.addStatement(
              "composite.encodeDoubleElement(descriptor, %L, value.%N)",
              index,
              property.name,
            )

            STRING -> serializerBody.addStatement(
              "composite.encodeStringElement(descriptor, %L, value.%N)",
              index,
              property.name,
            )

            else -> {
              val serializerId = serializerIds.computeIfAbsent(propertyType) {
                nextSerializerId++
              }
              if (property.isSerializable) {
                serializables += propertyType
              }
              serializerBody.addStatement(
                "composite.encodeSerializableElement(descriptor, %L, serializer_%L, value.%N)",
                index,
                serializerId,
                property.name,
              )
            }
          }
          if (property.defaultExpression != null) {
            serializerBody.endControlFlow()
          }
        }

        addType(
          TypeSpec.objectBuilder(serializerType)
            .addModifiers(INTERNAL)
            .addSuperinterface(KotlinxSerialization.KSerializer.parameterizedBy(modifierType))
            .addProperty(
              PropertySpec.builder("descriptor", KotlinxSerialization.SerialDescriptor)
                .addModifiers(OVERRIDE)
                .initializer(
                  CodeBlock.builder()
                    .beginControlFlow(
                      "%M(%S)",
                      KotlinxSerialization.buildClassSerialDescriptor,
                      modifierType.toString(),
                    )
                    .add(descriptorBody.build())
                    .endControlFlow()
                    .build(),
                )
                .build(),
            )
            .apply {
              for ((typeName, id) in serializerIds) {
                val typeSerializer = KotlinxSerialization.KSerializer.parameterizedBy(typeName)
                addProperty(
                  PropertySpec.builder("serializer_$id", typeSerializer)
                    .optIn(KotlinxSerialization.ExperimentalSerializationApi)
                    .addModifiers(PRIVATE)
                    .apply {
                      val parameters = mutableListOf(CodeBlock.of("%T::class", typeName))
                      if (typeName in serializables) {
                        parameters += CodeBlock.of("%T.serializer()", typeName)
                        parameters += CodeBlock.of("emptyArray()")
                      }

                      initializer("%T(%L)", KotlinxSerialization.ContextualSerializer, parameters.joinToCode())
                    }
                    .build(),
                )
              }
            }
            .addFunction(
              FunSpec.builder("serialize")
                .addModifiers(OVERRIDE)
                .addParameter("encoder", KotlinxSerialization.Encoder)
                .addParameter("value", modifierType)
                .apply {
                  if (modifier.properties.any { it.defaultExpression != null }) {
                    optIn(KotlinxSerialization.ExperimentalSerializationApi)
                  }
                }
                .addStatement("val composite = encoder.beginStructure(descriptor)")
                .addCode(serializerBody.build())
                .addStatement("composite.endStructure(descriptor)")
                .build(),
            )
            .addFunction(
              FunSpec.builder("deserialize")
                .addModifiers(OVERRIDE)
                .addParameter("decoder", KotlinxSerialization.Decoder)
                .returns(NOTHING)
                .addStatement("throw %T()", Stdlib.AssertionError)
                .build(),
            )
            .addFunction(
              FunSpec.builder("encode")
                .addParameter("json", KotlinxSerialization.Json)
                .addParameter("value", modifierType)
                .returns(Protocol.ModifierElement)
                .addStatement("val element = json.encodeToJsonElement(this, value)")
                .addStatement(
                  "return %T(%T(%L), element)",
                  Protocol.ModifierElement,
                  Protocol.ModifierTag,
                  modifier.tag,
                )
                .build(),
            )
            .build(),
        )
      }
    }
    .build()
}

internal fun generateComposeProtocolModifierSerialization(
  schemaSet: ProtocolSchemaSet,
): FileSpec {
  val schema = schemaSet.schema
  val name = schema.modifierToProtocol.simpleName
  return FileSpec.builder(schema.composePackage(), "modifierSerialization")
    .addAnnotation(suppressDeprecations)
    .addFunction(
      FunSpec.builder(name)
        .addModifiers(INTERNAL)
        .receiver(Redwood.Modifier)
        .addParameter("json", KotlinxSerialization.Json)
        .returns(LIST.parameterizedBy(Protocol.ModifierElement))
        .beginControlFlow("return %M", Stdlib.buildList)
        .addStatement(
          "this@%L.forEach { element -> add(element.%M(json)) }",
          name,
          schema.modifierToProtocol,
        )
        .endControlFlow()
        .build(),
    )
    .addFunction(
      FunSpec.builder(schema.modifierToProtocol)
        .addModifiers(PRIVATE)
        .receiver(Redwood.ModifierElement)
        .addParameter("json", KotlinxSerialization.Json)
        .returns(Protocol.ModifierElement)
        .beginControlFlow("return when (this)")
        .apply {
          val modifier = schemaSet.allModifiers()
          if (modifier.isEmpty()) {
            addAnnotation(
              AnnotationSpec.builder(Suppress::class)
                .addMember("%S, %S", "UNUSED_PARAMETER", "UNUSED_EXPRESSION")
                .build(),
            )
          } else {
            for ((localSchema, modifier) in modifier) {
              val modifierType = localSchema.modifierType(modifier)
              val surrogate = localSchema.modifierSerializer(modifier, schema)
              if (modifier.properties.isEmpty()) {
                addStatement(
                  "is %T -> %T(%T(%L))",
                  modifierType,
                  Protocol.ModifierElement,
                  Protocol.ModifierTag,
                  modifier.tag,
                )
              } else {
                addStatement("is %T -> %T.encode(json, this)", modifierType, surrogate)
              }
            }
          }
        }
        .addStatement("else -> throw %T()", Stdlib.AssertionError)
        .endControlFlow()
        .build(),
    )
    .build()
}
