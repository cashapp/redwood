package app.cash.treehouse.schema.generator

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName

internal val eventSink = ClassName("app.cash.treehouse.display", "EventSink")
internal val treeNode = ClassName("app.cash.treehouse.display", "TreeNode")
internal val treeNodeChildren = treeNode.nestedClass("Children")
internal val treeNodeFactory = ClassName("app.cash.treehouse.display", "TreeNodeFactory")

internal val eventType = ClassName("app.cash.treehouse.protocol", "Event")
internal val propertyDiff = ClassName("app.cash.treehouse.protocol", "PropertyDiff")

internal val composeNode = ClassName("app.cash.treehouse.compose", "Node")
internal val syntheticChildren = MemberName("app.cash.treehouse.compose", "-SyntheticChildren")
internal val treehouseScope = ClassName("app.cash.treehouse.compose", "TreehouseScope")

internal val applier = ClassName("androidx.compose.runtime", "Applier")
internal val composable = ClassName("androidx.compose.runtime", "Composable")
internal val composeNodeReference = MemberName("androidx.compose.runtime", "ComposeNode")

internal val iae = ClassName("kotlin", "IllegalArgumentException")

/** [Class.packageName] isn't available until Java 9. */
internal fun packageName(schemaType: Class<*>): String {
  require(!schemaType.isPrimitive && !schemaType.isArray)
  return schemaType.name.substringBeforeLast(".", missingDelimiterValue = "")
}
