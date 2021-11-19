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
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.PUBLIC
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.joinToCode

/*
@Composable
fun SunspotButton(
  text: String?,
  enabled: Boolean = true,
  onClick: (() -> Unit)? = null
): Unit {
  TreehouseComposeNode<SunspotWidgetFactory<*>, SunspotButton<*>>(
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
            .add("factory = %T::%N", widgetFactoryType, widget.flatName)
            .build()

          val updateLambda = CodeBlock.builder()
          val childrenLambda = CodeBlock.builder()
          for (trait in widget.traits) {
            @Exhaustive when (trait) {
              is Property,
              is Event -> {
                updateLambda.add("set(%1N) { %1N(%1N) }\n", trait.name)
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

          arguments += CodeBlock.builder()
            .add("content = {\n")
            .indent()
            .add(childrenLambda.build())
            .unindent()
            .add("}")
            .build()

          addStatement(
            "%M<%T, %T>(%L)", treehouseComposeNode, widgetFactoryType, widgetType,
            arguments.joinToCode(",\n", "\n", ",\n")
          )
        }
        .build()
    )
    .build()
}
