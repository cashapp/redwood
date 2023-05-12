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

import java.io.File
import org.jetbrains.kotlin.KtVirtualFileSourceFile
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY
import org.jetbrains.kotlin.cli.common.config.addKotlinSourceRoots
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSourceLocation
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.cli.jvm.compiler.VfsBasedProjectEnvironment
import org.jetbrains.kotlin.cli.jvm.compiler.pipeline.ModuleCompilerEnvironment
import org.jetbrains.kotlin.cli.jvm.compiler.pipeline.ModuleCompilerInput
import org.jetbrains.kotlin.cli.jvm.compiler.pipeline.compileModuleToAnalyzedFir
import org.jetbrains.kotlin.cli.jvm.config.addJvmClasspathRoots
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.com.intellij.openapi.vfs.StandardFileSystems
import org.jetbrains.kotlin.com.intellij.openapi.vfs.VirtualFileManager
import org.jetbrains.kotlin.com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.kotlin.config.CommonConfigurationKeys.MODULE_NAME
import org.jetbrains.kotlin.config.CommonConfigurationKeys.USE_FIR
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.descriptors.ClassKind.OBJECT
import org.jetbrains.kotlin.diagnostics.DiagnosticReporterFactory
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.declarations.FirRegularClass
import org.jetbrains.kotlin.fir.declarations.primaryConstructorIfAny
import org.jetbrains.kotlin.fir.declarations.utils.classId
import org.jetbrains.kotlin.fir.declarations.utils.isData
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.expressions.FirArrayOfCall
import org.jetbrains.kotlin.fir.expressions.FirConstExpression
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.expressions.FirGetClassCall
import org.jetbrains.kotlin.fir.expressions.FirResolvedQualifier
import org.jetbrains.kotlin.fir.expressions.FirVarargArgumentsExpression
import org.jetbrains.kotlin.fir.expressions.arguments
import org.jetbrains.kotlin.fir.expressions.builder.toAnnotationArgumentMapping
import org.jetbrains.kotlin.fir.resolve.fqName
import org.jetbrains.kotlin.metadata.jvm.deserialization.JvmProtoBufUtil.DEFAULT_MODULE_NAME
import org.jetbrains.kotlin.modules.TargetId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.platform.CommonPlatforms
import org.jetbrains.kotlin.platform.jvm.JvmPlatforms
import org.jetbrains.kotlin.text

public fun parseSchema(
  sources: Collection<File>,
  dependencies: Collection<File>,
  type: FqType,
): SchemaSet {
  return parseProtocolSchema(sources, dependencies, type)
}

