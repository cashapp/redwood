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
package app.cash.redwood.treehouse

import kotlin.native.ObjCName

@ObjCName("Saveable", exact = true)
public interface Saveable {
  public var saveCallback: SaveCallback?
  public val stateSnapshotId: StateSnapshot.Id

  @ObjCName("SaveableSaveCallback", exact = true)
  public interface SaveCallback {
    /** Called on the UI dispatcher to save the state for the current content. */
    public fun performSave(id: String)
  }
}
