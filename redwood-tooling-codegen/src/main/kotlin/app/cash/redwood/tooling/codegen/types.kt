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
  val Create = ClassName("app.cash.redwood.protocol", "Create")
  val Change = ClassName("app.cash.redwood.protocol", "Change")
  val ChildrenTag = ClassName("app.cash.redwood.protocol", "ChildrenTag")
  val Event = ClassName("app.cash.redwood.protocol", "Event")
  val EventTag = ClassName("app.cash.redwood.protocol", "EventTag")
  val EventSink = ClassName("app.cash.redwood.protocol", "EventSink")
  val Id = ClassName("app.cash.redwood.protocol", "Id")
  val ModifierChange = ClassName("app.cash.redwood.protocol", "ModifierChange")
  val ModifierElement = ClassName("app.cash.redwood.protocol", "ModifierElement")
  val ModifierTag = ClassName("app.cash.redwood.protocol", "ModifierTag")
  val PropertyChange = ClassName("app.cash.redwood.protocol", "PropertyChange")
  val PropertyTag = ClassName("app.cash.redwood.protocol", "PropertyTag")
  val WidgetTag = ClassName("app.cash.redwood.protocol", "WidgetTag")
}

internal object ComposeProtocol {
  val ProtocolBridge = ClassName("app.cash.redwood.protocol.compose", "ProtocolBridge")
  val ProtocolBridgeFactory = ProtocolBridge.nestedClass("Factory")
  val ProtocolState = ClassName("app.cash.redwood.protocol.compose", "ProtocolState")
  val ProtocolMismatchHandler =
    ClassName("app.cash.redwood.protocol.compose", "ProtocolMismatchHandler")
  val ProtocolWidget = ClassName("app.cash.redwood.protocol.compose", "ProtocolWidget")
}

internal object WidgetProtocol {
  val ProtocolMismatchHandler =
    ClassName("app.cash.redwood.protocol.widget", "ProtocolMismatchHandler")
  val ProtocolNode = ClassName("app.cash.redwood.protocol.widget", "ProtocolNode")
  val GeneratedProtocolFactory = ClassName("app.cash.redwood.protocol.widget", "GeneratedProtocolFactory")
}

internal object Redwood {
  val Modifier = ClassName("app.cash.redwood", "Modifier")
  val ModifierElement = Modifier.nestedClass("Element")
  val LayoutScopeMarker = ClassName("app.cash.redwood", "LayoutScopeMarker")
  val RedwoodCodegenApi = ClassName("app.cash.redwood", "RedwoodCodegenApi")
  val UiConfiguration = ClassName("app.cash.redwood.ui", "UiConfiguration")
}

internal object RedwoodTesting {
  val TestRedwoodComposition = ClassName("app.cash.redwood.testing", "TestRedwoodComposition")
  val TestSavedState = ClassName("app.cash.redwood.testing", "TestSavedState")
  val WidgetValue = ClassName("app.cash.redwood.testing", "WidgetValue")
}

internal object RedwoodWidget {
  val Widget = ClassName("app.cash.redwood.widget", "Widget")
  val WidgetChildren = Widget.nestedClass("Children")
  val WidgetChildrenOfW = WidgetChildren.parameterizedBy(typeVariableW)
  val WidgetProvider = Widget.nestedClass("Provider")
  val MutableListChildren = ClassName("app.cash.redwood.widget", "MutableListChildren")
}

internal object RedwoodCompose {
  val RedwoodComposeNode = MemberName("app.cash.redwood.compose", "RedwoodComposeNode")
  val WidgetNode = ClassName("app.cash.redwood.compose", "WidgetNode")
}

internal object ComposeRuntime {
  val Composable = ClassName("androidx.compose.runtime", "Composable")
  val Stable = ClassName("androidx.compose.runtime", "Stable")
}

internal fun composableLambda(
  receiver: TypeName?,
): TypeName {
  return LambdaTypeName.get(
    returnType = UNIT,
    receiver = receiver,
  ).copy(
    annotations = listOf(
      AnnotationSpec.builder(ComposeRuntime.Composable).build(),
    ),
  )
}

internal object Stdlib {
  val AssertionError = ClassName("kotlin", "AssertionError")
  val ExperimentalObjCName = ClassName("kotlin.experimental", "ExperimentalObjCName")
  val List = ClassName("kotlin.collections", "List")
  val ObjCName = ClassName("kotlin.native", "ObjCName")
  val buildList = MemberName("kotlin.collections", "buildList")
  val listOf = MemberName("kotlin.collections", "listOf")
}

internal val typeVariableW = TypeVariableName("W", listOf(ANY))

internal object KotlinxSerialization {
  val Contextual = ClassName("kotlinx.serialization", "Contextual")
  val ContextualSerializer = ClassName("kotlinx.serialization", "ContextualSerializer")
  val ExperimentalSerializationApi = ClassName("kotlinx.serialization", "ExperimentalSerializationApi")
  val KSerializer = ClassName("kotlinx.serialization", "KSerializer")
  val Serializable = ClassName("kotlinx.serialization", "Serializable")
  val serializer = MemberName("kotlinx.serialization", "serializer")

  val SerialDescriptor = ClassName("kotlinx.serialization.descriptors", "SerialDescriptor")
  val buildClassSerialDescriptor = MemberName("kotlinx.serialization.descriptors", "buildClassSerialDescriptor")
  val element = MemberName("kotlinx.serialization.descriptors", "element")

  val Decoder = ClassName("kotlinx.serialization.encoding", "Decoder")
  val Encoder = ClassName("kotlinx.serialization.encoding", "Encoder")

  val Json = ClassName("kotlinx.serialization.json", "Json")
  val JsonDefault = Json.nestedClass("Default")
  val JsonPrimitive = MemberName("kotlinx.serialization.json", "JsonPrimitive")
  val jsonBoolean = MemberName("kotlinx.serialization.json", "boolean")

  @JvmField val jsonPrimitive = MemberName("kotlinx.serialization.json", "jsonPrimitive")
}

internal object KotlinxCoroutines {
  val coroutineScope = MemberName("kotlinx.coroutines", "coroutineScope")
}
