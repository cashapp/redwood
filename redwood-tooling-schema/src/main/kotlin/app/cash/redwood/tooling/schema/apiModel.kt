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
package app.cash.redwood.tooling.schema

import app.cash.redwood.tooling.schema.ApiSchema.Difference.Type.Fatal
import app.cash.redwood.tooling.schema.ApiSchema.Difference.Type.Fixable
import app.cash.redwood.tooling.schema.ApiSchema.Difference.Type.Warning
import app.cash.redwood.tooling.schema.ProtocolWidget.ProtocolChildren
import app.cash.redwood.tooling.schema.ProtocolWidget.ProtocolEvent
import app.cash.redwood.tooling.schema.ProtocolWidget.ProtocolProperty
import app.cash.redwood.tooling.schema.ValidationMode.Check
import app.cash.redwood.tooling.schema.ValidationMode.Generate
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.EncodeDefault.Mode.ALWAYS
import kotlinx.serialization.EncodeDefault.Mode.NEVER
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XML

internal enum class ValidationMode {
  Check,
  Generate,
}

internal sealed interface ValidationResult {
  object Success : ValidationResult
  data class Failure(val message: String) : ValidationResult
}

@Serializable
@SerialName("schema")
internal data class ApiSchema(
  /** The format version of this XML. */
  @OptIn(ExperimentalSerializationApi::class) // Easier than second constructor setting fixed value.
  @EncodeDefault(ALWAYS)
  val version: UInt = 1U,
  val type: String,
  val widgets: List<ApiWidget> = emptyList(),
  val modifiers: List<ApiModifier> = emptyList(),
) {
  init {
    require(version == 1U) {
      "Only version 1 is supported"
    }
  }

  constructor(schema: ProtocolSchema) : this(
    type = schema.type.toString(),
    widgets = schema.widgets.map(::ApiWidget).sortedBy(ApiWidget::tag),
    modifiers = schema.modifiers.map(::ApiModifier).sortedBy(ApiModifier::tag),
  )

  fun validateAgainst(file: Path, mode: ValidationMode, fixCommand: String): ValidationResult {
    val fileApi = if (file.exists()) {
      xml.decodeFromString(serializer(), file.readText())
    } else {
      null
    }

    fun failure(differences: List<Difference>, message: String) = ValidationResult.Failure(
      buildString {
        appendLine(message)
        appendLine()
        appendLine("Differences:")
        for (difference in differences) {
          append(" - ")
          append(difference.message)
          append(" (")
          append(difference.type.name.lowercase())
          appendLine(')')
        }
        appendLine()
        append(
          if (differences.any { it.type == Fatal }) {
            """
            The API file cannot be updated automatically due to the presence of fatal
            differences. Either revert the fatal differences to the schema, or delete the
            file and run '$fixCommand' to regenerate. Note that regenerating the file
            means the new schema is fundamentally incompatible with the old one and will
            produce errors if both the host and guest code are not updated together.
            """.trimIndent()
          } else {
            "Run '$fixCommand' to automatically update the file."
          },
        )
      },
    )

    when (mode) {
      Check -> {
        if (fileApi == null) {
          val relativePath = file.fileSystem.getPath("").toAbsolutePath().relativize(file)
          return ValidationResult.Failure(
            """
            |API file $relativePath missing!
            |
            |Run '$fixCommand' to generate this file.
            """.trimMargin(),
          )
        }
        val differences = diffFrom(fileApi)
        if (differences.isNotEmpty()) {
          return failure(differences, "API file does not match!")
        }
      }

      Generate -> {
        if (fileApi != null) {
          val differences = diffFrom(fileApi).filter { it.type == Fatal }
          if (differences.isNotEmpty()) {
            return failure(differences, "Schema contains incompatible changes with current API!")
          }
        }
        file.writeText(xml.encodeToString(serializer(), this) + "\n")
      }
    }
    return ValidationResult.Success
  }

  private data class Difference(
    val message: String,
    val type: Type,
  ) {
    enum class Type { Fatal, Fixable, Warning }
  }

  private fun diffFrom(file: ApiSchema): List<Difference> {
    val diffs = mutableListOf<Difference>()

    if (type != file.type) {
      diffs += Difference("Schema type changed from ${file.type} to $type", Warning)
    }

    val (matchedWidgets, missingWidgets, extraWidgets) = widgets.diffFrom(file.widgets) { it.tag }
    for (widget in missingWidgets) {
      diffs += Difference("Widget(tag=${widget.tag}) is missing from the file, but it is a part of the Kotlin schema", Fixable)
    }
    for (widget in extraWidgets) {
      diffs += Difference("Widget(tag=${widget.tag}) is tracked in the file, but it is not part of the Kotlin schema", Fatal)
    }
    for ((widget, fileWidget) in matchedWidgets) {
      if (widget.type != fileWidget.type) {
        diffs += Difference("Widget(tag=${widget.tag}) type changed from ${fileWidget.type} to ${widget.type} in the Kotlin schema", Warning)
      }
      val (matchedTraits, missingTraits, extraTraits) = widget.traits.diffFrom(fileWidget.traits) { it.tag to it.javaClass }
      fun ApiWidgetTrait.humanType() = when (this) {
        is ApiWidgetChildren -> "children"
        is ApiWidgetEvent -> "event"
        is ApiWidgetProperty -> "property"
      }
      for (trait in missingTraits) {
        diffs += Difference("Widget(tag=${widget.tag}) is missing ${trait.humanType()}(tag=${trait.tag}) which is part of the Kotlin schema", Fixable)
      }
      for (trait in extraTraits) {
        diffs += Difference("Widget(tag=${widget.tag}) contains unknown ${trait.humanType()}(tag=${trait.tag}) which is not part of the Kotlin schema", Fatal)
      }
      for ((trait, fileTrait) in matchedTraits) {
        if (trait.name != fileTrait.name) {
          diffs += Difference("Widget(tag=${widget.tag}) ${trait.humanType()}(tag=${trait.tag}) name changed from ${fileTrait.name} to ${trait.name}", Fixable)
        }
        when (trait) {
          // Nothing else to verify for this type.
          is ApiWidgetChildren -> Unit

          is ApiWidgetEvent -> {
            val fileEvent = fileTrait as ApiWidgetEvent
            if (trait.params != fileEvent.params) {
              diffs += Difference("Widget(tag=${widget.tag}) event(tag=${trait.tag}) type changed from ${fileEvent.params} to ${trait.params}", Fatal)
            }
            if (trait.nullable != fileEvent.nullable) {
              val type = if (trait.nullable) Fatal else Fixable
              diffs += Difference("Widget(tag=${widget.tag}) event(tag=${trait.tag}) nullability changed from ${fileEvent.nullable} to ${trait.nullable}", type)
            }
          }

          is ApiWidgetProperty -> {
            val fileProperty = fileTrait as ApiWidgetProperty
            if (trait.type != fileProperty.type) {
              diffs += Difference("Widget(tag=${widget.tag}) property(tag=${trait.tag}) type changed from ${fileProperty.type} to ${trait.type}", Fatal)
            }
          }
        }
      }
    }

    val (matchedModifiers, missingModifiers, extraModifiers) = modifiers.diffFrom(file.modifiers) { it.tag }
    for (modifier in missingModifiers) {
      diffs += Difference("Modifier(tag=${modifier.tag}) is missing from the file, but it is part of the Kotlin schema", Fixable)
    }
    for (modifier in extraModifiers) {
      diffs += Difference("Modifier(tag=${modifier.tag}) is tracked in the file, but is not part of the Kotlin schema", Fatal)
    }
    for ((modifier, fileWidget) in matchedModifiers) {
      val (matchedProperties, missingProperties, extraProperties) = modifier.properties.diffFrom(fileWidget.properties) { it.name }
      for (property in missingProperties) {
        diffs += Difference("Modifier(tag=${modifier.tag}) is missing property(name=${property.name}) which is part of the Kotlin schema", Fixable)
      }
      for (property in extraProperties) {
        diffs += Difference("Modifier(tag=${modifier.tag}) contains unknown property(name=${property.name}) which is not part of the Kotlin schema", Fatal)
      }
      for ((property, oldProperty) in matchedProperties) {
        if (property.type != oldProperty.type) {
          diffs += Difference("Modifier(tag=${modifier.tag}) property(name=${property.name}) type changed from ${oldProperty.type} to ${property.type} in the Kotlin schema", Fatal)
        }
      }
    }

    return diffs
  }

  companion object {
    private val xml = XML {
      indent = 2
      autoPolymorphic = true
    }

    /**
     * Compute the difference between [this] and [previous] collections by [key].
     * Returns a triple of matching pairs, added items, and removed items.
     */
    private fun <T, K> Collection<T>.diffFrom(
      previous: Collection<T>,
      key: (T) -> K,
    ): Triple<List<Pair<T, T>>, List<T>, List<T>> {
      val currentsByTag = associateBy(key)
      val previousByTag = previous.associateBy(key)

      val matched = mapNotNull { item -> previousByTag[key(item)]?.let { item to it } }
      val added = filter { key(it) !in previousByTag }
      val removed = previous.filter { key(it) !in currentsByTag }

      return Triple(matched, added, removed)
    }
  }
}