public fun parseProtocolSchema(
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
      println("$severity: $message")
    }
  }

  val configuration = CompilerConfiguration()
  configuration.put(MODULE_NAME, "schema")
  configuration.put(MESSAGE_COLLECTOR_KEY, messageCollector)
  configuration.put(USE_FIR, true)
  configuration.addKotlinSourceRoots(sources.map { it.absolutePath })
  // TODO Figure out how to add the JDK modules to the classpath. Currently importing the stdlib
  //  allows a typealias to resolve to a JDK type which doesn't exist and thus breaks analysis.
  configuration.addJvmClasspathRoots(dependencies.filter { "kotlin-stdlib-" !in it.path })

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
  val files = buildList {
    for (source in sources) {
      source.walkTopDown().filter { it.isFile }.forEach {
        this += localFileSystem.findFileByPath(it.absolutePath)!!
      }
    }
  }

  val input = ModuleCompilerInput(
    targetId = TargetId(DEFAULT_MODULE_NAME, "redwood-parser"),
    commonPlatform = CommonPlatforms.defaultCommonPlatform,
    commonSources = emptyList(),
    platform = JvmPlatforms.unspecifiedJvmPlatform,
    platformSources = files.map(::KtVirtualFileSourceFile),
    configuration = configuration,
  )

  val reporter = DiagnosticReporterFactory.createReporter()

  val globalScope = GlobalSearchScope.allScope(project)
  val packagePartProvider = environment.createPackagePartProvider(globalScope)
  val projectEnvironment = VfsBasedProjectEnvironment(
    project = project,
    localFileSystem = localFileSystem,
    getPackagePartProviderFn = { packagePartProvider },
  )

  val output = compileModuleToAnalyzedFir(
    input = input,
    environment = ModuleCompilerEnvironment(
      projectEnvironment = projectEnvironment,
      diagnosticsReporter = reporter,
    ),
    previousStepsSymbolProviders = emptyList(),
    incrementalExcludesScope = null,
    diagnosticsReporter = reporter,
    performanceManager = null,
  )
  val platformOutput = output.platformOutput
  val firFiles = platformOutput.fir
  val firSession = platformOutput.session

  val types = firFiles
    .flatMap { it.declarations.findRegularClassesRecursive() }
    .associateBy { it.classId.asSingleFqName().toFqType() }

  val firContext = FirContext(types, firSession)

  val schema = firContext.parseSchema(type)

  disposable.dispose()

  return ParsedProtocolSchemaSet(
    schema = schema,
    dependencies = emptyMap(),
  )
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
    ?: throw IllegalArgumentException("Unable to locate schema type $type")

  val schemaAnnotation = firClass.annotations
    .find { it.fqName(firSession) == Annotations.Schema }
    ?: throw IllegalArgumentException("Schema $type missing @Schema annotation")

  val membersArray = schemaAnnotation.argumentMapping
    .mapping[Name.identifier("members")] as? FirArrayOfCall
    ?: throw AssertionError(schemaAnnotation.source?.text)

  val memberTypes = membersArray.argumentList
    .arguments
    .map {
      val getClassCall = it as? FirGetClassCall
        ?: throw AssertionError(schemaAnnotation.source?.text)
      val resolvedQualifier = getClassCall.argument as? FirResolvedQualifier
        ?: throw AssertionError(schemaAnnotation.source?.text)
      val classId = resolvedQualifier.classId
        ?: throw AssertionError(schemaAnnotation.source?.text)
      classId.asSingleFqName().toFqType()
    }

  val widgets = mutableListOf<ParsedProtocolWidget>()
  val layoutModifiers = mutableListOf<ParsedProtocolLayoutModifier>()
  for (memberType in memberTypes) {
    val memberClass = firClassByName[memberType]
      ?: throw IllegalArgumentException("Unable to locate schema type $memberType")

    val widgetAnnotation = memberClass.annotations
      .find { it.fqName(firSession) == Annotations.Widget }
    val layoutModifierAnnotation = memberClass.annotations
      .find { it.fqName(firSession) == Annotations.LayoutModifier }

    if ((widgetAnnotation == null) == (layoutModifierAnnotation == null)) {
      throw IllegalArgumentException(
        "$memberType must be annotated with either @Widget or @LayoutModifier",
      )
    } else if (widgetAnnotation != null) {
      widgets += parseWidget(memberType, memberClass, widgetAnnotation)
    } else if (layoutModifierAnnotation != null) {
      layoutModifiers += parseLayoutModifier(memberType, memberClass, layoutModifierAnnotation)
    } else {
      throw AssertionError()
    }
  }

  val dependenciesArray = schemaAnnotation.argumentMapping
    .mapping[Name.identifier("dependencies")] as? FirArrayOfCall
    ?: throw AssertionError(schemaAnnotation.source?.text)

  val dependencyTypesByTag = dependenciesArray.arguments
    .associate {
      val functionCall = it as? FirFunctionCall
        ?: throw AssertionError(schemaAnnotation.source?.text)
      val mapping = functionCall.argumentList.toAnnotationArgumentMapping().mapping

      @Suppress("UNCHECKED_CAST")
      val tagExpression = mapping[Name.identifier("tag")] as? FirConstExpression<Int>
        ?: throw AssertionError(schemaAnnotation.source?.text)
      val tag = tagExpression.value

      val getClassCall = mapping[Name.identifier("schema")] as? FirGetClassCall
        ?: throw AssertionError(schemaAnnotation.source?.text)
      val resolvedQualifier = getClassCall.argument as? FirResolvedQualifier
        ?: throw AssertionError(schemaAnnotation.source?.text)
      val classId = resolvedQualifier.classId
        ?: throw AssertionError(schemaAnnotation.source?.text)
      val fqType = classId.asSingleFqName().toFqType()

      tag to fqType
    }

  val widgetScopes = widgets
    .flatMap { it.traits }
    .filterIsInstance<Widget.Children>()
    .mapNotNull { it.scope }
  val layoutModifierScopes = layoutModifiers
    .flatMap { it.scopes }
  val scopes = buildSet {
    addAll(widgetScopes)
    addAll(layoutModifierScopes)
  }

  return ParsedProtocolSchema(
    type = type,
    scopes = scopes.toList(),
    widgets = widgets,
    layoutModifiers = layoutModifiers,
    taggedDependencies = dependencyTypesByTag,
  )
}

