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

import app.cash.redwood.tooling.schema.LayoutModifier
import app.cash.redwood.tooling.schema.ProtocolWidget.ProtocolTrait
import app.cash.redwood.tooling.schema.Schema
import app.cash.redwood.tooling.schema.Widget
import app.cash.redwood.tooling.schema.Widget.Children
import app.cash.redwood.tooling.schema.Widget.Event
import app.cash.redwood.tooling.schema.Widget.Property
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.INTERNAL
import com.squareup.kotlinpoet.KModifier.OVERRIDE
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
@Composable
@SunspotComposable
@OptIn(RedwoodCodegenApi::class)
fun Row(
  padding: Padding = Padding.Zero,
  overflow: Overflow = Overflow.Clip,
  layoutModifier: LayoutModifier = LayoutModifier,
  children: @Composable @SunspotComposable RowScope.() -> Unit,
): Unit {
  _RedwoodComposeNode<SunspotWidgetFactory<*>, Row<*>>(
    factory = { it.RedwoodLayout.Row() },
    update = {
      set(layoutModifier) { layoutModifiers = it }
      set(padding, Row<*>::padding)
      set(overflow, Row<*>::overflow)
    },
    content = {
      into(Row<*>::children) {
        RowScopeImpl.children()
      }
    },
  )
}
*/
internal fun generateComposable(
  schema: Schema,
  widget: Widget,
  host: Schema = schema,
): FileSpec {
  val widgetType = schema.widgetType(widget).parameterizedBy(STAR)
  val widgetFactoryType = host.getWidgetFactoryType().parameterizedBy(STAR)
  val flatName = widget.type.flatName
  return FileSpec.builder(schema.composePackage(), flatName)
    .addFunction(
      FunSpec.builder(flatName)
        .addModifiers(PUBLIC)
        .addAnnotation(ComposeRuntime.Composable)
        .addAnnotation(
          AnnotationSpec.builder(Stdlib.OptIn)
            .addMember("%T::class", Redwood.RedwoodCodegenApi)
            .build(),
        )
        .apply {
          // Set the layout modifier as the last non-child lambda in the function signature.
          // This ensures you can still use trailing lambda syntax.
          val layoutModifierIndex = widget.traits.indexOfLast { it !is Children } + 1

          var index = 0
          while (true) {
            if (index == layoutModifierIndex) {
              addParameter(
                ParameterSpec.builder("layoutModifier", Redwood.LayoutModifier)
                  .defaultValue("%T", Redwood.LayoutModifier)
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
                    .defaultValue(trait.defaultExpression ?: "null")
                    .build()
                }
                is Children -> {
                  val scope = trait.scope?.let { ClassName(schema.composePackage(), it.simpleName!!) }
                  ParameterSpec.builder(trait.name, composableLambda(scope))
                    .apply {
                      trait.defaultExpression?.let { defaultValue(it) }
                    }
                    .build()
                }
                is ProtocolTrait -> throw AssertionError()
              },
            )
            index++
          }

          val arguments = mutableListOf<CodeBlock>()

          arguments += if (schema === host) {
            CodeBlock.of("factory = %T::%N", widgetFactoryType, flatName)
          } else {
            CodeBlock.of("factory = { it.%N.%N() }", schema.name, flatName)
          }

          val updateLambda = CodeBlock.builder()
            .add("set(layoutModifier) { layoutModifiers = it }\n")

          val childrenLambda = CodeBlock.builder()
          for (trait in widget.traits) {
            when (trait) {
              is Property,
              is Event, -> {
                updateLambda.add("set(%1N, %2T::%1N)\n", trait.name, widgetType)
              }
              is Children -> {
                childrenLambda.apply {
                  add("into(%T::%N) {\n", widgetType, trait.name)
                  indent()
                  trait.scope?.let { scope ->
                    add("%T.", ClassName(schema.composePackage(), scope.simpleName!! + "Impl"))
                  }
                  add("%N()\n", trait.name)
                  unindent()
                  add("}\n")
                }
              }
              is ProtocolTrait -> throw AssertionError()
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
            RedwoodCompose.RedwoodComposeNode,
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
interface RowScope {
  @Stable
  fun LayoutModifier.something(...): LayoutModifier {
    return then(SomethingImpl(...))
  }
}

internal object RowScopeImpl : RowScope
*/
internal fun generateScope(schema: Schema, scope: KClass<*>): FileSpec {
  val scopeName = scope.simpleName!!
  val scopeType = ClassName(schema.composePackage(), scopeName)
  return FileSpec.builder(scopeType.packageName, scopeType.simpleName)
    .apply {
      val scopeBuilder = TypeSpec.interfaceBuilder(scopeType)
        .addAnnotation(Redwood.LayoutScopeMarker)

      for (layoutModifier in schema.layoutModifiers) {
        if (scope !in layoutModifier.scopes) {
          continue
        }

        scopeBuilder.addFunction(generateLayoutModifierFunction(schema, layoutModifier))
      }

      addType(scopeBuilder.build())
      addType(
        TypeSpec.objectBuilder(scopeName + "Impl")
          .addModifiers(INTERNAL)
          .addSuperinterface(scopeType)
          .build(),
      )
    }
    .build()
}

/*
internal class SomethingImpl(...): Something {
  public override fun equals(other: Any?): Boolean = ...
  public override fun hashCode(): Int = ...
  public override fun toString(): String = ...
}
*/
internal fun generateLayoutModifierImpls(schema: Schema): FileSpec? {
  if (schema.layoutModifiers.isEmpty()) return null

  return FileSpec.builder(schema.composePackage(), "layoutModifiers")
    .apply {
      for (layoutModifier in schema.layoutModifiers) {
        addType(generateLayoutModifierImpl(schema, layoutModifier))
      }
    }
    .build()
}

private fun generateLayoutModifierFunction(
  schema: Schema,
  layoutModifier: LayoutModifier,
): FunSpec {
  val simpleName = layoutModifier.type.simpleName!!
  return FunSpec.builder(simpleName.replaceFirstChar(Char::lowercaseChar))
    .addAnnotation(ComposeRuntime.Stable)
    .receiver(Redwood.LayoutModifier)
    .returns(Redwood.LayoutModifier)
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

      val typeName = schema.layoutModifierImpl(layoutModifier)
      if (arguments.isEmpty()) {
        addStatement("return then(%T)", typeName)
      } else {
        addStatement("return then(%T(%L))", typeName, arguments.joinToCode())
      }
    }
    .build()
}

private fun generateLayoutModifierImpl(
  schema: Schema,
  layoutModifier: LayoutModifier,
): TypeSpec {
  val typeName = schema.layoutModifierImpl(layoutModifier)
  val typeBuilder = if (layoutModifier.properties.isEmpty()) {
    TypeSpec.objectBuilder(typeName)
  } else {
    TypeSpec.classBuilder(typeName)
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

  return typeBuilder
    .addModifiers(INTERNAL)
    .addSuperinterface(schema.layoutModifierType(layoutModifier))
    .addFunction(layoutModifierEquals(schema, layoutModifier))
    .addFunction(layoutModifierHashCode(layoutModifier))
    .addFunction(layoutModifierToString(layoutModifier))
    .build()
}
