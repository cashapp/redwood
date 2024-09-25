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
  val ChildrenTag = ClassName("app.cash.redwood.protocol", "ChildrenTag")
  val Event = ClassName("app.cash.redwood.protocol", "Event")
  val EventTag = ClassName("app.cash.redwood.protocol", "EventTag")
  val EventSink = ClassName("app.cash.redwood.protocol", "EventSink")
  val Id = ClassName("app.cash.redwood.protocol", "Id")
  val ModifierElement = ClassName("app.cash.redwood.protocol", "ModifierElement")
  val ModifierTag = ClassName("app.cash.redwood.protocol", "ModifierTag")
  val PropertyChange = ClassName("app.cash.redwood.protocol", "PropertyChange")
  val PropertyTag = ClassName("app.cash.redwood.protocol", "PropertyTag")
  val WidgetTag = ClassName("app.cash.redwood.protocol", "WidgetTag")
}

internal object ProtocolGuest {
  val GuestProtocolAdapter = ClassName("app.cash.redwood.protocol.guest", "GuestProtocolAdapter")
  val ProtocolMismatchHandler =
    ClassName("app.cash.redwood.protocol.guest", "ProtocolMismatchHandler")
  val ProtocolWidget = ClassName("app.cash.redwood.protocol.guest", "ProtocolWidget")
  val ProtocolWidgetChildren = ClassName("app.cash.redwood.protocol.guest", "ProtocolWidgetChildren")
  val ProtocolWidgetChildrenVisitor = ProtocolWidget.nestedClass("ChildrenVisitor")
  val ProtocolWidgetSystemFactory = ClassName("app.cash.redwood.protocol.guest", "ProtocolWidgetSystemFactory")
}

internal object ProtocolHost {
  val IdVisitor = ClassName("app.cash.redwood.protocol.host", "IdVisitor")
  val ProtocolMismatchHandler =
    ClassName("app.cash.redwood.protocol.host", "ProtocolMismatchHandler")
  val ProtocolNode = ClassName("app.cash.redwood.protocol.host", "ProtocolNode")
  val ProtocolChildren = ClassName("app.cash.redwood.protocol.host", "ProtocolChildren")
  val GeneratedProtocolFactory = ClassName("app.cash.redwood.protocol.host", "GeneratedProtocolFactory")
  val UiEvent = ClassName("app.cash.redwood.protocol.host", "UiEvent")
  val UiEventSink = ClassName("app.cash.redwood.protocol.host", "UiEventSink")
}

internal object Redwood {
  val Modifier = ClassName("app.cash.redwood", "Modifier")
  val ModifierElement = Modifier.nestedClass("Element")
  val ModifierScopedElement = Modifier.nestedClass("ScopedElement")
  val ModifierUnscopedElement = Modifier.nestedClass("UnscopedElement")
  val LayoutScopeMarker = ClassName("app.cash.redwood", "LayoutScopeMarker")
  val RedwoodCodegenApi = ClassName("app.cash.redwood", "RedwoodCodegenApi")
  val OnBackPressedDispatcher = ClassName("app.cash.redwood.ui", "OnBackPressedDispatcher")
  val UiConfiguration = ClassName("app.cash.redwood.ui", "UiConfiguration")
}

internal object RedwoodTesting {
  val NoOpOnBackPressedDispatcher = ClassName("app.cash.redwood.testing", "NoOpOnBackPressedDispatcher")
  val TestRedwoodComposition = ClassName("app.cash.redwood.testing", "TestRedwoodComposition")
  val TestSavedState = ClassName("app.cash.redwood.testing", "TestSavedState")
  val WidgetValue = ClassName("app.cash.redwood.testing", "WidgetValue")
}

internal object RedwoodWidget {
  val Widget = ClassName("app.cash.redwood.widget", "Widget")
  val WidgetChildren = Widget.nestedClass("Children")
  val WidgetChildrenOfW = WidgetChildren.parameterizedBy(typeVariableW)
  val WidgetSystem = ClassName("app.cash.redwood.widget", "WidgetSystem")
  val WidgetFactoryOwner = ClassName("app.cash.redwood.widget", "WidgetFactoryOwner")
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

internal object AndroidxCollection {
  val IntObjectMap = ClassName("androidx.collection", "IntObjectMap")
  val MutableIntObjectMap = ClassName("androidx.collection", "MutableIntObjectMap")
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
  val Pair = ClassName("kotlin", "Pair")
  val listOf = MemberName("kotlin.collections", "listOf")
}

internal val typeVariableW = TypeVariableName("W", listOf(ANY))

internal object KotlinxSerialization {
  val Contextual = ClassName("kotlinx.serialization", "Contextual")
  val ContextualSerializer = ClassName("kotlinx.serialization", "ContextualSerializer")
  val ExperimentalSerializationApi = ClassName("kotlinx.serialization", "ExperimentalSerializationApi")
  val KSerializer = ClassName("kotlinx.serialization", "KSerializer")
  val Serializable = ClassName("kotlinx.serialization", "Serializable")
  val SerializationStrategy = ClassName("kotlinx.serialization", "SerializationStrategy")
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
