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
package app.cash.redwood.tooling.schema

import app.cash.redwood.layout.RedwoodLayout
import app.cash.redwood.layout.Row
import app.cash.redwood.schema.Children
import app.cash.redwood.schema.Modifier
import app.cash.redwood.schema.Property
import app.cash.redwood.schema.Schema
import app.cash.redwood.schema.Schema.Dependency
import app.cash.redwood.schema.Widget
import app.cash.redwood.tooling.schema.Widget.Children as ChildrenTrait
import app.cash.redwood.tooling.schema.Widget.Event
import app.cash.redwood.tooling.schema.Widget.Property as PropertyTrait
import assertk.all
import assertk.assertAll
import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.containsExactly
import assertk.assertions.containsMatch
import assertk.assertions.hasMessage
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isTrue
import assertk.assertions.message
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import example.redwood.ExampleSchema
import java.io.File
import kotlin.DeprecationLevel.HIDDEN
import kotlin.reflect.KClass
import kotlinx.serialization.Serializable
import org.junit.Assume.assumeTrue
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(TestParameterInjector::class)
class SchemaParserTest(
  @TestParameter private val parser: SchemaParser,
) {
  object TestScope

  interface NonAnnotationSchema

  @Test fun nonAnnotatedSchemaThrows() {
    assertFailure { parser.parse(NonAnnotationSchema::class) }
      .isInstanceOf<IllegalArgumentException>()
      .hasMessage(
        "Schema app.cash.redwood.tooling.schema.SchemaParserTest.NonAnnotationSchema missing @Schema annotation",
      )
  }

  @Schema(
    [
      NonAnnotatedMember::class,
    ],
  )
  interface NonAnnotatedWidgetSchema
  data class NonAnnotatedMember(
    @Property(1) val name: String,
  )

  @Test fun nonAnnotatedWidgetThrows() {
    assertFailure { parser.parse(NonAnnotatedWidgetSchema::class) }
      .isInstanceOf<IllegalArgumentException>()
      .hasMessage(
        "app.cash.redwood.tooling.schema.SchemaParserTest.NonAnnotatedMember must be annotated with either @Widget or @Modifier",
      )
  }

  @Schema(
    [
      DoubleAnnotatedWidget::class,
    ],
  )
  interface DoubleAnnotatedWidgetSchema

  @Widget(1)
  @Modifier(1, TestScope::class)
  data class DoubleAnnotatedWidget(
    @Property(1) val name: String,
  )

  @Test fun doubleAnnotatedWidgetThrows() {
    assertFailure { parser.parse(DoubleAnnotatedWidgetSchema::class) }
      .isInstanceOf<IllegalArgumentException>()
      .hasMessage(
        "app.cash.redwood.tooling.schema.SchemaParserTest.DoubleAnnotatedWidget must be annotated with either @Widget or @Modifier",
      )
  }

  @Schema(
    [
      DuplicateWidgetTagA::class,
      NonDuplicateWidgetTag::class,
      DuplicateWidgetTagB::class,
    ],
  )
  interface DuplicateWidgetTagSchema

  @Widget(1)
  data class DuplicateWidgetTagA(
    @Property(1) val name: String,
  )

  @Widget(2)
  data class NonDuplicateWidgetTag(
    @Property(1) val name: String,
  )

  @Widget(1)
  data class DuplicateWidgetTagB(
    @Property(1) val name: String,
  )

  @Test fun duplicateWidgetTagThrows() {
    assertFailure { parser.parse(DuplicateWidgetTagSchema::class) }
      .isInstanceOf<IllegalArgumentException>()
      .hasMessage(
        """
        |Schema @Widget tags must be unique
        |
        |- @Widget(1): app.cash.redwood.tooling.schema.SchemaParserTest.DuplicateWidgetTagA, app.cash.redwood.tooling.schema.SchemaParserTest.DuplicateWidgetTagB
        """.trimMargin(),
      )
  }

  @Schema(
    [
      DuplicateModifierTagA::class,
      NonDuplicateModifierTag::class,
      DuplicateModifierTagB::class,
    ],
  )
  interface DuplicateModifierTagSchema

  @Modifier(1, TestScope::class)
  data class DuplicateModifierTagA(
    val name: String,
  )

  @Modifier(2, TestScope::class)
  data class NonDuplicateModifierTag(
    val name: String,
  )

  @Modifier(1, TestScope::class)
  data class DuplicateModifierTagB(
    val name: String,
  )

  @Test fun duplicateModifierTagThrows() {
    assertFailure { parser.parse(DuplicateModifierTagSchema::class) }
      .isInstanceOf<IllegalArgumentException>()
      .hasMessage(
        """
        |Schema @Modifier tags must be unique
        |
        |- @Modifier(1): app.cash.redwood.tooling.schema.SchemaParserTest.DuplicateModifierTagA, app.cash.redwood.tooling.schema.SchemaParserTest.DuplicateModifierTagB
        """.trimMargin(),
      )
  }

  @Schema(
    [
      RepeatedWidget::class,
      RepeatedWidget::class,
    ],
  )
  interface RepeatedWidgetTypeSchema

  @Widget(1)
  data class RepeatedWidget(
    @Property(1) val name: String,
  )

  @Test fun repeatedWidgetTypeThrows() {
    assertFailure { parser.parse(RepeatedWidgetTypeSchema::class) }
      .isInstanceOf<IllegalArgumentException>()
      .hasMessage(
        """
        |Schema contains repeated member
        |
        |- app.cash.redwood.tooling.schema.SchemaParserTest.RepeatedWidget
        """.trimMargin(),
      )
  }

  @Schema(
    [
      DuplicatePropertyTagWidget::class,
    ],
  )
  interface DuplicatePropertyTagSchema

  @Widget(1)
  data class DuplicatePropertyTagWidget(
    @Property(1) val name: String,
    @Property(2) val age: Int,
    @Property(1) val nickname: String,
  )

  @Test fun duplicatePropertyTagThrows() {
    assertFailure { parser.parse(DuplicatePropertyTagSchema::class) }
      .isInstanceOf<IllegalArgumentException>()
      .hasMessage(
        """
        |app.cash.redwood.tooling.schema.SchemaParserTest.DuplicatePropertyTagWidget's @Property tags must be unique
        |
        |- @Property(1): name, nickname
        """.trimMargin(),
      )
  }

  @Schema(
    [
      DuplicateChildrenTagWidget::class,
    ],
  )
  interface DuplicateChildrenTagSchema

  @Widget(1)
  data class DuplicateChildrenTagWidget(
    @Children(1) val childrenA: () -> Unit,
    @Property(1) val name: String,
    @Children(1) val childrenB: () -> Unit,
  )

  @Test fun duplicateChildrenTagThrows() {
    assertFailure { parser.parse(DuplicateChildrenTagSchema::class) }
      .isInstanceOf<IllegalArgumentException>()
      .hasMessage(
        """
        |app.cash.redwood.tooling.schema.SchemaParserTest.DuplicateChildrenTagWidget's @Children tags must be unique
        |
        |- @Children(1): childrenA, childrenB
        """.trimMargin(),
      )
  }

  @Schema(
    [
      UnannotatedPrimaryParameterWidget::class,
    ],
  )
  interface UnannotatedPrimaryParameterSchema

  @Widget(1)
  data class UnannotatedPrimaryParameterWidget(
    @Property(1) val name: String,
    @Children(1) val children: () -> Unit,
    val unannotated: String,
  )

  @Test fun unannotatedPrimaryParameterThrows() {
    assertFailure { parser.parse(UnannotatedPrimaryParameterSchema::class) }
      .isInstanceOf<IllegalArgumentException>()
      .hasMessage(
        "Unannotated parameter \"unannotated\" on app.cash.redwood.tooling.schema.SchemaParserTest.UnannotatedPrimaryParameterWidget",
      )
  }

  @Schema(
    [
      NonDataClassWidget::class,
    ],
  )
  interface NonDataClassWidgetSchema

  @Widget(1)
  class NonDataClassWidget(
    @Property(1) val name: String,
  )

  @Test fun nonDataClassWidgetThrows() {
    assertFailure { parser.parse(NonDataClassWidgetSchema::class) }
      .isInstanceOf<IllegalArgumentException>()
      .hasMessage(
        "@Widget app.cash.redwood.tooling.schema.SchemaParserTest.NonDataClassWidget must be 'data' class or 'object'",
      )
  }

  @Schema(
    [
      NonDataClassModifier::class,
    ],
  )
  interface NonDataClassModifierSchema

  @Modifier(1, TestScope::class)
  class NonDataClassModifier(
    val name: String,
  )

  @Test fun nonDataClassModifierThrows() {
    assertFailure { parser.parse(NonDataClassModifierSchema::class) }
      .isInstanceOf<IllegalArgumentException>()
      .hasMessage(
        "@Modifier app.cash.redwood.tooling.schema.SchemaParserTest.NonDataClassModifier must be 'data' class or 'object'",
      )
  }

  @Schema(
    [
      InvalidChildrenTypeWidget::class,
    ],
  )
  interface InvalidChildrenTypeSchema

  @Widget(1)
  data class InvalidChildrenTypeWidget(
    @Children(1) val children: String,
  )

  @Test fun invalidChildrenTypeThrows() {
    assertFailure { parser.parse(InvalidChildrenTypeSchema::class) }
      .isInstanceOf<IllegalArgumentException>()
      .hasMessage(
        "@Children app.cash.redwood.tooling.schema.SchemaParserTest.InvalidChildrenTypeWidget#children must be of type '() -> Unit'",
      )
  }

  @Schema(
    [
      InvalidChildrenLambdaReturnTypeWidget::class,
    ],
  )
  interface InvalidChildrenLambdaReturnTypeSchema

  @Widget(1)
  data class InvalidChildrenLambdaReturnTypeWidget(
    @Children(1) val children: () -> String,
  )

  @Test fun invalidChildrenLambdaReturnTypeThrows() {
    assertFailure { parser.parse(InvalidChildrenLambdaReturnTypeSchema::class) }
      .isInstanceOf<IllegalArgumentException>()
      .hasMessage(
        "@Children app.cash.redwood.tooling.schema.SchemaParserTest.InvalidChildrenLambdaReturnTypeWidget#children must be of type '() -> Unit'",
      )
  }

  @Schema(
    [
      ChildrenArgumentsInvalidWidget::class,
    ],
  )
  interface ChildrenArgumentsInvalidSchema

  @Widget(1)
  data class ChildrenArgumentsInvalidWidget(
    @Children(1) val children: (String) -> Unit,
  )

  @Test fun childrenArgumentsInvalid() {
    assertFailure { parser.parse(ChildrenArgumentsInvalidSchema::class) }
      .isInstanceOf<IllegalArgumentException>()
      .hasMessage(
        "@Children app.cash.redwood.tooling.schema.SchemaParserTest.ChildrenArgumentsInvalidWidget#children lambda type must not have any arguments. " +
          "Found: [kotlin.String]",
      )
  }

  @Schema(
    [
      ScopedChildrenArgumentsInvalidWidget::class,
    ],
  )
  interface ScopedChildrenArgumentsInvalidSchema

  @Widget(1)
  data class ScopedChildrenArgumentsInvalidWidget(
    @Children(1) val children: String.(Int) -> Unit,
  )

  @Test fun scopedChildrenArgumentsInvalid() {
    assertFailure { parser.parse(ScopedChildrenArgumentsInvalidSchema::class) }
      .isInstanceOf<IllegalArgumentException>()
      .hasMessage(
        "@Children app.cash.redwood.tooling.schema.SchemaParserTest.ScopedChildrenArgumentsInvalidWidget#children lambda type must not have any arguments. " +
          "Found: [kotlin.Int]",
      )
  }

  @Schema(
    [
      ScopedChildrenInvalidWidget::class,
    ],
  )
  interface ScopedChildrenInvalidSchema

  @Widget(1)
  data class ScopedChildrenInvalidWidget(
    @Children(1) val children: List<Int>.() -> Unit,
  )

  @Test fun scopedChildrenInvalid() {
    assertFailure { parser.parse(ScopedChildrenInvalidSchema::class) }
      .isInstanceOf<IllegalArgumentException>()
      .message()
      .isNotNull()
      .all {
        contains("@Children app.cash.redwood.tooling.schema.SchemaParserTest.ScopedChildrenInvalidWidget#children lambda receiver can only be a class.")
        containsMatch(Regex("""Found: (kotlin\.collections\.)?List<(kotlin\.)?Int>"""))
      }
  }

  @Schema(
    [
      ScopedChildrenTypeParameterInvalidWidget::class,
    ],
  )
  interface ScopedChildrenTypeParameterInvalidSchema

  @Widget(1)
  data class ScopedChildrenTypeParameterInvalidWidget<T>(
    @Children(1) val children: T.() -> Unit,
  )

  @Test fun scopedChildrenTypeParameterInvalid() {
    assertFailure { parser.parse(ScopedChildrenTypeParameterInvalidSchema::class) }
      .isInstanceOf<IllegalArgumentException>()
      .hasMessage(
        "@Children app.cash.redwood.tooling.schema.SchemaParserTest.ScopedChildrenTypeParameterInvalidWidget#children lambda receiver can only be a class. " +
          "Found: T",
      )
  }

  @Schema(
    [
      EventTypeWidget::class,
    ],
  )
  interface EventTypeSchema

  @Widget(1)
  data class EventTypeWidget(
    @Property(1) val requiredEvent: () -> Unit,
    @Property(2) val optionalEvent: (() -> Unit)?,
  )

  @Test fun eventTypes() {
    val schema = parser.parse(EventTypeSchema::class).schema
    val widget = schema.widgets.single()
    assertThat(widget.traits.single { it.name == "requiredEvent" }).isInstanceOf<Event>()
    assertThat(widget.traits.single { it.name == "optionalEvent" }).isInstanceOf<Event>()
  }

  @Schema(
    [
      EventArgumentsWidget::class,
    ],
  )
  interface EventArgumentsSchema

  @Widget(1)
  data class EventArgumentsWidget(
    @Property(1) val noArguments: () -> Unit,
    @Property(2) val oneArgument: (String) -> Unit,
    @Property(3) val oneArgumentOptional: ((String) -> Unit)?,
    @Property(4) val manyArguments: (String, Boolean, Long) -> Unit,
    @Property(5) val manyArgumentsOptional: ((String, Boolean, Long) -> Unit)?,
  )

  @Test fun eventArguments() {
    val schema = parser.parse(EventArgumentsSchema::class).schema
    val widget = schema.widgets.single()

    val noArguments = widget.traits.single { it.name == "noArguments" } as Event
    assertThat(noArguments.parameterTypes).isEmpty()
    val oneArgument = widget.traits.single { it.name == "oneArgument" } as Event
    assertThat(oneArgument.parameterTypes).containsExactly(String::class.toFqType())
    val oneArgumentOptional = widget.traits.single { it.name == "oneArgumentOptional" } as Event
    assertThat(oneArgumentOptional.parameterTypes).containsExactly(String::class.toFqType())
    val manyArguments = widget.traits.single { it.name == "manyArguments" } as Event
    assertThat(manyArguments.parameterTypes).containsExactly(String::class.toFqType(), Boolean::class.toFqType(), Long::class.toFqType())
    val manyArgumentOptional = widget.traits.single { it.name == "manyArgumentsOptional" } as Event
    assertThat(manyArgumentOptional.parameterTypes).containsExactly(String::class.toFqType(), Boolean::class.toFqType(), Long::class.toFqType())
  }

  @Schema(
    [
      ObjectWidget::class,
    ],
  )
  interface ObjectSchema

  @Widget(1)
  object ObjectWidget

  @Test fun objectWidget() {
    val schema = parser.parse(ObjectSchema::class).schema
    val widget = schema.widgets.single()
    assertThat(widget.traits).isEmpty()
  }

  @Schema(
    [
      OneMillionWidget::class,
    ],
  )
  interface OneMillionWidgetSchema

  @Widget(1_000_000)
  object OneMillionWidget

  @Test fun widgetTagOneMillionThrows() {
    assertFailure { parser.parse(OneMillionWidgetSchema::class) }
      .isInstanceOf<IllegalArgumentException>()
      .hasMessage(
        "@Widget app.cash.redwood.tooling.schema.SchemaParserTest.OneMillionWidget " +
          "tag must be in range [1, 1000000): 1000000",
      )
  }

  @Schema(
    [
      ZeroWidget::class,
    ],
  )
  interface ZeroWidgetSchema

  @Widget(0)
  object ZeroWidget

  @Test fun widgetTagZeroThrows() {
    assertFailure { parser.parse(ZeroWidgetSchema::class) }
      .isInstanceOf<IllegalArgumentException>()
      .hasMessage(
        "@Widget app.cash.redwood.tooling.schema.SchemaParserTest.ZeroWidget " +
          "tag must be in range [1, 1000000): 0",
      )
  }

  @Schema(
    [
      OneMillionModifier::class,
    ],
  )
  interface OneMillionModifierSchema

  @Modifier(1_000_000, TestScope::class)
  data class OneMillionModifier(
    val value: Int,
  )

  @Test fun modifierTagOneMillionThrows() {
    assertFailure { parser.parse(OneMillionModifierSchema::class) }
      .isInstanceOf<IllegalArgumentException>()
      .hasMessage(
        "@Modifier app.cash.redwood.tooling.schema.SchemaParserTest.OneMillionModifier " +
          "tag must be in range [1, 1000000): 1000000",
      )
  }

  @Schema(
    [
      ZeroModifier::class,
    ],
  )
  interface ZeroModifierSchema

  @Modifier(0, TestScope::class)
  data class ZeroModifier(
    val value: Int,
  )

  @Test fun modifierTagZeroThrows() {
    assertFailure { parser.parse(ZeroModifierSchema::class) }
      .isInstanceOf<IllegalArgumentException>()
      .hasMessage(
        "@Modifier app.cash.redwood.tooling.schema.SchemaParserTest.ZeroModifier " +
          "tag must be in range [1, 1000000): 0",
      )
  }

  @Schema([SomeWidget::class, SomeModifier::class])
  interface SchemaTag

  @Widget(1)
  data class SomeWidget(
    @Property(1) val value: Int,
    @Children(1) val children: () -> Unit,
  )

  @Modifier(1, TestScope::class)
  data class SomeModifier(
    val value: Int,
  )

  @Test fun schemaTagDefault() {
    val schema = parser.parse(SchemaTag::class).schema

    val widget = schema.widgets.single()
    assertThat(widget.tag).isEqualTo(1)
    assertThat(widget.traits[0].tag).isEqualTo(1)
    assertThat(widget.traits[1].tag).isEqualTo(1)

    val modifier = schema.modifier.single()
    assertThat(modifier.tag).isEqualTo(1)
  }

  @Schema(
    members = [],
    dependencies = [
      Dependency(4, RedwoodLayout::class),
    ],
  )
  object SchemaDependencyTagOffsetsMemberTags

  @Test fun schemaTagOffsetsMemberTags() {
    val schema = parser.parse(SchemaDependencyTagOffsetsMemberTags::class)
    val dependency = schema.dependencies.values.single()

    val widget = dependency.widgets.single { it.type.names.last() == "Row" }
    assertThat(widget.tag).isEqualTo(4_000_001)
    val widgetProperty = widget.traits.first { it is PropertyTrait }
    assertThat(widgetProperty.tag).isEqualTo(1)
    val widgetChildren = widget.traits.first { it is ChildrenTrait }
    assertThat(widgetChildren.tag).isEqualTo(1)

    val modifier = dependency.modifier.single { it.type.names.last() == "Grow" }
    assertThat(modifier.tag).isEqualTo(4_000_001)
  }

  @Schema(
    members = [],
    dependencies = [
      Dependency(2001, RedwoodLayout::class),
    ],
  )
  object SchemaDependencyTagTooHigh

  @Schema(
    members = [],
    dependencies = [
      Dependency(-1, RedwoodLayout::class),
    ],
  )
  object SchemaDependencyTagTooLow

  @Test fun dependencyTagTooHighThrows() {
    assertFailure { parser.parse(SchemaDependencyTagTooHigh::class) }
      .isInstanceOf<IllegalArgumentException>()
      .hasMessage(
        "app.cash.redwood.layout.RedwoodLayout tag must be 0 for the root or in range (0, 2000] as a dependency: 2001",
      )

    assertFailure { parser.parse(SchemaDependencyTagTooLow::class) }
      .isInstanceOf<IllegalArgumentException>()
      .hasMessage(
        "app.cash.redwood.layout.RedwoodLayout tag must be 0 for the root or in range (0, 2000] as a dependency: -1",
      )
  }

  @Schema(
    members = [],
    dependencies = [
      Dependency(0, RedwoodLayout::class),
    ],
  )
  object SchemaDependencyTagZero

  @Test fun dependencyTagZeroThrows() {
    assertFailure { parser.parse(SchemaDependencyTagZero::class) }
      .isInstanceOf<IllegalArgumentException>()
      .hasMessage(
        "Dependency app.cash.redwood.layout.RedwoodLayout tag must not be non-zero",
      )
  }

  @Schema(
    members = [],
    dependencies = [
      Dependency(1, SchemaDuplicateDependencyTagA::class),
      Dependency(1, SchemaDuplicateDependencyTagB::class),
    ],
  )
  object SchemaDuplicateDependencyTag

  @Schema(members = [])
  object SchemaDuplicateDependencyTagA

  @Schema(members = [])
  object SchemaDuplicateDependencyTagB

  @Test fun schemaDuplicateDependencyTagThrows() {
    assertFailure { parser.parse(SchemaDuplicateDependencyTag::class) }
      .isInstanceOf<IllegalArgumentException>()
      .hasMessage(
        """
        |Schema dependency tags must be unique
        |
        |- Dependency tag 1: app.cash.redwood.tooling.schema.SchemaParserTest.SchemaDuplicateDependencyTagA, app.cash.redwood.tooling.schema.SchemaParserTest.SchemaDuplicateDependencyTagB
        """.trimMargin(),
      )
  }

  @Schema(
    members = [],
    dependencies = [
      Dependency(1, SchemaDuplicateDependencyTypeOther::class),
      Dependency(2, SchemaDuplicateDependencyTypeOther::class),
    ],
  )
  object SchemaDuplicateDependencyType

  @Schema(members = [])
  object SchemaDuplicateDependencyTypeOther

  @Test fun schemaDuplicateDependencyTypeThrows() {
    assertFailure { parser.parse(SchemaDuplicateDependencyType::class) }
      .isInstanceOf<IllegalArgumentException>()
      .hasMessage(
        """
        |Schema contains repeated dependency
        |
        |- app.cash.redwood.tooling.schema.SchemaParserTest.SchemaDuplicateDependencyTypeOther
        """.trimMargin(),
      )
  }

  @Schema(
    members = [],
    dependencies = [
      Dependency(1, ExampleSchema::class),
    ],
  )
  object SchemaDependencyHasDependency

  @Test fun schemaDependencyHasDependencyThrows() {
    assertFailure { parser.parse(SchemaDependencyHasDependency::class) }
      .isInstanceOf<IllegalArgumentException>()
      .hasMessage(
        "Schema dependency example.redwood.ExampleSchema also has its own dependencies. " +
          "For now, only a single level of dependencies is supported.",
      )
  }

  @Schema(
    members = [
      Row::class,
    ],
    dependencies = [
      Dependency(1, RedwoodLayout::class),
    ],
  )
  object SchemaWidgetDuplicateInDependency

  @Test fun schemaWidgetDuplicateInDependencyThrows() {
    assumeTrue(parser != SchemaParser.Fir)

    assertFailure { parser.parse(SchemaWidgetDuplicateInDependency::class) }
      .isInstanceOf<IllegalArgumentException>()
      .hasMessage(
        """
        |Schema dependency tree contains duplicated widgets
        |
        |- app.cash.redwood.layout.Row: app.cash.redwood.tooling.schema.SchemaParserTest.SchemaWidgetDuplicateInDependency, app.cash.redwood.layout.RedwoodLayout
        """.trimMargin(),
      )
  }

  @Test fun schemaMembersMustBeInSource() {
    assumeTrue(parser == SchemaParser.Fir)

    assertFailure { parser.parse(SchemaWidgetDuplicateInDependency::class) }
      .isInstanceOf<IllegalArgumentException>()
      .hasMessage("Unable to locate schema type app.cash.redwood.layout.Row")
  }

  @Schema(
    members = [
      UnscopedModifier::class,
    ],
  )
  interface UnscopedModifierSchema

  @Modifier(1)
  object UnscopedModifier

  @Test fun `layout modifier must have at least one scope`() {
    assertFailure { parser.parse(UnscopedModifierSchema::class) }
      .isInstanceOf<IllegalArgumentException>()
      .hasMessage(
        "@Modifier app.cash.redwood.tooling.schema.SchemaParserTest.UnscopedModifier " +
          "must have at least one scope.",
      )
  }

  @Schema(
    [
      SerializationModifier::class,
    ],
  )
  interface SerializationSchema

  @Modifier(1, TestScope::class)
  data class SerializationModifier(
    val no: String,
    val yes: SerializableType,
  )

  @Serializable
  data class SerializableType(
    val whevs: String,
  )

  @Test fun serializableModifierProperties() {
    assumeTrue(parser != SchemaParser.Fir)

    val schema = parser.parse(SerializationSchema::class).schema
    val modifier = schema.modifier.single()

    val yesProperty = modifier.properties.single { it.name == "yes" }
    assertThat(yesProperty.isSerializable).isTrue()

    val noProperty = modifier.properties.single { it.name == "no" }
    assertThat(noProperty.isSerializable).isFalse()
  }

  @Schema(
    [
      DeprecationHiddenWidget::class,
    ],
  )
  interface DeprecationHiddenSchema

  @Widget(1)
  data class DeprecationHiddenWidget(
    @Deprecated("", level = HIDDEN)
    val a: String,
  )

  @Test fun deprecationHiddenThrows() {
    assertFailure { parser.parse(DeprecationHiddenSchema::class) }
      .isInstanceOf<IllegalArgumentException>()
      .hasMessage(
        "Schema deprecation does not support level HIDDEN: " +
          "app.cash.redwood.tooling.schema.SchemaParserTest.DeprecationHiddenWidget.a",
      )
  }

  @Suppress("DEPRECATION")
  @Schema(
    [
      DeprecationReplaceWithWidget::class,
    ],
  )
  interface DeprecationReplaceWithSchema

  @Widget(1)
  @Deprecated("", ReplaceWith("Hello"))
  object DeprecationReplaceWithWidget

  @Test fun deprecationReplaceWithThrows() {
    assertFailure { parser.parse(DeprecationReplaceWithSchema::class) }
      .isInstanceOf<IllegalArgumentException>()
      .hasMessage(
        "Schema deprecation does not support replacements: " +
          "app.cash.redwood.tooling.schema.SchemaParserTest.DeprecationReplaceWithWidget",
      )
  }

  /** Schema single-line documentation. */
  @Schema(
    [
      CommentsWidget::class,
      CommentsModifier::class,
    ],
  )
  interface CommentsSchema

  /**
   * Widget
   * multi-line
   * documentation.
   */
  @Widget(1)
  data class CommentsWidget(
    /**
Property
     weird formatting
     documentation.
     */
    @Property(1) val id: Int,
    /**Children
     *missing
     *spaces documentation.*/
    @Children(1) val content: () -> Unit,
  )

  /**
   * Layout modifier
   * multi-line
   * documentation.
   */
  @Suppress("ktlint:standard:kdoc-wrapping")
  @Modifier(1, TestScope::class)
  data class CommentsModifier(
    /** Property same line documentation. */ val id: Int,
  )

  @Ignore("Missing handling of '/** */' noise")
  @Test
  fun comments() = assertAll {
    assumeTrue(parser == SchemaParser.Fir) // Comments are only available through the AST.

    val schema = parser.parse(CommentsSchema::class).schema
    assertThat(schema.documentation).isEqualTo("Schema single-line documentation.")

    val widget = schema.widgets.single()
    assertThat(widget.documentation).isEqualTo("Widget multi-line documentation.")

    val widgetProperty = widget.traits.single { it is PropertyTrait }
    assertThat(widgetProperty.documentation).isEqualTo("Property weird formatting documentation.")

    val widgetChildren = widget.traits.single { it is ChildrenTrait }
    assertThat(widgetChildren.documentation).isEqualTo("Children missing spaces documentation.")

    val modifier = schema.modifier.single()
    assertThat(modifier.documentation).isEqualTo("Layout modifier multi-line documentation.")

    val modifierProperty = modifier.properties.single()
    assertThat(modifierProperty.documentation).isEqualTo("Property same line documentation.")
  }

  @Suppress("unused") // Values used by TestParameterInjector.
  enum class SchemaParser {
    Reflection {
      override fun parse(type: KClass<*>): ProtocolSchemaSet {
        return parseProtocolSchemaSet(type)
      }
    },
    Fir {
      override fun parse(type: KClass<*>): ProtocolSchemaSet {
        val sources = System.getProperty("redwood.internal.sources")
          .split(File.pathSeparator)
          .map(::File)
          .filter(File::exists) // Entries that don't exist produce warnings.
        val classpath = System.getProperty("redwood.internal.classpath")
          .split(File.pathSeparator)
          .map(::File)
          .filter(File::exists) // Entries that don't exist produce warnings.
        return parseProtocolSchema(sources, classpath, type.toFqType())
      }
    },
    ;

    abstract fun parse(type: KClass<*>): ProtocolSchemaSet
  }
}
