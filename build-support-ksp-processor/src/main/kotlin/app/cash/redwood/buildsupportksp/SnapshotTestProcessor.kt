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
package app.cash.redwood.buildsupportksp

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSFunctionDeclaration

/** Confirm all snapshot `@Test` functions also have names starting with `test`. */
internal class SnapshotTestProcessor(
  private val environment: SymbolProcessorEnvironment,
) {
  fun process(resolver: Resolver) {
    // Only run on the first round.
    if (!resolver.getNewFiles().iterator().hasNext()) return

    checkAllTests(resolver)
  }

  private fun checkAllTests(resolver: Resolver) {
    for (symbol in resolver.getSymbolsWithAnnotation("org.junit.Test")) {
      when {
        symbol !is KSFunctionDeclaration -> {
          environment.logger.info("Unexpected @Test", symbol)
        }

        !symbol.simpleName.asString().startsWith("test") -> {
          environment.logger.error("Expected @Test to start with 'test'", symbol)
        }
      }
    }
  }
}
