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

import app.cash.redwood.schema.parser.Children
import app.cash.redwood.schema.parser.Event
import app.cash.redwood.schema.parser.Property
import app.cash.redwood.schema.parser.Schema
import app.cash.redwood.schema.parser.Widget
import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.ABSTRACT
import com.squareup.kotlinpoet.KModifier.OVERRIDE
import com.squareup.kotlinpoet.KModifier.PRIVATE
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.joinToCode

/*
class SchemaSunspotWidgetFactory : SunspotWidgetFactory<Nothing> {
  override fun SunspotBox(): SunspotBox<Nothing> = SchemaSunspotBox(scope)
  // ...
}

interface SchemaWidget {
  val schema: Any
  fun assertSchema(expected: Any, path: String = "$")
}
*/
internal fun generateSchemaWidgetFactory(schema: Schema): FileSpec {
  val widgetFactoryType = ClassName(schema.testPackage, "Schema${schema.name}WidgetFactory")
  val schemaWidgetType = ClassName(schema.testPackage, "SchemaWidget")

  return FileSpec.builder(schema.testPackage, widgetFactoryType.simpleName)
    .addType(
      TypeSpec.objectBuilder(widgetFactoryType)
        .addSuperinterface(schema.getWidgetFactoryType().parameterizedBy(schemaWidgetType))
        .apply {
          for (widget in schema.widgets) {
            addFunction(
              FunSpec.builder(widget.flatName)
                .addModifiers(OVERRIDE)
                .returns(schema.widgetType(widget).parameterizedBy(schemaWidgetType))
                .addStatement("return %T()", schema.testType(widget))
                .build()
            )
          }
        }
        .build()
    )
    .addType(
      TypeSpec.interfaceBuilder(schemaWidgetType.simpleName)
        .addProperty("schema", ANY, ABSTRACT)
        .addFunction(
          FunSpec.builder("assertSchema")
            .addModifiers(ABSTRACT)
            .addParameter("expected", ANY)
            .addParameter(
              ParameterSpec.builder("path", STRING)
                .defaultValue(""""$"""")
                .build()
            )
            .build()
        )
        .build()
    )
    .build()
}

/*
private class SchemaSunspotBox : SunspotBox<SchemaWidget>, SchemaWidget {
  // ...
}
 */
internal fun generateSchemaWidget(schema: Schema, widget: Widget): FileSpec {
  val className = schema.testType(widget)
  val widgetType = widget.type.asClassName()
  val schemaWidgetType = ClassName(schema.testPackage, "SchemaWidget")
  return FileSpec.builder(schema.testPackage, className.simpleName)
    .addType(
      TypeSpec.classBuilder(className)
        .addSuperinterface(schema.widgetType(widget).parameterizedBy(schemaWidgetType))
        .addSuperinterface(schemaWidgetType)
        .addProperty(
          PropertySpec.builder("value", schemaWidgetType, OVERRIDE)
            .getter(
              FunSpec.getterBuilder()
                .addStatement("return this")
                .build()
            )
            .build()
        )
        .apply {
          val schemaParameters = mutableListOf<CodeBlock>()
          for (trait in widget.traits) {
            when (trait) {
              is Property -> {
                addProperty(
                  PropertySpec.builder(
                    trait.name, trait.type.asTypeName().copy(nullable = true),
                    PRIVATE
                  )
                    .mutable(true)
                    .initializer("null")
                    .build()
                )
                addFunction(
                  FunSpec.builder(trait.name)
                    .addModifiers(OVERRIDE)
                    .addParameter(trait.name, trait.type.asTypeName())
                    .addStatement("this.%1N = %1N", trait.name)
                    .build()
                )
                schemaParameters += if (trait.type.isMarkedNullable) {
                  CodeBlock.of("%1N = %1N", trait.name)
                } else {
                  CodeBlock.of("%1N = %1N ?: error(%2S)", trait.name, "Required property '${trait.name}' not set")
                }
              }
              is Event -> {
                addProperty(
                  PropertySpec.builder(trait.name, trait.lambdaType, PRIVATE)
                    .mutable(true)
                    .initializer("null")
                    .build()
                )
                addFunction(
                  FunSpec.builder(trait.name)
                    .addModifiers(OVERRIDE)
                    .addParameter(trait.name, trait.lambdaType)
                    .addStatement("this.%1N = %1N", trait.name)
                    .build()
                )
                schemaParameters += CodeBlock.of(
                  "%1N = %1N ?: { error(%2S) }", trait.name, "No '${trait.name}' lambda set"
                )
              }
              is Children -> {
                addProperty(
                  PropertySpec.builder(trait.name, mutableListChildren.parameterizedBy(schemaWidgetType))
                    .addModifiers(OVERRIDE)
                    .initializer("%T()", mutableListChildren)
                    .build()
                )
                schemaParameters += CodeBlock.of("%1N = %1N.map(%2T::schema)", trait.name, schemaWidgetType)
              }
            }
          }
          addProperty(
            PropertySpec.builder("schema", widgetType, OVERRIDE)
              .getter(
                FunSpec.getterBuilder()
                  .apply {
                    if (schemaParameters.isEmpty()) {
                      addStatement("return %T", widgetType)
                    } else {
                      addStatement(
                        "return %T(%L)", widgetType,
                        schemaParameters.joinToCode(separator = ",\n", prefix = "\n", suffix = "\n")
                      )
                    }
                  }
                  .build()
              )
              .build()
          )
        }
        .addFunction(
          FunSpec.builder("assertSchema")
            .addModifiers(OVERRIDE)
            .addParameter("expected", ANY)
            .addParameter("path", STRING)
            .beginControlFlow("if (expected !is %T)", widgetType)
            .addStatement(
              "throw %T(\"Expected:·\${expected::class}·but·was:·${widgetType.simpleNames.joinToString(".")}\\n··at·\$path\")",
              ae
            )
            .endControlFlow()
            .apply {
              for (trait in widget.traits) {
                when (trait) {
                  is Property -> {
                    beginControlFlow("if (expected.%1N != %1N)", trait.name)
                    addStatement(
                      "throw %1T(\"Expected:·\${expected.%2N}·but·was:·\$%2N\\n··at·\$path.%2N\")",
                      ae, trait.name
                    )
                    endControlFlow()
                  }
                  is Children -> {
                    addStatement("%N.forEachIndexed { index, child ->", trait.name)
                    addStatement(
                      "⇥child.assertSchema(expected.%1N[index], \"\$path.%1N[\$index]\")⇤",
                      trait.name
                    )
                    addStatement("}")
                  }
                  is Event -> {
                    // Ignored for schema assertion.
                  }
                }
              }
            }
            .build()
        )
        .build()
    )
    .build()
}
