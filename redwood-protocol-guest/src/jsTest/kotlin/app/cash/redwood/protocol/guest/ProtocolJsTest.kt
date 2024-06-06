///*
// * Copyright (C) 2024 Square, Inc.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package app.cash.redwood.protocol.guest
//
//import app.cash.redwood.protocol.Change
//import app.cash.redwood.protocol.ChildrenChange
//import app.cash.redwood.protocol.ChildrenTag
//import app.cash.redwood.protocol.Create
//import app.cash.redwood.protocol.DynamicFactory
//import app.cash.redwood.protocol.Id
//import app.cash.redwood.protocol.ModifierChange
//import app.cash.redwood.protocol.ModifierElement
//import app.cash.redwood.protocol.ModifierTag
//import app.cash.redwood.protocol.PropertyChange
//import app.cash.redwood.protocol.PropertyTag
//import app.cash.redwood.protocol.WidgetTag
//import assertk.assertThat
//import assertk.assertions.isEqualTo
//import kotlin.test.Test
//import kotlinx.serialization.ExperimentalSerializationApi
//import kotlinx.serialization.json.Json
//import kotlinx.serialization.json.JsonPrimitive
//import kotlinx.serialization.json.encodeToDynamic
//
//@OptIn(ExperimentalSerializationApi::class)
//class ProtocolJsTest {
//  private val json = Json {
//    useArrayPolymorphism = true
//    ignoreUnknownKeys = true
//    allowStructuredMapKeys = true
//  }
//
//  @Test
//  fun consistentWithConstructorCall() {
//    val id = Id(3)
//    val childId = Id(13)
//    val widgetTag = WidgetTag(5)
//    val childrenTag = ChildrenTag(11)
//    val propertyTag = PropertyTag(17)
//    val modifierTag = ModifierTag(7)
//    val value = JsonPrimitive(9)
//    val childIndex = 15
//
//    assertEncoded(
//      serialName = "create",
//      constructorCall = Create(id, widgetTag),
//      dynamicCall = DynamicFactory.Create(id, widgetTag),
//      expectedJson = """{"id":3,"tag":5}""",
//    )
//
//    assertEncoded(
//      serialName = "property",
//      constructorCall = PropertyChange(id, propertyTag, value),
//      dynamicCall = DynamicFactory.PropertyChange(id, propertyTag, value),
//      expectedJson = """{"id":3,"tag":17,"value":9}""",
//    )
//
//    assertEncoded(
//      serialName = "modifier",
//      constructorCall = ModifierChange(id, listOf(ModifierElement(modifierTag, value))),
//      dynamicCall = DynamicFactory.ModifierChange(id, listOf(ModifierElement(modifierTag, value))),
//      expectedJson = """{"id":3,"elements":[[7,9]]}""",
//    )
//
//    assertEncoded(
//      serialName = "add",
//      constructorCall = ChildrenChange.Add(id, childrenTag, childId, childIndex),
//      dynamicCall = DynamicFactory.Add(id, childrenTag, childId, childIndex),
//      expectedJson = """{"id":3,"tag":11,"childId":13,"index":15}""",
//    )
//
//    assertEncoded(
//      serialName = "move",
//      constructorCall = ChildrenChange.Move(id, childrenTag, childIndex, childIndex + 1, 3),
//      dynamicCall = DynamicFactory.Move(id, childrenTag, childIndex, childIndex + 1, 3),
//      expectedJson = """{"id":3,"tag":11,"fromIndex":15,"toIndex":16,"count":3}""",
//    )
//
//    assertEncoded(
//      serialName = "remove",
//      constructorCall = ChildrenChange.Remove(id, childrenTag, childIndex, 3, listOf(childId)),
//      dynamicCall = DynamicFactory.Remove(id, childrenTag, childIndex, 3, listOf(childId)),
//      expectedJson = """{"id":3,"tag":11,"index":15,"count":3,"removedIds":[13]}""",
//    )
//  }
//
//  private inline fun <reified T : Change> assertEncoded(
//    serialName: String,
//    constructorCall: T,
//    dynamicCall: Change,
//    expectedJson: String,
//  ) {
//    println(serialName)
//    assertThat(JSON.stringify(json.encodeToDynamic<T>(constructorCall)))
//      .isEqualTo(expectedJson)
//    assertThat(JSON.stringify(json.encodeToDynamic<Change>(dynamicCall)))
//      .isEqualTo("""["$serialName",$expectedJson]""")
//  }
//}
