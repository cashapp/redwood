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

import app.cash.redwood.schema.parser.LayoutModifier
import app.cash.redwood.schema.parser.Schema
import app.cash.redwood.schema.parser.Widget
import app.cash.redwood.schema.parser.Widget.Children
import app.cash.redwood.schema.parser.Widget.Event
import app.cash.redwood.schema.parser.Widget.Property
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.DATA
import com.squareup.kotlinpoet.KModifier.OVERRIDE
import com.squareup.kotlinpoet.KModifier.PRIVATE
import com.squareup.kotlinpoet.KModifier.PUBLIC
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.joinToCode
import kotlin.reflect.KClass

/*
@Retention(AnnotationRetention.BINARY)
@ComposableTargetMarker(description = "Example Composable")
@Target(
  AnnotationTarget.FUNCTION,
  AnnotationTarget.PROPERTY_GETTER,
  AnnotationTarget.TYPE,
  AnnotationTarget.TYPE_PARAMETER,
)
public annotation class SunspotComposable
 */
internal fun generateComposableTargetMarker(schema: Schema): FileSpec {
  val name = schema.composeTargetMarker
  return FileSpec.builder(schema.composePackage, name.simpleName)
    .addType(
      TypeSpec.annotationBuilder(name)
        .addAnnotation(
          AnnotationSpec.builder(Retention::class)
            .addMember("%T.BINARY", AnnotationRetention::class)
            .build(),
        )
        .addAnnotation(
          AnnotationSpec.builder(composableTargetMarker)
            .addMember("description = %S", schema.name + " Composable")
            .build(),
        )
        .addAnnotation(
          AnnotationSpec.builder(Target::class)
            .addMember("%1T.FUNCTION, %1T.PROPERTY_GETTER, %1T.TYPE, %1T.TYPE_PARAMETER", AnnotationTarget::class)
            .build(),
        )
        .build(),
    )
    .build()
}

