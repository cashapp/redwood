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

import app.cash.redwood.tooling.schema.ValidationMode.Check
import app.cash.redwood.tooling.schema.ValidationMode.Generate
import app.cash.redwood.tooling.schema.ValidationResult.Failure
import assertk.Assert
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.prop
import com.google.common.jimfs.Configuration.unix
import com.google.common.jimfs.Jimfs
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText
import org.junit.Test

class ApiTest {
  private val fsRoot = Jimfs.newFileSystem(unix()).rootDirectories.single()

  // JimFs working directory is work/ so let's pretend that's our project directory.
  private val apiFile = fsRoot.resolve("work/api.xml")

  @Test fun checkNoFile() {
    val api = ApiSchema(type = "com.example.Schema")
    val result = api.validateAgainst(apiFile, Check, "COMMAND")
    assertThat(result).isFailureWithMessage(
      """
      |API file api.xml missing!
      |
      |Run 'COMMAND' to generate this file.
      """.trimMargin(),
    )
  }

  @Test fun checkFileUpToDate() {
    apiFile.writeText(
      """
      |<schema version="1" type="com.example.TestSchema">
      |  <widget tag="1" type="com.example.Widget">
      |    <property tag="1" name="first" type="kotlin.String"/>
      |    <property tag="2" name="second" type="kotlin.Double"/>
      |    <event tag="3" name="event" params="kotlin.String"/>
      |    <event tag="4" name="none"/>
      |    <event tag="5" name="eventOpt" params="kotlin.String" nullable="true"/>
      |    <event tag="6" name="noneOpt" nullable="true"/>
      |    <children tag="1" name="left"/>
      |    <children tag="2" name="right"/>
      |  </widget>
      |  <modifier tag="1" type="com.example.Modifier">
      |    <property name="first" type="kotlin.String"/>
      |    <property name="second" type="kotlin.Double"/>
      |  </modifier>
      |</schema>
      |
      """.trimMargin(),
    )
    val api = ApiSchema(
      type = "com.example.TestSchema",
      widgets = listOf(
        ApiWidget(
          tag = 1,
          type = "com.example.Widget",
          traits = listOf(
            ApiWidgetProperty(tag = 1, "first", "kotlin.String"),
            ApiWidgetProperty(tag = 2, "second", "kotlin.Double"),
            ApiWidgetEvent(tag = 3, "event", "kotlin.String", nullable = false),
            ApiWidgetEvent(tag = 4, "none", nullable = false),
            ApiWidgetEvent(tag = 5, "eventOpt", "kotlin.String", nullable = true),
            ApiWidgetEvent(tag = 6, "noneOpt", params = "", nullable = true),
            ApiWidgetChildren(tag = 1, "left"),
            ApiWidgetChildren(tag = 2, "right"),
          ),
        ),
      ),
      modifiers = listOf(
        ApiModifier(
          tag = 1,
          type = "com.example.Modifier",
          properties = listOf(
            ApiModifierProperty("first", "kotlin.String"),
            ApiModifierProperty("second", "kotlin.Double"),
          ),
        ),
      ),
    )
    val result = api.validateAgainst(apiFile, Check, "UNUSED")
    assertThat(result).isSuccess()
  }

