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

import app.cash.redwood.tooling.schema.Deprecation.Level
import app.cash.redwood.tooling.schema.FqType.Variance.In
import app.cash.redwood.tooling.schema.FqType.Variance.Invariant
import app.cash.redwood.tooling.schema.FqType.Variance.Out
import app.cash.redwood.tooling.schema.ProtocolWidget.ProtocolChildren
import app.cash.redwood.tooling.schema.ProtocolWidget.ProtocolProperty
import app.cash.redwood.tooling.schema.SchemaAnnotation.DependencyAnnotation
import java.io.File
import java.net.URLClassLoader
import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.KtVirtualFileSourceFile
import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.cli.common.GroupedKtSources
import org.jetbrains.kotlin.cli.common.config.addKotlinSourceRoots
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity.ERROR
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity.EXCEPTION
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity.LOGGING
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSourceLocation
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.cli.jvm.compiler.VfsBasedProjectEnvironment
import org.jetbrains.kotlin.cli.jvm.compiler.pipeline.IncrementalCompilationApi
import org.jetbrains.kotlin.cli.jvm.compiler.pipeline.ModuleCompilerInput
import org.jetbrains.kotlin.cli.jvm.compiler.pipeline.compileModuleToAnalyzedFirViaLightTreeIncrementally
import org.jetbrains.kotlin.cli.jvm.config.addJvmClasspathRoots
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.com.intellij.openapi.vfs.StandardFileSystems
import org.jetbrains.kotlin.com.intellij.openapi.vfs.VirtualFileManager
import org.jetbrains.kotlin.com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.kotlin.config.CommonConfigurationKeys.MESSAGE_COLLECTOR_KEY
import org.jetbrains.kotlin.config.CommonConfigurationKeys.MODULE_NAME
import org.jetbrains.kotlin.config.CommonConfigurationKeys.USE_FIR
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.JVMConfigurationKeys.JDK_HOME
import org.jetbrains.kotlin.descriptors.ClassKind.OBJECT
import org.jetbrains.kotlin.diagnostics.DiagnosticReporterFactory
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.declarations.FirProperty
import org.jetbrains.kotlin.fir.declarations.FirRegularClass
import org.jetbrains.kotlin.fir.declarations.primaryConstructorIfAny
import org.jetbrains.kotlin.fir.declarations.toAnnotationClassId
import org.jetbrains.kotlin.fir.declarations.utils.classId
import org.jetbrains.kotlin.fir.declarations.utils.isData
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.expressions.FirArrayLiteral
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.expressions.FirGetClassCall
import org.jetbrains.kotlin.fir.expressions.FirLiteralExpression
import org.jetbrains.kotlin.fir.expressions.FirPropertyAccessExpression
import org.jetbrains.kotlin.fir.expressions.FirResolvedQualifier
import org.jetbrains.kotlin.fir.expressions.FirVarargArgumentsExpression
import org.jetbrains.kotlin.fir.expressions.arguments
import org.jetbrains.kotlin.fir.expressions.impl.FirResolvedArgumentList
import org.jetbrains.kotlin.fir.references.FirNamedReference
import org.jetbrains.kotlin.fir.resolve.fqName
import org.jetbrains.kotlin.fir.resolve.toSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirValueParameterSymbol
import org.jetbrains.kotlin.fir.types.ConeClassLikeType
import org.jetbrains.kotlin.fir.types.ConeKotlinTypeProjection
import org.jetbrains.kotlin.fir.types.ConeStarProjection
import org.jetbrains.kotlin.fir.types.ConeTypeParameterType
import org.jetbrains.kotlin.fir.types.ConeTypeProjection
import org.jetbrains.kotlin.fir.types.classId
import org.jetbrains.kotlin.fir.types.customAnnotations
import org.jetbrains.kotlin.fir.types.isBasicFunctionType
import org.jetbrains.kotlin.fir.types.isMarkedNullable
import org.jetbrains.kotlin.fir.types.receiverType
import org.jetbrains.kotlin.fir.types.renderReadable
import org.jetbrains.kotlin.fir.types.type
import org.jetbrains.kotlin.fir.types.variance
import org.jetbrains.kotlin.metadata.jvm.deserialization.JvmProtoBufUtil.DEFAULT_MODULE_NAME
import org.jetbrains.kotlin.modules.TargetId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.text
import org.jetbrains.kotlin.types.Variance.INVARIANT
import org.jetbrains.kotlin.types.Variance.IN_VARIANCE
import org.jetbrains.kotlin.types.Variance.OUT_VARIANCE

