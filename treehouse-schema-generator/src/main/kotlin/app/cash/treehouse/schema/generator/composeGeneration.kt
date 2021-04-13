package app.cash.treehouse.schema.generator

import app.cash.exhaustive.Exhaustive
import app.cash.treehouse.schema.parser.Children
import app.cash.treehouse.schema.parser.Event
import app.cash.treehouse.schema.parser.Property
import app.cash.treehouse.schema.parser.Schema
import app.cash.treehouse.schema.parser.Widget
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.OVERRIDE
import com.squareup.kotlinpoet.KModifier.PRIVATE
import com.squareup.kotlinpoet.KModifier.PUBLIC
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.joinToCode
import com.squareup.kotlinpoet.jvm.jvmField

/*
@Composable
fun TreehouseScope.Button(
  text: String,
  enabled: Boolean = true,
  onClick: (() -> Unit)? = null,
) {
  ComposeNode<ButtonComposeNode, Applier<Node>>({ ButtonNode(nextId()) }) {
    set(text) {
      appendDiff(PropertyDiff(id, 1 /* text */, text))
    }
    set(enabled) {
      appendDiff(PropertyDiff(id, 2 /* enabled */, enabled))
    }
    set(onClick) {
      this.onClick = onClick
      appendDiff(PropertyDiff(id, 3 /* onClick */, onClick != null))
    }
  }
}

private class ButtonComposeNode(id: Long) : Node(id, 2) {
  var onClick: (() -> Unit)? = null

  override fun sendEvent(event: Event) {
    when (event.eventId) {
      1L -> onClick?.invoke()
      else -> throw IllegalArgumentException("Unknown event ID ${event.eventId}")
    }
  }
}
*/
internal fun generateComposeNode(schema: Schema, widget: Widget): FileSpec {
  val events = widget.traits.filterIsInstance<Event>()
  val nodeType = if (events.isEmpty()) {
    composeNode
  } else {
    schema.composeNodeType(widget)
  }
  val applierOfServerNode = applier.parameterizedBy(composeNode)
  return FileSpec.builder(schema.composePackage, widget.flatName)
    .addFunction(
      FunSpec.builder(widget.flatName)
        .addModifiers(PUBLIC)
        .addAnnotation(composable)
        .receiver(treehouseScope)
        .apply {
          for (trait in widget.traits) {
            addParameter(
              when (trait) {
                is Property -> {
                  ParameterSpec.builder(trait.name, trait.type.asTypeName())
                    .apply {
                      trait.defaultExpression?.let { defaultValue(it) }
                    }
                    .build()
                }
                is Event -> {
                  ParameterSpec.builder(trait.name, eventLambda.copy(nullable = true))
                    .defaultValue("null")
                    .build()
                }
                is Children -> {
                  ParameterSpec.builder(trait.name, composableLambda)
                    .build()
                }
              }
            )
          }

          val arguments = mutableListOf<CodeBlock>()

          arguments += CodeBlock.builder()
            .add("factory = {\n")
            .indent()
            .apply {
              if (events.isEmpty()) {
                add("%T(nextId(), %L)\n", nodeType, widget.tag)
              } else {
                add("%T(nextId())\n", nodeType)
              }
            }
            .unindent()
            .add("}")
            .build()

          val updateLambda = CodeBlock.builder()
          val childrenLambda = CodeBlock.builder()
          for (trait in widget.traits) {
            @Exhaustive when (trait) {
              is Property -> {
                updateLambda.apply {
                  add("set(%N) {\n", trait.name)
                  indent()
                  add("appendDiff(%T(this.id, %L, %N))\n", propertyDiff, trait.tag, trait.name)
                  unindent()
                  add("}\n")
                }
              }
              is Event -> {
                updateLambda.apply {
                  add("set(%N) {\n", trait.name)
                  indent()
                  add("this.%1N = %1N\n", trait.name)
                  add("appendDiff(%T(this.id, %L, %N != null))\n", propertyDiff, trait.tag, trait.name)
                  unindent()
                  add("}\n")
                }
              }
              is Children -> {
                childrenLambda.apply {
                  add("%M(%L) {\n", syntheticChildren, trait.tag)
                  indent()
                  add("%N()\n", trait.name)
                  unindent()
                  add("}\n")
                }
              }
            }
          }

          arguments += CodeBlock.builder()
            .add("update = {\n")
            .indent()
            .add(updateLambda.build())
            .unindent()
            .add("}")
            .build()

          if (childrenLambda.isNotEmpty()) {
            arguments += CodeBlock.builder()
              .add("content = {\n")
              .indent()
              .add(childrenLambda.build())
              .unindent()
              .add("}")
              .build()
          }

          addStatement(
            "%M<%T, %T>(%L)", composeNodeReference, nodeType, applierOfServerNode,
            arguments.joinToCode(",\n", "\n", ",\n")
          )
        }
        .build()
    )
    .apply {
      if (events.isNotEmpty()) {
        addType(
          TypeSpec.classBuilder(nodeType)
            .addModifiers(PRIVATE)
            .primaryConstructor(
              FunSpec.constructorBuilder()
                .addParameter("id", LONG)
                .build()
            )
            .superclass(composeNode)
            .addSuperclassConstructorParameter("id")
            .addSuperclassConstructorParameter("%L", widget.tag)
            .apply {
              for (event in events) {
                addProperty(
                  PropertySpec.builder(event.name, eventLambda.copy(nullable = true))
                    .mutable(true)
                    .initializer("null")
                    .jvmField() // Method count optimization as this is implementation detail.
                    .build()
                )
              }
            }
            .addFunction(
              FunSpec.builder("sendEvent")
                .addModifiers(PUBLIC, OVERRIDE)
                .addParameter("event", eventType)
                .beginControlFlow("when (val tag = event.tag)")
                .apply {
                  for (event in events) {
                    addStatement("%L -> %N?.invoke()", event.tag, event.name)
                  }
                }
                .addStatement("else -> throw %T(\"Unknown tag \$tag\")", iae)
                .endControlFlow()
                .build()
            )
            .build()
        )
      }
    }
    .build()
}
