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
package com.example.redwood.emojisearch.android.views

import app.cash.redwood.treehouse.StateSnapshot
import app.cash.redwood.treehouse.StateStore
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okio.FileNotFoundException
import okio.FileSystem
import okio.Path

/**
 * Limited state store that creates a new file for each snapshot.
 *
 * This doesn't implement eviction.
 */
class FileStateStore(
  private val json: Json,
  private val fileSystem: FileSystem,
  private val directory: Path,
) : StateStore {
  init {
    fileSystem.createDirectories(directory)
    // TODO add a mechanism to delete files older than 24 hours
  }

  override suspend fun put(id: String, value: StateSnapshot) {
    val path = directory / "$id.state"
    val tmpPath = directory / "$id.state.tmp"
    fileSystem.write(tmpPath) {
      writeUtf8(json.encodeToString(value))
    }
    fileSystem.atomicMove(tmpPath, path)
  }

  override suspend fun get(id: String): StateSnapshot? {
    return try {
      val path = directory / "$id.state"
      fileSystem.read(path) {
        json.decodeFromString<StateSnapshot>(readUtf8())
      }
    } catch (e: FileNotFoundException) {
      null
    }
  }
}
