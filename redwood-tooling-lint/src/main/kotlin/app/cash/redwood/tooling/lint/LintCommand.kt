/*
 * Copyright (C) 2022 Square, Inc.
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
@file:JvmName("Main")
@file:Suppress("UnstableApiUsage" /* Lint 🙄 */)

package app.cash.redwood.tooling.lint

import com.android.tools.lint.LintResourceRepository.Companion.EmptyRepository
import com.android.tools.lint.LintStats
import com.android.tools.lint.UastEnvironment
import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.client.api.LintClient
import com.android.tools.lint.client.api.LintDriver
import com.android.tools.lint.client.api.LintRequest
import com.android.tools.lint.client.api.ResourceRepositoryScope
import com.android.tools.lint.detector.api.CURRENT_API
import com.android.tools.lint.detector.api.Context
import com.android.tools.lint.detector.api.Desugaring
import com.android.tools.lint.detector.api.Incident
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Platform
import com.android.tools.lint.detector.api.Project
import com.android.tools.lint.detector.api.Scope.Companion.JAVA_FILE_SCOPE
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.Severity.ERROR
import com.android.tools.lint.detector.api.TextFormat
import com.android.tools.lint.helpers.DefaultUastParser
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import com.intellij.pom.java.LanguageLevel
import com.intellij.pom.java.LanguageLevel.JDK_1_7
import java.io.File
import org.jetbrains.kotlin.cli.jvm.config.addJvmClasspathRoots
import org.jetbrains.kotlin.config.JVMConfigurationKeys
import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.config.languageVersionSettings
import org.jetbrains.kotlin.utils.PathUtil.getJdkClassesRootsFromCurrentJre

internal class LintCommand : CliktCommand(name = "check") {
  private val projectDirectory by argument("PROJECT_DIR")
    .file()
  private val sourceDirectories by option("-s", "--sources", metavar = "DIR")
    .file()
    .multiple(required = true)
  private val classpath by option("-cp", "--class-path")
    .convert { it.split(File.pathSeparator).map(::File) }
    .default(emptyList())

  override fun run() {
    val uastConfig = UastEnvironment.Configuration.create().apply {
      javaLanguageLevel = JDK_1_7
      // UAST warns when you give it a directory that does not exist.
      addSourceRoots(sourceDirectories.filter(File::exists))
      addClasspathRoots(classpath)
      kotlinCompilerConfig.addJvmClasspathRoots(getJdkClassesRootsFromCurrentJre())
      kotlinCompilerConfig.put(JVMConfigurationKeys.NO_JDK, false)
    }
    val uastEnvironment = UastEnvironment.create(uastConfig)
    val ideaProject = uastEnvironment.ideaProject

    val sourceFiles = sourceDirectories.flatMap {
      it.walk().filter(File::isFile)
    }

    val client = RedwoodLintClient(ideaProject, uastEnvironment, projectDirectory, sourceFiles)
    val incidents = client.run()
    val stats = LintStats.create(incidents, emptyList())

    if (stats.errorCount > 0) {
      for (incident in incidents) {
        if (incident.severity.isError) {
          System.err.println(incident)
        }
      }
      throw ProgramResult(1)
    }
  }
}

private class RedwoodIssueRegistry : IssueRegistry() {
  override val api get() = CURRENT_API
  override val issues get() = emptyList<Issue>()
}

private class RedwoodProject(
  client: LintClient,
  projectDir: File,
  private val sources: List<File>,
) : Project(client, projectDir, null) {
  init {
    library = true
    directLibraries = emptyList()
  }

  override fun initialize() {
    // Not calling super as it is for Android projects only.
  }

  override fun getJavaSourceFolders() = sources
  override fun getSubset() = null // Check whole project.
  override fun isGradleProject() = false
  override fun isAndroidProject() = false
  override fun getDesugaring() = Desugaring.NONE
  override fun getManifestFiles() = emptyList<File>()
  override fun getProguardFiles() = emptyList<File>()
  override fun getResourceFolders() = emptyList<File>()
  override fun getAssetFolders() = emptyList<File>()
  override fun getGeneratedSourceFolders() = emptyList<File>()
  override fun getGeneratedResourceFolders() = emptyList<File>()
  override fun getTestSourceFolders() = emptyList<File>()
  override fun getTestLibraries() = emptyList<File>()
  override fun getTestFixturesSourceFolders() = emptyList<File>()
  override fun getTestFixturesLibraries() = emptyList<File>()
  override fun getPropertyFiles() = emptyList<File>()
  override fun getGradleBuildScripts() = emptyList<File>()
  override fun getJavaClassFolders() = emptyList<File>()
  override fun getJavaLibraries(includeProvided: Boolean) = emptyList<File>()
}

private class RedwoodLintClient(
  private val ideaProject: com.intellij.openapi.project.Project,
  private val uastEnvironment: UastEnvironment,
  private val projectDir: File,
  private val sourceFiles: List<File>,
) : LintClient("redwood-lint") {
  private var run = false
  private val incidents = mutableListOf<Incident>()

  fun run(): List<Incident> {
    check(!run) { "run() can only be called once per client instance" }
    run = true

    val request = LintRequest(this, sourceFiles)
    request.setProjects(listOf(RedwoodProject(this, projectDir, sourceFiles)))
    request.setScope(JAVA_FILE_SCOPE)
    request.setPlatform(Platform.UNSPECIFIED)
    request.setReleaseMode(false)

    val driver = LintDriver(RedwoodIssueRegistry(), this, request)
    driver.analyze()

    return incidents
  }

  override fun readFile(file: File) = file.readText()
  override val xmlParser get() = throw UnsupportedOperationException()
  override fun getGradleVisitor() = throw UnsupportedOperationException()
  override fun getResources(project: Project, scope: ResourceRepositoryScope) = EmptyRepository
  override fun getUastParser(project: Project?) = object : DefaultUastParser(project, ideaProject) {
    override fun prepare(
      contexts: List<JavaContext>,
      javaLanguageLevel: LanguageLevel?,
      kotlinLanguageLevel: LanguageVersionSettings?,
    ): Boolean {
      if (kotlinLanguageLevel != null) {
        uastEnvironment.kotlinCompilerConfig.languageVersionSettings = kotlinLanguageLevel
      }

      val kotlinFiles = contexts.map(JavaContext::file).filter { it.path.endsWith(".kt") }
      uastEnvironment.analyzeFiles(kotlinFiles)

      return super.prepare(contexts, javaLanguageLevel, kotlinLanguageLevel)
    }
  }

  override fun report(
    context: Context,
    incident: Incident,
    format: TextFormat,
  ) {
    incidents += incident
  }

  override fun log(
    severity: Severity,
    exception: Throwable?,
    format: String?,
    vararg args: Any,
  ) {
    val ps = if (severity == ERROR) System.err else System.out
    ps.println("$severity ${format?.format(*args) ?: ""}")
  }
}
