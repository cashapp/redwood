/*
 * Copyright (C) 2025 Square, Inc.
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
package app.cash.redwood.ui.core.api

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * The [FocusRequester] works in two ways:
 *
 *  1. As an identifier that can be attached to a widget.
 *  2. As a service that can tell the UI to focus the widget with a particular identifier.
 *
 * As a modifier this serves the first role only, and can be safely serialized as data. And when
 * instances of [FocusRequester] are serialized, they lose the other capability.
 */
internal object FocusRequesterSerializer : KSerializer<FocusRequester> {
  private val delegate = FocusRequesterId.serializer()

  override val descriptor = SerialDescriptor(
    serialName = "app.cash.redwood.ui.core.api.FocusRequester",
    original = delegate.descriptor,
  )

  override fun serialize(encoder: Encoder, value: FocusRequester) {
    (value as? SerializableFocusRequester) ?: error("cannot serialize $value")
    encoder.encodeSerializableValue(delegate, FocusRequesterId(value.id))
  }

  override fun deserialize(decoder: Decoder): FocusRequester {
    return decoder.decodeSerializableValue(delegate)
  }
}

/**
 * A [FocusRequester] that is eligible to be serialized. Regardless of the source object's class,
 * the deserialized object will always be an instance of [FocusRequesterId].
 */
internal interface SerializableFocusRequester : FocusRequester {
  val id: Int
}

/**
 * A [FocusRequester] that has been deserialized and serves only as an identifier. This is a data
 * class to support [equals] and [hashCode], and two instances with the same ID are equal.
 */
@Serializable
private data class FocusRequesterId(
  override val id: Int,
) : SerializableFocusRequester {
  override fun requestFocus() {
    error("unexpected call to requestFocus() on deserialized FocusRequester")
  }
}
