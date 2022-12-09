/*
 * Copyright (C) 2021 Square, Inc.
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
package app.cash.redwood.tooling.schema

import com.google.common.truth.Subject
import com.google.common.truth.ThrowableSubject
import com.google.common.truth.Truth.assertThat

inline fun <reified T : Throwable> assertThrows(body: () -> Unit): ThrowableSubject {
  try {
    body()
  } catch (t: Throwable) {
    if (t is T) {
      return assertThat(t)
    }
    throw t
  }
  throw AssertionError(
    "Expect body to throw ${T::class.java.simpleName} but it completed successfully",
  )
}

inline fun <reified T : Any> Subject.isInstanceOf() {
  isInstanceOf(T::class.java)
}
