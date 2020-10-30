package app.cash.treehouse.schema.generator

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
import com.squareup.kotlinpoet.jvm.jvmField

/*
@Composable
fun TreehouseScope.Button(
  text: String,
  enabled: Boolean = true,
  onClick: (() -> Unit)? = null,
) {
  emit<ButtonNode, Applier<Node>>({ ButtonNode(nextId()) }) {
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
fun generateServerNode(schema: Schema, node: Node): FileSpec {
  val events = node.traits.filterIsInstance<Event>()
  val nodeType = if (events.isEmpty()) {
    serverNode
  } else {
    schema.serverNodeType(node)
  }
  val nodeInitializer = if (events.isEmpty()) {
    CodeBlock.of("{ %T(nextId(), %L) }", nodeType, node.tag)
  } else {
    CodeBlock.of("{ %T(nextId()) }", nodeType)
  }
  val applierOfServerNode = applier.parameterizedBy(serverNode)
  return FileSpec.builder(schema.serverPackage, node.name)
    .addFunction(FunSpec.builder(node.name)
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
          })
        }
      }
      .beginControlFlow("%M<%T, %T>(%L)", emitReference, nodeType, applierOfServerNode, nodeInitializer)
      .apply {
        for (trait in node.traits) {
          beginControlFlow("set(%N)", trait.name)
          when (trait) {
            is Property -> {
              addStatement("appendDiff(%T(id, %L, %N))", propertyDiff, trait.tag, trait.name)
            }
            is Event -> {
              addStatement("this.%1N = %1N", trait.name)
              addStatement("appendDiff(%T(id, %L, %N != null))", propertyDiff, trait.tag, trait.name)
            }
          }
          endControlFlow()
        }
      }
      .endControlFlow()
      .build())
    .apply {
      if (events.isNotEmpty()) {
        addType(TypeSpec.classBuilder(nodeType)
          .addModifiers(PRIVATE)
          .primaryConstructor(FunSpec.constructorBuilder()
            .addParameter("id", LONG)
            .build())
          .superclass(serverNode)
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
