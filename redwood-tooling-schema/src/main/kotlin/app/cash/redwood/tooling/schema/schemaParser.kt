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
package app.cash.redwood.tooling.schema

import app.cash.redwood.schema.Children as ChildrenAnnotation
import app.cash.redwood.schema.Default as DefaultAnnotation
import app.cash.redwood.schema.Modifier as ModifierAnnotation
import app.cash.redwood.schema.Property as PropertyAnnotation
import app.cash.redwood.schema.Schema as SchemaAnnotation
import app.cash.redwood.schema.Widget as WidgetAnnotation
import app.cash.redwood.tooling.schema.Deprecation.Level
import app.cash.redwood.tooling.schema.ProtocolWidget.ProtocolChildren
import app.cash.redwood.tooling.schema.ProtocolWidget.ProtocolProperty
import java.io.InputStream
import kotlin.DeprecationLevel.ERROR
import kotlin.DeprecationLevel.WARNING
import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.full.withNullability
import kotlin.reflect.typeOf

private val childrenType = Function::class.starProjectedType
private val eventType = Function::class.starProjectedType
private val optionalEventType = eventType.withNullability(true)

private const val MAX_SCHEMA_TAG = 2_000
internal const val MAX_MEMBER_TAG = 1_000_000

private val KClass<*>.schemaAnnotation: SchemaAnnotation get() {
  return requireNotNull(findAnnotation()) { "Schema $qualifiedName missing @Schema annotation" }
}

internal fun loadProtocolSchemaSet(
  type: FqType,
  classLoader: ClassLoader,
): ProtocolSchemaSet {
  val schema = loadProtocolSchema(type, classLoader)
  val dependencies = schema.taggedDependencies.map { (tag, dependency) ->
    loadProtocolSchema(dependency, classLoader, tag)
  }
  return ParsedProtocolSchemaSet(
    schema = schema,
    dependencies = dependencies.associateBy { it.type },
  )
}

internal fun loadProtocolSchema(
  type: FqType,
  classLoader: ClassLoader,
  tag: Int = 0,
): ProtocolSchema {
  require(tag in 0..MAX_SCHEMA_TAG) {
    "$type tag must be 0 for the root or in range (0, $MAX_SCHEMA_TAG] as a dependency: $tag"
  }
  val tagOffset = tag * MAX_MEMBER_TAG

  val path = ParsedProtocolSchema.toEmbeddedPath(type)
  val schema = classLoader
    .getResourceAsStream(path)
    ?.use(InputStream::readBytes)
    ?.decodeToString()
    ?.let { json -> ParsedProtocolSchema.parseEmbeddedJson(json, tagOffset) }
    ?: throw IllegalArgumentException("Unable to locate JSON for $type at $path")

  require(tag == 0 || schema.dependencies.isEmpty()) {
    "Schema dependency $type also has its own dependencies. " +
      "For now, only a single level of dependencies is supported."
  }

  return schema
}

