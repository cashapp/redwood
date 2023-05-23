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
package app.cash.redwood.protocol

import app.cash.redwood.protocol.ChildrenChange.Move
import app.cash.redwood.protocol.ChildrenChange.Remove
import kotlin.jvm.JvmInline
import kotlinx.serialization.Serializable

/**
 * A set of [Change]s for creating a view hierarchy from scratch.
 * Intended for use in tests, debugging, and development tools.
 */
@JvmInline
@Serializable
public value class SnapshotChangeList(
  public val changes: List<Change>,
) {
  init {
    val badChanges = changes.filter { it is Move || it is Remove }
    require(badChanges.isEmpty()) {
      buildString {
        append("Snapshot change list cannot contain move or remove operations\n\nFound:\n")
        badChanges.joinTo(this, separator = "\n") { " - $it" }
      }
    }
  }
}
