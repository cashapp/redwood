/*
 * Copyright (C) 2024 Square, Inc.
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
package app.cash.redwood.treehouse

import app.cash.redwood.Modifier
import app.cash.redwood.RedwoodCodegenApi
import app.cash.redwood.protocol.Change
import app.cash.redwood.protocol.guest.DefaultProtocolBridge
import app.cash.redwood.protocol.guest.ProtocolMismatchHandler
import app.cash.redwood.protocol.guest.guestRedwoodVersion
import app.cash.redwood.widget.Widget
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.example.redwood.testapp.compose.backgroundColor
import com.example.redwood.testapp.protocol.guest.TestSchemaProtocolWidgetSystemFactory
import com.example.redwood.testapp.widget.TestSchemaWidgetSystem
import kotlin.test.Test
import kotlin.time.Duration
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PrimitiveKind.STRING
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromDynamic
import kotlinx.serialization.modules.SerializersModule

/**
 * Confirm that [FastProtocolBridge] behaves the same as [DefaultProtocolBridge].
 */
@OptIn(ExperimentalSerializationApi::class, RedwoodCodegenApi::class)
class FastProtocolBridgeTest {
  @Test fun consistentWithDefaultProtocolBridge() {
    assertChangesEqual { root, widgetSystem ->
      val button = widgetSystem.TestSchema.Button()
      button.onClick { error("unexpected call") }
      root.insert(0, button)
      button.text("Click Me")

      val textInput = widgetSystem.TestSchema.TextInput()
      root.insert(1, textInput)
      textInput.modifier = Modifier.backgroundColor(0xff0000u)
      textInput.text("hello")

      root.move(0, 1, 1)
      root.remove(0, 2)
    }
  }

  /** Test our special case for https://github.com/Kotlin/kotlinx.serialization/issues/2713 */
  @Test fun consistentWithDefaultProtocolBridgeForUint() {
    assertChangesEqual { root, widgetSystem ->
      val button = widgetSystem.TestSchema.Button()
      button.color(0xffeeddccu)
    }
  }

  private fun assertChangesEqual(
    block: (Widget.Children<Unit>, TestSchemaWidgetSystem<Unit>) -> Unit,
  ) {
    val json = Json {
      useArrayPolymorphism = true
      serializersModule = SerializersModule {
        contextual(Duration::class, DurationIsoSerializer)
        contextual(UInt::class, UInt.serializer())
      }
    }

    val fastUpdates = collectChangesFromFastProtocolBridge(json, block)
    val defaultUpdates = collectChangesFromDefaultProtocolBridge(json, block)
    assertThat(fastUpdates).isEqualTo(defaultUpdates)
  }

  private fun collectChangesFromDefaultProtocolBridge(
    json: Json,
    block: (Widget.Children<Unit>, TestSchemaWidgetSystem<Unit>) -> Unit,
  ): List<Change> {
    val bridge = DefaultProtocolBridge(
      // Use latest guest version as the host version to avoid any compatibility behavior.
      hostVersion = guestRedwoodVersion,
      widgetSystemFactory = TestSchemaProtocolWidgetSystemFactory,
      json = json,
      mismatchHandler = ProtocolMismatchHandler.Throwing,
    )

    val result = mutableListOf<Change>()
    bridge.initChangesSink(
      object : ChangesSinkService {
        override fun sendChanges(changes: List<Change>) {
          result += changes
        }
      },
    )

    block(
      bridge.root,
      bridge.widgetSystem as TestSchemaWidgetSystem<Unit>,
    )
    bridge.emitChanges()

    return result
  }

  private fun collectChangesFromFastProtocolBridge(
    json: Json,
    block: (Widget.Children<Unit>, TestSchemaWidgetSystem<Unit>) -> Unit,
  ): List<Change> {
    val bridge = FastProtocolBridge(
      // Use latest guest version as the host version to avoid any compatibility behavior.
      hostVersion = guestRedwoodVersion,
      widgetSystemFactory = TestSchemaProtocolWidgetSystemFactory,
      json = json,
      mismatchHandler = ProtocolMismatchHandler.Throwing,
    )

    val result = mutableListOf<Change>()
    bridge.initChangesSink(
      changesSinkService = object : ChangesSinkService {
        override fun sendChanges(changes: List<Change>) {
        }
      },
      sendChanges = { _, args ->
        result += json.decodeFromDynamic<List<Change>>(args.single())
        Unit
      },
    )

    block(
      bridge.root,
      bridge.widgetSystem as TestSchemaWidgetSystem<Unit>,
    )
    bridge.emitChanges()

    return result
  }

  object DurationIsoSerializer : KSerializer<Duration> {
    override val descriptor get() = PrimitiveSerialDescriptor("Duration", STRING)
    override fun serialize(encoder: Encoder, value: Duration) = encoder.encodeString(value.toIsoString())

    override fun deserialize(decoder: Decoder): Duration {
      return Duration.parseIsoString(decoder.decodeString())
    }
  }
}