public fun parseSchema(
  javaHome: File,
  sources: Collection<File>,
  dependencies: Collection<File>,
  type: FqType,
): SchemaSet {
  return parseProtocolSchema(javaHome, sources, dependencies, type)
}

public fun parseProtocolSchema(
  javaHome: File,
  sources: Collection<File>,
  dependencies: Collection<File>,
  type: FqType,
): ProtocolSchemaSet {
  val messageCollector = object : MessageCollector {
    override fun clear() = Unit
    override fun hasErrors() = false

    override fun report(
      severity: CompilerMessageSeverity,
      message: String,
      location: CompilerMessageSourceLocation?,
    ) {
      val destination = when (severity) {
        LOGGING -> null
        EXCEPTION, ERROR -> System.err
        else -> System.out
      }
      destination?.println(message)
    }
  }

  val configuration = CompilerConfiguration()
  configuration.put(MODULE_NAME, "schema")
  configuration.put(MESSAGE_COLLECTOR_KEY, messageCollector)
  configuration.put(USE_FIR, true)
  configuration.put(JDK_HOME, javaHome)
  configuration.addKotlinSourceRoots(sources.map { it.absolutePath })
  configuration.addJvmClasspathRoots(dependencies.toList())

  val disposable = Disposer.newDisposable()
  val environment = KotlinCoreEnvironment.createForProduction(
    disposable,
    configuration,
    EnvironmentConfigFiles.JVM_CONFIG_FILES,
  )
  val project = environment.project

  val localFileSystem = VirtualFileManager.getInstance().getFileSystem(
    StandardFileSystems.FILE_PROTOCOL,
  )
  val files = sources
    .flatMap { it.walkTopDown().filter(File::isFile) }
    .map { localFileSystem.findFileByPath(it.absolutePath)!! }

  val sourceFiles = files.map(::KtVirtualFileSourceFile).toSet()
  val input = ModuleCompilerInput(
    targetId = TargetId(DEFAULT_MODULE_NAME, "redwood-parser"),
    groupedSources = GroupedKtSources(
      platformSources = sourceFiles,
      commonSources = emptyList(),
      sourcesByModuleName = mapOf(DEFAULT_MODULE_NAME to sourceFiles),
    ),
    configuration = configuration,
  )

  val reporter = DiagnosticReporterFactory.createReporter(messageCollector)

  val globalScope = GlobalSearchScope.allScope(project)
  val packagePartProvider = environment.createPackagePartProvider(globalScope)
  val projectEnvironment = VfsBasedProjectEnvironment(
    project = project,
    localFileSystem = localFileSystem,
    getPackagePartProviderFn = { packagePartProvider },
  )

  @OptIn(IncrementalCompilationApi::class)
  val output = compileModuleToAnalyzedFirViaLightTreeIncrementally(
    projectEnvironment = projectEnvironment,
    messageCollector = messageCollector,
    compilerConfiguration = configuration,
    input = input,
    diagnosticsReporter = reporter,
    incrementalExcludesScope = null,
  )
  val platformOutput = output.outputs.first()
  val firFiles = platformOutput.fir
  val firSession = platformOutput.session

  val types = firFiles
    .flatMap { it.declarations.findRegularClassesRecursive() }
    .associateBy { it.classId.toFqType() }

  val firContext = FirContext(types, firSession)

  val schema = firContext.parseSchema(type)

  disposable.dispose()

  val dependencyClassLoader = URLClassLoader(dependencies.map { it.toURI().toURL() }.toTypedArray())
  val schemaSet = loadProtocolSchemaDependencies(schema, dependencyClassLoader)

  val duplicatedWidgets = schemaSet.all
    .flatMap { it.widgets.map { widget -> widget to it } }
    .groupBy { it.first.type }
    .filterValues { it.size > 1 }
    .mapValues { it.value.map(Pair<*, Schema>::second) }
  if (duplicatedWidgets.isNotEmpty()) {
    throw IllegalArgumentException(
      buildString {
        appendLine("Schema dependency tree contains duplicated widgets")
        for ((widget, schemas) in duplicatedWidgets) {
          append("\n- $widget: ")
          schemas.joinTo(this) { it.type.toString() }
        }
      },
    )
  }

  return schemaSet
}

