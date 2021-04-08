package app.cash.treehouse.schema.generator

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName

internal val eventSink = ClassName("app.cash.treehouse.widget", "EventSink")
internal val widget = ClassName("app.cash.treehouse.widget", "Widget")
internal val widgetChildren = widget.nestedClass("Children")
internal val widgetFactory = widget.nestedClass("Factory")

internal val eventType = ClassName("app.cash.treehouse.protocol", "Event")
internal val propertyDiff = ClassName("app.cash.treehouse.protocol", "PropertyDiff")

internal val composeNode = ClassName("app.cash.treehouse.compose", "Node")
internal val syntheticChildren = MemberName("app.cash.treehouse.compose", "\$SyntheticChildren")
internal val treehouseScope = ClassName("app.cash.treehouse.compose", "TreehouseScope")

internal val applier = ClassName("androidx.compose.runtime", "Applier")
internal val composable = ClassName("androidx.compose.runtime", "Composable")
internal val composeNodeReference = MemberName("androidx.compose.runtime", "ComposeNode")

internal val iae = ClassName("kotlin", "IllegalArgumentException")
