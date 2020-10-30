package app.cash.treehouse.schema.generator

import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.ABSTRACT
import com.squareup.kotlinpoet.KModifier.OUT
import com.squareup.kotlinpoet.KModifier.OVERRIDE
import com.squareup.kotlinpoet.KModifier.PRIVATE
import com.squareup.kotlinpoet.KModifier.PUBLIC
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.UNIT
import com.squareup.kotlinpoet.joinToCode

private val eventSink = ClassName("app.cash.treehouse.client", "EventSink")
private val treeBridge = ClassName("app.cash.treehouse.client", "TreeBridge")
private val treeMutator = ClassName("app.cash.treehouse.client", "TreeMutator")
private val treeNode = ClassName("app.cash.treehouse.client", "TreeNode")
private val eventType = ClassName("app.cash.treehouse.protocol", "Event")
private val nodeDiff = ClassName("app.cash.treehouse.protocol", "NodeDiff")
private val nodeDiffInsert = nodeDiff.nestedClass("Insert")
private val nodeDiffMove = nodeDiff.nestedClass("Move")
private val nodeDiffRemove = nodeDiff.nestedClass("Remove")
private val propertyDiff = ClassName("app.cash.treehouse.protocol", "PropertyDiff")
private val iae = ClassName("kotlin", "IllegalArgumentException")

/*
interface SunspotNode<out T : Any> : TreeNode {
  val value: T
}

interface SunspotNodeFactory<T : Any> {
  fun text(parent: SunspotNode<T>): SunspotText<T>
  fun button(parent: SunspotNode<T>, onClick: () -> Unit): SunspotButton<T>
}

@Suppress("MoveLambdaOutsideParentheses") // Generated code ergo not important.
class SunspotBridge<N : Any>(
  override val root: SunspotNode<N>,
  private val factory: SunspotNodeFactory<N>,
  private val mutator: TreeMutator<N>,
) : TreeBridge<SunspotNode<N>> {
  override fun insert(
    parent: SunspotNode<N>,
    insert: NodeDiff.Insert,
    events: EventSink,
  ): SunspotNode<N> {
    val id = insert.childId
    val node = when (val type = insert.kind) {
      1 -> factory.text(parent)
      2 -> factory.button(parent, { events.send(Event(id, 1L /* click */, null)) })
      else -> throw IllegalArgumentException("Unknown type $type")
    }
    mutator.insert(parent.value, insert.index, node.value)
    return node
  }

  override fun move(parent: SunspotNode<N>, move: NodeDiff.Move) {
    mutator.move(parent.value, move.fromIndex, move.toIndex, move.count)
  }

  override fun remove(parent: SunspotNode<N>, remove: NodeDiff.Remove) {
    mutator.remove(parent.value, remove.index, remove.count)
  }

  override fun clear() {
    mutator.clear(root.value)
  }
}