private fun List<FirDeclaration>.findRegularClassesRecursive(): List<FirRegularClass> {
  val classes = filterIsInstance<FirRegularClass>()
  return classes + classes.flatMap { it.declarations.findRegularClassesRecursive() }
}

private class FirContext(
  val firClassByName: Map<FqType, FirRegularClass>,
  val firSession: FirSession,
)

private fun FirContext.parseSchema(type: FqType): ParsedProtocolSchema {
  val firClass = firClassByName[type]
    ?: throw IllegalArgumentException("Unable to locate schema type $type in compilation unit")

  val schemaAnnotation = findSchemaAnnotation(firClass.annotations)
    ?: throw IllegalArgumentException("Schema $type missing @Schema annotation")

  val duplicatedMembers = schemaAnnotation.members
    .groupBy { it }
    .filterValues { it.size > 1 }
    .keys
  if (duplicatedMembers.isNotEmpty()) {
    throw IllegalArgumentException(
      buildString {
        append("Schema contains repeated member")
        if (duplicatedMembers.size > 1) {
          append('s')
        }
        duplicatedMembers.joinTo(this, prefix = "\n\n- ", separator = "\n- ")
      },
    )
  }

  val widgets = mutableListOf<ParsedProtocolWidget>()
  val modifiers = mutableListOf<ParsedProtocolModifier>()
  for (memberType in schemaAnnotation.members) {
    val memberClass = firClassByName[memberType]
      ?: throw IllegalArgumentException("Unable to locate schema member $memberType in compilation unit")

    val widgetAnnotation = findWidgetAnnotation(memberClass.annotations)
    val modifierAnnotation = findModifierAnnotation(memberClass.annotations)

    if ((widgetAnnotation == null) == (modifierAnnotation == null)) {
      throw IllegalArgumentException(
        "$memberType must be annotated with either @Widget or @Modifier",
      )
    } else if (widgetAnnotation != null) {
      widgets += parseWidget(memberType, memberClass, widgetAnnotation)
    } else if (modifierAnnotation != null) {
      modifiers += parseModifier(memberType, memberClass, modifierAnnotation)
    } else {
      throw AssertionError()
    }
  }

  val badWidgets = widgets.groupBy(ProtocolWidget::tag).filterValues { it.size > 1 }
  if (badWidgets.isNotEmpty()) {
    throw IllegalArgumentException(
      buildString {
        appendLine("Schema @Widget tags must be unique")
        for ((widgetTag, group) in badWidgets) {
          append("\n- @Widget($widgetTag): ")
          group.joinTo(this) { it.type.toString() }
        }
      },
    )
  }

  val badReservedWidgets = schemaAnnotation.reservedWidgets
    .filterNotTo(HashSet(), HashSet<Int>()::add)
  require(badReservedWidgets.isEmpty()) {
    "Schema reserved widgets contains duplicates $badReservedWidgets"
  }

  val reservedWidgets = widgets.filter { it.tag in schemaAnnotation.reservedWidgets }
  if (reservedWidgets.isNotEmpty()) {
    throw IllegalArgumentException(
      buildString {
        append("Schema @Widget tags must not be included in reserved set ")
        appendLine(schemaAnnotation.reservedWidgets.toString())
        for (widget in reservedWidgets) {
          append("\n- @Widget(${widget.tag}) ${widget.type}")
        }
      },
    )
  }

  val badModifiers = modifiers.groupBy(ProtocolModifier::tag).filterValues { it.size > 1 }
  if (badModifiers.isNotEmpty()) {
    throw IllegalArgumentException(
      buildString {
        appendLine("Schema @Modifier tags must be unique")
        for ((modifierTag, group) in badModifiers) {
          append("\n- @Modifier($modifierTag): ")
          group.joinTo(this) { it.type.toString() }
        }
      },
    )
  }

  val badReservedModifiers = schemaAnnotation.reservedModifiers
    .filterNotTo(HashSet(), HashSet<Int>()::add)
  require(badReservedModifiers.isEmpty()) {
    "Schema reserved modifiers contains duplicates $badReservedModifiers"
  }

  val reservedModifiers = modifiers.filter { it.tag in schemaAnnotation.reservedModifiers }
  if (reservedModifiers.isNotEmpty()) {
    throw IllegalArgumentException(
      buildString {
        append("Schema @Modifier tags must not be included in reserved set ")
        appendLine(schemaAnnotation.reservedModifiers.toString())
        for (widget in reservedModifiers) {
          append("\n- @Modifier(${widget.tag}, …) ${widget.type}")
        }
      },
    )
  }

  val widgetScopes = widgets
    .flatMap { it.traits }
    .filterIsInstance<Widget.Children>()
    .mapNotNull { it.scope }
  val modifierScopes = modifiers
    .flatMap { it.scopes }
  val scopes = buildSet {
    addAll(widgetScopes)
    addAll(modifierScopes)
  }

  val badDependencyTags = schemaAnnotation.dependencies
    .groupBy { it.tag }
    .filterValues { it.size > 1 }
  if (badDependencyTags.isNotEmpty()) {
    throw IllegalArgumentException(
      buildString {
        appendLine("Schema dependency tags must be unique")
        for ((dependencyTag, group) in badDependencyTags) {
          append("\n- Dependency tag $dependencyTag: ")
          group.joinTo(this) { it.schema.toString() }
        }
      },
    )
  }

  val badDependencyTypes = schemaAnnotation.dependencies
    .groupBy { it.schema }
    .filterValues { it.size > 1 }
    .keys
  if (badDependencyTypes.isNotEmpty()) {
    throw IllegalArgumentException(
      buildString {
        append("Schema contains repeated ")
        append(if (badDependencyTypes.size > 1) "dependencies" else "dependency")
        badDependencyTypes.joinTo(this, prefix = "\n\n- ", separator = "\n- ")
      },
    )
  }

  val documentation = firClass.source?.findAndParseKDoc()

  return ParsedProtocolSchema(
    type = type,
    documentation = documentation,
    scopes = scopes.toList(),
    widgets = widgets,
    modifiers = modifiers,
    taggedDependencies = schemaAnnotation.dependencies.associate { it.tag to it.schema },
  )
}

