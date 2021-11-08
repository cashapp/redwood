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

import app.cash.treehouse.schema.parser.Event
import app.cash.treehouse.schema.parser.Property
import app.cash.treehouse.schema.parser.Widget
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.OVERRIDE
import com.squareup.kotlinpoet.KModifier.PUBLIC
import com.squareup.kotlinpoet.asTypeName

/** Returns a function that returns a map from tag to serializer. */
internal fun createSerializers(widget: Widget) =
  FunSpec.builder("createSerializers")
    .addModifiers(PUBLIC, OVERRIDE)
    .addParameter("module", serializersModule)
    .returns(mapIntKSerializer)
    .addCode("return mapOf(⇥\n")
    .apply {
      for (trait in widget.traits) {
        val type = when {
          trait is Event && trait.parameterType != null -> trait.parameterType!!
          trait is Property -> trait.type
          else -> continue
        }
        addCode("%L to module.%M<%T>(),\n", trait.tag, serializer, type.asTypeName())
      }
    }
    .addCode("⇤)")
    .build()

