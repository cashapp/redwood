/*
 * Copyright (C) 2022 Square, Inc.
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
package app.cash.redwood.tooling.lint

import com.google.common.truth.Truth.assertThat
import kotlin.test.assertFailsWith
import org.junit.Test

class ApiMergerTest {
  @Test fun requiresAtLeastOne() {
    val merger = ApiMerger()
    val t = assertFailsWith<IllegalStateException> {
      merger.merge()
    }
    assertThat(t).hasMessageThat()
      .isEqualTo("One or more API definitions must be added in order to merge")
  }

  @Test fun version1Only() {
    val merger = ApiMerger()
    val t = assertFailsWith<IllegalArgumentException> {
      merger.add("""<api version="2"/>""")
    }
    assertThat(t).hasMessageThat()
      .isEqualTo("Only Redwood API XML version 1 is supported. Found: 2")
  }

  @Test fun widgetTakesHigherSince() {
    val merger = ApiMerger()
    merger += """
      |<api version="1">
      |  <widget tag="1" since="2"/>
      |</api>
      |
    """.trimMargin()
    merger += """
      |<api version="1">
      |  <widget tag="1" since="3"/>
      |</api>
      |
    """.trimMargin()
    val actual = merger.merge()

    assertThat(actual).isEqualTo(
      """
      |<api version="1">
      |  <widget tag="1" since="3"/>
      |</api>
      """.trimMargin(),
    )
  }

  @Test fun widgetRemovedIfNotPresentInAll() {
    val merger = ApiMerger()
    merger += """
      |<api version="1">
      |  <widget tag="1" since="2"/>
      |</api>
      |
    """.trimMargin()
    merger += """
      |<api version="1"/>
      |
    """.trimMargin()
    val actual = merger.merge()

    assertThat(actual).isEqualTo(
      """
      |<api version="1"/>
      """.trimMargin(),
    )
  }

  @Test fun widgetTraitTakesHigherSince() {
    val merger = ApiMerger()
    merger += """
      |<api version="1">
      |  <widget tag="1" since="2">
      |    <trait tag="1" since="2"/>
      |  </widget>
      |</api>
      |
    """.trimMargin()
    merger += """
      |<api version="1">
      |  <widget tag="1" since="2">
      |    <trait tag="1" since="3"/>
      |  </widget>
      |</api>
      |
    """.trimMargin()
    val actual = merger.merge()

    assertThat(actual).isEqualTo(
      """
      |<api version="1">
      |  <widget tag="1" since="2">
      |    <trait tag="1" since="3"/>
      |  </widget>
      |</api>
      """.trimMargin(),
    )
  }

  @Test fun widgetTraitRemovedIfNotPresentInAll() {
    val merger = ApiMerger()
    merger += """
      |<api version="1">
      |  <widget tag="1" since="2">
      |    <trait tag="1" since="2"/>
      |  </widget>
      |</api>
      |
    """.trimMargin()
    merger += """
      |<api version="1">
      |  <widget tag="1" since="2"/>
      |</api>
      |
    """.trimMargin()
    val actual = merger.merge()

    assertThat(actual).isEqualTo(
      """
      |<api version="1">
      |  <widget tag="1" since="2"/>
      |</api>
      """.trimMargin(),
    )
  }

  @Test fun layoutModifierTakesHigherSince() {
    val merger = ApiMerger()
    merger += """
      |<api version="1">
      |  <layout-modifier tag="1" since="2"/>
      |</api>
      |
    """.trimMargin()
    merger += """
      |<api version="1">
      |  <layout-modifier tag="1" since="3"/>
      |</api>
      |
    """.trimMargin()
    val actual = merger.merge()

    assertThat(actual).isEqualTo(
      """
      |<api version="1">
      |  <layout-modifier tag="1" since="3"/>
      |</api>
      """.trimMargin(),
    )
  }

  @Test fun layoutModifierRemovedIfNotPresentInAll() {
    val merger = ApiMerger()
    merger += """
      |<api version="1">
      |  <layout-modifier tag="1" since="2"/>
      |</api>
      |
    """.trimMargin()
    merger += """
      |<api version="1"/>
      |
    """.trimMargin()
    val actual = merger.merge()

    assertThat(actual).isEqualTo(
      """
      |<api version="1"/>
      """.trimMargin(),
    )
  }

  @Test fun layoutModifierPropertyTakesHigherSince() {
    val merger = ApiMerger()
    merger += """
      |<api version="1">
      |  <layout-modifier tag="1" since="2">
      |    <property tag="1" since="2"/>
      |  </layout-modifier>
      |</api>
      |
    """.trimMargin()
    merger += """
      |<api version="1">
      |  <layout-modifier tag="1" since="2">
      |    <property tag="1" since="3"/>
      |  </layout-modifier>
      |</api>
      |
    """.trimMargin()
    val actual = merger.merge()

    assertThat(actual).isEqualTo(
      """
      |<api version="1">
      |  <layout-modifier tag="1" since="2">
      |    <property tag="1" since="3"/>
      |  </layout-modifier>
      |</api>
      """.trimMargin(),
    )
  }

  @Test fun layoutModifierRemovedIfNotInAll() {
    val merger = ApiMerger()
    merger += """
      |<api version="1">
      |  <layout-modifier tag="1" since="2">
      |    <property tag="1" since="2"/>
      |  </layout-modifier>
      |</api>
      |
    """.trimMargin()
    merger += """
      |<api version="1">
      |  <layout-modifier tag="1" since="2"/>
      |</api>
      |
    """.trimMargin()
    val actual = merger.merge()

    assertThat(actual).isEqualTo(
      """
      |<api version="1">
      |  <layout-modifier tag="1" since="2"/>
      |</api>
      """.trimMargin(),
    )
  }

  @Test fun sortedByTag() {
    val merger = ApiMerger()
    merger += """
      |<api version="1">
      |  <widget tag="2" since="1">
      |    <trait tag="4" since="1"/>
      |    <trait tag="2" since="1"/>
      |  </widget>
      |  <widget tag="1" since="1">
      |    <trait tag="3" since="1"/>
      |    <trait tag="1" since="1"/>
      |  </widget>
      |  <layout-modifier tag="2" since="1">
      |    <property tag="4" since="1"/>
      |    <property tag="2" since="1"/>
      |  </layout-modifier>
      |  <layout-modifier tag="1" since="1">
      |    <property tag="3" since="1"/>
      |    <property tag="1" since="1"/>
      |  </layout-modifier>
      |</api>
      |
    """.trimMargin()
    val actual = merger.merge()

    assertThat(actual).isEqualTo(
      """
      |<api version="1">
      |  <widget tag="1" since="1">
      |    <trait tag="1" since="1"/>
      |    <trait tag="3" since="1"/>
      |  </widget>
      |  <widget tag="2" since="1">
      |    <trait tag="2" since="1"/>
      |    <trait tag="4" since="1"/>
      |  </widget>
      |  <layout-modifier tag="1" since="1">
      |    <property tag="1" since="1"/>
      |    <property tag="3" since="1"/>
      |  </layout-modifier>
      |  <layout-modifier tag="2" since="1">
      |    <property tag="2" since="1"/>
      |    <property tag="4" since="1"/>
      |  </layout-modifier>
      |</api>
      """.trimMargin(),
    )
  }
}
