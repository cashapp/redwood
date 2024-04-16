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
package app.cash.redwood.gradle

import org.gradle.api.Action
import org.gradle.api.provider.Property

public interface RedwoodComposeExtension {
  /**
   * The version of the JetBrains Compose compiler to use, or a Maven coordinate triple of
   * the custom Compose compiler to use.
   *
   * Example: using a custom version of the JetBrains Compose compiler
   * ```kotlin
   * redwood {
   *   kotlinCompilerPlugin.set("1.4.8")
   * }
   * ```
   *
   * Example: using a custom Maven coordinate for the Compose compiler
   * ```kotlin
   * redwood {
   *   kotlinCompilerPlugin.set("com.example:custom-compose-compiler:1.0.0")
   * }
   * ```
   */
  public val kotlinCompilerPlugin: Property<String>

  /**
   * Configuration options that require extra care when used.
   *
   * @see DangerZone
   */
  public fun dangerZone(body: Action<DangerZone>)

  /**
   * Configuration options that require extra care when used. Please read the documentation of
   * each member carefully to understand how it affects your build.
   */
  public interface DangerZone {
    /**
     * Enable the output of metrics from the Compose compiler.
     *
     * Text files will be written to `generated/redwood/compose-metrics/` in the project's build
     * directory. See
     * [the compiler documentation](https://github.com/androidx/androidx/blob/androidx-main/compose/compiler/design/compiler-metrics.md#reports-breakdown)
     * for more information about the contents.
     *
     * **NOTE:** This should only be enabled during investigation as it breaks the use of
     * Gradle's build cache for this project's Kotlin compilation tasks.
     *
     * @see enableReports
     */
    public fun enableMetrics()

    /**
     * Enable the output of reports from the Compose compiler.
     *
     * Text files will be written to `generated/redwood/compose-reports/` in the project's build
     * directory. See
     * [the compiler documentation](https://github.com/androidx/androidx/blob/androidx-main/compose/compiler/design/compiler-metrics.md#reports-breakdown)
     * for more information about the contents.
     *
     * **NOTE:** This should only be enabled during investigation as it breaks the use of
     * Gradle's build cache for this project's Kotlin compilation tasks.
     *
     * @see enableMetrics
     */
    public fun enableReports()
  }
}
