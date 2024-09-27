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

import app.cash.redwood.tooling.schema.Modifier
import app.cash.redwood.tooling.schema.ProtocolSchema
import app.cash.redwood.tooling.schema.ProtocolSchemaSet
import app.cash.redwood.tooling.schema.ProtocolWidget
import app.cash.redwood.tooling.schema.ProtocolWidget.ProtocolChildren
import app.cash.redwood.tooling.schema.ProtocolWidget.ProtocolEvent
import app.cash.redwood.tooling.schema.ProtocolWidget.ProtocolProperty
import app.cash.redwood.tooling.schema.Schema
import app.cash.redwood.tooling.schema.Widget
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.BYTE
import com.squareup.kotlinpoet.CHAR
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.DOUBLE
import com.squareup.kotlinpoet.FLOAT
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.KModifier.INTERNAL
import com.squareup.kotlinpoet.KModifier.OVERRIDE
import com.squareup.kotlinpoet.KModifier.PRIVATE
import com.squareup.kotlinpoet.KModifier.PUBLIC
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.SHORT
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.UNIT
import com.squareup.kotlinpoet.U_INT
import com.squareup.kotlinpoet.joinToCode

private val protocolViewType = UNIT

/*
object ExampleProtocolWidgetSystemFactory : ProtocolWidgetSystemFactory {
  override fun create(
    guestAdapter: GuestProtocolAdapter,
    mismatchHandler: ProtocolMismatchHandler,
  ): ExampleWidgetSystem {
    return ExampleWidgetSystem(
      Example = ProtocolExampleWidgetFactory(guestAdapter, mismatchHandler),
      RedwoodLayout = ProtocolRedwoodLayoutWidgetFactory(guestAdapter, mismatchHandler),
    )
  }

  public override fun modifierTag(element: Modifier.Element): ModifierTag { ... }

  public override fun <T : Modifier.Element> modifierTagAndSerializationStrategy(
    element: T
  ): Pair<ModifierTag, SerializationStrategy<T>?> { ... }
}
*/
internal fun generateProtocolWidgetSystemFactory(
  schemaSet: ProtocolSchemaSet,
): FileSpec {
  val schema = schemaSet.schema
  val type = ClassName(schema.guestProtocolPackage(), "${schema.type.flatName}ProtocolWidgetSystemFactory")
  val widgetSystemType = schema.getWidgetSystemType().parameterizedBy(protocolViewType)
  return buildFileSpec(type) {
    addAnnotation(suppressDeprecations)
    addType(
      TypeSpec.objectBuilder(type)
        .addSuperinterface(ProtocolGuest.ProtocolWidgetSystemFactory)
        .addFunction(
          FunSpec.builder("create")
            .optIn(Redwood.RedwoodCodegenApi)
            .addModifiers(OVERRIDE)
            .addParameter("guestAdapter", ProtocolGuest.GuestProtocolAdapter)
            .addParameter("mismatchHandler", ProtocolGuest.ProtocolMismatchHandler)
            .returns(widgetSystemType)
            .apply {
              val arguments = buildList {
                for (dependency in schemaSet.all) {
                  add(CodeBlock.of("%N = %T(guestAdapter, mismatchHandler)", dependency.type.flatName, dependency.protocolWidgetFactoryType(schema)))
                }
              }
              addStatement("return %T(\n%L)", schema.getWidgetSystemType(), arguments.joinToCode(separator = ",\n"))
            }
            .build(),
        )
        .addFunction(modifierTagAndSerializationStrategy(schemaSet))
        .build(),
    )
  }
}

