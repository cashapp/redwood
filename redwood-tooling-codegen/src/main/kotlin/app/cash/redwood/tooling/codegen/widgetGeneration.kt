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
import app.cash.redwood.tooling.schema.SchemaSet
import app.cash.redwood.tooling.schema.Widget
import app.cash.redwood.tooling.schema.Widget.Children
import app.cash.redwood.tooling.schema.Widget.Event
import app.cash.redwood.tooling.schema.Widget.Property
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.ABSTRACT
import com.squareup.kotlinpoet.KModifier.OVERRIDE
import com.squareup.kotlinpoet.KModifier.PUBLIC
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec

/*
@ObjCName("ExampleWidgetFactories", exact = true)
class ExampleWidgetFactories<W : Any>(
  override val Example: ExampleWidgetFactory<W>,
  override val RedwoodLayout: RedwoodLayoutWidgetFactory<W>,
) : ExampleWidgetFactoryProvider<W>

interface ExampleWidgetFactoryProvider<W : Any> : RedwoodLayoutWidgetFactoryProvider<W> {
  val Example: ExampleWidgetFactory<W>
}
 */
internal fun generateWidgetFactories(schemaSet: SchemaSet): FileSpec {
  val schema = schemaSet.schema
  val widgetFactoriesType = schema.getWidgetFactoriesType()
  return FileSpec.builder(widgetFactoriesType)
    .addType(
      TypeSpec.classBuilder(widgetFactoriesType)
        .addTypeVariable(typeVariableW)
        .addSuperinterface(schema.getWidgetFactoryProviderType().parameterizedBy(typeVariableW))
        .addAnnotation(
          AnnotationSpec.builder(Stdlib.OptIn)
            .addMember("%T::class", Stdlib.ExperimentalObjCName)
            .build(),
        )
        .addAnnotation(
          AnnotationSpec.builder(Stdlib.ObjCName)
            .addMember("%S", widgetFactoriesType.simpleName)
            .addMember("exact = true")
            .build(),
        )
        .apply {
          val constructorBuilder = FunSpec.constructorBuilder()

          for (dependency in schemaSet.all) {
            val dependencyType = dependency.getWidgetFactoryType().parameterizedBy(typeVariableW)
            addProperty(
              PropertySpec.builder(dependency.type.flatName, dependencyType, OVERRIDE)
                .initializer(dependency.type.flatName)
                .build(),
            )
            constructorBuilder.addParameter(dependency.type.flatName, dependencyType)
          }

          primaryConstructor(constructorBuilder.build())
        }
        .build(),
    )
    .addType(
      TypeSpec.interfaceBuilder(schema.getWidgetFactoryProviderType())
        .addTypeVariable(typeVariableW)
        .addSuperinterface(RedwoodWidget.WidgetProvider.parameterizedBy(typeVariableW))
        .addProperty(schema.type.flatName, schema.getWidgetFactoryType().parameterizedBy(typeVariableW))
        .apply {
          for (dependency in schemaSet.dependencies.values) {
            addSuperinterface(dependency.getWidgetFactoryProviderType().parameterizedBy(typeVariableW))
          }
        }
        .build(),
    )
    .build()
}

/*
@ObjCName("ExampleWidgetFactory", exact = true)
interface ExampleWidgetFactory<W : Any> : Widget.Factory<W> {
  /** {tag=1} */
  fun Text(): Text<W>
  /** {tag=2} */
  fun Button(): Button<W>
}
*/
internal fun generateWidgetFactory(schema: Schema): FileSpec {
  val widgetFactoryType = schema.getWidgetFactoryType()
  return FileSpec.builder(widgetFactoryType)
    .addType(
      TypeSpec.interfaceBuilder(widgetFactoryType)
        .addTypeVariable(typeVariableW)
        .addAnnotation(
          AnnotationSpec.builder(Stdlib.OptIn)
            .addMember("%T::class", Stdlib.ExperimentalObjCName)
            .build(),
        )
        .addAnnotation(
          AnnotationSpec.builder(Stdlib.ObjCName)
            .addMember("%S", widgetFactoryType.simpleName)
            .addMember("exact = true")
            .build(),
        )
        .apply {
          schema.documentation?.let { documentation ->
            addKdoc(documentation)
          }

          for (widget in schema.widgets) {
            addFunction(
              FunSpec.builder(widget.type.flatName)
                .addModifiers(PUBLIC, ABSTRACT)
                .returns(schema.widgetType(widget).parameterizedBy(typeVariableW))
                .apply {
                  widget.deprecation?.let { deprecation ->
                    addAnnotation(deprecation.toAnnotationSpec())
                  }
                  widget.documentation?.let { documentation ->
                    addKdoc(documentation)
                  }
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
@ObjCName("Button", exact = true)
interface Button<W: Any> : Widget<W> {
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
        .addAnnotation(
          AnnotationSpec.builder(Stdlib.OptIn)
            .addMember("%T::class", Stdlib.ExperimentalObjCName)
            .build(),
        )
        .addAnnotation(
          AnnotationSpec.builder(Stdlib.ObjCName)
            .addMember("%S", flatName)
            .addMember("exact = true")
            .build(),
        )
        .apply {
          widget.deprecation?.let { deprecation ->
            addAnnotation(deprecation.toAnnotationSpec())
          }

          widget.documentation?.let { documentation ->
            addKdoc(documentation)
          }
          if (widget is ProtocolWidget) {
            addKdoc("{tag=${widget.tag}}")
          }

          for (trait in widget.traits) {
            when (trait) {
              is Property -> {
                addFunction(
                  FunSpec.builder(trait.name)
                    .addModifiers(PUBLIC, ABSTRACT)
                    .addParameter(trait.name, trait.type.asTypeName())
                    .apply {
                      trait.deprecation?.let { deprecation ->
                        addAnnotation(deprecation.toAnnotationSpec())
                      }
                      trait.documentation?.let { documentation ->
                        addKdoc(documentation)
                      }
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
                      trait.deprecation?.let { deprecation ->
                        addAnnotation(deprecation.toAnnotationSpec())
                      }
                      trait.documentation?.let { documentation ->
                        addKdoc(documentation)
                      }
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
                      trait.deprecation?.let { deprecation ->
                        addAnnotation(deprecation.toAnnotationSpec())
                      }
                      trait.documentation?.let { documentation ->
                        addKdoc(documentation)
                      }
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