private fun FirContext.parseWidget(
  memberType: FqType,
  firClass: FirRegularClass,
  annotation: WidgetAnnotation,
): ParsedProtocolWidget {
  val tag = annotation.tag
  require(tag in 1 until MAX_MEMBER_TAG) {
    "@Widget $memberType tag must be in range [1, $MAX_MEMBER_TAG): $tag"
  }

  val traits = if (firClass.classKind == OBJECT) {
    emptyList()
  } else if (firClass.isData) {
    firClass.primaryConstructorIfAny(firSession)!!.valueParameterSymbols.map { parameter ->
      val name = parameter.name.identifier
      val type = parameter.resolvedReturnType
      val property = firClass.declarations.filterIsInstance<FirProperty>().single { it.name == parameter.name }

      val propertyAnnotation = findPropertyAnnotation(property.annotations)
      val childrenAnnotation = findChildrenAnnotation(property.annotations)
      val defaultExpression = findDefaultExpression(parameter)
      val deprecation = findDeprecationAnnotation(property.annotations)
        ?.toDeprecation { "$memberType.$name" }
      val documentation = parameter.source?.findAndParseKDoc()

      if (propertyAnnotation != null) {
        if (type.isBasicFunctionType(firSession)) {
          val arguments = type.typeArguments.dropLast(1) // Drop Unit return type.
          ParsedProtocolEvent(
            tag = propertyAnnotation.tag,
            name = name,
            documentation = documentation,
            parameters = arguments.map {
              ParsedParameter(
                name = (it as? ConeClassLikeType)?.extractParameterName(firSession),
                type = it.toFqType(),
              )
            },
            isNullable = type.isMarkedNullable,
            defaultExpression = defaultExpression,
            deprecation = deprecation,
          )
        } else {
          ParsedProtocolProperty(
            tag = propertyAnnotation.tag,
            name = name,
            documentation = documentation,
            type = type.toFqType(),
            defaultExpression = defaultExpression,
            deprecation = deprecation,
          )
        }
      } else if (childrenAnnotation != null) {
        val typeArguments = type.typeArguments
        val lastArgument = typeArguments.lastOrNull()?.type?.classId?.asSingleFqName()
        require(lastArgument == FqNames.Unit) {
          "@Children $memberType#$name must be of type '() -> Unit'"
        }
        var arguments = typeArguments.dropLast(1) // Drop Unit return type.
        val scope = type.receiverType(firSession)
        if (scope != null) {
          require(scope.typeArguments.isEmpty() && scope !is ConeTypeParameterType && !scope.isMarkedNullable) {
            "@Children $memberType#$name lambda receiver can only be a non-null class. Found: ${scope.renderReadable()}"
          }
          arguments = arguments.drop(1)
        }
        require(arguments.isEmpty()) {
          "@Children $memberType#$name lambda type must not have any arguments. " +
            "Found: ${arguments.map { it.toFqType() }}"
        }
        ParsedProtocolChildren(
          tag = childrenAnnotation.tag,
          name = name,
          documentation = documentation,
          scope = scope?.toFqType(),
          defaultExpression = defaultExpression,
          deprecation = deprecation,
        )
      } else {
        throw IllegalArgumentException("Unannotated parameter \"$name\" on $memberType")
      }
    }
  } else {
    throw IllegalArgumentException(
      "@Widget $memberType must be 'data' class or object",
    )
  }

  val badChildren = traits.filterIsInstance<ProtocolChildren>()
    .groupBy(ProtocolChildren::tag)
    .filterValues { it.size > 1 }
  if (badChildren.isNotEmpty()) {
    throw IllegalArgumentException(
      buildString {
        appendLine("$memberType's @Children tags must be unique")
        for ((childTag, group) in badChildren) {
          append("\n- @Children($childTag): ")
          group.joinTo(this) { it.name }
        }
      },
    )
  }

  val badReservedChildren = annotation.reservedChildren
    .filterNotTo(HashSet(), HashSet<Int>()::add)
  require(badReservedChildren.isEmpty()) {
    "Widget $memberType reserved children contains duplicates $badReservedChildren"
  }

  val reservedChildren = traits.filterIsInstance<ProtocolChildren>()
    .filter { it.tag in annotation.reservedChildren }
  if (reservedChildren.isNotEmpty()) {
    throw IllegalArgumentException(
      buildString {
        append("Widget $memberType @Children tags must not be included in reserved set ")
        appendLine(annotation.reservedChildren.toString())
        for (children in reservedChildren) {
          append("\n- @Children(${children.tag}) ${children.name}")
        }
      },
    )
  }

  val badProperties = traits.filterIsInstance<ProtocolProperty>()
    .groupBy(ProtocolProperty::tag)
    .filterValues { it.size > 1 }
  if (badProperties.isNotEmpty()) {
    throw IllegalArgumentException(
      buildString {
        appendLine("$memberType's @Property tags must be unique")
        for ((propertyTag, group) in badProperties) {
          append("\n- @Property($propertyTag): ")
          group.joinTo(this) { it.name }
        }
      },
    )
  }

  val badReservedProperties = annotation.reservedProperties
    .filterNotTo(HashSet(), HashSet<Int>()::add)
  require(badReservedProperties.isEmpty()) {
    "Widget $memberType reserved properties contains duplicates $badReservedProperties"
  }

  val reservedProperties = traits.filterIsInstance<ProtocolProperty>()
    .filter { it.tag in annotation.reservedProperties }
  if (reservedProperties.isNotEmpty()) {
    throw IllegalArgumentException(
      buildString {
        append("Widget $memberType @Property tags must not be included in reserved set ")
        appendLine(annotation.reservedProperties.toString())
        for (children in reservedProperties) {
          append("\n- @Property(${children.tag}) ${children.name}")
        }
      },
    )
  }

  val deprecation = findDeprecationAnnotation(firClass.annotations)
    ?.toDeprecation { memberType.toString() }
  val documentation = firClass.source?.findAndParseKDoc()

  return ParsedProtocolWidget(
    tag = tag,
    type = memberType,
    documentation = documentation,
    deprecation = deprecation,
    traits = traits,
  )
}

