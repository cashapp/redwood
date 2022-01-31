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

import kotlin.reflect.KClass
import kotlin.reflect.KTypeProjection.Companion.invariant
import kotlin.reflect.full.createType
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.full.withNullability
import app.cash.redwood.schema.Children as ChildrenAnnotation
import app.cash.redwood.schema.Default as DefaultAnnotation
import app.cash.redwood.schema.Property as PropertyAnnotation
import app.cash.redwood.schema.Schema as SchemaAnnotation
import app.cash.redwood.schema.Widget as WidgetAnnotation

private val childrenType = List::class.createType(
  arguments = listOf(invariant(Any::class.createType()))
)
private val eventType = Function::class.starProjectedType
private val optionalEventType = eventType.withNullability(true)

public fun parseSchema(schemaType: KClass<*>): Schema {
  val widgets = mutableListOf<Widget>()

  val widgetTypes = requireNotNull(schemaType.findAnnotation<SchemaAnnotation>()) {
    "Schema ${schemaType.qualifiedName} missing @Schema annotation"
  }.widgets

  val duplicatedWidgets = widgetTypes.groupBy { it }.filterValues { it.size > 1 }.keys
  if (duplicatedWidgets.isNotEmpty()) {
    throw IllegalArgumentException(
      buildString {
        append("Schema contains repeated widget")
        if (duplicatedWidgets.size > 1) {
          append('s')
        }
        duplicatedWidgets.joinTo(this, prefix = "\n\n- ", separator = "\n- ") { it.qualifiedName!! }
      }
    )
  }

  for (widgetType in widgetTypes) {
    val widgetAnnotation = requireNotNull(widgetType.findAnnotation<WidgetAnnotation>()) {
      "${widgetType.qualifiedName} missing @Widget annotation"
    }

    val traits = if (widgetType.isData) {
      widgetType.primaryConstructor!!.parameters.map {
        val property = it.findAnnotation<PropertyAnnotation>()
        val children = it.findAnnotation<ChildrenAnnotation>()
        val defaultExpression = it.findAnnotation<DefaultAnnotation>()?.expression

        if (property != null) {
          if (it.type.isSubtypeOf(eventType) || it.type.isSubtypeOf(optionalEventType)) {
            val arguments = it.type.arguments.dropLast(1) // Drop return type.
            require(arguments.size <= 1) {
              "@Property ${widgetType.qualifiedName}#${it.name} lambda type can only have zero or one arguments. Found: $arguments"
            }
            Event(property.tag, it.name!!, defaultExpression, arguments.singleOrNull()?.type)
          } else {
            Property(property.tag, it.name!!, it.type, defaultExpression)
          }
        } else if (children != null) {
          require(it.type == childrenType) {
            "@Children ${widgetType.qualifiedName}#${it.name} must be of type 'List<Any>'"
          }
          Children(children.tag, it.name!!)
        } else {
          throw IllegalArgumentException("Unannotated parameter \"${it.name}\" on ${widgetType.qualifiedName}")
        }
      }
    } else if (widgetType.objectInstance != null) {
      emptyList()
    } else {
      throw IllegalArgumentException(
        "@Widget ${widgetType.qualifiedName} must be 'data' class or 'object'"
      )
    }

    val badChildren =
      traits.filterIsInstance<Children>().groupBy(Children::tag).filterValues { it.size > 1 }
    if (badChildren.isNotEmpty()) {
      throw IllegalArgumentException(
        buildString {
          appendLine("Widget ${widgetType.qualifiedName}'s @Children tags must be unique")
          for ((tag, group) in badChildren) {
            append("\n- @Children($tag): ")
            group.joinTo(this) { it.name }
          }
        }
      )
    }

    val badProperties =
      traits.filterIsInstance<Property>().groupBy(Property::tag).filterValues { it.size > 1 }
    if (badProperties.isNotEmpty()) {
      throw IllegalArgumentException(
        buildString {
          appendLine("Widget ${widgetType.qualifiedName}'s @Property tags must be unique")
          for ((tag, group) in badProperties) {
            append("\n- @Property($tag): ")
            group.joinTo(this) { it.name }
          }
        }
      )
    }

    widgets += Widget(widgetAnnotation.tag, widgetType, traits)
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
      }
    )
  }

  return Schema(schemaType.simpleName!!, schemaType.packageName, widgets)
}