/*
internal class ProtocolExampleWidgetFactory(
  private val guestAdapter: GuestProtocolAdapter,
  private val mismatchHandler: ProtocolMismatchHandler,
) : ExampleWidgetFactory<Unit> {
  override fun Box(): Box<Unit> {
    val widget = ProtocolExampleBox(guestAdapter, json, mismatchHandler)
    guestAdapter.appendCreate(widget.id, widget.tag)
    return widget
  }
  override fun Text(): Text<Unit> {
    return ProtocolExampleText(guestAdapter, json, mismatchHandler)
    guestAdapter.appendCreate(widget.id, widget.tag)
    return widget
  }
  override fun Button(): Button<Unit> {
    val widget = ProtocolExampleButton(guestAdapter, json, mismatchHandler)
    guestAdapter.appendCreate(widget.id, widget.tag)
    return widget
  }
}
*/
internal fun generateProtocolWidgetFactory(
  schema: ProtocolSchema,
  host: ProtocolSchema = schema,
): FileSpec {
  val type = schema.protocolWidgetFactoryType(host)
  return buildFileSpec(type) {
    addAnnotation(suppressDeprecations)
    addType(
      TypeSpec.classBuilder(type)
        .addModifiers(INTERNAL)
        .addSuperinterface(schema.getWidgetFactoryType().parameterizedBy(protocolViewType))
        .addAnnotation(Redwood.RedwoodCodegenApi)
        .primaryConstructor(
          FunSpec.constructorBuilder()
            .addParameter("guestAdapter", ProtocolGuest.GuestProtocolAdapter)
            .addParameter("mismatchHandler", ProtocolGuest.ProtocolMismatchHandler)
            .build(),
        )
        .addProperty(
          PropertySpec.builder("guestAdapter", ProtocolGuest.GuestProtocolAdapter, PRIVATE)
            .initializer("guestAdapter")
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
                .returns(schema.widgetType(widget).parameterizedBy(protocolViewType))
                .addStatement(
                  "val widget = %T(guestAdapter, mismatchHandler)",
                  schema.protocolWidgetType(widget, host),
                )
                .addStatement("guestAdapter.appendCreate(widget.id, widget.tag)")
                .addStatement("return widget")
                .build(),
            )
          }
          for (modifier in schema.unscopedModifiers) {
            addFunction(
              FunSpec.builder(modifier.type.flatName)
                .addModifiers(OVERRIDE)
                .addParameter("value", protocolViewType)
                .addParameter("modifier", schema.modifierType(modifier))
                .build(),
            )
          }
        }
        .build(),
    )
  }
}