private fun FirContext.parseModifier(
  memberType: FqType,
  firClass: FirRegularClass,
  annotation: ModifierAnnotation,
): ParsedProtocolModifier {
  val tag = annotation.tag
  require(tag in 1 until MAX_MEMBER_TAG || isSpecialModifier(tag, memberType)) {
    "@Modifier $memberType tag must be in range [1, $MAX_MEMBER_TAG): $tag"
  }

  val properties = if (firClass.classKind == OBJECT) {
    emptyList()
  } else if (firClass.isData) {
    firClass.primaryConstructorIfAny(firSession)!!.valueParameterSymbols.map { parameter ->
      val name = parameter.name.identifier
      val parameterType = parameter.resolvedReturnType.toFqType()
      val property = firClass.declarations.filterIsInstance<FirProperty>().single { it.name == parameter.name }

      val defaultExpression = findDefaultExpression(parameter)
      val deprecation = findDeprecationAnnotation(property.annotations)
        ?.toDeprecation { "$memberType.$name" }
      val documentation = parameter.source?.findAndParseKDoc()

      val isSerializable = parameter.resolvedReturnType
        .toSymbol(firSession)
        ?.annotations.orEmpty()
        .any { it.fqName(firSession) == FqNames.Serializable }

      ParsedProtocolModifierProperty(
        name = name,
        documentation = documentation,
        type = parameterType,
        isSerializable = isSerializable,
        defaultExpression = defaultExpression,
        deprecation = deprecation,
      )
    }
  } else {
    throw IllegalArgumentException(
      "@Modifier $memberType must be 'data' class or object",
    )
  }

  val deprecation = findDeprecationAnnotation(firClass.annotations)
    ?.toDeprecation { memberType.toString() }
  val documentation = firClass.source?.findAndParseKDoc()

  return ParsedProtocolModifier(
    tag = tag,
    scopes = annotation.scopes,
    type = memberType,
    documentation = documentation,
    deprecation = deprecation,
    properties = properties,
  )
}