  @Test fun checkFileOutOfDateNonFatal() {
    apiFile.writeText(
      """
      |<schema version="1" type="com.example.TestSchema">
      |  <widget tag="1" type="com.example.Widget">
      |    <property tag="1" name="first" type="kotlin.String"/>
      |    <property tag="2" name="second" type="kotlin.Double"/>
      |    <event tag="4" name="event" params="kotlin.String"/>
      |    <event tag="5" name="nullableChange" nullable="true"/>
      |    <children tag="1" name="left"/>
      |    <children tag="2" name="right"/>
      |  </widget>
      |  <modifier tag="1" type="com.example.Modifier">
      |    <property name="first" type="kotlin.String"/>
      |    <property name="second" type="kotlin.Double"/>
      |  </modifier>
      |</schema>
      |
      """.trimMargin(),
    )
    val api = ApiSchema(
      type = "com.example.TestSchemaTypeChange",
      widgets = listOf(
        ApiWidget(
          tag = 1,
          type = "com.example.WidgetTypeChange",
          traits = listOf(
            ApiWidgetProperty(tag = 1, "firstNameChange", "kotlin.String"),
            ApiWidgetProperty(tag = 2, "second", "kotlin.Double"),
            ApiWidgetProperty(tag = 3, "missingFromFile", "kotlin.Long"),
            ApiWidgetEvent(tag = 4, "eventNameChange", "kotlin.String"),
            ApiWidgetEvent(tag = 5, "nullableChange"),
            ApiWidgetEvent(tag = 6, "missingFromFile"),
            ApiWidgetChildren(tag = 1, "leftNameChange"),
            ApiWidgetChildren(tag = 2, "right"),
            ApiWidgetChildren(tag = 3, "missingFromFile"),
          ),
        ),
        ApiWidget(
          tag = 2,
          type = "com.example.WidgetMissingFromFile",
        ),
      ),
      modifiers = listOf(
        ApiModifier(
          tag = 1,
          type = "com.example.ModifierTypeChange",
          properties = listOf(
            ApiModifierProperty("first", "kotlin.String"),
            ApiModifierProperty("second", "kotlin.Double"),
          ),
        ),
        ApiModifier(
          tag = 2,
          type = "com.example.ModifierMissingFromFile",
        ),
      ),
    )
    val result = api.validateAgainst(apiFile, Check, "COMMAND")
    assertThat(result).isFailureWithMessage(
      """
      |API file does not match!
      |
      |Differences:
      | - Schema type changed from com.example.TestSchema to com.example.TestSchemaTypeChange (warning)
      | - Widget(tag=2) is missing from the file, but it is a part of the Kotlin schema (fixable)
      | - Widget(tag=1) type changed from com.example.Widget to com.example.WidgetTypeChange in the Kotlin schema (warning)
      | - Widget(tag=1) is missing property(tag=3) which is part of the Kotlin schema (fixable)
      | - Widget(tag=1) is missing event(tag=6) which is part of the Kotlin schema (fixable)
      | - Widget(tag=1) is missing children(tag=3) which is part of the Kotlin schema (fixable)
      | - Widget(tag=1) property(tag=1) name changed from first to firstNameChange (fixable)
      | - Widget(tag=1) event(tag=4) name changed from event to eventNameChange (fixable)
      | - Widget(tag=1) event(tag=5) nullability changed from true to false (fixable)
      | - Widget(tag=1) children(tag=1) name changed from left to leftNameChange (fixable)
      | - Modifier(tag=2) is missing from the file, but it is part of the Kotlin schema (fixable)
      |
      |Run 'COMMAND' to automatically update the file.
      """.trimMargin(),
    )
  }

  @Test fun checkFileOutOfDateFatal() {
    apiFile.writeText(
      """
      |<schema version="1" type="com.example.TestSchema">
      |  <widget tag="1" type="com.example.Widget">
      |    <property tag="1" name="typeChange" type="kotlin.String"/>
      |    <property tag="2" name="extraInFile" type="kotlin.Int"/>
      |    <event tag="3" name="paramChange" params="kotlin.String"/>
      |    <event tag="4" name="nullableChange"/>
      |    <event tag="5" name="extraInFile" nullable="true"/>
      |    <children tag="1" name="extraInFile"/>
      |  </widget>
      |  <widget tag="2" type="com.example.WidgetExtraInFile"/>
      |  <modifier tag="1" type="com.example.Modifier">
      |    <property name="first" type="kotlin.String"/>
      |    <property name="extraInFile" type="kotlin.String"/>
      |  </modifier>
      |  <modifier tag="2" type="com.example.ModifierExtraInFile"/>
      |</schema>
      |
      """.trimMargin(),
    )
    val api = ApiSchema(
      type = "com.example.TestSchema",
      widgets = listOf(
        ApiWidget(
          tag = 1,
          type = "com.example.Widget",
          traits = listOf(
            ApiWidgetProperty(tag = 1, "typeChange", "kotlin.Double"),
            ApiWidgetEvent(tag = 3, "paramChange", "kotlin.Double"),
            ApiWidgetEvent(tag = 4, "nullableChange", nullable = true),
          ),
        ),
      ),
      modifiers = listOf(
        ApiModifier(
          tag = 1,
          type = "com.example.ModifierTypeChange",
          properties = listOf(
            ApiModifierProperty("first", "kotlin.StringTypeChange"),
          ),
        ),
      ),
    )
    val result = api.validateAgainst(apiFile, Check, "COMMAND")
    assertThat(result).isFailureWithMessage(
      """
      |API file does not match!
      |
      |Differences:
      | - Widget(tag=2) is tracked in the file, but it is not part of the Kotlin schema (fatal)
      | - Widget(tag=1) contains unknown property(tag=2) which is not part of the Kotlin schema (fatal)
      | - Widget(tag=1) contains unknown event(tag=5) which is not part of the Kotlin schema (fatal)
      | - Widget(tag=1) contains unknown children(tag=1) which is not part of the Kotlin schema (fatal)
      | - Widget(tag=1) property(tag=1) type changed from kotlin.String to kotlin.Double (fatal)
      | - Widget(tag=1) event(tag=3) type changed from kotlin.String to kotlin.Double (fatal)
      | - Widget(tag=1) event(tag=4) nullability changed from false to true (fatal)
      | - Modifier(tag=2) is tracked in the file, but is not part of the Kotlin schema (fatal)
      | - Modifier(tag=1) contains unknown property(name=extraInFile) which is not part of the Kotlin schema (fatal)
      | - Modifier(tag=1) property(name=first) type changed from kotlin.String to kotlin.StringTypeChange in the Kotlin schema (fatal)
      |
      |The API file cannot be updated automatically due to the presence of fatal
      |differences. Either revert the fatal differences to the schema, or delete the
      |file and run 'COMMAND' to regenerate. Note that regenerating the file
      |means the new schema is fundamentally incompatible with the old one and will
      |produce errors if both the host and guest code are not updated together.
      """.trimMargin(),
    )
  }

