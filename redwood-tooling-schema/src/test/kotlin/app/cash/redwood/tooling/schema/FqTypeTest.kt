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

import DefaultPackage
import app.cash.redwood.tooling.schema.FqType.Variance.In
import app.cash.redwood.tooling.schema.FqType.Variance.Out
import assertk.assertThat
import assertk.assertions.hasMessage
import assertk.assertions.isEqualTo
import java.util.PrimitiveIterator
import kotlin.reflect.typeOf
import org.junit.Test

class FqTypeTest {
  @Test fun classes() {
    assertThat(String::class.toFqType())
      .isEqualTo(FqType(listOf("kotlin", "String")))
    assertThat(FqTypeTest::class.toFqType())
      .isEqualTo(FqType(listOf("app.cash.redwood.tooling.schema", "FqTypeTest")))
    assertThat(PrimitiveIterator.OfInt::class.toFqType())
      .isEqualTo(FqType(listOf("java.util", "PrimitiveIterator", "OfInt")))
    assertThat(DefaultPackage::class.toFqType())
      .isEqualTo(FqType(listOf("", "DefaultPackage")))
    assertThat(DefaultPackage.Nested::class.toFqType())
      .isEqualTo(FqType(listOf("", "DefaultPackage", "Nested")))
  }

  @Test fun classSpecialCases() {
    assertThat(Boolean.Companion::class.toFqType())
      .isEqualTo(FqType(listOf("kotlin", "Boolean", "Companion")))
    assertThat(Byte.Companion::class.toFqType())
      .isEqualTo(FqType(listOf("kotlin", "Byte", "Companion")))
    assertThat(Char.Companion::class.toFqType())
      .isEqualTo(FqType(listOf("kotlin", "Char", "Companion")))
    assertThat(Double.Companion::class.toFqType())
      .isEqualTo(FqType(listOf("kotlin", "Double", "Companion")))
    assertThat(Enum.Companion::class.toFqType())
      .isEqualTo(FqType(listOf("kotlin", "Enum", "Companion")))
    assertThat(Float.Companion::class.toFqType())
      .isEqualTo(FqType(listOf("kotlin", "Float", "Companion")))
    assertThat(Int.Companion::class.toFqType())
      .isEqualTo(FqType(listOf("kotlin", "Int", "Companion")))
    assertThat(Long.Companion::class.toFqType())
      .isEqualTo(FqType(listOf("kotlin", "Long", "Companion")))
    assertThat(Short.Companion::class.toFqType())
      .isEqualTo(FqType(listOf("kotlin", "Short", "Companion")))
    assertThat(String.Companion::class.toFqType())
      .isEqualTo(FqType(listOf("kotlin", "String", "Companion")))
  }

  @Test fun types() {
    assertThat(typeOf<String?>().toFqType())
      .isEqualTo(FqType(listOf("kotlin", "String"), nullable = true))

    assertThat(typeOf<Array<String>>().toFqType()).isEqualTo(
      FqType(
        listOf("kotlin", "Array"),
        parameterTypes = listOf(FqType(listOf("kotlin", "String"))),
      ),
    )

    assertThat(typeOf<Array<out String>>().toFqType()).isEqualTo(
      FqType(
        names = listOf("kotlin", "Array"),
        parameterTypes = listOf(FqType(listOf("kotlin", "String"), variance = Out)),
      ),
    )

    assertThat(typeOf<Array<in String>>().toFqType()).isEqualTo(
      FqType(
        names = listOf("kotlin", "Array"),
        parameterTypes = listOf(FqType(listOf("kotlin", "String"), variance = In)),
      ),
    )

    assertThat(typeOf<Array<*>>().toFqType()).isEqualTo(
      FqType(
        listOf("kotlin", "Array"),
        parameterTypes = listOf(FqType.Star),
      ),
    )
  }

  @Test fun classTooFewNamesThrows() {
    assertFailsWith<IllegalArgumentException> {
      FqType(listOf())
    }.hasMessage("At least two names are required: package and a simple name: []")

    assertFailsWith<IllegalArgumentException> {
      FqType(listOf("kotlin"))
    }.hasMessage("At least two names are required: package and a simple name: [kotlin]")
  }

  @Test fun star() {
    assertThat(FqType.Star).isEqualTo(FqType(listOf("", "*")))
  }

  @Test fun starOtherPropertiesThrows() {
    assertFailsWith<IllegalArgumentException> {
      FqType(listOf("kotlin", "*"))
    }.hasMessage("Star projection must use empty package name: kotlin")

    assertFailsWith<IllegalArgumentException> {
      FqType(listOf("", "*", "Nested"))
    }.hasMessage("Star projection cannot have nested types: [*, Nested]")

    assertFailsWith<IllegalArgumentException> {
      FqType(listOf("", "*"), parameterTypes = listOf(Int::class.toFqType()))
    }.hasMessage("Star projection must not have parameter types: [kotlin.Int]")

    assertFailsWith<IllegalArgumentException> {
      FqType(listOf("", "*"), variance = In)
    }.hasMessage("Star projection must be Invariant: In")

    assertFailsWith<IllegalArgumentException> {
      FqType(listOf("", "*"), nullable = true)
    }.hasMessage("Star projection must not be nullable")
  }

  @Test fun bestGuessValid() {
    assertThat(FqType.bestGuess("Map"))
      .isEqualTo(FqType(listOf("", "Map")))
    assertThat(FqType.bestGuess("Map.Entry"))
      .isEqualTo(FqType(listOf("", "Map", "Entry")))
    assertThat(FqType.bestGuess("java.Map.Entry"))
      .isEqualTo(FqType(listOf("java", "Map", "Entry")))
    assertThat(FqType.bestGuess("java.util.concurrent.Map.Entry"))
      .isEqualTo(FqType(listOf("java.util.concurrent", "Map", "Entry")))
  }

  @Test fun bestGuessInvalid() {
    assertFailsWith<IllegalArgumentException> {
      FqType.bestGuess("java")
    }.hasMessage("Couldn't guess: java")

    assertFailsWith<IllegalArgumentException> {
      FqType.bestGuess("java.util.concurrent")
    }.hasMessage("Couldn't guess: java.util.concurrent")

    assertFailsWith<IllegalArgumentException> {
      FqType.bestGuess("java..concurrent")
    }.hasMessage("Couldn't guess: java..concurrent")

    assertFailsWith<IllegalArgumentException> {
      FqType.bestGuess("java.util.concurrent.Map..Entry")
    }.hasMessage("Couldn't guess: java.util.concurrent.Map..Entry")

    assertFailsWith<IllegalArgumentException> {
      FqType.bestGuess("java.util.concurrent.Map.entry")
    }.hasMessage("Couldn't guess: java.util.concurrent.Map.entry")
  }
}
