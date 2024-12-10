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
package app.cash.redwood.protocol

import kotlin.jvm.JvmInline
import kotlinx.serialization.Serializable

/**
 * Version string for the Redwood project. This is a very strict subset of
 * [Gradle's version parsing and ordering](https://docs.gradle.org/current/userguide/single_versions.html#version_ordering)
 * semantics.
 *
 * Format is `X.Y.Z[-label]` (with square brackets indicating an optional part). `X`, `Y`, and `Z`
 * are integers and separated by a period (`.`). `label` is an optional string delimited by a
 * hyphen (`-`) and whose valid characters are a-z, A-Z, 0-9, period (`.`), underscore (`_`),
 * and hyphen (`-`).
 *
 * Ordering relative to another version string is done by comparing `X` numerically, then `Y`
 * numerically, then `Z` numerically, and finally by comparing `label` lexicographically. The label
 * value "SNAPSHOT" always sorting higher than any other label. The absence of a label sorts higher
 * than any present label.
 *
 * Examples:
 * - 2.1.0 > 1.2.3 (`X` numerically higher)
 * - 1.3.1 > 1.2.0 (`X` same, `Y` numerically higher)
 * - 1.1.4 > 1.1.2 (`X` and `Y` same, `Z` numerically higher)
 * - 1.0.0 > 1.0.0-beta (`X`, `Y`, and `Z` same, absent label higher than present label)
 * - 1.0.0-SNAPSHOT > 1.0.0-beta (`X`, `Y`, and `Z` same, "SNAPSHOT" label higher than any label)
 * - 1.0.0-beta > 1.0.0-alpha (`X`, `Y`, and `Z` same, "beta" lexicographically higher than "alpha")
 * - 1.0.0-beta2 > 1.0.0-beta (`X`, `Y`, and `Z` same, "beta2" lexicographically higher than "beta")
 */
@JvmInline
@Serializable
public value class RedwoodVersion(public val value: String) : Comparable<RedwoodVersion> {
  init {
    require(format.matches(value)) {
      "Invalid version format: $value"
    }
  }

  override fun toString(): String = value

  /**
   * Compare two versions to see which is newer.
   *
   * Note: Comparing instances is not particularly efficient, so the result should be cached
   * instead of comparing each time.
   */
  override fun compareTo(other: RedwoodVersion): Int {
    val thisMatch = format.matchEntire(value)!!
    val otherMatch = format.matchEntire(other.value)!!

    // First three parts are digits (1-indexed because part 0 is whole match).
    for (i in 1..3) {
      val thisPart = thisMatch.groups[i]!!.value.toInt()
      val otherPart = otherMatch.groups[i]!!.value.toInt()
      if (thisPart > otherPart) return 1
      if (thisPart < otherPart) return -1
    }

    val thisLabel = thisMatch.groups[4]
    val otherLabel = otherMatch.groups[4]

    // The absence of a label sorts higher than the presence of one.
    if (thisLabel == null) {
      return if (otherLabel == null) 0 else 1
    } else if (otherLabel == null) {
      return -1
    }

    // SNAPSHOT label sorts higher than any other label.
    if (thisLabel.value == "-SNAPSHOT") {
      return if (otherLabel.value == "-SNAPSHOT") 0 else 1
    } else if (otherLabel.value == "-SNAPSHOT") {
      return -1
    }

    return thisLabel.value.compareTo(otherLabel.value)
  }

  public companion object {
    private val format = Regex("""^(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)(-[a-zA-Z0-9._-]+)?$""")
    public val Unknown: RedwoodVersion = RedwoodVersion("0.0.0")
  }
}