  @Test fun generateNoFile() {
    assertThat(apiFile).prop("exists", Path::exists).isFalse()
    val api = ApiSchema(type = "com.example.Schema")
    val result = api.validateAgainst(apiFile, Generate, "UNUSED")
    assertThat(result).isSuccess()
    assertThat(apiFile.readText()).isEqualTo(
      """
      |<schema version="1" type="com.example.Schema"/>
      |
      """.trimMargin(),
    )
  }

  @Test fun generateFileUpToDate() {
    apiFile.writeText(
      """
      |<schema version="1" type="com.example.Schema"/>
      |
      """.trimMargin(),
    )
    val api = ApiSchema(type = "com.example.Schema")
    val result = api.validateAgainst(apiFile, Generate, "UNUSED")
    assertThat(result).isSuccess()
    assertThat(apiFile.readText()).isEqualTo(
      """
      |<schema version="1" type="com.example.Schema"/>
      |
      """.trimMargin(),
    )
  }

  @Test fun generateFileOutOfDateWarning() {
    apiFile.writeText(
      """
      |<schema version="1" type="com.example.Schema"/>
      |
      """.trimMargin(),
    )
    val api = ApiSchema(type = "com.example.SchemaNew")
    val result = api.validateAgainst(apiFile, Generate, "UNUSED")
    assertThat(result).isSuccess()
    assertThat(apiFile.readText()).isEqualTo(
      """
      |<schema version="1" type="com.example.SchemaNew"/>
      |
      """.trimMargin(),
    )
  }

  @Test fun generateFileOutOfDateFixable() {
    apiFile.writeText(
      """
      |<schema version="1" type="com.example.Schema"/>
      |
      """.trimMargin(),
    )
    val api = ApiSchema(
      type = "com.example.Schema",
      widgets = listOf(
        ApiWidget(tag = 1, type = "com.example.Widget"),
      ),
    )
    val result = api.validateAgainst(apiFile, Generate, "UNUSED")
    assertThat(result).isSuccess()
    assertThat(apiFile.readText()).isEqualTo(
      """
      |<schema version="1" type="com.example.Schema">
      |  <widget tag="1" type="com.example.Widget"/>
      |</schema>
      |
      """.trimMargin(),
    )
  }

  @Test fun generateFileOutOfDateFatal() {
    val xml = """
      |<schema version="1" type="com.example.Schema">
      |  <widget tag="1" type="com.example.Widget"/>
      |</schema>
      |
    """.trimMargin()

    apiFile.writeText(xml)
    val api = ApiSchema(
      type = "com.example.Schema",
      widgets = listOf(
        // The presence of this widget will produce a fixable difference.
        ApiWidget(tag = 2, type = "com.example.OtherWidget"),
      ),
    )
    val result = api.validateAgainst(apiFile, Generate, "COMMAND")

    // Ensure only the fatal problems are reported as problematic when trying to generate.
    assertThat(result).isFailureWithMessage(
      """
      |Schema contains incompatible changes with current API!
      |
      |Differences:
      | - Widget(tag=1) is tracked in the file, but it is not part of the Kotlin schema (fatal)
      |
      |The API file cannot be updated automatically due to the presence of fatal
      |differences. Either revert the fatal differences to the schema, or delete the
      |file and run 'COMMAND' to regenerate. Note that regenerating the file
      |means the new schema is fundamentally incompatible with the old one and will
      |produce errors if both the host and guest code are not updated together.
      """.trimMargin(),
    )
    // Ensure the file was not changed in any way.
    assertThat(apiFile.readText()).isEqualTo(xml)
  }

  private fun Assert<ValidationResult>.isFailureWithMessage(message: String) {
    isInstanceOf<Failure>().prop(Failure::message).isEqualTo(message)
  }
  private fun Assert<ValidationResult>.isSuccess() {
    isEqualTo(ValidationResult.Success)
  }
}
