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

import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.UNIT

internal val eventType = ClassName("app.cash.redwood.protocol", "Event")
internal val eventSink = ClassName("app.cash.redwood.protocol", "EventSink")
internal val propertyDiff = ClassName("app.cash.redwood.protocol", "PropertyDiff")
internal val LayoutModifiers = ClassName("app.cash.redwood.protocol", "LayoutModifiers")

internal val abstractDiffProducingWidget = ClassName("app.cash.redwood.protocol.compose", "AbstractDiffProducingWidget")
internal val diffProducingWidget = ClassName("app.cash.redwood.protocol.compose", "DiffProducingWidget")
internal val diffProducingWidgetFactory = diffProducingWidget.nestedClass("Factory")
internal val syntheticChildren = MemberName("app.cash.redwood.protocol.compose", "_SyntheticChildren")
internal val ComposeProtocolMismatchHandler = ClassName("app.cash.redwood.protocol.compose", "ProtocolMismatchHandler")

internal val DiffConsumingWidget = ClassName("app.cash.redwood.protocol.widget", "DiffConsumingWidget")
internal val DiffConsumingWidgetFactory = DiffConsumingWidget.nestedClass("Factory")
internal val WidgetProtocolMismatchHandler = ClassName("app.cash.redwood.protocol.widget", "ProtocolMismatchHandler")

internal val LayoutModifier = ClassName("app.cash.redwood", "LayoutModifier")
internal val LayoutModifierElement = LayoutModifier.nestedClass("Element")
internal val LayoutScopeMarker = ClassName("app.cash.redwood", "LayoutScopeMarker")

internal val widgetType = ClassName("app.cash.redwood.widget", "Widget")
internal val widgetChildren = widgetType.nestedClass("Children")
internal val widgetFactory = widgetType.nestedClass("Factory")

internal val redwoodComposeNode = MemberName("app.cash.redwood.compose", "RedwoodComposeNode")

internal val composable = ClassName("androidx.compose.runtime", "Composable")
internal val composableTargetMarker = ClassName("androidx.compose.runtime", "ComposableTargetMarker")
internal val stable = ClassName("androidx.compose.runtime", "Stable")

internal fun composableLambda(
  receiver: TypeName?,
  composeTargetMarker: ClassName,
): TypeName {
  return LambdaTypeName.get(
    returnType = UNIT,
    receiver = receiver,
  ).copy(
    annotations = listOf(
      AnnotationSpec.builder(composable).build(),
      AnnotationSpec.builder(composeTargetMarker).build(),
    ),
  )
}

internal val ae = ClassName("kotlin", "AssertionError")

internal val typeVariableT = TypeVariableName("T", listOf(ANY))
internal val childrenOfT = widgetChildren.parameterizedBy(typeVariableT)

@JvmField internal val Json = ClassName("kotlinx.serialization.json", "Json")

@JvmField internal val jsonCompanion = ClassName("kotlinx.serialization.json", "Json", "Default")

@JvmField internal val JsonArray = ClassName("kotlinx.serialization.json", "JsonArray")

@JvmField internal val JsonElement = ClassName("kotlinx.serialization.json", "JsonElement")

@JvmField internal val buildJsonArray = MemberName("kotlinx.serialization.json", "buildJsonArray")

@JvmField internal val buildJsonObject = MemberName("kotlinx.serialization.json", "buildJsonObject")

@JvmField internal val jsonArray = MemberName("kotlinx.serialization.json", "jsonArray")

@JvmField internal val jsonObject = MemberName("kotlinx.serialization.json", "jsonObject")

@JvmField internal val jsonPrimitive = MemberName("kotlinx.serialization.json", "jsonPrimitive")

@JvmField internal val jsonInt = MemberName("kotlinx.serialization.json", "int")

@JvmField internal val JsonPrimitive = MemberName("kotlinx.serialization.json", "JsonPrimitive")

@JvmField internal val jsonPrimitiveToBoolean = MemberName("kotlinx.serialization.json", "boolean")

@JvmField internal val jsonElementToJsonPrimitive = MemberName("kotlinx.serialization.json", "jsonPrimitive")

@JvmField internal val Contextual = ClassName("kotlinx.serialization", "Contextual")

@JvmField internal val Serializable = ClassName("kotlinx.serialization", "Serializable")

@JvmField internal val serializer = MemberName("kotlinx.serialization", "serializer")

@JvmField internal val KSerializer = ClassName("kotlinx.serialization", "KSerializer")
