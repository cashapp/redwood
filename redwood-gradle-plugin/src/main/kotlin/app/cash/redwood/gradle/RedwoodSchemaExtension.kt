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

import org.gradle.api.provider.Property

public abstract class RedwoodSchemaExtension {
  /** The fully-qualified name of the `@Schema`-annotated interface. */
  public abstract val type: Property<String>

  /**
   * Control whether an API file is generated for tracking schema compatibility.
   * The default is true.
   */
  public abstract val apiTracking: Property<Boolean>

  /** Set to true to use the new, FIR-based schema parser. */
  public abstract val useFir: Property<Boolean>
}