/*
@Composable
@SunspotComposable
fun SunspotButton(
  layoutModifier: LayoutModifier = LayoutModifier,
  text: String?,
  enabled: Boolean = true,
  onClick: (() -> Unit)? = null
): Unit {
  RedwoodComposeNode<SunspotWidgetFactory<*>, SunspotButton<*>>(
    factory = SunspotWidgetFactory<*>::SunspotButton,
    update = {
      set(text) { text(text) }
      set(enabled) { enabled(enabled) }
      set(onClick) { onClick(onClick) }
    },
  )
}
*/
internal fun generateComposable(schema: Schema, widget: Widget): FileSpec {
  val widgetType = schema.widgetType(widget).parameterizedBy(STAR)
  val widgetFactoryType = schema.getWidgetFactoryType().parameterizedBy(STAR)
  val composeTargetMarker = schema.composeTargetMarker
  return FileSpec.builder(schema.composePackage, widget.flatName)
    .addFunction(
      FunSpec.builder(widget.flatName)
        .addModifiers(PUBLIC)
        .addAnnotation(composable)
        .addAnnotation(composeTargetMarker)
        .apply {
          // If the last trait is a child lambda move the layout modifier position to be
          // second-to-last. This ensures you can still use trailing lambda syntax.
          val layoutModifierIndex = if (widget.traits.lastOrNull() is Children) {
            widget.traits.size - 1
          } else {
            widget.traits.size
          }

          var index = 0
          while (true) {
            if (index == layoutModifierIndex) {
              addParameter(
                ParameterSpec.builder("layoutModifier", LayoutModifier)
                  .defaultValue("%T", LayoutModifier)
                  .build(),
              )
            }
            if (index >= widget.traits.size) {
              break
            }
            val trait = widget.traits[index]
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
                  val scope = trait.scope?.let { ClassName(schema.composePackage, it.simpleName!!) }
                  ParameterSpec.builder(trait.name, composableLambda(scope, composeTargetMarker))
                    .build()
                }
              },
            )
            index++
          }

          val arguments = mutableListOf<CodeBlock>()

          arguments += CodeBlock.builder()
            .add("factory = %T::%N", widgetFactoryType, widget.flatName)
            .build()

          val updateLambda = CodeBlock.builder()
            .add("set(layoutModifier) { layoutModifiers = layoutModifier }\n")

          val childrenLambda = CodeBlock.builder()
          for (trait in widget.traits) {
            when (trait) {
              is Property,
              is Event, -> {
                updateLambda.add("set(%1N) { %1N(%1N) }\n", trait.name)
              }
              is Children -> {
                childrenLambda.apply {
                  add("%M(%L) {\n", syntheticChildren, trait.tag)
                  indent()
                  trait.scope?.let { scope ->
                    add("%T.", ClassName(schema.composePackage, scope.simpleName!!))
                  }
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

          arguments += CodeBlock.builder()
            .add("content = {\n")
            .indent()
            .add(childrenLambda.build())
            .unindent()
            .add("}")
            .build()

          addStatement(
            "%M<%T, %T>(%L)",
            redwoodComposeNode,
            widgetFactoryType,
            widgetType,
            arguments.joinToCode(",\n", "\n", ",\n"),
          )
        }
        .build(),
    )
    .build()
}

/*
object RowScope {
  fun LayoutModifier.something(...): LayoutModifier {
    return then(Something(...))
  }
}

private data class SomethingImpl(...) : Something
*/
internal fun generateScopeAndScopedModifiers(schema: Schema, scope: KClass<*>): FileSpec {
  val scopeName = scope.simpleName!!
  return FileSpec.builder(schema.composePackage, scopeName)
    .apply {
      val scopeObject = TypeSpec.objectBuilder(scopeName)

      for (layoutModifier in schema.layoutModifiers) {
        if (scope !in layoutModifier.scopes) {
          continue
        }

        val (function, type) = generateLayoutModifier(schema, layoutModifier)
        scopeObject.addFunction(function)
        addType(type)
      }

      addType(scopeObject.build())
    }
    .build()
}

internal fun generateUnscopedModifiers(schema: Schema): FileSpec {
  return FileSpec.builder(schema.composePackage, "unscopedLayoutModifiers")
    .apply {
      for (layoutModifier in schema.layoutModifiers) {
        if (layoutModifier.scopes.isNotEmpty()) {
          continue
        }

        val (function, type) = generateLayoutModifier(schema, layoutModifier)
        addFunction(function)
        addType(type)
      }
    }
    .build()
}

private fun generateLayoutModifier(
  schema: Schema,
  layoutModifier: LayoutModifier,
): Pair<FunSpec, TypeSpec> {
  val simpleName = layoutModifier.type.simpleName!!
  val typeName = ClassName(schema.composePackage, simpleName + "Impl")

  val function = FunSpec.builder(simpleName.replaceFirstChar(Char::lowercaseChar))
    .receiver(LayoutModifier)
    .returns(LayoutModifier)
    .apply {
      val arguments = mutableListOf<CodeBlock>()
      for (property in layoutModifier.properties) {
        arguments += CodeBlock.of("%N", property.name)

        addParameter(
          ParameterSpec.builder(property.name, property.type.asTypeName())
            .apply {
              property.defaultExpression?.let { defaultValue(it) }
            }
            .build(),
        )
      }

      if (arguments.isEmpty()) {
        addStatement("return then(%T)", typeName)
      } else {
        addStatement("return then(%T(%L))", typeName, arguments.joinToCode())
      }
    }
    .build()

  val interfaceType = schema.layoutModifierType(layoutModifier)

  val typeBuilder = if (layoutModifier.properties.isEmpty()) {
    TypeSpec.objectBuilder(typeName)
  } else {
    TypeSpec.classBuilder(typeName)
      .addModifiers(DATA)
      .apply {
        val primaryConstructor = FunSpec.constructorBuilder()
        for (property in layoutModifier.properties) {
          val propertyType = property.type.asTypeName()
          primaryConstructor.addParameter(property.name, propertyType)
          addProperty(
            PropertySpec.builder(property.name, propertyType)
              .addModifiers(OVERRIDE)
              .initializer("%N", property.name)
              .build(),
          )
        }
        primaryConstructor(primaryConstructor.build())
      }
  }

  val type = typeBuilder
    .addModifiers(PRIVATE)
    .addSuperinterface(interfaceType)
    .addFunction(layoutModifierEquals(schema, layoutModifier))
    .addFunction(layoutModifierHashCode(layoutModifier))
    .build()

  return function to type
}