/*
internal class ProtocolButton(
  private val guestAdapter: GuestProtocolAdapter,
  private val mismatchHandler: ProtocolMismatchHandler,
) : ProtocolWidget, Button<Unit> {
  public override val id: Id = guestAdapter.nextId()
  public override val tag: WidgetTag get() = WidgetTag(3)

  private var onClick: (() -> Unit)? = null

  private val serializer_0: KSerializer<String?> = guestAdapter.json.serializersModule.serializer()
  private val serializer_1: KSerializer<Boolean> = guestAdapter.json.serializersModule.serializer()

  override var modifier: Modifier
    get() = throw AssertionError()
    set(value) {
      guestAdapter.appendModifierChange(id, value)
    }

  override fun text(text: String?) {
    this.guestAdapter.appendPropertyChange(this.id, PropertyTag(1), serializer_0, text)
  }

  override fun onClick(onClick: (() -> Unit)?) {
    val onClickSet = onClick != null
    if (onClickSet != (this.onClick != null)) {
      guestAdapter.appendPropertyChange(this.id, PropertyTag(3), onClickSet)
    }
    this.onClick = onClick
  }

  override fun sendEvent(event: Event) {
    when (event.tag.value) {
      3 -> onClick?.invoke()
      else -> mismatchHandler.onUnknownEvent(12, event.tag)
    }
  }

  override fun depthFirstWalk(block: (ProtocolWidget, ChildrenTag, ProtocolWidgetChildren) -> Unit) {
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
  return buildFileSpec(type) {
    addAnnotation(suppressDeprecations)
    addType(
      TypeSpec.classBuilder(type)
        .addModifiers(INTERNAL)
        .addSuperinterface(ProtocolGuest.ProtocolWidget)
        .addSuperinterface(widgetName.parameterizedBy(protocolViewType))
        .addAnnotation(Redwood.RedwoodCodegenApi)
        .primaryConstructor(
          FunSpec.constructorBuilder()
            .addParameter("guestAdapter", ProtocolGuest.GuestProtocolAdapter)
            .addParameter("mismatchHandler", ProtocolGuest.ProtocolMismatchHandler)
            .build(),
        )
        .addProperty(
          PropertySpec.builder("guestAdapter", ProtocolGuest.GuestProtocolAdapter, PRIVATE)
            .initializer("guestAdapter")
            .build(),
        )
        .addProperty(
          PropertySpec.builder("mismatchHandler", ProtocolGuest.ProtocolMismatchHandler, PRIVATE)
            .initializer("mismatchHandler")
            .build(),
        )
        .addProperty(
          PropertySpec.builder("id", Protocol.Id, OVERRIDE)
            .initializer("guestAdapter.nextId()")
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
                addFunction(
                  FunSpec.builder(trait.name)
                    .addModifiers(OVERRIDE)
                    .addParameter(trait.name, traitTypeName)
                    .apply {
                      // Work around https://github.com/Kotlin/kotlinx.serialization/issues/2713.
                      if (traitTypeName == U_INT) {
                        addStatement(
                          "this.guestAdapter.appendPropertyChange(this.id, %T(%L), %N)",
                          Protocol.PropertyTag,
                          trait.tag,
                          trait.name,
                        )
                      } else {
                        val serializerId = serializerIds.computeIfAbsent(traitTypeName) {
                          nextSerializerId++
                        }
                        addStatement(
                          "this.guestAdapter.appendPropertyChange(this.id, %T(%L), serializer_%L, %N)",
                          Protocol.PropertyTag,
                          trait.tag,
                          serializerId,
                          trait.name,
                        )
                      }
                    }
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
                        "this.guestAdapter.appendPropertyChange(this.id, %T(%L), %L)",
                        Protocol.PropertyTag,
                        trait.tag,
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
                  PropertySpec.builder(trait.name, ProtocolGuest.ProtocolWidgetChildren)
                    .addModifiers(OVERRIDE)
                    .initializer("%T(id, %T(%L), guestAdapter)", ProtocolGuest.ProtocolWidgetChildren, Protocol.ChildrenTag, trait.tag)
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
                  for ((index, parameter) in event.parameters.withIndex()) {
                    val parameterType = parameter.type.asTypeName()
                    val serializerId = serializerIds.computeIfAbsent(parameterType) {
                      nextSerializerId++
                    }
                    arguments += CodeBlock.of(
                      "guestAdapter.json.decodeFromJsonElement(serializer_%L, event.args[%L])",
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
                .initializer("guestAdapter.json.serializersModule.%M()", KotlinxSerialization.serializer)
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
                .addStatement("guestAdapter.appendModifierChange(id, value)")
                .build(),
            )
            .build(),
        )
        .addFunction(
          FunSpec.builder("depthFirstWalk")
            .addModifiers(OVERRIDE)
            .addParameter("visitor", ProtocolGuest.ProtocolWidgetChildrenVisitor)
            .apply {
              for (trait in widget.traits) {
                if (trait is ProtocolChildren) {
                  if (workAroundLazyListPlaceholderRemoveCrash(widget, trait)) {
                    addComment("Work around the LazyList.placeholder remove crash.")
                    beginControlFlow("if (!guestAdapter.synthesizeSubtreeRemoval)")
                    addStatement("%N.depthFirstWalk(this, visitor)", trait.name)
                    endControlFlow()
                  } else {
                    addStatement("%N.depthFirstWalk(this, visitor)", trait.name)
                  }
                }
              }
            }
            .build(),
        )
        .build(),
    )
  }
}

private val placeholderParentTypeNames = listOf(
  listOf("app.cash.redwood.lazylayout", "LazyList"),
  listOf("app.cash.redwood.lazylayout", "RefreshableLazyList"),
)

/**
 * Returns true if this is the `LazyList.placeholder` trait, which had a severe bug in host code
 * by assuming `Widget.Children.remove()` is never be called. (This started crashing when we fixed
 * host-side memory leaks by removing guest-side children.)
 *
 * We work around this by not attempting to fix the host-side memory leak. This turns out to not
 * be a problem in practice anyway, because we never remove placeholders until we remove the
 * entire LazyList.
 */
private fun workAroundLazyListPlaceholderRemoveCrash(
  widget: ProtocolWidget,
  trait: ProtocolWidget.ProtocolTrait,
): Boolean = widget.type.names in placeholderParentTypeNames && trait.name == "placeholder"