internal fun parseProtocolSchemaSet(schemaType: KClass<*>): ProtocolSchemaSet {
  val schemaAnnotation = schemaType.schemaAnnotation
  val memberTypes = schemaAnnotation.members

  val duplicatedMembers = memberTypes.groupBy { it }.filterValues { it.size > 1 }.keys
  if (duplicatedMembers.isNotEmpty()) {
    throw IllegalArgumentException(
      buildString {
        append("Schema contains repeated member")
        if (duplicatedMembers.size > 1) {
          append('s')
        }
        duplicatedMembers.joinTo(this, prefix = "\n\n- ", separator = "\n- ") { it.qualifiedName!! }
      },
    )
  }

  val widgets = mutableListOf<ParsedProtocolWidget>()
  val modifiers = mutableListOf<ParsedProtocolModifier>()
  for (memberType in memberTypes) {
    val widgetAnnotation = memberType.findAnnotation<WidgetAnnotation>()
    val modifierAnnotation = memberType.findAnnotation<ModifierAnnotation>()

    if ((widgetAnnotation == null) == (modifierAnnotation == null)) {
      throw IllegalArgumentException(
        "${memberType.qualifiedName} must be annotated with either @Widget or @Modifier",
      )
    } else if (widgetAnnotation != null) {
      widgets += parseWidget(memberType, widgetAnnotation)
    } else if (modifierAnnotation != null) {
      modifiers += parseModifier(memberType, modifierAnnotation)
    } else {
      throw AssertionError()
    }
  }

  val badWidgets = widgets.groupBy(ProtocolWidget::tag).filterValues { it.size > 1 }
  if (badWidgets.isNotEmpty()) {
    throw IllegalArgumentException(
      buildString {
        appendLine("Schema @Widget tags must be unique")
        for ((widgetTag, group) in badWidgets) {
          append("\n- @Widget($widgetTag): ")
          group.joinTo(this) { it.type.toString() }
        }
      },
    )
  }

  val badReservedWidgets = schemaAnnotation.reservedWidgets
    .filterNotTo(HashSet(), HashSet<Int>()::add)
  require(badReservedWidgets.isEmpty()) {
    "Schema reserved widgets contains duplicates $badReservedWidgets"
  }

  val reservedWidgets = widgets.filter { it.tag in schemaAnnotation.reservedWidgets }
  if (reservedWidgets.isNotEmpty()) {
    throw IllegalArgumentException(
      buildString {
        append("Schema @Widget tags must not be included in reserved set ")
        appendLine(schemaAnnotation.reservedWidgets.contentToString())
        for (widget in reservedWidgets) {
          append("\n- @Widget(${widget.tag}) ${widget.type}")
        }
      },
    )
  }

  val badModifiers = modifiers.groupBy(ProtocolModifier::tag).filterValues { it.size > 1 }
  if (badModifiers.isNotEmpty()) {
    throw IllegalArgumentException(
      buildString {
        appendLine("Schema @Modifier tags must be unique")
        for ((modifierTag, group) in badModifiers) {
          append("\n- @Modifier($modifierTag): ")
          group.joinTo(this) { it.type.toString() }
        }
      },
    )
  }

  val badReservedModifiers = schemaAnnotation.reservedModifiers
    .filterNotTo(HashSet(), HashSet<Int>()::add)
  require(badReservedModifiers.isEmpty()) {
    "Schema reserved modifiers contains duplicates $badReservedModifiers"
  }

  val reservedModifiers = modifiers.filter { it.tag in schemaAnnotation.reservedModifiers }
  if (reservedModifiers.isNotEmpty()) {
    throw IllegalArgumentException(
      buildString {
        append("Schema @Modifier tags must not be included in reserved set ")
        appendLine(schemaAnnotation.reservedModifiers.contentToString())
        for (widget in reservedModifiers) {
          append("\n- @Modifier(${widget.tag}, …) ${widget.type}")
        }
      },
    )
  }

  val widgetScopes = widgets
    .flatMap { it.traits }
    .filterIsInstance<Widget.Children>()
    .mapNotNull { it.scope }
  val modifierScopes = modifiers
    .flatMap { it.scopes }
  val scopes = buildSet {
    addAll(widgetScopes)
    addAll(modifierScopes)
  }

  val badDependencyTags = schemaAnnotation.dependencies
    .groupBy { it.tag }
    .filterValues { it.size > 1 }
  if (badDependencyTags.isNotEmpty()) {
    throw IllegalArgumentException(
      buildString {
        appendLine("Schema dependency tags must be unique")
        for ((dependencyTag, group) in badDependencyTags) {
          append("\n- Dependency tag $dependencyTag: ")
          group.joinTo(this) { it.schema.qualifiedName!! }
        }
      },
    )
  }

  val badDependencyTypes = schemaAnnotation.dependencies
    .groupBy { it.schema }
    .filterValues { it.size > 1 }
    .keys
  if (badDependencyTypes.isNotEmpty()) {
    throw IllegalArgumentException(
      buildString {
        append("Schema contains repeated ")
        append(if (badDependencyTypes.size > 1) "dependencies" else "dependency")
        badDependencyTypes.joinTo(this, prefix = "\n\n- ", separator = "\n- ") { it.qualifiedName!! }
      },
    )
  }

  val dependencies = schemaAnnotation.dependencies
    .associate {
      val dependencyTag = it.tag
      val dependencyType = it.schema.toFqType()
      require(dependencyTag != 0) {
        "Dependency $dependencyType tag must not be non-zero"
      }

      val schema = loadProtocolSchema(
        type = dependencyType,
        classLoader = schemaType.java.classLoader,
        tag = dependencyTag,
      )
      dependencyTag to schema
    }

  val schema = ParsedProtocolSchema(
    type = schemaType.toFqType(),
    scopes = scopes.toList(),
    widgets = widgets,
    modifiers = modifiers,
    taggedDependencies = dependencies.mapValues { (_, schema) -> schema.type },
  )
  val schemaSet = ParsedProtocolSchemaSet(
    schema,
    dependencies.values.associateBy { it.type },
  )

  val duplicatedWidgets = schemaSet.all
    .flatMap { it.widgets.map { widget -> widget to it } }
    .groupBy { it.first.type }
    .filterValues { it.size > 1 }
    .mapValues { it.value.map(Pair<*, Schema>::second) }
  if (duplicatedWidgets.isNotEmpty()) {
    throw IllegalArgumentException(
      buildString {
        appendLine("Schema dependency tree contains duplicated widgets")
        for ((widget, schemas) in duplicatedWidgets) {
          append("\n- $widget: ")
          schemas.joinTo(this) { it.type.toString() }
        }
      },
    )
  }

  return schemaSet
}

