/*
 * Copyright (C) 2023 Square, Inc.
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

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import kotlin.jvm.JvmInline
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

@Serializable
public class StateSnapshot(
  public val content: Map<String, List<@Polymorphic Any?>>,
) {

  @JvmInline
  @Serializable
  public value class Id(public val value: String?)
}

// TODO Add support for rest of built-ins serializers.
public val SaveableStateSerializersModule: SerializersModule = SerializersModule {
  polymorphic(Any::class) {
    subclass(Boolean::class)
    subclass(Double::class)
    subclass(Float::class)
    subclass(Int::class)
    subclass(String::class)
  }
  polymorphicDefaultSerializer(Any::class) { value ->
    @Suppress("UNCHECKED_CAST")
    when (value) {
      is List<*> -> ListSerializer(PolymorphicSerializer(Any::class)) as SerializationStrategy<Any>
      is MutableState<*> -> MutableStateSerializer as SerializationStrategy<Any>
      else -> null
    }
  }
  polymorphicDefaultDeserializer(Any::class) { className ->
    when (className) {
      "kotlin.collections.ArrayList" -> ListSerializer(PolymorphicSerializer(Any::class))
      "MutableState" -> MutableStateSerializer
      else -> null
    }
  }
}

@Serializable
@SerialName("MutableState")
private class MutableStateSurrogate(val value: @Polymorphic Any?)

private object MutableStateSerializer : KSerializer<MutableState<Any?>> {
  override val descriptor = MutableStateSurrogate.serializer().descriptor

  override fun serialize(encoder: Encoder, value: MutableState<Any?>) {
    val surrogate = MutableStateSurrogate(value.value)
    encoder.encodeSerializableValue(MutableStateSurrogate.serializer(), surrogate)
  }

  override fun deserialize(decoder: Decoder): MutableState<Any?> {
    val surrogate = decoder.decodeSerializableValue(MutableStateSurrogate.serializer())
    return mutableStateOf(surrogate.value)
  }
}
