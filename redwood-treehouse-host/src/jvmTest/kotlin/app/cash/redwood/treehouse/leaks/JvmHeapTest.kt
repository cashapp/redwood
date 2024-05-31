/*
 * Copyright (C) 2024 Square, Inc.
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
package app.cash.redwood.treehouse.leaks

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import kotlin.test.Test

class JvmHeapTest {
  @Test
  fun happyPath() {
    val album = Album(
      name = "Lateralus",
      artist = Artist("Tool", 1990),
      releaseYear = 2001,
      tracks = arrayOf("Schism", "Parabola"),
    )

    assertThat(JvmHeap.references(album)).containsExactly(
      Edge("name", album.name),
      Edge("artist", album.artist),
      Edge("tracks", album.tracks),
    )

    assertThat(JvmHeap.references(album.artist)).containsExactly(
      Edge("name", album.artist.name),
    )

    assertThat(JvmHeap.references(album.tracks)).containsExactly(
      Edge("[0]", album.tracks[0]),
      Edge("[1]", album.tracks[1]),
    )
  }

  @Test
  fun platformTypes() {
    assertThat(JvmHeap.references("Tool")).isEmpty()
    assertThat(JvmHeap.references(Album::class.java)).isEmpty()
  }

  @Test
  fun list() {
    val list = listOf("Schism", "Parabola")
    assertThat(JvmHeap.references(list)).containsExactly(
      Edge("[0]", "Schism"),
      Edge("[1]", "Parabola"),
    )
  }

  @Test
  fun map() {
    val map = mapOf("single" to "Schism")
    assertThat(JvmHeap.references(map)).containsExactly(
      Edge("[0]", map.entries.single()),
    )
    assertThat(JvmHeap.references(map.entries.single())).containsExactly(
      Edge("key", map.entries.single().key),
      Edge("value", map.entries.single().value),
    )
  }

  class Album(
    val name: String,
    val artist: Artist,
    val releaseYear: Int,
    val tracks: Array<String>,
  )

  class Artist(
    val name: String,
    val inceptionYear: Int,
  )
}