private fun KtSourceElement.findAndParseKDoc(): String? {
  return null
  // TODO We should be finding the KDeclaration on which we can get a KDoc instance and,
  //  hopefully, parsed KDoc rather than raw KDoc.
  // return treeStructure.getChildrenArray(lighterASTNode)
  //   .filterNotNull()
  //   .firstOrNull { it.tokenType == KDocTokens.KDOC }
  //   ?.let { treeStructure.toString(it).toString() }
}

private fun FirContext.findSchemaAnnotation(
  annotations: List<FirAnnotation>,
): SchemaAnnotation? {
  val annotation = annotations.find { it.fqName(firSession) == FqNames.Schema }
    ?: return null

  val membersArray = annotation.argumentMapping
    .mapping[Name.identifier("members")] as? FirArrayLiteral
    ?: throw AssertionError(annotation.source?.text)
  val members = membersArray.argumentList
    .arguments
    .map {
      val getClassCall = it as? FirGetClassCall
        ?: throw AssertionError(annotation.source?.text)
      val resolvedQualifier = getClassCall.argument as? FirResolvedQualifier
        ?: throw AssertionError(annotation.source?.text)
      val classId = resolvedQualifier.classId
        ?: throw AssertionError(annotation.source?.text)
      classId.toFqType()
    }

  val dependenciesArray = annotation.argumentMapping
    .mapping[Name.identifier("dependencies")] as? FirArrayLiteral
  val dependencies = dependenciesArray?.arguments.orEmpty()
    .map {
      val functionCall = it as? FirFunctionCall
        ?: throw AssertionError(annotation.source?.text)
      val mapping = (functionCall.argumentList as? FirResolvedArgumentList)
        ?.let { list ->
          list.mapping.entries.associate { (argument, parameter) ->
            parameter.name to argument
          }
        } ?: emptyMap()

      val tagExpression = mapping[Name.identifier("tag")] as? FirLiteralExpression
        ?: throw AssertionError(annotation.source?.text)

      val getClassCall = mapping[Name.identifier("schema")] as? FirGetClassCall
        ?: throw AssertionError(annotation.source?.text)
      val resolvedQualifier = getClassCall.argument as? FirResolvedQualifier
        ?: throw AssertionError(annotation.source?.text)
      val classId = resolvedQualifier.classId
        ?: throw AssertionError(annotation.source?.text)
      val fqType = classId.toFqType()

      DependencyAnnotation(tagExpression.valueAsInt(), fqType)
    }

  val reservedWidgetsArray = annotation.argumentMapping
    .mapping[Name.identifier("reservedWidgets")] as FirArrayLiteral?
  val reservedWidgets = reservedWidgetsArray?.arguments.orEmpty()
    .map {
      (it as FirLiteralExpression).valueAsInt()
    }

  val reservedModifiersArray = annotation.argumentMapping
    .mapping[Name.identifier("reservedModifiers")] as FirArrayLiteral?
  val reservedModifiers = reservedModifiersArray?.arguments.orEmpty()
    .map {
      (it as FirLiteralExpression).valueAsInt()
    }

  return SchemaAnnotation(members, dependencies, reservedWidgets, reservedModifiers)
}

