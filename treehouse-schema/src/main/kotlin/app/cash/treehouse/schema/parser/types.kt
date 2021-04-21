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
package app.cash.treehouse.schema.parser

import kotlin.reflect.KClass

internal val KClass<*>.packageName: String
  get() {
    // Replace with https://youtrack.jetbrains.com/issue/KT-18104 once it ships.
    // Note: Class.packageName isn't available until Java 9.
    val javaClass = java
    require(!javaClass.isPrimitive && !javaClass.isArray)
    return javaClass.name.substringBeforeLast(".", missingDelimiterValue = "")
  }