private fun parseWidget(
  memberType: KClass<*>,
  annotation: WidgetAnnotation,
): ParsedProtocolWidget {
  val memberFqType = memberType.toFqType()
  val tag = annotation.tag
  require(tag in 1 until MAX_MEMBER_TAG) {
    "@Widget $memberFqType tag must be in range [1, $MAX_MEMBER_TAG): $tag"
  }

  val traits = if (memberType.isData) {
    memberType.primaryConstructor!!.parameters.map { parameter ->
      val kProperty = memberType.memberProperties.single { it.name == parameter.name }
      val name = kProperty.name
      val type = kProperty.returnType

      val property = kProperty.findAnnotation<PropertyAnnotation>()
      val children = kProperty.findAnnotation<ChildrenAnnotation>()
      val defaultExpression = kProperty.findAnnotation<DefaultAnnotation>()?.expression
      val deprecation = kProperty.parseDeprecation { "$memberFqType.$name" }

      if (property != null) {
        if (type.isSubtypeOf(eventType) || type.isSubtypeOf(optionalEventType)) {
          val arguments = type.arguments.dropLast(1) // Drop return type.
          ParsedProtocolEvent(
            tag = property.tag,
            name = name,
            parameterTypes = arguments.map { it.type!!.toFqType() },
            isNullable = type.isMarkedNullable,
            defaultExpression = defaultExpression,
            deprecation = deprecation,
          )
        } else {
          ParsedProtocolProperty(
            tag = property.tag,
            name = name,
            type = type.toFqType(),
            defaultExpression = defaultExpression,
            deprecation = deprecation,
          )
        }
      } else if (children != null) {
        require(type.isSubtypeOf(childrenType) && type.arguments.last().type == typeOf<Unit>()) {
          "@Children ${memberType.qualifiedName}#$name must be of type '() -> Unit'"
        }
        var scope: FqType? = null
        var arguments = type.arguments.dropLast(1) // Drop Unit return type.
        if (type.annotations.any(ExtensionFunctionType::class::isInstance)) {
          val receiverType = type.arguments.first().type
          val receiverClassifier = receiverType?.classifier
          require(receiverClassifier is KClass<*> && receiverType.arguments.isEmpty()) {
            "@Children ${memberType.qualifiedName}#$name lambda receiver can only be a class. Found: $receiverType"
          }
          scope = receiverClassifier.toFqType()
          arguments = arguments.drop(1)
        }
        require(arguments.isEmpty()) {
          "@Children ${memberType.qualifiedName}#$name lambda type must not have any arguments. Found: $arguments"
        }
        ParsedProtocolChildren(
          tag = children.tag,
          name = name,
          scope = scope,
          defaultExpression = defaultExpression,
          deprecation = deprecation,
        )
      } else {
        throw IllegalArgumentException("Unannotated parameter \"$name\" on ${memberType.qualifiedName}")
      }
    }
  } else if (memberType.objectInstance != null) {
    emptyList()
  } else {
    throw IllegalArgumentException(
      "@Widget ${memberType.qualifiedName} must be 'data' class or 'object'",
    )
  }

  val badChildren = traits.filterIsInstance<ProtocolChildren>()
    .groupBy(ProtocolChildren::tag)
    .filterValues { it.size > 1 }
  if (badChildren.isNotEmpty()) {
    throw IllegalArgumentException(
      buildString {
        appendLine("${memberType.qualifiedName}'s @Children tags must be unique")
        for ((childTag, group) in badChildren) {
          append("\n- @Children($childTag): ")
          group.joinTo(this) { it.name }
        }
      },
    )
  }

  val badProperties = traits.filterIsInstance<ProtocolProperty>()
    .groupBy(ProtocolProperty::tag)
    .filterValues { it.size > 1 }
  if (badProperties.isNotEmpty()) {
    throw IllegalArgumentException(
      buildString {
        appendLine("${memberType.qualifiedName}'s @Property tags must be unique")
        for ((propertyTag, group) in badProperties) {
          append("\n- @Property($propertyTag): ")
          group.joinTo(this) { it.name }
        }
      },
    )
  }

  return ParsedProtocolWidget(
    tag = tag,
    type = memberFqType,
    deprecation = memberType.parseDeprecation { memberFqType.toString() },
    traits = traits,
  )
}

