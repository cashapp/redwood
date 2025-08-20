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

internal object FocusRequesterSerializer : KSerializer<FocusRequester> {
  private val delegate = FocusRequesterId.serializer()

  override val descriptor = SerialDescriptor(
    serialName = "app.cash.redwood.ui.core.api.FocusRequester",
    original = delegate.descriptor,
  )

  override fun serialize(encoder: Encoder, value: FocusRequester) {
    encoder.encodeSerializableValue(delegate, FocusRequesterId(value.id))
  }

  override fun deserialize(decoder: Decoder): FocusRequester {
    return decoder.decodeSerializableValue(delegate)
  }
}

@Serializable
private data class FocusRequesterId(
  override val id: Int,
) : FocusRequester {
  override fun requestFocus() {
    error("unexpected call to requestFocus() on deserialized FocusRequester")
  }
}
