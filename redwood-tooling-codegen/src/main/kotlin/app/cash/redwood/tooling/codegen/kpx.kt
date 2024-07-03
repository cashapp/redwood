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
package app.cash.redwood.tooling.codegen

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.MemberName

internal fun buildFileSpec(className: ClassName, builder: FileSpec.Builder.() -> Unit): FileSpec {
  return FileSpec.builder(className)
    .apply(builder)
    .build()
}

internal fun buildFileSpec(memberName: MemberName, builder: FileSpec.Builder.() -> Unit): FileSpec {
  return FileSpec.builder(memberName)
    .apply(builder)
    .build()
}

internal fun buildFileSpec(packageName: String, fileName: String, builder: FileSpec.Builder.() -> Unit): FileSpec {
  return FileSpec.builder(packageName, fileName)
    .apply(builder)
    .build()
}
