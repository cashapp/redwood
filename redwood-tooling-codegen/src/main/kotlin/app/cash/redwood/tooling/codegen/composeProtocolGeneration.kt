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
import app.cash.redwood.tooling.schema.ProtocolWidget
import app.cash.redwood.tooling.schema.ProtocolWidget.ProtocolChildren
import app.cash.redwood.tooling.schema.ProtocolWidget.ProtocolEvent
import app.cash.redwood.tooling.schema.ProtocolWidget.ProtocolProperty
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.INTERNAL
import com.squareup.kotlinpoet.KModifier.OVERRIDE
import com.squareup.kotlinpoet.KModifier.PRIVATE
import com.squareup.kotlinpoet.KModifier.PUBLIC
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.NOTHING
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.UNIT
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.joinToCode

/*
class ExampleProtocolBridge(
  private val state: ProtocolState,
  private val mismatchHandler: ProtocolMismatchHandler,
  override val root: Widget.Children<Nothing>,
  override val provider: ExampleWidgetFactoryProvider<Nothing>,
) : ProtocolBridge {
  override fun createDiffOrNull(): Diff? = state.createDiffOrNull()
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
      val factories = ExampleWidgetFactories(
          Example = ProtocolExampleWidgetFactory(bridge, json, mismatchHandler),
          RedwoodLayout = ProtocolRedwoodLayoutWidgetFactory(bridge, json, mismatchHandler),
      )
      return ExampleProtocolBridge(bridge, mismatchHandler, root, factories)
    }
  }
}
*/
internal fun generateProtocolBridge(
  schema: ProtocolSchema,
): FileSpec {
  val type = schema.protocolBridgeType()
  val providerType = schema.getWidgetFactoryProviderType().parameterizedBy(NOTHING)
  return FileSpec.builder(type.packageName, type.simpleName)
    .addType(
      TypeSpec.classBuilder(type)
        .addSuperinterface(ComposeProtocol.ProtocolBridge)
        .primaryConstructor(
          FunSpec.constructorBuilder()
            .addParameter("state", ComposeProtocol.ProtocolState)
            .addParameter("mismatchHandler", ComposeProtocol.ProtocolMismatchHandler)
            .addParameter("root", RedwoodWidget.WidgetChildren.parameterizedBy(NOTHING))
            .addParameter("provider", providerType)
            .build(),
        )
        .addProperty(
          PropertySpec.builder("state", ComposeProtocol.ProtocolState, PRIVATE)
            .initializer("state")
            .build(),
        )
        .addProperty(
          PropertySpec.builder("mismatchHandler", ComposeProtocol.ProtocolMismatchHandler, PRIVATE)
            .initializer("mismatchHandler")
            .build(),
        )
        .addProperty(
          PropertySpec.builder("root", RedwoodWidget.WidgetChildren.parameterizedBy(NOTHING), OVERRIDE)
            .initializer("root")
            .build(),
        )
        .addProperty(
          PropertySpec.builder("provider", providerType, OVERRIDE)
            .initializer("provider")
            .build(),
        )
        .addFunction(
          FunSpec.builder("createDiffOrNull")
            .addModifiers(OVERRIDE)
            .returns(Protocol.Diff.copy(nullable = true))
            .addStatement("return state.createDiffOrNull()")
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
            .addSuperinterface(ComposeProtocol.ProtocolBridgeFactory)
            .addFunction(
              FunSpec.builder("create")
                .addModifiers(OVERRIDE)
                .addParameter("json", KotlinxSerialization.Json)
                .addParameter("mismatchHandler", ComposeProtocol.ProtocolMismatchHandler)
                .returns(type)
                .addStatement("val bridge = %T()", ComposeProtocol.ProtocolState)
                .addStatement("val root = bridge.widgetChildren(%T.Root, %T.Root)", Protocol.Id, Protocol.ChildrenTag)
                .apply {
                  val arguments = buildList<CodeBlock> {
                    for (dependency in schema.allSchemas) {
                      add(CodeBlock.of("%N = %T(bridge, json, mismatchHandler)", dependency.name, dependency.protocolWidgetFactoryType(schema)))
                    }
                  }
                  addStatement("val factories = %T(\n%L)", schema.getWidgetFactoriesType(), arguments.joinToCode(separator = ",\n"))
                }
                .addStatement("return %T(bridge, mismatchHandler, root, factories)", type)
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
  private val bridge: ProtocolBridge,
  private val json: Json,
  private val mismatchHandler: ProtocolMismatchHandler,
) : ExampleWidgetFactory<Nothing> {
  override fun Box(): Box<Nothing> = ProtocolExampleBox(bridge.nextId(), json, mismatchHandler)
  override fun Text(): Text<Nothing> = ProtocolExampleText(bridge.nextId(), json, mismatchHandler)
  override fun Button(): Button<Nothing> = ProtocolExampleButton(bridge.nextId(), json, mismatchHandler)
}
*/
internal fun generateProtocolWidgetFactory(
  schema: ProtocolSchema,
  host: ProtocolSchema = schema,
): FileSpec {
  val type = schema.protocolWidgetFactoryType(host)
  return FileSpec.builder(type.packageName, type.simpleName)
    .addType(
      TypeSpec.classBuilder(type)
        .addModifiers(INTERNAL)
        .addSuperinterface(schema.getWidgetFactoryType().parameterizedBy(NOTHING))
        .primaryConstructor(
          FunSpec.constructorBuilder()
            .addParameter("bridge", ComposeProtocol.ProtocolState)
            .addParameter("json", KotlinxSerialization.Json)
            .addParameter("mismatchHandler", ComposeProtocol.ProtocolMismatchHandler)
            .build(),
        )
        .addProperty(
          PropertySpec.builder("bridge", ComposeProtocol.ProtocolState, PRIVATE)
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
          for (widget in schema.widgets) {
            addFunction(
              FunSpec.builder(widget.type.flatName)
                .addModifiers(OVERRIDE)
                .returns(schema.widgetType(widget).parameterizedBy(NOTHING))
                .addStatement(
                  "return %T(bridge, json, mismatchHandler)",
                  schema.protocolWidgetType(widget, host),
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

  override var layoutModifiers: LayoutModifier
    get() = throw AssertionError()
    set(value) {
      val json = buildJsonArray {
        value.forEach { element -> add(element.toJsonElement(json))
      }
      state.append(LayoutModifiers(id, json))
    }

  override fun text(text: String?) {
    state.append(PropertyDiff(this.id, PropertyTag(1), json.encodeToJsonElement(serializer_0, text)))
  }

  override fun onClick(onClick: (() -> Unit)?) {
    val onClickSet = onClick != null
    if (onClickSet != (this.onClick != null)) {
      state.append(PropertyDiff(this.id, PropertyTag(3), onClickSet))
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
  return FileSpec.builder(type.packageName, type.simpleName)
    .addType(
      TypeSpec.classBuilder(type)
        .addModifiers(INTERNAL)
        .addSuperinterface(ComposeProtocol.ProtocolWidget)
        .addSuperinterface(widgetName.parameterizedBy(NOTHING))
        .primaryConstructor(
          FunSpec.constructorBuilder()
            .addParameter("state", ComposeProtocol.ProtocolState)
            .addParameter("json", KotlinxSerialization.Json)
            .addParameter("mismatchHandler", ComposeProtocol.ProtocolMismatchHandler)
            .build(),
        )
        .addProperty(
          PropertySpec.builder("state", ComposeProtocol.ProtocolState, PRIVATE)
            .initializer("state")
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
            .initializer("state.nextId()")
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
                      Protocol.PropertyDiff,
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
                      "this.state.append(%T(this.id, %T(%L), %M(%NSet)))",
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
              is ProtocolChildren -> {
                addProperty(
                  PropertySpec.builder(trait.name, RedwoodWidget.WidgetChildren.parameterizedBy(NOTHING))
                    .addModifiers(OVERRIDE)
                    .getter(
                      FunSpec.getterBuilder()
                        .addStatement(
                          "return state.widgetChildren(id, %T(%L))",
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
                for (event in widget.traits.filterIsInstance<ProtocolEvent>()) {
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
                .addStatement("state.append(%T(id, value.%M(json)))", Protocol.LayoutModifiers, host.layoutModifierToProtocol)
                .build(),
            )
            .build(),
        )
        .build(),
    )
    .build()
}

internal fun generateProtocolLayoutModifierSerialization(
  schema: ProtocolSchema,
): FileSpec {
  return FileSpec.builder(schema.composePackage(), "layoutModifierSerialization")
    .addFunction(generateToProtocolList(schema))
    .addFunction(generateToProtocol(schema))
    .build()
}

internal fun generateProtocolLayoutModifierSurrogates(
  schema: ProtocolSchema,
  host: ProtocolSchema,
): FileSpec {
  return FileSpec.builder(schema.composePackage(host), "layoutModifierSurrogates")
    .apply {
      for (layoutModifier in schema.layoutModifiers) {
        val surrogateName = schema.layoutModifierSurrogate(layoutModifier, host)
        val modifierType = schema.layoutModifierType(layoutModifier)

        addType(
          if (layoutModifier.properties.isEmpty()) {
            TypeSpec.objectBuilder(surrogateName)
              .addModifiers(INTERNAL)
              .addFunction(
                FunSpec.builder("encode")
                  .returns(Protocol.LayoutModifierElement)
                  .addStatement(
                    "return %T(%T(%L))",
                    Protocol.LayoutModifierElement,
                    Protocol.LayoutModifierTag,
                    layoutModifier.tag,
                  )
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
                      .returns(Protocol.LayoutModifierElement)
                      .addStatement("val element = json.encodeToJsonElement(serializer(), %T(value))", surrogateName)
                      .addStatement(
                        "return %T(%T(%L), element)",
                        Protocol.LayoutModifierElement,
                        Protocol.LayoutModifierTag,
                        layoutModifier.tag,
                      )
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

private fun generateToProtocolList(schema: ProtocolSchema): FunSpec {
  val name = schema.layoutModifierToProtocol.simpleName
  return FunSpec.builder(name)
    .addModifiers(INTERNAL)
    .receiver(Redwood.LayoutModifier)
    .addParameter("json", KotlinxSerialization.Json)
    .returns(LIST.parameterizedBy(Protocol.LayoutModifierElement))
    .beginControlFlow("return %M", Stdlib.buildList)
    .addStatement("this@%L.forEach { element -> add(element.%M(json)) }", name, schema.layoutModifierToProtocol)
    .endControlFlow()
    .build()
}

private fun generateToProtocol(schema: ProtocolSchema): FunSpec {
  return FunSpec.builder(schema.layoutModifierToProtocol.simpleName)
    .addModifiers(PRIVATE)
    .receiver(Redwood.LayoutModifierElement)
    .addParameter("json", KotlinxSerialization.Json)
    .returns(Protocol.LayoutModifierElement)
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
