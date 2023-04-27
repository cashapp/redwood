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
package app.cash.redwood.tooling.schema

import assertk.Assert
import assertk.assertThat
import assertk.assertions.isInstanceOf
import kotlin.reflect.KClass

inline fun <reified T : Throwable> assertFailsWith(noinline body: () -> Any?): Assert<T> {
  // https://github.com/willowtreeapps/assertk/issues/453
  return assertFailsWith(T::class, body)
}

fun <T : Throwable> assertFailsWith(type: KClass<T>, body: () -> Any?): Assert<T> {
  try {
    body()
  } catch (t: Throwable) {
    if (type.isInstance(t)) {
      @Suppress("UNCHECKED_CAST")
      return assertThat(t as T)
    }
    // https://github.com/willowtreeapps/assertk/issues/456
    throw t
  }
  throw AssertionError("Expected body to fail but completed successfully")
}

inline fun <reified T : Any> Assert<Any>.isInstanceOf(): Assert<T> {
  // https://github.com/willowtreeapps/assertk/issues/454

  return isInstanceOf(T::class)
}
