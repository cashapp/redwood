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
package app.cash.redwood.gradle

import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.provider.Property

public abstract class RedwoodGeneratorExtension {
  /**
   * Reference to the project or dependency which contains the Redwood schema.
   * This value must be a type supported by [DependencyHandler].
   */
  public abstract val source: Property<Any>

  /** The fully-qualified name of the `@Schema`-annotated interface. */
  public abstract val type: Property<String>
}
