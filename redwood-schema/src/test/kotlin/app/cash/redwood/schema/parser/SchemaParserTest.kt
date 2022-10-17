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
package app.cash.redwood.schema.parser

import app.cash.redwood.schema.Children
import app.cash.redwood.schema.LayoutModifier
import app.cash.redwood.schema.Property
import app.cash.redwood.schema.Schema
import app.cash.redwood.schema.Schema.Dependency
import app.cash.redwood.schema.Widget
import app.cash.redwood.schema.parser.Widget.Event
import com.google.common.truth.Truth.assertThat
import kotlin.reflect.full.createType
import org.junit.Test

class SchemaParserTest {
  interface NonAnnotationSchema

  @Test fun nonAnnotatedSchemaThrows() {
    assertThrows<IllegalArgumentException> {
      parseSchema(NonAnnotationSchema::class)
    }.hasMessageThat().isEqualTo(
      "Schema app.cash.redwood.schema.parser.SchemaParserTest.NonAnnotationSchema missing @Schema annotation",
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
    assertThrows<IllegalArgumentException> {
      parseSchema(NonAnnotatedWidgetSchema::class)
    }.hasMessageThat().isEqualTo(
      "app.cash.redwood.schema.parser.SchemaParserTest.NonAnnotatedMember must be annotated with either @Widget or @LayoutModifier",
    )
  }

  @Schema(
    [
      DoubleAnnotatedWidget::class,
    ],
  )
  interface DoubleAnnotatedWidgetSchema

  @Widget(1)
  @LayoutModifier(1)
  data class DoubleAnnotatedWidget(
    @Property(1) val name: String,
  )

  @Test fun doubleAnnotatedWidgetThrows() {
    assertThrows<IllegalArgumentException> {
      parseSchema(DoubleAnnotatedWidgetSchema::class)
    }.hasMessageThat().isEqualTo(
      "app.cash.redwood.schema.parser.SchemaParserTest.DoubleAnnotatedWidget must be annotated with either @Widget or @LayoutModifier",
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
    assertThrows<IllegalArgumentException> {
      parseSchema(DuplicateWidgetTagSchema::class)
    }.hasMessageThat().isEqualTo(
      """
      |Schema @Widget tags must be unique
      |
      |- @Widget(1): app.cash.redwood.schema.parser.SchemaParserTest.DuplicateWidgetTagA, app.cash.redwood.schema.parser.SchemaParserTest.DuplicateWidgetTagB
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

  @LayoutModifier(1)
  data class DuplicateLayoutModifierTagA(
    val name: String,
  )

  @LayoutModifier(2)
  data class NonDuplicateLayoutModifierTag(
    val name: String,
  )

  @LayoutModifier(1)
  data class DuplicateLayoutModifierTagB(
    val name: String,
  )

  @Test fun duplicateLayoutModifierTagThrows() {
    assertThrows<IllegalArgumentException> {
      parseSchema(DuplicateLayoutModifierTagSchema::class)
    }.hasMessageThat().isEqualTo(
      """
      |Schema @LayoutModifier tags must be unique
      |
      |- @LayoutModifier(1): app.cash.redwood.schema.parser.SchemaParserTest.DuplicateLayoutModifierTagA, app.cash.redwood.schema.parser.SchemaParserTest.DuplicateLayoutModifierTagB
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
    assertThrows<IllegalArgumentException> {
      parseSchema(RepeatedWidgetTypeSchema::class)
    }.hasMessageThat().isEqualTo(
      """
      |Schema contains repeated member
      |
      |- app.cash.redwood.schema.parser.SchemaParserTest.RepeatedWidget
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
    assertThrows<IllegalArgumentException> {
      parseSchema(DuplicatePropertyTagSchema::class)
    }.hasMessageThat().isEqualTo(
      """
      |app.cash.redwood.schema.parser.SchemaParserTest.DuplicatePropertyTagWidget's @Property tags must be unique
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
    assertThrows<IllegalArgumentException> {
      parseSchema(DuplicateChildrenTagSchema::class)
    }.hasMessageThat().isEqualTo(
      """
      |app.cash.redwood.schema.parser.SchemaParserTest.DuplicateChildrenTagWidget's @Children tags must be unique
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
    assertThrows<IllegalArgumentException> {
      parseSchema(UnannotatedPrimaryParameterSchema::class)
    }.hasMessageThat().isEqualTo(
      "Unannotated parameter \"unannotated\" on app.cash.redwood.schema.parser.SchemaParserTest.UnannotatedPrimaryParameterWidget",
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
    assertThrows<IllegalArgumentException> {
      parseSchema(NonDataClassWidgetSchema::class)
    }.hasMessageThat().isEqualTo(
      "@Widget app.cash.redwood.schema.parser.SchemaParserTest.NonDataClassWidget must be 'data' class or 'object'",
    )
  }

  @Schema(
    [
      NonDataClassLayoutModifier::class,
    ],
  )
  interface NonDataClassLayoutModifierSchema

  @LayoutModifier(1)
  class NonDataClassLayoutModifier(
    @Property(1) val name: String,
  )

  @Test fun nonDataClassLayoutModifierThrows() {
    assertThrows<IllegalArgumentException> {
      parseSchema(NonDataClassLayoutModifierSchema::class)
    }.hasMessageThat().isEqualTo(
      "@LayoutModifier app.cash.redwood.schema.parser.SchemaParserTest.NonDataClassLayoutModifier must be 'data' class or 'object'",
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
    assertThrows<IllegalArgumentException> {
      parseSchema(InvalidChildrenTypeSchema::class)
    }.hasMessageThat().isEqualTo(
      "@Children app.cash.redwood.schema.parser.SchemaParserTest.InvalidChildrenTypeWidget#children must be of type '() -> Unit'",
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
    assertThrows<IllegalArgumentException> {
      parseSchema(ChildrenArgumentsInvalidSchema::class)
    }.hasMessageThat().isEqualTo(
      "@Children app.cash.redwood.schema.parser.SchemaParserTest.ChildrenArgumentsInvalidWidget#children lambda type must not have any arguments. " +
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
    assertThrows<IllegalArgumentException> {
      parseSchema(ScopedChildrenArgumentsInvalidSchema::class)
    }.hasMessageThat().isEqualTo(
      "@Children app.cash.redwood.schema.parser.SchemaParserTest.ScopedChildrenArgumentsInvalidWidget#children lambda type must not have any arguments. " +
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
    assertThrows<IllegalArgumentException> {
      parseSchema(ScopedChildrenInvalidSchema::class)
    }.hasMessageThat().isEqualTo(
      "@Children app.cash.redwood.schema.parser.SchemaParserTest.ScopedChildrenInvalidWidget#children lambda receiver can only be a class. " +
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
    assertThrows<IllegalArgumentException> {
      parseSchema(ScopedChildrenTypeParameterInvalidSchema::class)
    }.hasMessageThat().isEqualTo(
      "@Children app.cash.redwood.schema.parser.SchemaParserTest.ScopedChildrenTypeParameterInvalidWidget#children lambda receiver can only be a class. " +
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
    val schema = parseSchema(EventTypeSchema::class)
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
    val schema = parseSchema(EventArgumentsSchema::class)
    val widget = schema.widgets.single()

    val noArguments = widget.traits.single { it.name == "noArguments" } as Event
    assertThat(noArguments.parameterType).isNull()
    val argument = widget.traits.single { it.name == "argument" } as Event
    assertThat(argument.parameterType).isEqualTo(String::class.createType())
    val argumentOptionalLambda = widget.traits.single { it.name == "argumentOptionalLambda" } as Event
    assertThat(argumentOptionalLambda.parameterType).isEqualTo(String::class.createType())
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
    assertThrows<IllegalArgumentException> {
      parseSchema(EventArgumentsInvalidSchema::class)
    }.hasMessageThat().isEqualTo(
      "@Property app.cash.redwood.schema.parser.SchemaParserTest.EventArgumentsInvalidWidget#tooManyArguments lambda type can only have zero or one arguments. " +
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
    val schema = parseSchema(ObjectSchema::class)
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
    assertThrows<IllegalArgumentException> {
      parseSchema(OneMillionWidgetSchema::class)
    }.hasMessageThat().isEqualTo(
      "@Widget app.cash.redwood.schema.parser.SchemaParserTest.OneMillionWidget " +
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
    assertThrows<IllegalArgumentException> {
      parseSchema(ZeroWidgetSchema::class)
    }.hasMessageThat().isEqualTo(
      "@Widget app.cash.redwood.schema.parser.SchemaParserTest.ZeroWidget " +
        "tag must be in range [1, 1000000): 0",
    )
  }

  @Schema(
    [
      OneMillionLayoutModifier::class,
    ],
  )
  interface OneMillionLayoutModifierSchema

  @LayoutModifier(1_000_000)
  data class OneMillionLayoutModifier(
    val value: Int,
  )

  @Test fun layoutModifierTagOneMillionThrows() {
    assertThrows<IllegalArgumentException> {
      parseSchema(OneMillionLayoutModifierSchema::class)
    }.hasMessageThat().isEqualTo(
      "@LayoutModifier app.cash.redwood.schema.parser.SchemaParserTest.OneMillionLayoutModifier " +
        "tag must be in range [1, 1000000): 1000000",
    )
  }

  @Schema(
    [
      ZeroLayoutModifier::class,
    ],
  )
  interface ZeroLayoutModifierSchema

  @LayoutModifier(0)
  data class ZeroLayoutModifier(
    val value: Int,
  )

  @Test fun layoutModifierTagZeroThrows() {
    assertThrows<IllegalArgumentException> {
      parseSchema(ZeroLayoutModifierSchema::class)
    }.hasMessageThat().isEqualTo(
      "@LayoutModifier app.cash.redwood.schema.parser.SchemaParserTest.ZeroLayoutModifier " +
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

  @LayoutModifier(1)
  data class SomeLayoutModifier(
    val value: Int,
  )

  @Test fun schemaTagDefault() {
    val schema = parseSchema(SchemaTag::class)

    val widget = schema.widgets.single()
    assertThat(widget.tag).isEqualTo(1U)
    assertThat(widget.traits[0].tag).isEqualTo(1U)
    assertThat(widget.traits[1].tag).isEqualTo(1U)

    val layoutModifier = schema.layoutModifiers.single()
    assertThat(layoutModifier.tag).isEqualTo(1U)
  }

  @Schema(
    members = [],
    dependencies = [
      Dependency(4, SchemaTag::class),
    ],
  )
  object SchemaDependencyTagOffsetsMemberTags

  @Test fun schemaTagOffsetsMemberTags() {
    val schema = parseSchema(SchemaDependencyTagOffsetsMemberTags::class)
    val dependency = schema.dependencies.single()

    val widget = dependency.widgets.single()
    assertThat(widget.tag).isEqualTo(4_000_001U)
    assertThat(widget.traits[0].tag).isEqualTo(1U)
    assertThat(widget.traits[1].tag).isEqualTo(1U)

    val layoutModifier = dependency.layoutModifiers.single()
    assertThat(layoutModifier.tag).isEqualTo(4_000_001U)
  }

  @Test fun schemaTagOutOfRangeThrows() {
    assertThrows<IllegalArgumentException> {
      parseSchema(SchemaTag::class, tag = 4001U)
    }.hasMessageThat().isEqualTo("Schema tag must be in range [0, 4000]: 4001")
  }

  @Schema(
    members = [],
    dependencies = [
      Dependency(4001, SchemaTag::class),
    ],
  )
  object SchemaDependencyTagTooHigh

  @Schema(
    members = [],
    dependencies = [
      Dependency(0, SchemaTag::class),
    ],
  )
  object SchemaDependencyTagTooLow

  @Test fun dependencyTagOutOfRangeThrows() {
    assertThrows<IllegalArgumentException> {
      parseSchema(SchemaDependencyTagTooHigh::class)
    }.hasMessageThat().isEqualTo(
      "Dependency app.cash.redwood.schema.parser.SchemaParserTest.SchemaTag tag must be in range (0, 4000]: 4001",
    )

    assertThrows<IllegalArgumentException> {
      parseSchema(SchemaDependencyTagTooLow::class)
    }.hasMessageThat().isEqualTo(
      "Dependency app.cash.redwood.schema.parser.SchemaParserTest.SchemaTag tag must be in range (0, 4000]: 0",
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
    assertThrows<IllegalArgumentException> {
      parseSchema(SchemaDuplicateDependencyTag::class)
    }.hasMessageThat().isEqualTo(
      """
      |Schema dependency tags must be unique
      |
      |- Dependency tag 1: app.cash.redwood.schema.parser.SchemaParserTest.SchemaDuplicateDependencyTagA, app.cash.redwood.schema.parser.SchemaParserTest.SchemaDuplicateDependencyTagB
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
    assertThrows<IllegalArgumentException> {
      parseSchema(SchemaDuplicateDependencyType::class)
    }.hasMessageThat().isEqualTo(
      """
      |Schema contains repeated dependency
      |
      |- app.cash.redwood.schema.parser.SchemaParserTest.SchemaDuplicateDependencyTypeOther
      """.trimMargin(),
    )
  }

  @Schema(
    members = [],
    dependencies = [
      Dependency(1, SchemaDependencyHasDependencyA::class),
    ],
  )
  object SchemaDependencyHasDependency

  @Schema(
    members = [],
    dependencies = [
      Dependency(2, SchemaDependencyHasDependencyB::class),
    ],
  )
  object SchemaDependencyHasDependencyA

  @Schema(members = [])
  object SchemaDependencyHasDependencyB

  @Test fun schemaDependencyHasDependencyThrows() {
    assertThrows<IllegalArgumentException> {
      parseSchema(SchemaDependencyHasDependency::class)
    }.hasMessageThat().isEqualTo(
      "Schema dependency app.cash.redwood.schema.parser.SchemaParserTest.SchemaDependencyHasDependencyA also has its own dependencies. " +
        "For now, only a single level of dependencies is supported.",
    )
  }

  @Schema(
    members = [],
    dependencies = [
      Dependency(1, SchemaWidgetDuplicateAcrossDependenciesA::class),
      Dependency(2, SchemaWidgetDuplicateAcrossDependenciesB::class),
    ],
  )
  object SchemaWidgetDuplicateAcrossDependencies

  @Widget(1)
  object WidgetDuplicateAcrossDependencies

  @Schema([WidgetDuplicateAcrossDependencies::class])
  object SchemaWidgetDuplicateAcrossDependenciesA

  @Schema([WidgetDuplicateAcrossDependencies::class])
  object SchemaWidgetDuplicateAcrossDependenciesB

  @Test fun schemaWidgetDuplicateAcrossDependenciesThrows() {
    assertThrows<IllegalArgumentException> {
      parseSchema(SchemaWidgetDuplicateAcrossDependencies::class)
    }.hasMessageThat().isEqualTo(
      """
      |Schema dependency tree contains duplicated widgets
      |
      |- app.cash.redwood.schema.parser.SchemaParserTest.WidgetDuplicateAcrossDependencies: app.cash.redwood.schema.parser.SchemaWidgetDuplicateAcrossDependenciesA, app.cash.redwood.schema.parser.SchemaWidgetDuplicateAcrossDependenciesB
      """.trimMargin(),
    )
  }

  @Schema(
    members = [
      WidgetDuplicateAcrossDependencies::class,
    ],
    dependencies = [
      Dependency(1, SchemaWidgetDuplicateAcrossDependenciesA::class),
    ],
  )
  object SchemaWidgetDuplicateInDependency

  @Test fun schemaWidgetDuplicateInDependencyThrows() {
    assertThrows<IllegalArgumentException> {
      parseSchema(SchemaWidgetDuplicateInDependency::class)
    }.hasMessageThat().isEqualTo(
      """
      |Schema dependency tree contains duplicated widgets
      |
      |- app.cash.redwood.schema.parser.SchemaParserTest.WidgetDuplicateAcrossDependencies: app.cash.redwood.schema.parser.SchemaWidgetDuplicateInDependency, app.cash.redwood.schema.parser.SchemaWidgetDuplicateAcrossDependenciesA
      """.trimMargin(),
    )
  }
}
