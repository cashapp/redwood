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

import app.cash.redwood.tooling.schema.ProtocolWidget
import app.cash.redwood.tooling.schema.ProtocolWidget.ProtocolTrait
import app.cash.redwood.tooling.schema.Schema
import app.cash.redwood.tooling.schema.Widget
import app.cash.redwood.tooling.schema.Widget.Children
import app.cash.redwood.tooling.schema.Widget.Event
import app.cash.redwood.tooling.schema.Widget.Property
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.ABSTRACT
import com.squareup.kotlinpoet.KModifier.OVERRIDE
import com.squareup.kotlinpoet.KModifier.PUBLIC
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName

/*
class SunspotWidgetFactories<W : Any>(
  override val Sunspot: SunspotWidgetFactory<W>,
  override val RedwoodLayout: RedwoodLayoutWidgetFactory<W>,
) : SunspotWidgetFactoryProvider<W>

interface SunspotWidgetFactoryProvider<W : Any> : RedwoodLayoutWidgetFactoryProvider<W> {
  val Sunspot: SunspotWidgetFactory<W>
}
 */
internal fun generateWidgetFactories(schema: Schema): FileSpec {
  val widgetFactoriesType = schema.getWidgetFactoriesType()
  return FileSpec.builder(widgetFactoriesType.packageName, widgetFactoriesType.simpleName)
    .addType(
      TypeSpec.classBuilder(widgetFactoriesType)
        .addTypeVariable(typeVariableW)
        .addSuperinterface(schema.getWidgetFactoryProviderType().parameterizedBy(typeVariableW))
        .apply {
          val constructorBuilder = FunSpec.constructorBuilder()

          for (dependency in listOf(schema) + schema.dependencies) {
            val dependencyType = dependency.getWidgetFactoryType().parameterizedBy(typeVariableW)
            addProperty(
              PropertySpec.builder(dependency.name, dependencyType, OVERRIDE)
                .initializer(dependency.name)
                .build(),
            )
            constructorBuilder.addParameter(dependency.name, dependencyType)
          }

          primaryConstructor(constructorBuilder.build())
        }
        .build(),
    )
    .addType(
      TypeSpec.interfaceBuilder(schema.getWidgetFactoryProviderType())
        .addTypeVariable(typeVariableW)
        .addSuperinterface(RedwoodWidget.WidgetProvider.parameterizedBy(typeVariableW))
        .addProperty(schema.name, schema.getWidgetFactoryType().parameterizedBy(typeVariableW))
        .apply {
          for (dependency in schema.dependencies) {
            addSuperinterface(dependency.getWidgetFactoryProviderType().parameterizedBy(typeVariableW))
          }
        }
        .build(),
    )
    .build()
}

/*
interface SunspotWidgetFactory<W : Any> : Widget.Factory<W> {
  /** {tag=1} */
  fun SunspotText(): SunspotText<W>
  /** {tag=2} */
  fun SunspotButton(): SunspotButton<W>
}
*/
internal fun generateWidgetFactory(schema: Schema): FileSpec {
  val widgetFactoryType = schema.getWidgetFactoryType()
  return FileSpec.builder(widgetFactoryType.packageName, widgetFactoryType.simpleName)
    .addType(
      TypeSpec.interfaceBuilder(widgetFactoryType)
        .addTypeVariable(typeVariableW)
        .apply {
          for (widget in schema.widgets) {
            addFunction(
              FunSpec.builder(widget.type.flatName)
                .addModifiers(PUBLIC, ABSTRACT)
                .returns(schema.widgetType(widget).parameterizedBy(typeVariableW))
                .apply {
                  if (widget is ProtocolWidget) {
                    addKdoc("{tag=${widget.tag}}")
                  }
                }
                .build(),
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
        .apply {
          if (widget is ProtocolWidget) {
            addKdoc("{tag=${widget.tag}}")
          }
        }
        .apply {
          for (trait in widget.traits) {
            when (trait) {
              is Property -> {
                addFunction(
                  FunSpec.builder(trait.name)
                    .addModifiers(PUBLIC, ABSTRACT)
                    .addParameter(trait.name, trait.type.asTypeName())
                    .apply {
                      if (trait is ProtocolTrait) {
                        addKdoc("{tag=${trait.tag}}")
                      }
                    }
                    .build(),
                )
              }
              is Event -> {
                addFunction(
                  FunSpec.builder(trait.name)
                    .addModifiers(PUBLIC, ABSTRACT)
                    .addParameter(trait.name, trait.lambdaType)
                    .apply {
                      if (trait is ProtocolTrait) {
                        addKdoc("{tag=${trait.tag}}")
                      }
                    }
                    .build(),
                )
              }
              is Children -> {
                addProperty(
                  PropertySpec.builder(trait.name, RedwoodWidget.WidgetChildrenOfW)
                    .addModifiers(PUBLIC, ABSTRACT)
                    .apply {
                      if (trait is ProtocolTrait) {
                        addKdoc("{tag=${trait.tag}}")
                      }
                    }
                    .build(),
                )
              }
              is ProtocolTrait -> throw AssertionError()
            }
          }
        }
        .build(),
    )
    .build()
}
