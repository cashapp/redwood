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
  return FileSpec.builder(schema.composePackage(), name.simpleName)
    .addType(
      TypeSpec.annotationBuilder(name)
        .addAnnotation(
          AnnotationSpec.builder(Retention::class)
            .addMember("%T.BINARY", AnnotationRetention::class)
            .build(),
        )
        .addAnnotation(
          AnnotationSpec.builder(ComposeRuntime.ComposableTargetMarker)
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
  text: String?,
  enabled: Boolean = true,
  onClick: (() -> Unit)? = null,
  layoutModifier: LayoutModifier = LayoutModifier,
): Unit {
  RedwoodComposeNode<SunspotWidgetFactory<*>, SunspotButton<*>>(
    factory = SunspotWidgetFactory<*>::SunspotButton,
    update = {
      set(layoutModifier) { layoutModifiers = it }
      set(text, SunspotButton<*>::text)
      set(enabled, SunspotButton<*>::enabled)
      set(onClick, SunspotButton<*>::onClick)
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
  val composeTargetMarker = host.composeTargetMarker
  val flatName = widget.type.flatName
  return FileSpec.builder(schema.composePackage(), flatName)
    .addFunction(
      FunSpec.builder(flatName)
        .addModifiers(PUBLIC)
        .addAnnotation(ComposeRuntime.Composable)
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
                    .defaultValue("null")
                    .build()
                }
                is Children -> {
                  val scope = trait.scope?.let { ClassName(schema.composePackage(), it.simpleName!!) }
                  ParameterSpec.builder(trait.name, composableLambda(scope, composeTargetMarker))
                    .build()
                }
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
                  add("%M(%LU) {\n", ComposeProtocol.SyntheticChildren, trait.tag)
                  indent()
                  trait.scope?.let { scope ->
                    add("%T.", ClassName(schema.composePackage(), scope.simpleName!! + "Impl"))
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
