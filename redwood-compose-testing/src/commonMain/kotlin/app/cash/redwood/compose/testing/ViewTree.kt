/*
 * Copyright (C) 2023 Square, Inc.
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

package app.cash.redwood.compose.testing

import app.cash.redwood.protocol.ChildrenDiff
import app.cash.redwood.protocol.ChildrenTag
import app.cash.redwood.protocol.Diff
import app.cash.redwood.protocol.Id
import app.cash.redwood.protocol.PropertyDiff
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
public class ViewTree(
  public val diff: Diff
) {
  public class Builder {
    public var nextId: Int = Id.Root.value
    public val json: Json = Json

    public val childrenDiffs: MutableList<ChildrenDiff> = mutableListOf<ChildrenDiff>()
    public val propertyDiffs: MutableList<PropertyDiff> = mutableListOf<PropertyDiff>()

    public fun build(): ViewTree = ViewTree(
      Diff(
        childrenDiffs = childrenDiffs.toList(),
        propertyDiffs = propertyDiffs.toList(),
      ),
    )
  }
}

public val List<WidgetValue>.viewTree: ViewTree
  get() {
    val builder = ViewTree.Builder()
    val root = builder.nextId

    for (widget in this) {
      widget.addTo(Id(root), ChildrenTag.Root, builder)
    }

    return builder.build()
  }

public val WidgetValue.viewTree: ViewTree
  get() {
    val builder = ViewTree.Builder()
    addTo(Id(builder.nextId), ChildrenTag.Root, builder)
    return builder.build()
  }
