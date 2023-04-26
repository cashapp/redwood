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
import assertk.assertions.isFailure
import assertk.assertions.isInstanceOf

inline fun <reified T : Throwable> assertFailsWith(body: () -> Any?): Assert<T> {
  // https://github.com/willowtreeapps/assertk/issues/453

  return assertThat(body)
    .isFailure()
    .isInstanceOf(T::class)
}

inline fun <reified T : Any> Assert<Any>.isInstanceOf(): Assert<T> {
  // https://github.com/willowtreeapps/assertk/issues/454

  return isInstanceOf(T::class)
}
