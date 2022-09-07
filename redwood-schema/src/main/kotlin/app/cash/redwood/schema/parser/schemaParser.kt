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
package app.cash.redwood.schema.parser

import app.cash.redwood.schema.Children as ChildrenAnnotation
import app.cash.redwood.schema.Default as DefaultAnnotation
import app.cash.redwood.schema.LayoutModifier as LayoutModifierAnnotation
import app.cash.redwood.schema.Property as PropertyAnnotation
import app.cash.redwood.schema.Schema as SchemaAnnotation
import app.cash.redwood.schema.Widget as WidgetAnnotation
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.full.withNullability

private val childrenType = Function::class.starProjectedType
private val eventType = Function::class.starProjectedType
private val optionalEventType = eventType.withNullability(true)

public fun parseSchema(schemaType: KClass<*>): Schema {
  val memberTypes = requireNotNull(schemaType.findAnnotation<SchemaAnnotation>()) {
    "Schema ${schemaType.qualifiedName} missing @Schema annotation"
  }.members

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

  val widgets = mutableListOf<Widget>()
  val layoutModifiers = mutableListOf<LayoutModifier>()
  for (memberType in memberTypes) {
    val widgetAnnotation = memberType.findAnnotation<WidgetAnnotation>()
    val layoutModifierAnnotation = memberType.findAnnotation<LayoutModifierAnnotation>()

    if ((widgetAnnotation == null) == (layoutModifierAnnotation == null)) {
      throw IllegalArgumentException(
        "${memberType.qualifiedName} must be annotated with either @Widget or @LayoutModifier",
      )
    } else if (widgetAnnotation != null) {
      widgets += parseWidget(memberType, widgetAnnotation)
    } else if (layoutModifierAnnotation != null) {
      layoutModifiers += parseLayoutModifier(memberType, layoutModifierAnnotation)
    } else {
      throw AssertionError()
    }
  }

  val badWidgets = widgets.groupBy(Widget::tag).filterValues { it.size > 1 }
  if (badWidgets.isNotEmpty()) {
    throw IllegalArgumentException(
      buildString {
        appendLine("Schema @Widget tags must be unique")
        for ((tag, group) in badWidgets) {
          append("\n- @Widget($tag): ")
          group.joinTo(this) { it.type.qualifiedName!! }
        }
      },
    )
  }

  val badLayoutModifiers = layoutModifiers.groupBy(LayoutModifier::tag).filterValues { it.size > 1 }
  if (badLayoutModifiers.isNotEmpty()) {
    throw IllegalArgumentException(
      buildString {
        appendLine("Schema @LayoutModifier tags must be unique")
        for ((tag, group) in badLayoutModifiers) {
          append("\n- @LayoutModifier($tag): ")
          group.joinTo(this) { it.type.qualifiedName!! }
        }
      },
    )
  }

  val scopes = widgets
    .flatMap { it.traits }
    .filterIsInstance<Widget.Children>()
    .mapNotNullTo(mutableSetOf()) { it.scope }

  return Schema(
    schemaType.simpleName!!,
    schemaType.java.packageName,
    scopes.toList(),
    widgets,
    layoutModifiers,
  )
}

private fun parseWidget(memberType: KClass<*>, annotation: WidgetAnnotation): Widget {
  val traits = if (memberType.isData) {
    memberType.primaryConstructor!!.parameters.map {
      val property = it.findAnnotation<PropertyAnnotation>()
      val children = it.findAnnotation<ChildrenAnnotation>()
      val defaultExpression = it.findAnnotation<DefaultAnnotation>()?.expression

      if (property != null) {
        if (it.type.isSubtypeOf(eventType) || it.type.isSubtypeOf(optionalEventType)) {
          val arguments = it.type.arguments.dropLast(1) // Drop return type.
          require(arguments.size <= 1) {
            "@Property ${memberType.qualifiedName}#${it.name} lambda type can only have zero or one arguments. Found: $arguments"
          }
          Widget.Event(property.tag, it.name!!, defaultExpression, arguments.singleOrNull()?.type)
        } else {
          Widget.Property(property.tag, it.name!!, it.type, defaultExpression)
        }
      } else if (children != null) {
        require(it.type.isSubtypeOf(childrenType)) {
          "@Children ${memberType.qualifiedName}#${it.name} must be of type '() -> Unit'"
        }
        val arguments = it.type.arguments.dropLast(1) // Drop return type.
        require(arguments.isEmpty()) {
          "@Children ${memberType.qualifiedName}#${it.name} lambda type must not have any arguments. Found: $arguments"
        }
        Widget.Children(children.tag, it.name!!, children.scope.takeIf { it != Unit::class })
      } else {
        throw IllegalArgumentException("Unannotated parameter \"${it.name}\" on ${memberType.qualifiedName}")
      }
    }
  } else if (memberType.objectInstance != null) {
    emptyList()
  } else {
    throw IllegalArgumentException(
      "@Widget ${memberType.qualifiedName} must be 'data' class or 'object'",
    )
  }

  val badChildren = traits.filterIsInstance<Widget.Children>()
    .groupBy(Widget.Children::tag)
    .filterValues { it.size > 1 }
  if (badChildren.isNotEmpty()) {
    throw IllegalArgumentException(
      buildString {
        appendLine("${memberType.qualifiedName}'s @Children tags must be unique")
        for ((tag, group) in badChildren) {
          append("\n- @Children($tag): ")
          group.joinTo(this) { it.name }
        }
      },
    )
  }

  val badProperties = traits.filterIsInstance<Widget.Property>()
    .groupBy(Widget.Property::tag)
    .filterValues { it.size > 1 }
  if (badProperties.isNotEmpty()) {
    throw IllegalArgumentException(
      buildString {
        appendLine("${memberType.qualifiedName}'s @Property tags must be unique")
        for ((tag, group) in badProperties) {
          append("\n- @Property($tag): ")
          group.joinTo(this) { it.name }
        }
      },
    )
  }

  return Widget(annotation.tag, memberType, traits)
}

private fun parseLayoutModifier(
  memberType: KClass<*>,
  annotation: LayoutModifierAnnotation,
): LayoutModifier {
  val properties = if (memberType.isData) {
    memberType.primaryConstructor!!.parameters.map {
      val defaultExpression = it.findAnnotation<DefaultAnnotation>()?.expression
      LayoutModifier.Property(it.name!!, it.type, defaultExpression)
    }
  } else if (memberType.objectInstance != null) {
    emptyList()
  } else {
    throw IllegalArgumentException(
      "@Widget ${memberType.qualifiedName} must be 'data' class or 'object'",
    )
  }

  return LayoutModifier(
    annotation.tag,
    annotation.scopes.toList(),
    memberType,
    properties,
  )
}