private fun parseModifier(
  memberType: KClass<*>,
  annotation: ModifierAnnotation,
): ParsedProtocolModifier {
  val memberFqType = memberType.toFqType()
  val tag = annotation.tag
  require(tag in 1 until MAX_MEMBER_TAG) {
    "@Modifier $memberFqType tag must be in range [1, $MAX_MEMBER_TAG): $tag"
  }
  require(annotation.scopes.isNotEmpty()) {
    "@Modifier $memberFqType must have at least one scope."
  }

  val properties = if (memberType.objectInstance != null) {
    emptyList()
  } else if (memberType.isData) {
    memberType.primaryConstructor!!.parameters.map { parameter ->
      val kProperty = memberType.memberProperties.single { it.name == parameter.name }
      val name = kProperty.name
      val type = kProperty.returnType

      val defaultExpression = kProperty.findAnnotation<DefaultAnnotation>()?.expression
      val isSerializable = (type.classifier as? KClass<*>)
        ?.annotations
        ?.any { annotation ->
          annotation.annotationClass.qualifiedName == "kotlinx.serialization.Serializable"
        }
        ?: false
      val deprecation = kProperty.parseDeprecation { "$memberFqType.$name" }

      ParsedProtocolModifierProperty(
        name = name,
        type = type.toFqType(),
        isSerializable = isSerializable,
        defaultExpression = defaultExpression,
        deprecation = deprecation,
      )
    }
  } else {
    throw IllegalArgumentException(
      "@Modifier ${memberType.qualifiedName} must be 'data' class or 'object'",
    )
  }

  return ParsedProtocolModifier(
    tag = tag,
    scopes = annotation.scopes.map { it.toFqType() },
    type = memberFqType,
    deprecation = memberType.parseDeprecation { memberFqType.toString() },
    properties = properties,
  )
}

private fun KAnnotatedElement.parseDeprecation(source: () -> String): ParsedDeprecation? {
  return findAnnotation<Deprecated>()
    ?.let { deprecated ->
      require(deprecated.replaceWith.expression.isEmpty() && deprecated.replaceWith.imports.isEmpty()) {
        "Schema deprecation does not support replacements: ${source()}"
      }
      ParsedDeprecation(
        level = when (deprecated.level) {
          WARNING -> Level.WARNING
          ERROR -> Level.ERROR
          else -> {
            throw IllegalArgumentException(
              "Schema deprecation does not support level ${deprecated.level}: ${source()}",
            )
          }
        },
        message = deprecated.message,
      )
    }
}