@Serializable
@SerialName("widget")
internal data class ApiWidget(
  val tag: Int,
  val type: String,
  val traits: List<ApiWidgetTrait> = emptyList(),
) {
  constructor(widget: ProtocolWidget) : this(
    type = widget.type.toString(),
    tag = widget.tag,
    traits = widget.traits
      .map {
        when (it) {
          is ProtocolEvent -> ApiWidgetEvent(it)
          is ProtocolProperty -> ApiWidgetProperty(it)
          is ProtocolChildren -> ApiWidgetChildren(it)
        }
      }
      .sortedWith(ApiWidgetTrait.comparator),
  )
}

@Serializable
internal sealed interface ApiWidgetTrait {
  val tag: Int
  val name: String

  companion object {
    val comparator = compareByDescending<ApiWidgetTrait> { it.javaClass.name }.thenBy { it.tag }
  }
}

@Serializable
@SerialName("property")
internal data class ApiWidgetProperty(
  override val tag: Int,
  override val name: String,
  val type: String,
) : ApiWidgetTrait {
  constructor(property: ProtocolProperty) : this(
    tag = property.tag,
    name = property.name,
    type = property.type.toString(),
  )
}

@Serializable
@SerialName("event")
@OptIn(ExperimentalSerializationApi::class) // Easier than second constructor setting fixed value.
internal data class ApiWidgetEvent(
  override val tag: Int,
  override val name: String,
  @EncodeDefault(NEVER)
  val params: String = "",
  @EncodeDefault(NEVER)
  val nullable: Boolean = false,
) : ApiWidgetTrait {
  constructor(event: ProtocolEvent) : this(
    tag = event.tag,
    name = event.name,
    params = event.parameterTypes.joinToString(","),
    nullable = event.isNullable,
  )
}

@Serializable
@SerialName("children")
internal data class ApiWidgetChildren(
  override val tag: Int,
  override val name: String,
) : ApiWidgetTrait {
  constructor(children: ProtocolChildren) : this(
    tag = children.tag,
    name = children.name,
  )
}

@Serializable
@SerialName("modifier")
internal data class ApiModifier(
  val tag: Int,
  val type: String,
  val properties: List<ApiModifierProperty> = emptyList(),
) {
  constructor(modifier: ProtocolModifier) : this(
    tag = modifier.tag,
    type = modifier.type.toString(),
    properties = modifier.properties.map(::ApiModifierProperty).sortedBy(ApiModifierProperty::name),
  )
}

@Serializable
@SerialName("property")
internal data class ApiModifierProperty(
  val name: String,
  val type: String,
) {
  constructor(property: Modifier.Property) : this(
    name = property.name,
    type = property.type.toString(),
  )
}
