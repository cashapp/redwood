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

internal object Protocol {
  val Event = ClassName("app.cash.redwood.protocol", "Event")
  val EventSink = ClassName("app.cash.redwood.protocol", "EventSink")
  val Id = ClassName("app.cash.redwood.protocol", "Id")
  val PropertyDiff = ClassName("app.cash.redwood.protocol", "PropertyDiff")
  val LayoutModifiers = ClassName("app.cash.redwood.protocol", "LayoutModifiers")
}

internal object ComposeProtocol {
  val DiffProducingWidget = ClassName("app.cash.redwood.protocol.compose", "DiffProducingWidget")
  val DiffProducingWidgetFactory = DiffProducingWidget.nestedClass("Factory")
  val ProtocolMismatchHandler =
    ClassName("app.cash.redwood.protocol.compose", "ProtocolMismatchHandler")
  val ProtocolState = ClassName("app.cash.redwood.protocol.compose", "ProtocolState")
}

internal object WidgetProtocol {
  val DiffConsumingWidget = ClassName("app.cash.redwood.protocol.widget", "DiffConsumingWidget")
  val DiffConsumingWidgetFactory = DiffConsumingWidget.nestedClass("Factory")
  val ProtocolMismatchHandler =
    ClassName("app.cash.redwood.protocol.widget", "ProtocolMismatchHandler")
}

internal object Redwood {
  val LayoutModifier = ClassName("app.cash.redwood", "LayoutModifier")
  val LayoutModifierElement = LayoutModifier.nestedClass("Element")
  val LayoutScopeMarker = ClassName("app.cash.redwood", "LayoutScopeMarker")
}

internal object RedwoodWidget {
  val Widget = ClassName("app.cash.redwood.widget", "Widget")
  val WidgetChildren = Widget.nestedClass("Children")
  val WidgetChildrenOfT = WidgetChildren.parameterizedBy(typeVariableT)
  val WidgetFactory = Widget.nestedClass("Factory")
}

internal object RedwoodCompose {
  val RedwoodComposeNode = MemberName("app.cash.redwood.compose", "_RedwoodComposeNode")
}

internal object ComposeRuntime {
  val Composable = ClassName("androidx.compose.runtime", "Composable")
  val ComposableTargetMarker = ClassName("androidx.compose.runtime", "ComposableTargetMarker")
  val Stable = ClassName("androidx.compose.runtime", "Stable")
}

internal fun composableLambda(
  receiver: TypeName?,
  composeTargetMarker: ClassName,
): TypeName {
  return LambdaTypeName.get(
    returnType = UNIT,
    receiver = receiver,
  ).copy(
    annotations = listOf(
      AnnotationSpec.builder(ComposeRuntime.Composable).build(),
      AnnotationSpec.builder(composeTargetMarker).build(),
    ),
  )
}

internal object Stdlib {
  val AssertionError = ClassName("kotlin", "AssertionError")
}

internal val typeVariableT = TypeVariableName("T", listOf(ANY))

internal object KotlinxSerialization {
  val Json = ClassName("kotlinx.serialization.json", "Json")
  val JsonDefault = Json.nestedClass("Default")
  val JsonArray = ClassName("kotlinx.serialization.json", "JsonArray")
  val JsonElement = ClassName("kotlinx.serialization.json", "JsonElement")
  val buildJsonArray = MemberName("kotlinx.serialization.json", "buildJsonArray")
  val buildJsonObject = MemberName("kotlinx.serialization.json", "buildJsonObject")
  val jsonArray = MemberName("kotlinx.serialization.json", "jsonArray")
  val jsonObject = MemberName("kotlinx.serialization.json", "jsonObject")

  @JvmField val jsonPrimitive = MemberName("kotlinx.serialization.json", "jsonPrimitive")
  val jsonInt = MemberName("kotlinx.serialization.json", "int")
  val JsonPrimitive = MemberName("kotlinx.serialization.json", "JsonPrimitive")
  val jsonBoolean = MemberName("kotlinx.serialization.json", "boolean")
  val Contextual = ClassName("kotlinx.serialization", "Contextual")
  val Serializable = ClassName("kotlinx.serialization", "Serializable")
  val serializer = MemberName("kotlinx.serialization", "serializer")
  val KSerializer = ClassName("kotlinx.serialization", "KSerializer")
}
