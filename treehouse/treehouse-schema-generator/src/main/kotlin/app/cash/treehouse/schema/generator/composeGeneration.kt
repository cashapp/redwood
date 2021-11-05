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
package app.cash.treehouse.schema.generator

import app.cash.exhaustive.Exhaustive
import app.cash.treehouse.schema.parser.Children
import app.cash.treehouse.schema.parser.Event
import app.cash.treehouse.schema.parser.Property
import app.cash.treehouse.schema.parser.Schema
import app.cash.treehouse.schema.parser.Widget
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.OVERRIDE
import com.squareup.kotlinpoet.KModifier.PRIVATE
import com.squareup.kotlinpoet.KModifier.PUBLIC
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.joinToCode
import com.squareup.kotlinpoet.jvm.jvmField

/*
@Composable
fun Button(
  text: String,
  enabled: Boolean = true,
  onClick: (() -> Unit)? = null,
) {
  ComposeNode<ButtonComposeNode, ProtocolApplier>(
    factory = ::ButtonNode,
    update = {
      set(text) {
        appendDiff(PropertyDiff(id, 1, text))
      }
      set(enabled) {
        appendDiff(PropertyDiff(id, 2, enabled))
      }
      set(onClick) {
        val onClickSet = onClick != null
        if (onClickSet != (this.onClick != null)) {
          appendDiff(PropertyDiff(id, 3, onClickSet))
        }
        this.onClick = onClick
      }
    }
  )
}

private class ButtonComposeNode(id: Long) : Node(id, 2) {
  var onClick: (() -> Unit)? = null

  override fun sendEvent(event: Event) {
    when (event.eventId) {
      1L -> onClick?.invoke()
      else -> throw IllegalArgumentException("Unknown event ID ${event.eventId}")
    }
  }
}
*/
internal fun generateComposeNode(schema: Schema, widget: Widget): FileSpec {
  val events = widget.traits.filterIsInstance<Event>()
  val nodeType = if (events.isEmpty()) {
    protocolNode
  } else {
    schema.composeNodeType(widget)
  }
  val applierOfServerNode = applier.parameterizedBy(protocolNode)
  return FileSpec.builder(schema.composePackage, widget.flatName)
    .addFunction(
      FunSpec.builder(widget.flatName)
        .addModifiers(PUBLIC)
        .addAnnotation(composable)
        .apply {
          for (trait in widget.traits) {
            addParameter(
              when (trait) {
                is Property -> {
                  ParameterSpec.builder(trait.name, trait.type.asTypeName())
                    .apply {
                      trait.defaultExpression?.let { defaultValue(it) }
                    }
                    .build()
                }
                is Event -> {
                  ParameterSpec.builder(trait.name, trait.lambdaType)
                    .defaultValue("null")
                    .build()
                }
                is Children -> {
                  ParameterSpec.builder(trait.name, composableLambda)
                    .build()
                }
              }
            )
          }

          val arguments = mutableListOf<CodeBlock>()

          arguments += CodeBlock.builder()
            .add("factory = ")
            .apply {
              if (events.isEmpty()) {
                add("{\n⇥%T(%L)\n⇤}", nodeType, widget.tag)
              } else {
                add("::%T", nodeType)
              }
            }
            .build()

          val updateLambda = CodeBlock.builder()
          val childrenLambda = CodeBlock.builder()
          for (trait in widget.traits) {
            @Exhaustive when (trait) {
              is Property -> {
                updateLambda.apply {
                  add("set(%N) {\n", trait.name)
                  indent()
                  add(
                    "appendDiff(%T(this.id, %L, %L))\n",
                    propertyDiff,
                    trait.tag,
                    trait.type.jsonEncode("%N", trait.name)
                  )
                  unindent()
                  add("}\n")
                }
              }
              is Event -> {
                updateLambda.apply {
                  add("set(%N) {\n", trait.name)
                  indent()
                  add("val %1NSet = %1N != null\n", trait.name)
                  add("if (%1NSet != (this.%1N != null)) {\n", trait.name)
                  indent()
                  add(
                    "appendDiff(%T(this.id, %L, %L))\n",
                    propertyDiff,
                    trait.tag,
                    BOOLEAN.jsonEncode("%NSet", trait.name)
                  )
                  unindent()
                  add("}\n")
                  add("this.%1N = %1N\n", trait.name)
                  unindent()
                  add("}\n")
                }
              }
              is Children -> {
                childrenLambda.apply {
                  add("%M(%L) {\n", syntheticChildren, trait.tag)
                  indent()
                  add("%N()\n", trait.name)
                  unindent()
                  add("}\n")
                }
              }
            }
          }

          arguments += CodeBlock.builder()
            .add("update = {\n")
            .indent()
            .add(updateLambda.build())
            .unindent()
            .add("}")
            .build()

          if (childrenLambda.isNotEmpty()) {
            arguments += CodeBlock.builder()
              .add("content = {\n")
              .indent()
              .add(childrenLambda.build())
              .unindent()
              .add("}")
              .build()
          }

          addStatement(
            "%M<%T, %T>(%L)", composeNode, nodeType, applierOfServerNode,
            arguments.joinToCode(",\n", "\n", ",\n")
          )
        }
        .build()
    )
    .apply {
      if (events.isNotEmpty()) {
        addType(
          TypeSpec.classBuilder(nodeType)
            .addModifiers(PRIVATE)
            .superclass(protocolNode)
            .addSuperclassConstructorParameter("%L", widget.tag)
            .apply {
              for (event in events) {
                addProperty(
                  PropertySpec.builder(event.name, event.lambdaType)
                    .mutable(true)
                    .initializer("null")
                    .jvmField() // Method count optimization as this is implementation detail.
                    .build()
                )
              }
            }
            .addFunction(
              FunSpec.builder("sendEvent")
                .addModifiers(PUBLIC, OVERRIDE)
                .addParameter("event", eventType)
                .beginControlFlow("when (val tag = event.tag)")
                .apply {
                  for (event in events) {
                    if (event.parameterType != null) {
                      addStatement(
                        "%L -> %N?.invoke(event.value as %T)", event.tag, event.name,
                        event.parameterType!!.asTypeName()
                      )
                    } else {
                      addStatement("%L -> %N?.invoke()", event.tag, event.name)
                    }
                  }
                }
                .addStatement("else -> throw %T(\"Unknown tag \$tag\")", iae)
                .endControlFlow()
                .build()
            )
            .build()
        )
      }
    }
    .build()
}
