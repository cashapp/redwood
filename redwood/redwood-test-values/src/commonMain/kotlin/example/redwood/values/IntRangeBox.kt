/*
 * Copyright (C) 2022 Square, Inc.
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
package example.redwood.values

import kotlinx.serialization.Contextual
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
public data class IntRangeBox(
  @Contextual val intRange: IntRange,
)

public object IntRangeAsStringSerializer : KSerializer<IntRange> {
  override val descriptor: SerialDescriptor =
    PrimitiveSerialDescriptor("example.redwood.values.IntRangeAsString", PrimitiveKind.STRING)

  override fun deserialize(decoder: Decoder): IntRange {
    val (start, endInclusive) = decoder.decodeString().split("..").map(String::toInt)
    return IntRange(start, endInclusive)
  }

  override fun serialize(encoder: Encoder, value: IntRange) {
    encoder.encodeString(value.toString())
  }
}