private fun FirContext.parseWidget(
  memberType: FqType,
  firClass: FirRegularClass,
  annotation: FirAnnotation,
): ParsedProtocolWidget {
  @Suppress("UNCHECKED_CAST")
  val tagExpression = annotation.argumentMapping.mapping[Name.identifier("tag")] as? FirConstExpression<Int>
    ?: throw AssertionError(annotation.source?.text)
  val tag = tagExpression.value
  require(tag in 1 until maxMemberTag) {
    "@Widget $memberType tag must be in range [1, $maxMemberTag): $tag"
  }

  val traits = if (firClass.isData) {
    firClass.primaryConstructorIfAny(firSession)!!.valueParameterSymbols.map { parameter ->
      val name = parameter.name.identifier

      val propertyAnnotation =
        parameter.annotations.find { it.fqName(firSession) == Annotations.Property }
      val childrenAnnotation =
        parameter.annotations.find { it.fqName(firSession) == Annotations.Children }

      if (propertyAnnotation != null) {
        @Suppress("UNCHECKED_CAST")
        val propertyTagExpression = propertyAnnotation.argumentMapping.mapping[Name.identifier("tag")] as? FirConstExpression<Int>
          ?: throw AssertionError(annotation.source?.text)
        val propertyTag = propertyTagExpression.value

        ParsedProtocolProperty(
          tag = propertyTag,
          name = name,
          type = TODO(),
          defaultExpression = TODO(),
          deprecation = TODO(),
        )
      } else if (childrenAnnotation != null) {
        @Suppress("UNCHECKED_CAST")
        val childrenTagExpression = childrenAnnotation.argumentMapping.mapping[Name.identifier("tag")] as? FirConstExpression<Int>
          ?: throw AssertionError(annotation.source?.text)
        val childrenTag = childrenTagExpression.value

        ParsedProtocolChildren(
          tag = childrenTag,
          name = name,
          scope = TODO(),
          defaultExpression = TODO(),
          deprecation = TODO(),
        )
      } else {
        throw IllegalArgumentException("Unannotated parameter \"$name\" on $memberType")
      }
    }
  } else if (firClass.classKind == OBJECT) {
    emptyList()
  } else {
    throw IllegalArgumentException(
      "@Widget $memberType must be 'data' class or 'object'",
    )
  }

  return ParsedProtocolWidget(
    tag = tag,
    type = memberType,
    deprecation = TODO(),
    traits = traits,
  )
}

private fun FirContext.parseLayoutModifier(
  memberType: FqType,
  firClass: FirRegularClass,
  annotation: FirAnnotation,
): ParsedProtocolLayoutModifier {
  @Suppress("UNCHECKED_CAST")
  val tagExpression = annotation.argumentMapping.mapping[Name.identifier("tag")] as? FirConstExpression<Int>
    ?: throw AssertionError(annotation.source?.text)
  val tag = tagExpression.value
  require(tag in 1 until maxMemberTag) {
    "@LayoutModifier $memberType tag must be in range [1, $maxMemberTag): $tag"
  }

  val scopesExpression = annotation.argumentMapping.mapping[Name.identifier("scopes")] as? FirVarargArgumentsExpression
    ?: throw AssertionError(annotation.source?.text)
  val scopes = scopesExpression.arguments
    .map {
      val getClassCall = it as? FirGetClassCall
        ?: throw AssertionError(annotation.source?.text)
      val resolvedQualifier = getClassCall.argument as? FirResolvedQualifier
        ?: throw AssertionError(annotation.source?.text)
      val classId = resolvedQualifier.classId
        ?: throw AssertionError(annotation.source?.text)
      classId.asSingleFqName().toFqType()
    }

  val properties = if (firClass.isData) {
    TODO()
  } else if (firClass.classKind == OBJECT) {
    emptyList<ParsedProtocolLayoutModifierProperty>()
  } else {
    throw IllegalArgumentException(
      "@LayoutModifier $memberType must be 'data' class or 'object'",
    )
  }

  return ParsedProtocolLayoutModifier(
    tag = tag,
    scopes = scopes,
    type = memberType,
    deprecation = TODO(),
    properties = properties,
  )
}

private fun FqName.toFqType() = FqType.bestGuess(asString())

private object Annotations {
  val Children = FqName("app.cash.redwood.schema.Children")
  val LayoutModifier = FqName("app.cash.redwood.schema.LayoutModifier")
  val Property = FqName("app.cash.redwood.schema.Property")
  val Schema = FqName("app.cash.redwood.schema.Schema")
  val Widget = FqName("app.cash.redwood.schema.Widget")
}
