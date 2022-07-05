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
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.ABSTRACT
import com.squareup.kotlinpoet.KModifier.PUBLIC
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName

/*
interface SunspotWidgetFactory<T : Any> : Widget.Factory<T> {
  fun SunspotText(): SunspotText<T>
  fun SunspotButton(): SunspotButton<T>
}
*/
internal fun generateWidgetFactory(schema: Schema): FileSpec {
  val widgetFactoryType = schema.getWidgetFactoryType()
  return FileSpec.builder(widgetFactoryType.packageName, widgetFactoryType.simpleName)
    .addType(
      TypeSpec.interfaceBuilder(widgetFactoryType)
        .addTypeVariable(typeVariableT)
        .addSuperinterface(widgetFactory.parameterizedBy(typeVariableT))
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
        .build()
    )
    .build()
}

/*
interface SunspotButton<T: Any> : Widget<T> {
  fun text(text: String?)
  fun enabled(enabled: Boolean)
  fun onClick(onClick: (() -> Unit)?)
}
*/
internal fun generateWidget(schema: Schema, widget: Widget): FileSpec {
  return FileSpec.builder(schema.widgetPackage, widget.flatName)
    .addType(
      TypeSpec.interfaceBuilder(widget.flatName)
        .addModifiers(PUBLIC)
        .addTypeVariable(typeVariableT)
        .addSuperinterface(widgetType.parameterizedBy(typeVariableT))
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
        }
        .build()
    )
    .build()
}
