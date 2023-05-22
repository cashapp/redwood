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
package app.cash.redwood.tooling.lint

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XML

@Serializable
@SerialName("api")
internal data class RedwoodApi(
  val version: UInt,
  val widgets: List<RedwoodWidget> = emptyList(),
  val modifiers: List<RedwoodModifier> = emptyList(),
) {
  fun serialize() = xml.encodeToString(serializer(), this)

  companion object {
    private val xml = XML {
      indent = 2
    }

    @JvmStatic
    fun deserialize(string: String) = xml.decodeFromString(serializer(), string)
  }
}

@Serializable
@SerialName("widget")
internal data class RedwoodWidget(
  val tag: Int,
  val since: UInt,
  val traits: List<RedwoodWidgetTrait> = emptyList(),
) {
  init {
    val badTraits = traits.filter { it.since < since }
    require(badTraits.isEmpty()) {
      buildString {
        appendLine("Trait since values must be greater than or equal to widget")
        appendLine("""  Widget tag="$tag" since="$since"""")
        for (badTrait in badTraits) {
          appendLine("""    Trait tag="${badTrait.tag}" since="${badTrait.since}"""")
        }
        deleteCharAt(lastIndex) // Remove trailing newline.
      }
    }
  }
}

@Serializable
@SerialName("trait")
internal data class RedwoodWidgetTrait(
  val tag: Int,
  val since: UInt,
)

@Serializable
@SerialName("layout-modifier")
internal data class RedwoodModifier(
  val tag: Int,
  val since: UInt,
  val properties: List<RedwoodModifierProperty> = emptyList(),
) {
  init {
    val badProperties = properties.filter { it.since < since }
    require(badProperties.isEmpty()) {
      buildString {
        appendLine("Property since values must be greater than or equal to layout modifier")
        appendLine("""  Layout modifier tag="$tag" since="$since"""")
        for (badProperty in badProperties) {
          appendLine("""    Property tag="${badProperty.tag}" since="${badProperty.since}"""")
        }
        deleteCharAt(lastIndex) // Remove trailing newline.
      }
    }
  }
}

@Serializable
@SerialName("property")
internal data class RedwoodModifierProperty(
  val tag: Int,
  val since: UInt,
)
