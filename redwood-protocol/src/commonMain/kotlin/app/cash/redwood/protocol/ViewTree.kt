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

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * A snapshot of a view hierarchy, intended for use in tests, debugging, and development tools.
 */
@Serializable
public class ViewTree(
  public val changes: List<Change>,
) {
  public class Builder {
    public var nextId: Int = Id.Root.value
    public val json: Json = Json

    public val changes: MutableList<Change> = mutableListOf()

    public fun build(): ViewTree = ViewTree(changes.toList())
  }
}
