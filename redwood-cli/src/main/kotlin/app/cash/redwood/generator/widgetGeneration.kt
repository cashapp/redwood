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
interface SunspotWidgetFactory<W : Any> : Widget.Factory<W> {
  /** {tag=1} */
  fun SunspotText(): SunspotText<W>
  /** {tag=2} */
  fun SunspotButton(): SunspotButton<W>

  val RedwoodLayout: RedwoodLayoutWidgetFactory<W>
}
*/
internal fun generateWidgetFactory(schema: Schema): FileSpec {
  val widgetFactoryType = schema.getWidgetFactoryType()
  return FileSpec.builder(widgetFactoryType.packageName, widgetFactoryType.simpleName)
    .addType(
      TypeSpec.interfaceBuilder(widgetFactoryType)
        .addTypeVariable(typeVariableW)
        .addSuperinterface(RedwoodWidget.WidgetFactory.parameterizedBy(typeVariableW))
        .apply {
          for (node in schema.widgets) {
            addFunction(
              FunSpec.builder(node.type.flatName)
                .addModifiers(PUBLIC, ABSTRACT)
                .returns(schema.widgetType(node).parameterizedBy(typeVariableW))
                .addKdoc("{tag=${node.tag}}")
                .build(),
            )
          }
          for (dependency in schema.dependencies) {
            addProperty(
              dependency.name,
              dependency.getWidgetFactoryType().parameterizedBy(typeVariableW),
            )
          }
        }
        .build(),
    )
    .build()
}

/*
/** {tag=2} */
interface SunspotButton<W: Any> : Widget<W> {
  /** {tag=1} */
  fun text(text: String?)
  /** {tag=2} */
  fun enabled(enabled: Boolean)
  /** {tag=3} */
  fun onClick(onClick: (() -> Unit)?)
}
*/
internal fun generateWidget(schema: Schema, widget: Widget): FileSpec {
  val flatName = widget.type.flatName
  return FileSpec.builder(schema.widgetPackage(), flatName)
    .addType(
      TypeSpec.interfaceBuilder(flatName)
        .addModifiers(PUBLIC)
        .addTypeVariable(typeVariableW)
        .addSuperinterface(RedwoodWidget.Widget.parameterizedBy(typeVariableW))
        .addKdoc("{tag=${widget.tag}}")
        .apply {
          for (trait in widget.traits) {
            when (trait) {
              is Property -> {
                addFunction(
                  FunSpec.builder(trait.name)
                    .addModifiers(PUBLIC, ABSTRACT)
                    .addParameter(trait.name, trait.type.asTypeName())
                    .addKdoc("{tag=${trait.tag}}")
                    .build(),
                )
              }
              is Event -> {
                addFunction(
                  FunSpec.builder(trait.name)
                    .addModifiers(PUBLIC, ABSTRACT)
                    .addParameter(trait.name, trait.lambdaType)
                    .addKdoc("{tag=${trait.tag}}")
                    .build(),
                )
              }
              is Children -> {
                addProperty(
                  PropertySpec.builder(trait.name, RedwoodWidget.WidgetChildrenOfW)
                    .addModifiers(PUBLIC, ABSTRACT)
                    .addKdoc("{tag=${trait.tag}}")
                    .build(),
                )
              }
            }
          }
        }
        .build(),
    )
    .build()
}
