package app.cash.treehouse.schema.generator

import app.cash.treehouse.schema.parser.Schema
import app.cash.treehouse.schema.parser.Widget
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.asClassName

/**
 * Returns a single string that is likely to be unique within a schema, like `MapEntry` or
 * `NavigationBarButton`.
 */
internal val Widget.flatName: String
  get() = className.asClassName().simpleNames.joinToString(separator = "")

internal fun Schema.composeNodeType(widget: Widget): ClassName {
  return ClassName(composePackage, widget.flatName + "ComposeNode")
}

internal val Schema.composePackage get() = "$`package`.compose"

internal fun Schema.widgetType(widget: Widget): ClassName {
  return ClassName(displayPackage, widget.flatName)
}

internal fun Schema.getWidgetFactoryType(): ClassName {
  return ClassName(displayPackage, "${name}WidgetFactory")
}

internal val Schema.displayPackage get() = "$`package`.widget"
