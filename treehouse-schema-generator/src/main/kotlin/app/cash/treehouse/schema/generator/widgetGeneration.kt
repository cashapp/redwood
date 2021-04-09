package app.cash.treehouse.schema.generator

import app.cash.treehouse.schema.parser.Children
import app.cash.treehouse.schema.parser.Event
import app.cash.treehouse.schema.parser.Property
import app.cash.treehouse.schema.parser.Schema
import app.cash.treehouse.schema.parser.Widget
import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.KModifier.ABSTRACT
import com.squareup.kotlinpoet.KModifier.OVERRIDE
import com.squareup.kotlinpoet.KModifier.PUBLIC
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.UNIT
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.joinToCode

/*
interface SunspotWidgetFactory<T : Any> : Widget.Factory<T> {
  fun SunspotText(parent: TreeNode<T>): SunspotText<T>
  fun SunspotButton(parent: TreeNode<T>, onClick: () -> Unit): SunspotButton<T>

  @Suppress("MoveLambdaOutsideParentheses") // Generated code ergo not important.
  override fun create(parent: TreeNode<T>, kind: Int, id: Long, events: EventSink): TreeNode<T> {
    return when (kind) {
      1 -> SunspotText(parent)
      2 -> SunspotButton(parent, { events.send(Event(id, 3, null)) })
      else -> throw IllegalArgumentException("Unknown kind $kind")
    }
  }
}
*/
fun generateWidgetFactory(schema: Schema): FileSpec {
  val typeVariableT = TypeVariableName("T", listOf(ANY))
  return FileSpec.builder(schema.displayPackage, schema.getWidgetFactoryType().simpleName)
    .addType(TypeSpec.interfaceBuilder(schema.getWidgetFactoryType())
      .addModifiers(PUBLIC)
      .addTypeVariable(typeVariableT)
      .addSuperinterface(widgetFactory.parameterizedBy(typeVariableT))
      .apply {
        for (node in schema.widgets) {
          addFunction(FunSpec.builder(node.flatName)
            .addModifiers(PUBLIC, ABSTRACT)
            .addParameter("parent", typeVariableT)
            .apply {
              for (event in node.traits.filterIsInstance<Event>()) {
                addParameter(event.name, LambdaTypeName.get(returnType = UNIT))
              }
            }
            .returns(schema.widgetType(node).parameterizedBy(typeVariableT))
            .build())
        }
      }
      .addFunction(FunSpec.builder("create")
        .addModifiers(PUBLIC, OVERRIDE)
        .addParameter("parent", typeVariableT)
        .addParameter("kind", INT)
        .addParameter("id", LONG)
        .addParameter("events", eventSink)
        .returns(widget.parameterizedBy(typeVariableT))
        .beginControlFlow("return when (kind)")
        .apply {
          for (node in schema.widgets.sortedBy { it.tag }) {
            val factoryArguments = mutableListOf(CodeBlock.of("parent"))
            for (event in node.traits.filterIsInstance<Event>()) {
              factoryArguments += CodeBlock.of("{ events(%T(id, %L, null)) }", eventType, event.tag)
            }
            addStatement("%L -> %N(%L)", node.tag, node.flatName, factoryArguments.joinToCode())
          }
        }
        .addStatement("else -> throw %T(\"Unknown kind \$kind\")", iae)
        .endControlFlow()
        .build())
      .build())
    .build()
}

/*
interface SunspotButton<out T: Any> : SunspotNode<T> {
  fun text(text: String?)
  fun enabled(enabled: Boolean)
  fun onClick(onClick: Boolean)

  override fun apply(diff: PropertyDiff) {
    when (val tag = diff.tag) {
      1 -> text(diff.value as String?)
      2 -> enabled(diff.value as Boolean)
      3 -> onClick(diff.value as Boolean)
      else -> throw IllegalArgumentException("Unknown tag $tag")
    }
  }
}
*/
fun generateWidget(schema: Schema, widget: Widget): FileSpec {
  val typeVariableT = TypeVariableName("T", listOf(ANY))
  val childrenOfT = widgetChildren.parameterizedBy(typeVariableT)
  return FileSpec.builder(schema.displayPackage, widget.flatName)
    .addType(TypeSpec.interfaceBuilder(widget.flatName)
      .addModifiers(PUBLIC)
      .addTypeVariable(typeVariableT)
      .addSuperinterface(app.cash.treehouse.schema.generator.widget.parameterizedBy(typeVariableT))
      .apply {
        for (trait in widget.traits) {
          when (trait) {
            is Property -> {
              addFunction(FunSpec.builder(trait.name)
                .addModifiers(PUBLIC, ABSTRACT)
                .addParameter(trait.name, trait.type.asTypeName())
                .build())
            }
            is Event -> {
              addFunction(FunSpec.builder(trait.name)
                .addModifiers(PUBLIC, ABSTRACT)
                .addParameter(trait.name, BOOLEAN)
                .build())
            }
            is Children -> {
              addProperty(PropertySpec.builder(trait.name, childrenOfT)
                .addModifiers(PUBLIC, ABSTRACT)
                .build())
            }
          }
        }
        val childrens = widget.traits.filterIsInstance<Children>()
        if (childrens.isNotEmpty()) {
          addFunction(FunSpec.builder("children")
            .addModifiers(PUBLIC, OVERRIDE)
            .addParameter("tag", INT)
            .returns(childrenOfT)
            .beginControlFlow("return when (tag)")
            .apply {
              for (children in childrens) {
                addStatement("%L -> %N", children.tag, children.name)
              }
            }
            .addStatement("else -> throw %T(\"Unknown tag \$tag\")", iae)
            .endControlFlow()
            .build())
        }
      }
      .addFunction(FunSpec.builder("apply")
        .addModifiers(PUBLIC, OVERRIDE)
        .addParameter("diff", propertyDiff)
        .beginControlFlow("when (val tag = diff.tag)")
        .apply {
          for (trait in widget.traits) {
            val type = when (trait) {
              is Property -> trait.type.asTypeName()
              is Event -> BOOLEAN
              is Children -> continue
            }
            addStatement("%L -> %N(diff.value as %T)", trait.tag, trait.name, type)
          }
        }
        .addStatement("else -> throw %T(\"Unknown tag \$tag\")", iae)
        .endControlFlow()
        .build())
      .build())
    .build()
}
