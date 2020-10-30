package app.cash.treehouse.schema.generator

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName

internal val eventSink = ClassName("app.cash.treehouse.client", "EventSink")
internal val treeBridge = ClassName("app.cash.treehouse.client", "TreeBridge")
internal val treeMutator = ClassName("app.cash.treehouse.client", "TreeMutator")
internal val treeNode = ClassName("app.cash.treehouse.client", "TreeNode")

internal val eventType = ClassName("app.cash.treehouse.protocol", "Event")
internal val nodeDiff = ClassName("app.cash.treehouse.protocol", "NodeDiff")
internal val nodeDiffInsert = nodeDiff.nestedClass("Insert")
internal val nodeDiffMove = nodeDiff.nestedClass("Move")
internal val nodeDiffRemove = nodeDiff.nestedClass("Remove")
internal val propertyDiff = ClassName("app.cash.treehouse.protocol", "PropertyDiff")

internal val serverNode = ClassName("app.cash.treehouse.server", "Node")
internal val treehouseScope = ClassName("app.cash.treehouse.server", "TreehouseScope")

internal val applier = ClassName("androidx.compose.runtime", "Applier")
internal val composable = ClassName("androidx.compose.runtime", "Composable")
internal val emitReference = MemberName("androidx.compose.runtime", "emit")

internal val iae = ClassName("kotlin", "IllegalArgumentException")
