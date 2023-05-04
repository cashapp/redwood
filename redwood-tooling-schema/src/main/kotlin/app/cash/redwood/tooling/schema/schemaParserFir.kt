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
import org.jetbrains.kotlin.config.JVMConfigurationKeys.NO_JDK
import org.jetbrains.kotlin.diagnostics.DiagnosticReporterFactory
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.declarations.FirRegularClass
import org.jetbrains.kotlin.fir.declarations.utils.classId
import org.jetbrains.kotlin.fir.resolve.fqName
import org.jetbrains.kotlin.modules.TargetId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.platform.CommonPlatforms
import org.jetbrains.kotlin.platform.jvm.JvmPlatforms
import org.jetbrains.kotlin.utils.PathUtil.getJdkClassesRootsFromCurrentJre

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
  configuration.put(NO_JDK, false)
  configuration.addJvmClasspathRoots(getJdkClassesRootsFromCurrentJre())
  // TODO Adding these breaks resolution of JVM types. But they have to go somewhere!
  // configuration.addJvmClasspathRoots(dependencies.toList())
  configuration.addKotlinSourceRoots(sources.map { it.absolutePath })

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
    targetId = TargetId("redwood-schema", "redwood-parser"),
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

  return ParsedProtocolSchema(
    type = type,
    scopes = TODO(),
    widgets = TODO(),
    layoutModifiers = TODO(),
    taggedDependencies = TODO(),
  )
}

private fun FqName.toFqType() = FqType.bestGuess(asString())

private object Annotations {
  val Schema = FqName("app.cash.redwood.schema.Schema")
}