private data class SchemaAnnotation(
  val members: List<FqType>,
  val dependencies: List<DependencyAnnotation>,
  val reservedWidgets: List<Int>,
  val reservedModifiers: List<Int>,
) {
  data class DependencyAnnotation(
    val tag: Int,
    val schema: FqType,
  )
}

private fun FirContext.findWidgetAnnotation(
  annotations: List<FirAnnotation>,
): WidgetAnnotation? {
  val annotation = annotations.find { it.fqName(firSession) == FqNames.Widget }
    ?: return null

  val tagExpression = annotation.argumentMapping
    .mapping[Name.identifier("tag")] as? FirLiteralExpression
    ?: throw AssertionError(annotation.source?.text)

  val reservedPropertiesArray = annotation.argumentMapping
    .mapping[Name.identifier("reservedProperties")] as FirArrayLiteral?
  val reservedProperties = reservedPropertiesArray?.arguments.orEmpty()
    .map {
      (it as FirLiteralExpression).valueAsInt()
    }

  val reservedChildrenArray = annotation.argumentMapping
    .mapping[Name.identifier("reservedChildren")] as FirArrayLiteral?
  val reservedChildren = reservedChildrenArray?.arguments.orEmpty()
    .map {
      (it as FirLiteralExpression).valueAsInt()
    }

  return WidgetAnnotation(tagExpression.valueAsInt(), reservedProperties, reservedChildren)
}

private data class WidgetAnnotation(
  val tag: Int,
  val reservedProperties: List<Int>,
  val reservedChildren: List<Int>,
)

private fun FirContext.findPropertyAnnotation(
  annotations: List<FirAnnotation>,
): PropertyAnnotation? {
  val annotation = annotations.find { it.fqName(firSession) == FqNames.Property }
    ?: return null

  val tagExpression = annotation.argumentMapping
    .mapping[Name.identifier("tag")] as? FirLiteralExpression
    ?: throw AssertionError(annotation.source?.text)

  return PropertyAnnotation(tagExpression.valueAsInt())
}

private data class PropertyAnnotation(
  val tag: Int,
)

private fun FirContext.findChildrenAnnotation(
  annotations: List<FirAnnotation>,
): ChildrenAnnotation? {
  val annotation = annotations.find { it.fqName(firSession) == FqNames.Children }
    ?: return null

  val tagExpression = annotation.argumentMapping
    .mapping[Name.identifier("tag")] as? FirLiteralExpression
    ?: throw AssertionError(annotation.source?.text)

  return ChildrenAnnotation(tagExpression.valueAsInt())
}

private data class ChildrenAnnotation(
  val tag: Int,
)

private fun FirContext.findDefaultExpression(
  parameter: FirValueParameterSymbol,
): String? {
  return parameter.defaultValueSource?.let { defaultValue ->
    defaultValue.treeStructure
      .toString(defaultValue.lighterASTNode)
      .toString()
  }
}

