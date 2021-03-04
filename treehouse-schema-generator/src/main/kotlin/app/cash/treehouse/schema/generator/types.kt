package app.cash.treehouse.schema.generator

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName

internal val eventSink = ClassName("app.cash.treehouse.display", "EventSink")
internal val treeNode = ClassName("app.cash.treehouse.display", "TreeNode")
internal val treeNodeChildren = treeNode.nestedClass("Children")
internal val treeNodeFactory = ClassName("app.cash.treehouse.display", "TreeNodeFactory")

internal val eventType = ClassName("app.cash.treehouse.protocol", "Event")
internal val nodeDiff = ClassName("app.cash.treehouse.protocol", "NodeDiff")
internal val nodeDiffInsert = nodeDiff.nestedClass("Insert")
internal val nodeDiffMove = nodeDiff.nestedClass("Move")
internal val nodeDiffRemove = nodeDiff.nestedClass("Remove")
internal val propertyDiff = ClassName("app.cash.treehouse.protocol", "PropertyDiff")

internal val composeNode = ClassName("app.cash.treehouse.compose", "Node")
internal val treehouseScope = ClassName("app.cash.treehouse.compose", "TreehouseScope")

internal val applier = ClassName("androidx.compose.runtime", "Applier")
internal val composable = ClassName("androidx.compose.runtime", "Composable")
internal val emitReference = MemberName("androidx.compose.runtime", "emit")

internal val iae = ClassName("kotlin", "IllegalArgumentException")

/** [Class.packageName] isn't available until Java 9. */
internal fun packageName(schemaType: Class<*>): String {
  require(!schemaType.isPrimitive && !schemaType.isArray)
  return schemaType.name.substringBeforeLast(".", missingDelimiterValue = "")
}
