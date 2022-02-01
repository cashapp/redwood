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
package app.cash.redwood.schema.generator

import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.UNIT

internal val eventType = ClassName("app.cash.redwood.protocol", "Event")
internal val eventSink = ClassName("app.cash.redwood.protocol", "EventSink")
internal val propertyDiff = ClassName("app.cash.redwood.protocol", "PropertyDiff")

internal val abstractDiffProducingWidget = ClassName("app.cash.redwood.protocol.compose", "AbstractDiffProducingWidget")
internal val diffProducingWidget = ClassName("app.cash.redwood.protocol.compose", "DiffProducingWidget")
internal val diffProducingWidgetFactory = diffProducingWidget.nestedClass("Factory")
internal val syntheticChildren = MemberName("app.cash.redwood.protocol.compose", "\$SyntheticChildren")

internal val DiffConsumingWidget = ClassName("app.cash.redwood.protocol.widget", "DiffConsumingWidget")
internal val DiffConsumingWidgetFactory = DiffConsumingWidget.nestedClass("Factory")

internal val widgetType = ClassName("app.cash.redwood.widget", "Widget")
internal val widgetChildren = widgetType.nestedClass("Children")
internal val widgetFactory = widgetType.nestedClass("Factory")
internal val mutableListChildren = ClassName("app.cash.redwood.widget", "MutableListChildren")

internal val redwoodComposeNode = MemberName("app.cash.redwood.compose", "RedwoodComposeNode")

internal val composable = ClassName("androidx.compose.runtime", "Composable")

internal val composableLambda = LambdaTypeName.get(returnType = UNIT)
  .copy(
    annotations = listOf(
      AnnotationSpec.builder(composable).build(),
    )
  )

internal val ae = ClassName("kotlin", "AssertionError")
internal val iae = ClassName("kotlin", "IllegalArgumentException")

internal val typeVariableT = TypeVariableName("T", listOf(ANY))
internal val childrenOfT = widgetChildren.parameterizedBy(typeVariableT)

private val jsonCompanion = ClassName("kotlinx.serialization.json", "Json", "Default")
internal val jsonPrimitive = MemberName("kotlinx.serialization.json", "JsonPrimitive")
internal val jsonPrimitiveToBoolean = MemberName("kotlinx.serialization.json", "boolean")
internal val jsonElementToJsonPrimitive = MemberName("kotlinx.serialization.json", "jsonPrimitive")
internal val encodeToJsonElement = MemberName(jsonCompanion, "encodeToJsonElement")
internal val decodeFromJsonElement = MemberName(jsonCompanion, "decodeFromJsonElement")
internal val serializer = MemberName("kotlinx.serialization", "serializer")
internal val serializersModule = ClassName("kotlinx.serialization.modules", "SerializersModule")
internal val kSerializer = ClassName("kotlinx.serialization", "KSerializer")
