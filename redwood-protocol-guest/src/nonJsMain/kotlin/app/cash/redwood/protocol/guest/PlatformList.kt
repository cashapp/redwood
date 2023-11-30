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
package app.cash.redwood.protocol.guest

@Suppress(
  // ArrayList itself aliases to j.u.ArrayList on JVM.
  "ACTUAL_TYPE_ALIAS_NOT_TO_CLASS",
  // https://youtrack.jetbrains.com/issue/KT-37316
  "ACTUAL_WITHOUT_EXPECT",
)
internal actual typealias PlatformList<E> = ArrayList<E>

@Suppress(
  // Explicitly trying to be zero-overhead.
  "NOTHING_TO_INLINE",
  // Inline warning only happens on JVM source set.
  "KotlinRedundantDiagnosticSuppress",
)
internal actual inline fun <E> PlatformList<E>.asList(): List<E> {
  return this
}