/*
internal val GrowTagAndSerializer: Pair<ModifierTag, SerializationStrategy<Grow>?> =
    ModifierTag(1_000_001) to object : SerializationStrategy<Grow> {
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
}
*/
internal fun generateProtocolModifierSerializers(
  schema: ProtocolSchema,
  host: ProtocolSchema,
): FileSpec? {
  if (schema.modifiers.isEmpty()) {
    return null
  }
  return buildFileSpec(schema.guestProtocolPackage(host), "modifierSerializers") {
    addAnnotation(suppressDeprecations)

    for (modifier in schema.modifiers) {
      val serializerTagAndSerializer = schema.modifierTagAndSerializer(modifier, host)
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

      val serializationStrategyType =
        KotlinxSerialization.SerializationStrategy.parameterizedBy(modifierType)
      val serializationStrategyValue = if (modifier.properties.isNotEmpty()) {
        TypeSpec.anonymousClassBuilder()
          .addSuperinterface(serializationStrategyType)
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

                    initializer(
                      "%T(%L)",
                      KotlinxSerialization.ContextualSerializer,
                      parameters.joinToCode(),
                    )
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
          .build()
      } else {
        null
      }

      addProperty(
        PropertySpec.builder(
          name = serializerTagAndSerializer.simpleName,
          type = Stdlib.Pair.parameterizedBy(
            Protocol.ModifierTag,
            serializationStrategyType.copy(nullable = true),
          ),
          INTERNAL,
        )
          .initializer(
            "%T(%L) to %L",
            Protocol.ModifierTag,
            modifier.tag,
            serializationStrategyValue,
          )
          .build(),
      )
    }
  }
}

/*
@RedwoodCodegenApi
@Suppress("UNCHECKED_CAST")
public override fun <T : Modifier.Element> modifierTagAndSerializationStrategy(
  element: T,
): Pair<ModifierTag, SerializationStrategy<T>?> =
  when (element) {
    is CustomType -> CustomTypeTagAndSerializer
    is CustomTypeStateless -> CustomTypeStatelessTagAndSerializer
    else -> throw AssertionError()
  }
*/
internal fun modifierTagAndSerializationStrategy(
  schemaSet: ProtocolSchemaSet,
): FunSpec {
  val schema = schemaSet.schema
  val allModifiers = schemaSet.allModifiers()
  val t = TypeVariableName("T", Redwood.ModifierElement)
  val returnType = Stdlib.Pair.parameterizedBy(
    Protocol.ModifierTag,
    KotlinxSerialization.KSerializer.parameterizedBy(t).copy(nullable = true),
  )
  return FunSpec.builder("modifierTagAndSerializationStrategy")
    .addAnnotation(Redwood.RedwoodCodegenApi)
    .addAnnotation(
      AnnotationSpec.builder(Suppress::class)
        .addMember("%S", "UNCHECKED_CAST")
        .build(),
    )
    .addModifiers(PUBLIC, OVERRIDE)
    .addTypeVariable(t)
    .addParameter("element", t)
    .returns(returnType)
    .addCode("return when·(element)·{⇥\n")
    .apply {
      for ((localSchema, modifier) in allModifiers) {
        val modifierType = localSchema.modifierType(modifier)
        addStatement(
          "is %T -> %M",
          modifierType,
          localSchema.modifierTagAndSerializer(modifier, schema),
        )
      }
    }
    .addStatement("else -> throw %T()", Stdlib.AssertionError)
    .addCode("⇤} as %T\n", returnType)
    .build()
}

private fun Schema.protocolWidgetFactoryType(host: Schema): ClassName {
  return ClassName(guestProtocolPackage(host), "Protocol${type.flatName}WidgetFactory")
}

private fun Schema.protocolWidgetType(widget: Widget, host: Schema): ClassName {
  return ClassName(guestProtocolPackage(host), "Protocol${widget.type.flatName}")
}

private fun Schema.modifierTagAndSerializer(modifier: Modifier, host: Schema): MemberName {
  return MemberName(guestProtocolPackage(host), modifier.type.flatName + "TagAndSerializer")
}
