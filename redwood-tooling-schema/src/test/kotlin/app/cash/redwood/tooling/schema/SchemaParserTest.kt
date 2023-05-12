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
import app.cash.redwood.schema.LayoutModifier
import app.cash.redwood.schema.Property
import app.cash.redwood.schema.Schema
import app.cash.redwood.schema.Schema.Dependency
import app.cash.redwood.schema.Widget
import app.cash.redwood.tooling.schema.Widget.Children as ChildrenTrait
import app.cash.redwood.tooling.schema.Widget.Event
import app.cash.redwood.tooling.schema.Widget.Property as PropertyTrait
import assertk.assertThat
import assertk.assertions.hasMessage
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import example.redwood.ExampleSchema
import java.io.File
import kotlin.DeprecationLevel.HIDDEN
import kotlin.reflect.KClass
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(TestParameterInjector::class)
class SchemaParserTest(
  @TestParameter private val parser: SchemaParser,
) {
  object TestScope

  interface NonAnnotationSchema

  @Test fun nonAnnotatedSchemaThrows() {
    assertFailsWith<IllegalArgumentException> {
      parser.parse(NonAnnotationSchema::class)
    }.hasMessage(
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
    assertFailsWith<IllegalArgumentException> {
      parser.parse(NonAnnotatedWidgetSchema::class)
    }.hasMessage(
      "app.cash.redwood.tooling.schema.SchemaParserTest.NonAnnotatedMember must be annotated with either @Widget or @LayoutModifier",
    )
  }

  @Schema(
    [
      DoubleAnnotatedWidget::class,
    ],
  )
  interface DoubleAnnotatedWidgetSchema

  @Widget(1)
  @LayoutModifier(1, TestScope::class)
  data class DoubleAnnotatedWidget(
    @Property(1) val name: String,
  )

  @Test fun doubleAnnotatedWidgetThrows() {
    assertFailsWith<IllegalArgumentException> {
      parser.parse(DoubleAnnotatedWidgetSchema::class)
    }.hasMessage(
      "app.cash.redwood.tooling.schema.SchemaParserTest.DoubleAnnotatedWidget must be annotated with either @Widget or @LayoutModifier",
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
    assertFailsWith<IllegalArgumentException> {
      parser.parse(DuplicateWidgetTagSchema::class)
    }.hasMessage(
      """
      |Schema @Widget tags must be unique
      |
      |- @Widget(1): app.cash.redwood.tooling.schema.SchemaParserTest.DuplicateWidgetTagA, app.cash.redwood.tooling.schema.SchemaParserTest.DuplicateWidgetTagB
      """.trimMargin(),
    )
  }

  @Schema(
    [
      DuplicateLayoutModifierTagA::class,
      NonDuplicateLayoutModifierTag::class,
      DuplicateLayoutModifierTagB::class,
    ],
  )
  interface DuplicateLayoutModifierTagSchema

  @LayoutModifier(1, TestScope::class)
  data class DuplicateLayoutModifierTagA(
    val name: String,
  )

  @LayoutModifier(2, TestScope::class)
  data class NonDuplicateLayoutModifierTag(
    val name: String,
  )

  @LayoutModifier(1, TestScope::class)
  data class DuplicateLayoutModifierTagB(
    val name: String,
  )

  @Test fun duplicateLayoutModifierTagThrows() {
    assertFailsWith<IllegalArgumentException> {
      parser.parse(DuplicateLayoutModifierTagSchema::class)
    }.hasMessage(
      """
      |Schema @LayoutModifier tags must be unique
      |
      |- @LayoutModifier(1): app.cash.redwood.tooling.schema.SchemaParserTest.DuplicateLayoutModifierTagA, app.cash.redwood.tooling.schema.SchemaParserTest.DuplicateLayoutModifierTagB
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
    assertFailsWith<IllegalArgumentException> {
      parser.parse(RepeatedWidgetTypeSchema::class)
    }.hasMessage(
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
    assertFailsWith<IllegalArgumentException> {
      parser.parse(DuplicatePropertyTagSchema::class)
    }.hasMessage(
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
    assertFailsWith<IllegalArgumentException> {
      parser.parse(DuplicateChildrenTagSchema::class)
    }.hasMessage(
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
    assertFailsWith<IllegalArgumentException> {
      parser.parse(UnannotatedPrimaryParameterSchema::class)
    }.hasMessage(
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
    assertFailsWith<IllegalArgumentException> {
      parser.parse(NonDataClassWidgetSchema::class)
    }.hasMessage(
      "@Widget app.cash.redwood.tooling.schema.SchemaParserTest.NonDataClassWidget must be 'data' class or 'object'",
    )
  }

  @Schema(
    [
      NonDataClassLayoutModifier::class,
    ],
  )
  interface NonDataClassLayoutModifierSchema

  @LayoutModifier(1, TestScope::class)
  class NonDataClassLayoutModifier(
    val name: String,
  )

  @Test fun nonDataClassLayoutModifierThrows() {
    assertFailsWith<IllegalArgumentException> {
      parser.parse(NonDataClassLayoutModifierSchema::class)
    }.hasMessage(
      "@LayoutModifier app.cash.redwood.tooling.schema.SchemaParserTest.NonDataClassLayoutModifier must be 'data' class or 'object'",
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
    assumeTrue(parser != SchemaParser.Fir)

    assertFailsWith<IllegalArgumentException> {
      parser.parse(InvalidChildrenTypeSchema::class)
    }.hasMessage(
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
    assumeTrue(parser != SchemaParser.Fir)

    assertFailsWith<IllegalArgumentException> {
      parser.parse(InvalidChildrenLambdaReturnTypeSchema::class)
    }.hasMessage(
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
    assumeTrue(parser != SchemaParser.Fir)

    assertFailsWith<IllegalArgumentException> {
      parser.parse(ChildrenArgumentsInvalidSchema::class)
    }.hasMessage(
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
    assumeTrue(parser != SchemaParser.Fir)

    assertFailsWith<IllegalArgumentException> {
      parser.parse(ScopedChildrenArgumentsInvalidSchema::class)
    }.hasMessage(
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
    assumeTrue(parser != SchemaParser.Fir)

    assertFailsWith<IllegalArgumentException> {
      parser.parse(ScopedChildrenInvalidSchema::class)
    }.hasMessage(
      "@Children app.cash.redwood.tooling.schema.SchemaParserTest.ScopedChildrenInvalidWidget#children lambda receiver can only be a class. " +
        "Found: kotlin.collections.List<kotlin.Int>",
    )
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
    assumeTrue(parser != SchemaParser.Fir)

    assertFailsWith<IllegalArgumentException> {
      parser.parse(ScopedChildrenTypeParameterInvalidSchema::class)
    }.hasMessage(
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
    assumeTrue(parser != SchemaParser.Fir)

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
    @Property(2) val argument: (String) -> Unit,
    @Property(3) val argumentOptionalLambda: ((String) -> Unit)?,
  )

  @Test fun eventArguments() {
    assumeTrue(parser != SchemaParser.Fir)

    val schema = parser.parse(EventArgumentsSchema::class).schema
    val widget = schema.widgets.single()

    val noArguments = widget.traits.single { it.name == "noArguments" } as Event
    assertThat(noArguments.parameterType).isNull()
    val argument = widget.traits.single { it.name == "argument" } as Event
    assertThat(argument.parameterType).isEqualTo(String::class.toFqType())
    val argumentOptionalLambda = widget.traits.single { it.name == "argumentOptionalLambda" } as Event
    assertThat(argumentOptionalLambda.parameterType).isEqualTo(String::class.toFqType())
  }

  @Schema(
    [
      EventArgumentsInvalidWidget::class,
    ],
  )
  interface EventArgumentsInvalidSchema

  @Widget(1)
  data class EventArgumentsInvalidWidget(
    @Property(3) val tooManyArguments: ((String, Boolean, Long) -> Unit)?,
  )

  @Test fun eventArgumentsInvalid() {
    assumeTrue(parser != SchemaParser.Fir)

    assertFailsWith<IllegalArgumentException> {
      parser.parse(EventArgumentsInvalidSchema::class)
    }.hasMessage(
      "@Property app.cash.redwood.tooling.schema.SchemaParserTest.EventArgumentsInvalidWidget#tooManyArguments lambda type can only have zero or one arguments. " +
        "Found: [kotlin.String, kotlin.Boolean, kotlin.Long]",
    )
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
    assertFailsWith<IllegalArgumentException> {
      parser.parse(OneMillionWidgetSchema::class)
    }.hasMessage(
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
    assertFailsWith<IllegalArgumentException> {
      parser.parse(ZeroWidgetSchema::class)
    }.hasMessage(
      "@Widget app.cash.redwood.tooling.schema.SchemaParserTest.ZeroWidget " +
        "tag must be in range [1, 1000000): 0",
    )
  }

  @Schema(
    [
      OneMillionLayoutModifier::class,
    ],
  )
  interface OneMillionLayoutModifierSchema

  @LayoutModifier(1_000_000, TestScope::class)
  data class OneMillionLayoutModifier(
    val value: Int,
  )

  @Test fun layoutModifierTagOneMillionThrows() {
    assertFailsWith<IllegalArgumentException> {
      parser.parse(OneMillionLayoutModifierSchema::class)
    }.hasMessage(
      "@LayoutModifier app.cash.redwood.tooling.schema.SchemaParserTest.OneMillionLayoutModifier " +
        "tag must be in range [1, 1000000): 1000000",
    )
  }

  @Schema(
    [
      ZeroLayoutModifier::class,
    ],
  )
  interface ZeroLayoutModifierSchema

  @LayoutModifier(0, TestScope::class)
  data class ZeroLayoutModifier(
    val value: Int,
  )

  @Test fun layoutModifierTagZeroThrows() {
    assertFailsWith<IllegalArgumentException> {
      parser.parse(ZeroLayoutModifierSchema::class)
    }.hasMessage(
      "@LayoutModifier app.cash.redwood.tooling.schema.SchemaParserTest.ZeroLayoutModifier " +
        "tag must be in range [1, 1000000): 0",
    )
  }

  @Schema([SomeWidget::class, SomeLayoutModifier::class])
  interface SchemaTag

  @Widget(1)
  data class SomeWidget(
    @Property(1) val value: Int,
    @Children(1) val children: () -> Unit,
  )

  @LayoutModifier(1, TestScope::class)
  data class SomeLayoutModifier(
    val value: Int,
  )

  @Test fun schemaTagDefault() {
    val schema = parser.parse(SchemaTag::class).schema

    val widget = schema.widgets.single()
    assertThat(widget.tag).isEqualTo(1)
    assertThat(widget.traits[0].tag).isEqualTo(1)
    assertThat(widget.traits[1].tag).isEqualTo(1)

    val layoutModifier = schema.layoutModifiers.single()
    assertThat(layoutModifier.tag).isEqualTo(1)
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

    val layoutModifier = dependency.layoutModifiers.single { it.type.names.last() == "Grow" }
    assertThat(layoutModifier.tag).isEqualTo(4_000_001)
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
    assertFailsWith<IllegalArgumentException> {
      parser.parse(SchemaDependencyTagTooHigh::class)
    }.hasMessage(
      "app.cash.redwood.layout.RedwoodLayout tag must be 0 for the root or in range (0, 2000] as a dependency: 2001",
    )

    assertFailsWith<IllegalArgumentException> {
      parser.parse(SchemaDependencyTagTooLow::class)
    }.hasMessage(
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
    assertFailsWith<IllegalArgumentException> {
      parser.parse(SchemaDependencyTagZero::class)
    }.hasMessage(
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
    assertFailsWith<IllegalArgumentException> {
      parser.parse(SchemaDuplicateDependencyTag::class)
    }.hasMessage(
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
    assertFailsWith<IllegalArgumentException> {
      parser.parse(SchemaDuplicateDependencyType::class)
    }.hasMessage(
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
    assertFailsWith<IllegalArgumentException> {
      parser.parse(SchemaDependencyHasDependency::class)
    }.hasMessage(
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

    assertFailsWith<IllegalArgumentException> {
      parser.parse(SchemaWidgetDuplicateInDependency::class)
    }.hasMessage(
      """
      |Schema dependency tree contains duplicated widgets
      |
      |- app.cash.redwood.layout.Row: app.cash.redwood.tooling.schema.SchemaParserTest.SchemaWidgetDuplicateInDependency, app.cash.redwood.layout.RedwoodLayout
      """.trimMargin(),
    )
  }

  @Test fun schemaMembersMustBeInSource() {
    assumeTrue(parser == SchemaParser.Fir)

    assertFailsWith<IllegalArgumentException> {
      parser.parse(SchemaWidgetDuplicateInDependency::class)
    }.hasMessage("Unable to locate schema type app.cash.redwood.layout.Row")
  }

  @Schema(
    members = [
      UnscopedLayoutModifier::class,
    ],
  )
  interface UnscopedModifierSchema

  @LayoutModifier(1)
  object UnscopedLayoutModifier

  @Test fun `layout modifier must have at least one scope`() {
    assertFailsWith<IllegalArgumentException> {
      parser.parse(UnscopedModifierSchema::class)
    }.hasMessage(
      "@LayoutModifier app.cash.redwood.tooling.schema.SchemaParserTest.UnscopedLayoutModifier " +
        "must have at least one scope.",
    )
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
    assertFailsWith<IllegalArgumentException> {
      parser.parse(DeprecationHiddenSchema::class)
    }.hasMessage(
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
    assertFailsWith<IllegalArgumentException> {
      parser.parse(DeprecationReplaceWithSchema::class)
    }.hasMessage(
      "Schema deprecation does not support replacements: " +
        "app.cash.redwood.tooling.schema.SchemaParserTest.DeprecationReplaceWithWidget",
    )
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
