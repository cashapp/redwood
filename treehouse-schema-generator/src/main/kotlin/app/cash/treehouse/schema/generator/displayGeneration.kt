package app.cash.treehouse.schema.generator

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
import com.squareup.kotlinpoet.joinToCode

/*
interface SunspotNodeFactory<T : Any> : TreeNodeFactory<T> {
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
fun generateDisplayNodeFactory(schema: Schema): FileSpec {
  val typeVariableT = TypeVariableName("T", listOf(ANY))
  return FileSpec.builder(schema.displayPackage, schema.nodeFactoryType.simpleName)
    .addType(TypeSpec.interfaceBuilder(schema.nodeFactoryType)
      .addModifiers(PUBLIC)
      .addTypeVariable(typeVariableT)
      .addSuperinterface(treeNodeFactory.parameterizedBy(typeVariableT))
      .apply {
        for (node in schema.nodes) {
          addFunction(FunSpec.builder(node.flatName)
            .addModifiers(PUBLIC, ABSTRACT)
            .addParameter("parent", typeVariableT)
            .apply {
              for (event in node.traits.filterIsInstance<Event>()) {
                addParameter(event.name, LambdaTypeName.get(returnType = UNIT))
              }
            }
            .returns(schema.displayNodeType(node).parameterizedBy(typeVariableT))
            .build())
        }
      }
      .addFunction(FunSpec.builder("create")
        .addModifiers(PUBLIC, OVERRIDE)
        .addParameter("parent", typeVariableT)
        .addParameter("kind", INT)
        .addParameter("id", LONG)
        .addParameter("events", eventSink)
        .returns(treeNode.parameterizedBy(typeVariableT))
        .beginControlFlow("return when (kind)")
        .apply {
          for (node in schema.nodes) {
            val factoryArguments = mutableListOf(CodeBlock.of("parent"))
            for (event in node.traits.filterIsInstance<Event>()) {
              factoryArguments += CodeBlock.of("{ events.send(%T(id, %L, null)) }", eventType, event.tag)
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
fun generateDisplayNode(schema: Schema, node: Node): FileSpec {
  val typeVariableT = TypeVariableName("T", listOf(ANY))
  val childrenOfT = treeNodeChildren.parameterizedBy(typeVariableT)
  return FileSpec.builder(schema.displayPackage, node.flatName)
    .addType(TypeSpec.interfaceBuilder(node.flatName)
      .addModifiers(PUBLIC)
      .addTypeVariable(typeVariableT)
      .addSuperinterface(treeNode.parameterizedBy(typeVariableT))
      .apply {
        for (trait in node.traits) {
          when (trait) {
            is Property -> {
              addFunction(FunSpec.builder(trait.name)
                .addModifiers(PUBLIC, ABSTRACT)
                .addParameter(trait.name, trait.type)
                .build())
            }
            is Event -> {
              addFunction(FunSpec.builder(trait.name)
                .addModifiers(PUBLIC, ABSTRACT)
                .addParameter(trait.name, BOOLEAN)
                .build())
            }
            is Children -> {
              addProperty(PropertySpec.builder("children", childrenOfT)
                .addModifiers(PUBLIC, ABSTRACT, OVERRIDE)
                .build())
            }
          }
        }
      }
      .addFunction(FunSpec.builder("apply")
        .addModifiers(PUBLIC, OVERRIDE)
        .addParameter("diff", propertyDiff)
        .beginControlFlow("when (val tag = diff.tag)")
        .apply {
          for (trait in node.traits) {
            val type = when (trait) {
              is Property -> trait.type
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