*/
fun generateClientNodeFactory(schema: Schema): FileSpec {
  val typeVariableOutT = TypeVariableName("T", listOf(ANY), OUT)
  val typeVariableT = TypeVariableName("T", listOf(ANY))
  val baseNodeTypeOfT = schema.baseNodeType.parameterizedBy(typeVariableT)
  val nodeFactoryOfT = schema.nodeFactoryType.parameterizedBy(typeVariableT)
  val treeMutatorOfT = treeMutator.parameterizedBy(typeVariableT)
  return FileSpec.builder(schema.clientPackage, schema.name)
    .addType(TypeSpec.interfaceBuilder(schema.baseNodeType)
      .addModifiers(PUBLIC)
      .addTypeVariable(typeVariableOutT)
      .addSuperinterface(treeNode)
      .addProperty("value", typeVariableOutT, PUBLIC, ABSTRACT)
      .build())
    .addType(TypeSpec.interfaceBuilder(schema.nodeFactoryType)
      .addModifiers(PUBLIC)
      .addTypeVariable(typeVariableT)
      .apply {
        for (node in schema.nodes) {
          addFunction(FunSpec.builder(node.name)
            .addModifiers(PUBLIC, ABSTRACT)
            .addParameter("parent", baseNodeTypeOfT)
            .apply {
              for (event in node.traits.filterIsInstance<Event>()) {
                addParameter(event.name, LambdaTypeName.get(returnType = UNIT))
              }
            }
            .returns(schema.clientNodeType(node).parameterizedBy(typeVariableT))
            .build())
        }
      }
      .build())
    .addType(TypeSpec.classBuilder(schema.bridgeType)
      .addTypeVariable(typeVariableT)
      .addSuperinterface(treeBridge.parameterizedBy(baseNodeTypeOfT))
      .primaryConstructor(FunSpec.constructorBuilder()
        .addParameter("root", baseNodeTypeOfT)
        .addParameter("factory", nodeFactoryOfT)
        .addParameter("mutator", treeMutatorOfT)
        .build())
      .addProperty(PropertySpec.builder("root", baseNodeTypeOfT, PUBLIC, OVERRIDE)
        .initializer("root")
        .build())
      .addProperty(PropertySpec.builder("factory", nodeFactoryOfT, PRIVATE)
        .initializer("factory")
        .build())
      .addProperty(PropertySpec.builder("mutator", treeMutatorOfT, PRIVATE)
        .initializer("mutator")
        .build())
      .addFunction(FunSpec.builder("insert")
        .addModifiers(PUBLIC, OVERRIDE)
        .addParameter("parent", baseNodeTypeOfT)
        .addParameter("insert", nodeDiffInsert)
        .addParameter("events", eventSink)
        .returns(baseNodeTypeOfT)
        .addStatement("val id = insert.childId")
        .beginControlFlow("val node = when (val kind = insert.kind)")
        .apply {
          for (node in schema.nodes) {
            val factoryArguments = mutableListOf(CodeBlock.of("parent"))
            for (event in node.traits.filterIsInstance<Event>()) {
              factoryArguments += CodeBlock.of("{ events.send(%T(id, %L, null)) }", eventType, event.tag)
            }
            addStatement("%L -> factory.%N(%L)", node.tag, node.name, factoryArguments.joinToCode())
          }
        }
        .addStatement("else -> throw %T(\"Unknown kind \$kind\")", iae)
        .endControlFlow()
        .addStatement("mutator.insert(parent.value, insert.index, node.value)")
        .addStatement("return node")
        .build())
      .addFunction(FunSpec.builder("move")
        .addModifiers(PUBLIC, OVERRIDE)
        .addParameter("parent", baseNodeTypeOfT)
        .addParameter("move", nodeDiffMove)
        .addStatement("mutator.move(parent.value, move.fromIndex, move.toIndex, move.count)")
        .build())
      .addFunction(FunSpec.builder("remove")
        .addModifiers(PUBLIC, OVERRIDE)
        .addParameter("parent", baseNodeTypeOfT)
        .addParameter("remove", nodeDiffRemove)
        .addStatement("mutator.remove(parent.value, remove.index, remove.count)")
        .build())
      .addFunction(FunSpec.builder("clear")
        .addModifiers(PUBLIC, OVERRIDE)
        .addStatement("mutator.clear(root.value)")
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
fun generateClientNode(schema: Schema, node: Node): FileSpec {
  val typeVariableT = TypeVariableName("T", listOf(ANY), OUT)
  return FileSpec.builder(schema.clientPackage, node.name)
    .addType(TypeSpec.interfaceBuilder(node.name)
      .addModifiers(PUBLIC)
      .addTypeVariable(typeVariableT)
      .addSuperinterface(schema.baseNodeType.parameterizedBy(typeVariableT))
      .apply {
        for (trait in node.traits) {
          addFunction(FunSpec.builder(trait.name)
            .addModifiers(PUBLIC, ABSTRACT)
            .addParameter(trait.name, trait.type)
            .build())
        }
      }
      .addFunction(FunSpec.builder("apply")
        .addModifiers(PUBLIC, OVERRIDE)
        .addParameter("diff", propertyDiff)
        .beginControlFlow("when (val tag = diff.tag)")
        .apply {
          for (property in node.traits) {
            addStatement("%L -> %N(diff.value as %T)", property.tag, property.name, property.type)
          }
        }
        .addStatement("else -> throw %T(\"Unknown tag \$tag\")", iae)
        .endControlFlow()
        .build())
      .build())
    .build()
}

private val Trait.type: TypeName get() = when (this) {
  is Property -> type
  is Event -> BOOLEAN
}
