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
import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.KModifier.ABSTRACT
import com.squareup.kotlinpoet.KModifier.OVERRIDE
import com.squareup.kotlinpoet.KModifier.PUBLIC
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asTypeName

/*
interface SunspotWidgetFactory<T : Any> : Widget.Factory<T> {
  fun SunspotText(): SunspotText<T>
  fun SunspotButton(): SunspotButton<T>

  override fun create(kind: Int): TreeNode<T> {
    return when (kind) {
      1 -> SunspotText()
      2 -> SunspotButton()
      else -> throw IllegalArgumentException("Unknown kind $kind")
    }
  }
}
*/
internal fun generateWidgetFactory(schema: Schema): FileSpec {
  return FileSpec.builder(schema.displayPackage, schema.getWidgetFactoryType().simpleName)
    .addType(
      TypeSpec.interfaceBuilder(schema.getWidgetFactoryType())
        .addModifiers(PUBLIC)
        .addTypeVariable(typeVariableT)
        .addSuperinterface(factoryOfT)
        .apply {
          for (node in schema.widgets) {
            addFunction(
              FunSpec.builder(node.flatName)
                .addModifiers(PUBLIC, ABSTRACT)
                .returns(schema.widgetType(node).parameterizedBy(typeVariableT))
                .build()
            )
          }
        }
        .addFunction(
          FunSpec.builder("create")
            .addModifiers(PUBLIC, OVERRIDE)
            .addParameter("kind", INT)
            .returns(widgetOfT)
            .beginControlFlow("return when (kind)")
            .apply {
              for (node in schema.widgets.sortedBy { it.tag }) {
                addStatement("%L -> %N()", node.tag, node.flatName)
              }
            }
            .addStatement("else -> throw %T(\"Unknown kind \$kind\")", iae)
            .endControlFlow()
            .build()
        )
        .build()
    )
    .build()
}

/*
interface SunspotButton<out T: Any> : Widget<T> {
  fun text(text: String?)
  fun enabled(enabled: Boolean)
  fun onClick(onClick: (() -> Unit)?)

  override fun apply(diff: PropertyDiff, eventSink: EventSink) {
    when (val tag = diff.tag) {
      1 -> text(diff.value as String?)
      2 -> enabled(diff.value as Boolean)
      3 -> {
        val onClick: (() -> Unit)? = if (diff.value as Boolean) {
          val event = Event(diff.id, 3, null);
          { eventSink.sendEvent(event) }
        } else {
          null
        }
        onClick(onClick)
      }
      else -> throw IllegalArgumentException("Unknown tag $tag")
    }
  }
}
*/
internal fun generateWidget(schema: Schema, widget: Widget): FileSpec {
  return FileSpec.builder(schema.displayPackage, widget.flatName)
    .addType(
      TypeSpec.interfaceBuilder(widget.flatName)
        .addModifiers(PUBLIC)
        .addTypeVariable(typeVariableT)
        .addSuperinterface(widgetOfT)
        .apply {
          for (trait in widget.traits) {
            when (trait) {
              is Property -> {
                addFunction(
                  FunSpec.builder(trait.name)
                    .addModifiers(PUBLIC, ABSTRACT)
                    .addParameter(trait.name, trait.type.asTypeName())
                    .build()
                )
              }
              is Event -> {
                addFunction(
                  FunSpec.builder(trait.name)
                    .addModifiers(PUBLIC, ABSTRACT)
                    .addParameter(trait.name, trait.lambdaType)
                    .build()
                )
              }
              is Children -> {
                addProperty(
                  PropertySpec.builder(trait.name, childrenOfT)
                    .addModifiers(PUBLIC, ABSTRACT)
                    .build()
                )
              }
            }
          }
          val childrens = widget.traits.filterIsInstance<Children>()
          if (childrens.isNotEmpty()) {
            addFunction(
              FunSpec.builder("children")
                .addModifiers(PUBLIC, OVERRIDE)
                .addParameter("tag", INT)
                .returns(childrenOfT)
                .beginControlFlow("return when (tag)")
                .apply {
                  for (children in childrens) {
                    addStatement("%L -> %N", children.tag, children.name)
                  }
                }
                .addStatement("else -> throw %T(\"Unknown tag \$tag\")", iae)
                .endControlFlow()
                .build()
            )
          }
        }
        .addFunction(
          FunSpec.builder("apply")
            .addModifiers(PUBLIC, OVERRIDE)
            .addParameter("diff", propertyDiff)
            .addParameter("eventSink", eventSink)
            .beginControlFlow("when (val tag = diff.tag)")
            .apply {
              for (trait in widget.traits) {
                @Exhaustive when (trait) {
                  is Property -> {
                    addStatement(
                      "%L -> %N(diff.value as %T)", trait.tag, trait.name,
                      trait.type.asTypeName()
                    )
                  }
                  is Event -> {
                    beginControlFlow("%L ->", trait.tag)
                    beginControlFlow("val %N: %T = if (diff.value as %T)", trait.name, trait.lambdaType, BOOLEAN)
                    addStatement(
                      "{ eventSink.sendEvent(%T(diff.id, %L, %L)) }", eventType, trait.tag,
                      if (trait.parameterType != null) "it" else "null"
                    )
                    nextControlFlow("else")
                    addStatement("null")
                    endControlFlow()
                    addStatement("%1N(%1N)", trait.name)
                    endControlFlow()
                  }
                  is Children -> {}
                }
              }
            }
            .addStatement("else -> throw %T(\"Unknown tag \$tag\")", iae)
            .endControlFlow()
            .build()
        )
        .build()
    )
    .build()
}

private val typeVariableT = TypeVariableName("T", listOf(ANY))
private val widgetOfT = widget.parameterizedBy(typeVariableT)
private val childrenOfT = widgetChildren.parameterizedBy(typeVariableT)
private val factoryOfT = widgetFactory.parameterizedBy(typeVariableT)
