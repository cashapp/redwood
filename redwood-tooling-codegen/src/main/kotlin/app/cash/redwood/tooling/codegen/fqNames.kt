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
package app.cash.redwood.tooling.codegen

import app.cash.redwood.tooling.schema.FqType
import app.cash.redwood.tooling.schema.FqType.Variance.In
import app.cash.redwood.tooling.schema.FqType.Variance.Invariant
import app.cash.redwood.tooling.schema.FqType.Variance.Out
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.WildcardTypeName

internal fun FqType.asTypeName(): TypeName {
  if (this == FqType.Star) {
    return STAR
  }
  val className = ClassName(names[0], names.drop(1))
  val typeName = if (parameterTypes.isEmpty()) {
    className
  } else {
    className.parameterizedBy(parameterTypes.map(FqType::asTypeName))
  }
  return when (variance) {
    Invariant -> typeName
    In -> WildcardTypeName.consumerOf(typeName)
    Out -> WildcardTypeName.producerOf(typeName)
  }.copy(nullable = nullable)
}