private fun FirContext.findModifierAnnotation(
  annotations: List<FirAnnotation>,
): ModifierAnnotation? {
  val annotation = annotations.find { it.fqName(firSession) == FqNames.Modifier }
    ?: return null

  val tagExpression = annotation.argumentMapping.mapping[Name.identifier("tag")] as? FirLiteralExpression
    ?: throw AssertionError(annotation.source?.text)

  val scopesExpression = annotation.argumentMapping.mapping[Name.identifier("scopes")] as? FirVarargArgumentsExpression
  val scopes = scopesExpression?.arguments.orEmpty()
    .map {
      val getClassCall = it as? FirGetClassCall
        ?: throw AssertionError(annotation.source?.text)
      val resolvedQualifier = getClassCall.argument as? FirResolvedQualifier
        ?: throw AssertionError(annotation.source?.text)
      val classId = resolvedQualifier.classId
        ?: throw AssertionError(annotation.source?.text)
      classId.toFqType()
    }

  return ModifierAnnotation(tagExpression.valueAsInt(), scopes)
}

private data class ModifierAnnotation(
  val tag: Int,
  val scopes: List<FqType>,
)

private fun FirContext.findDeprecationAnnotation(
  annotations: List<FirAnnotation>,
): DeprecationAnnotation? {
  val annotation = annotations.find { it.fqName(firSession) == FqNames.Deprecated }
    ?: return null

  val messageExpression = annotation.argumentMapping
    .mapping[Name.identifier("message")] as? FirLiteralExpression
    ?: throw AssertionError(annotation.source?.text)

  val levelExpression = annotation.argumentMapping
    .mapping[Name.identifier("level")] as? FirPropertyAccessExpression
  val levelReference = levelExpression?.calleeReference as? FirNamedReference
  val level = levelReference?.name?.identifier ?: "WARNING"

  val hasReplaceWith = Name.identifier("replaceWith") in annotation.argumentMapping.mapping

  return DeprecationAnnotation(messageExpression.value as String, level, hasReplaceWith)
}

private data class DeprecationAnnotation(
  val message: String,
  val level: String,
  val hasReplaceWith: Boolean,
)

private fun DeprecationAnnotation.toDeprecation(source: () -> String): ParsedDeprecation {
  require(!hasReplaceWith) {
    "Schema deprecation does not support replacements: ${source()}"
  }
  return ParsedDeprecation(
    level = when (level) {
      "WARNING" -> Level.WARNING

      "ERROR" -> Level.ERROR

      else -> {
        throw IllegalArgumentException(
          "Schema deprecation does not support level $level: ${source()}",
        )
      }
    },
    message = message,
  )
}

private fun ConeTypeProjection.toFqType(): FqType {
  return when (this) {
    ConeStarProjection -> FqType.Star
    is ConeKotlinTypeProjection -> {
      when (val type = type) {
        is ConeClassLikeType -> {
          val parameterTypes = type.typeArguments.map { it.toFqType() }
          val variance = when (variance) {
            INVARIANT -> Invariant
            IN_VARIANCE -> In
            OUT_VARIANCE -> Out
          }
          type.lookupTag.classId.toFqType()
            .copy(
              nullable = type.isMarkedNullable,
              parameterTypes = parameterTypes,
              variance = variance,
            )
        }
        else -> throw UnsupportedOperationException()
      }
    }
  }
}

private fun FirLiteralExpression.valueAsInt() = (value as Long).toInt()

private fun ConeClassLikeType.extractParameterName(firSession: FirSession): String? {
  val annotation = customAnnotations
    .find { it.toAnnotationClassId(firSession) == StandardNames.FqNames.parameterNameClassId }
    ?: return null
  val name = annotation.argumentMapping
    .mapping[Name.identifier("name")] as? FirLiteralExpression
    ?: throw AssertionError(annotation.source?.text ?: annotation)
  return name.value as String
}

private fun ClassId.toFqType() = FqType(
  buildList {
    add(packageFqName.asString())
    addAll(relativeClassName.asString().split('.'))
  },
)

private object FqNames {
  val Children = FqName("app.cash.redwood.schema.Children")
  val Deprecated = FqName("kotlin.Deprecated")
  val Modifier = FqName("app.cash.redwood.schema.Modifier")
  val Property = FqName("app.cash.redwood.schema.Property")
  val Schema = FqName("app.cash.redwood.schema.Schema")
  val Serializable = FqName("kotlinx.serialization.Serializable")
  val Widget = FqName("app.cash.redwood.schema.Widget")
  val Unit = FqName("kotlin.Unit")
}
