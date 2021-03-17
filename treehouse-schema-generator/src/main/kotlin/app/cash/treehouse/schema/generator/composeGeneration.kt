package app.cash.treehouse.schema.generator

import app.cash.exhaustive.Exhaustive
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.OVERRIDE
import com.squareup.kotlinpoet.KModifier.PRIVATE
import com.squareup.kotlinpoet.KModifier.PUBLIC
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.UNIT
import com.squareup.kotlinpoet.joinToCode
import com.squareup.kotlinpoet.jvm.jvmField

/*
@Composable
fun TreehouseScope.Button(
  text: String,
  enabled: Boolean = true,
  onClick: (() -> Unit)? = null,
) {
  ComposeNode<ButtonNode, Applier<Node>>({ ButtonNode(nextId()) }) {
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

private class ButtonNode(id: Long) : Node(id, 2) {
  var onClick: (() -> Unit)? = null

  override fun sendEvent(event: Event) {
    when (event.eventId) {
      1L -> onClick?.invoke()
      else -> throw IllegalArgumentException("Unknown event ID ${event.eventId}")
    }
  }
}
*/
fun generateComposeNode(schema: Schema, node: Node): FileSpec {
  val events = node.traits.filterIsInstance<Event>()
  val nodeType = if (events.isEmpty()) {
    composeNode
  } else {
    schema.composeNodeType(node)
  }
  val applierOfServerNode = applier.parameterizedBy(composeNode)
  return FileSpec.builder(schema.composePackage, node.flatName)
    .addFunction(FunSpec.builder(node.flatName)
      .addModifiers(PUBLIC)
      .addAnnotation(composable)
      .receiver(treehouseScope)
      .apply {
        for (trait in node.traits) {
          addParameter(when (trait) {
            is Property -> {
              ParameterSpec.builder(trait.name, trait.type)
                .apply {
                  trait.defaultExpression?.let { defaultValue(it) }
                }
                .build()
            }
            is Event -> {
              val type = LambdaTypeName.get(returnType = UNIT).copy(nullable = true)
              ParameterSpec.builder(trait.name, type)
                .defaultValue("null")
                .build()
            }
            is Children -> {
              val type = LambdaTypeName.get(returnType = UNIT)
                .copy(annotations = listOf(
                  AnnotationSpec.builder(composable).build(),
                ))
              ParameterSpec.builder(trait.name, type)
                .build()
            }
          })
        }

        val arguments = mutableListOf<CodeBlock>()

        // ctor
        arguments += CodeBlock.builder()
          .add("factory = {\n")
          .indent()
          .apply {
            if (events.isEmpty()) {
              add("%T(nextId(), %L)\n", nodeType, node.tag)
            } else {
              add("%T(nextId())\n", nodeType)
            }
          }
          .unindent()
          .add("}")
          .build()

        // update
        arguments += CodeBlock.builder()
          .add("update = {\n")
          .indent()
          .apply {
            for (trait in node.traits) {
              @Exhaustive when (trait) {
                is Property -> {
                  add("set(%N) {\n", trait.name)
                  indent()
                  add("appendDiff(%T(this.id, %L, %N))\n", propertyDiff, trait.tag, trait.name)
                  unindent()
                  add("}\n")
                }
                is Event -> {
                  add("set(%N) {\n", trait.name)
                  indent()
                  add("this.%1N = %1N\n", trait.name)
                  add("appendDiff(%T(this.id, %L, %N != null))\n", propertyDiff, trait.tag, trait.name)
                  unindent()
                  add("}\n")
                }
                is Children -> {
                  // No-op
                }
              }
            }

          }
          .unindent()
          .add("}")
          .build()

        // children
        if (node.traits.any { it is Children }) {
          val children = node.traits.single { it is Children }
          arguments += CodeBlock.builder()
            .add("content = {\n")
            .indent()
            .add("%N()\n", children.name)
            .unindent()
            .add("}")
            .build()
        }

        addStatement("%M<%T, %T>(%L)", composeNodeReference, nodeType, applierOfServerNode,
          arguments.joinToCode(",\n", "\n", ",\n"))
      }
      .build())
    .apply {
      if (events.isNotEmpty()) {
        addType(TypeSpec.classBuilder(nodeType)
          .addModifiers(PRIVATE)
          .primaryConstructor(FunSpec.constructorBuilder()
            .addParameter("id", LONG)
            .build())
          .superclass(composeNode)
          .addSuperclassConstructorParameter("id")
          .addSuperclassConstructorParameter("%L", node.tag)
          .apply {
            for (event in events) {
              val type = LambdaTypeName.get(returnType = UNIT).copy(nullable = true)
              addProperty(PropertySpec.builder(event.name, type)
                .mutable(true)
                .initializer("null")
                .jvmField() // Method count optimization as this is implementation detail.
                .build())
            }
          }
          .addFunction(FunSpec.builder("sendEvent")
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
            .build())
          .build())
      }
    }
    .build()
}

/*
@Composable
fun TreehouseScope.Text(
  text: String,
  color: String? = "black",
) {
  emit<Node, Applier<Node>>({ Node(nextId(), 1) }) {
    set(text) {
      appendDiff(PropertyDiff(id, 1 /* text */, text))
    }
    set(color) {
      appendDiff(PropertyDiff(id, 2 /* color */, color))
    }
  }
}
*/
