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
package app.cash.redwood.buildsupport

import app.cash.redwood.buildsupport.JsTests.NodeJs
import app.cash.redwood.buildsupport.TargetGroup.Common
import app.cash.redwood.buildsupport.TargetGroup.CommonWithAndroid
import app.cash.redwood.buildsupport.TargetGroup.Tooling
import app.cash.redwood.buildsupport.TargetGroup.ToolkitAllWithoutAndroid
import app.cash.redwood.buildsupport.TargetGroup.ToolkitAndroid
import app.cash.redwood.buildsupport.TargetGroup.ToolkitComposeUi
import app.cash.redwood.buildsupport.TargetGroup.ToolkitHtml
import app.cash.redwood.buildsupport.TargetGroup.ToolkitIos
import app.cash.redwood.buildsupport.TargetGroup.TreehouseCommon
import app.cash.redwood.buildsupport.TargetGroup.TreehouseGuest
import app.cash.redwood.buildsupport.TargetGroup.TreehouseHost
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.gradle.BaseExtension
import com.diffplug.gradle.spotless.SpotlessExtension
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.SonatypeHost
import java.io.File
import kotlinx.validation.ApiValidationExtension
import kotlinx.validation.ExperimentalBCVApi
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Action
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.attributes.Attribute
import org.gradle.api.attributes.java.TargetJvmEnvironment
import org.gradle.api.plugins.AppliedPlugin
import org.gradle.api.plugins.JavaApplication
import org.gradle.api.plugins.JavaPlugin.TEST_TASK_NAME
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Zip
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.AbstractTestTask
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED
import org.jetbrains.dokka.gradle.DokkaTaskPartial
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJsCompile
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation.Companion.TEST_COMPILATION_NAME
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
import org.jetbrains.kotlin.gradle.tasks.FatFrameworkTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

private const val REDWOOD_GROUP_ID = "app.cash.redwood"

// HEY! If you change the major version update release.yaml doc folder.
private const val REDWOOD_VERSION = "0.16.0-SNAPSHOT"

private val isCiEnvironment = System.getenv("CI") == "true"

@Suppress("unused") // Invoked reflectively by Gradle.
class RedwoodBuildPlugin : Plugin<Project> {
  private lateinit var libs: LibrariesForLibs

  override fun apply(target: Project) {
    target.group = REDWOOD_GROUP_ID
    target.version = REDWOOD_VERSION

    libs = target.extensions.getByName("libs") as LibrariesForLibs

    target.extensions.add(
      RedwoodBuildExtension::class.java,
      "redwoodBuild",
      RedwoodBuildExtensionImpl(target),
    )

    target.configureCommonSpotless()
    target.configureCommonTesting()
    target.configureCommonAndroid()
    target.configureCommonKotlin()
  }

  private fun Project.configureCommonSpotless() {
    plugins.apply("com.diffplug.spotless")
    val spotless = extensions.getByName("spotless") as SpotlessExtension
    val licenseHeaderFile = rootProject.file("gradle/license-header.txt")
    spotless.apply {
      // The nested build-support Gradle project contains Java sources. Use our root project to
      // target its sources rather than duplicating the Spotless setup in multiple places.
      if (path == ":") {
        java {
          it.target("build-support/redwood-settings/src/**/*.java")
          it.googleJavaFormat(libs.googleJavaFormat.get().version)
          it.licenseHeaderFile(licenseHeaderFile)
        }
      }

      kotlin {
        // The nested build-support Gradle project contains Kotlin sources. Use our root project to
        // target its sources rather than duplicating the Spotless setup in multiple places.
        if (path == ":") {
          it.target("build-support/src/**/*.kt")
        } else {
          it.target("src/**/*.kt")
          // Avoid 'build' folders within test fixture projects which may contain generated sources.
          it.targetExclude("src/test/fixture/**/build/**")
        }
        it.ktlint(libs.ktlint.get().version)
          .customRuleSets(
            listOf(
              libs.ktlintComposeRules.get().toString(),
            ),
          )
          .editorConfigOverride(
            mapOf(
              // Lowercase names are great for grouping multiple functions and/or types.
              "ktlint_standard_filename" to "disabled",
              // Making something an expression body should be a choice around readability.
              "ktlint_standard_function-expression-body" to "disabled",
              "ktlint_function_naming_ignore_when_annotated_with" to "Composable",
              "ktlint_compose_compositionlocal-allowlist" to "disabled",
            ),
          )
        it.licenseHeaderFile(licenseHeaderFile)
      }
    }
  }

  private fun Project.configureCommonTesting() {
    tasks.withType(AbstractTestTask::class.java).configureEach { task ->
      task.testLogging {
        if (isCiEnvironment) {
          it.events = setOf(FAILED, SKIPPED, PASSED)
        }
        it.exceptionFormat = FULL
      }
    }
  }

  private fun Project.configureCommonAndroid() {
    plugins.withId("com.android.base") {
      val android = extensions.getByName("android") as BaseExtension
      android.apply {
        compileSdkVersion(35)
        compileOptions {
          it.sourceCompatibility = JavaVersion.VERSION_1_8
          it.targetCompatibility = JavaVersion.VERSION_1_8
        }
        defaultConfig {
          it.minSdk = 21
          it.targetSdk = 33
        }
        lintOptions {
          it.isCheckDependencies = true
          it.isCheckReleaseBuilds = false // Full lint runs as part of 'build' task.
        }
      }
    }

    plugins.withId("com.android.application") {
      val android = extensions.getByName("android") as BaseExtension
      android.packagingOptions.apply {
        // Keep native symbols for diagnosing sample application crashes.
        doNotStrip("**/*.so")
      }
      val androidComponents = extensions.getByType(AndroidComponentsExtension::class.java)
      androidComponents.beforeVariants {
        // Disable the release build type for sample applications because we never need it.
        if (it.buildType == "release") {
          it.enable = false
        }
      }
    }

    // Work around Guava problem. See https://github.com/cashapp/paparazzi/issues/906.
    plugins.withId("app.cash.paparazzi") {
      dependencies.constraints {
        it.add("testImplementation", "com.google.guava:guava") {
          it.attributes {
            it.attribute(
              TargetJvmEnvironment.TARGET_JVM_ENVIRONMENT_ATTRIBUTE,
              objects.named(TargetJvmEnvironment::class.java, TargetJvmEnvironment.STANDARD_JVM),
            )
          }
          it.because(
            "LayoutLib and sdk-common depend on Guava's -jre published variant." +
              "See https://github.com/cashapp/paparazzi/issues/906.",
          )
        }
      }
    }
  }

  private fun Project.configureCommonKotlin() {
    tasks.withType(KotlinCompilationTask::class.java).configureEach {
      it.compilerOptions.freeCompilerArgs.addAll(
        // https://kotlinlang.org/docs/whatsnew13.html#progressive-mode
        "-progressive",
        "-Xexpect-actual-classes",
      )
    }

    val javaVersion = JavaVersion.VERSION_1_8
    tasks.withType(KotlinJvmCompile::class.java).configureEach {
      it.compilerOptions {
        jvmTarget.set(JvmTarget.fromTarget(javaVersion.toString()))
        freeCompilerArgs.addAll(
          "-Xjvm-default=all",
        )
      }
    }
    // Kotlin requires the Java compatibility matches.
    tasks.withType(JavaCompile::class.java).configureEach {
      it.sourceCompatibility = javaVersion.toString()
      it.targetCompatibility = javaVersion.toString()
    }

    withKotlinPlugins {
      // Apply opt-in annotations everywhere except the test-app schema where we want to ensure the
      // generated code isn't relying on them (without also generating appropriate opt-ins).
      if (!path.startsWith(":test-app:schema:")) {
        sourceSets.configureEach {
          it.languageSettings.optIn("app.cash.redwood.leaks.RedwoodLeakApi")
          it.languageSettings.optIn("app.cash.redwood.yoga.RedwoodYogaApi")
          it.languageSettings.optIn("kotlin.experimental.ExperimentalObjCName")
          it.languageSettings.optIn("kotlinx.cinterop.BetaInteropApi")
          it.languageSettings.optIn("kotlinx.cinterop.ExperimentalForeignApi")
        }
      }
    }

    plugins.withId("org.jetbrains.kotlin.multiplatform") {
      val kotlin = extensions.getByName("kotlin") as KotlinMultiplatformExtension

      // We set the JVM target (the bytecode version) above for all Kotlin-based Java bytecode
      // compilations, but we also need to set the JDK API version for the Kotlin JVM targets to
      // prevent linking against newer JDK APIs (the Android targets link against the android.jar).
      kotlin.targets.withType(KotlinJvmTarget::class.java) { target ->
        target.compilations.configureEach {
          it.kotlinOptions.freeCompilerArgs += listOf(
            "-Xjdk-release=$javaVersion",
          )
        }
      }

      kotlin.targets.withType(KotlinNativeTarget::class.java) { target ->
        target.binaries.withType(Framework::class.java) {
          it.linkerOpts += "-lsqlite3"
        }
      }

      // Disable the release linking tasks because we never need it for iOS sample applications.
      // TODO Switch to https://youtrack.jetbrains.com/issue/KT-54424 when it is supported.
      kotlin.targets.withType(KotlinNativeTarget::class.java) { target ->
        target.binaries.configureEach {
          if (it.buildType == NativeBuildType.RELEASE) {
            it.linkTaskProvider.configure {
              it.enabled = false
            }
          }
        }
      }
      tasks.withType(FatFrameworkTask::class.java).configureEach {
        if (it.name.contains("Release")) {
          it.enabled = false
        }
      }
    }

    tasks.withType(KotlinJsCompile::class.java) {
      it.compilerOptions.freeCompilerArgs.addAll(
        // https://github.com/JetBrains/compose-multiplatform/issues/3421
        "-Xpartial-linkage=disable",
        // https://github.com/JetBrains/compose-multiplatform/issues/3418
        "-Xklib-enable-signature-clash-checks=false",
        // Translate capturing lambdas into anonymous JS functions rather than hoisting parameters
        // and creating a named sibling function. Only affects targets which produce actual JS.
        "-Xir-generate-inline-anonymous-functions",
      )
    }
  }
}

private class RedwoodBuildExtensionImpl(private val project: Project) : RedwoodBuildExtension {
  override fun targets(modifiedGroup: ModifiedTargetGroup) {
    when (modifiedGroup.group) {
      Common -> {
        project.applyKotlinMultiplatform {
          iosTargets()
          modifiedGroup[JsTests, NodeJs].applyTo(js())
          jvm()
        }
        // Needed for lint in downstream Android projects to analyze this dependency.
        project.plugins.apply("com.android.lint")
      }
      CommonWithAndroid -> {
        project.plugins.apply("com.android.library")
        project.applyKotlinMultiplatform {
          androidTarget().publishLibraryVariants("release")
          iosTargets()
          modifiedGroup[JsTests, NodeJs].applyTo(js())
          jvm()
        }
      }
      Tooling -> {
        project.applyKotlinJvm {
          // Not every project needs this, but it's cheap to do everywhere.
          project.injectTestSourcesForParsing(this)
        }
        // Needed for lint in downstream Android projects to analyze this dependency.
        project.plugins.apply("com.android.lint")
      }
      ToolkitAllWithoutAndroid -> {
        project.applyKotlinMultiplatform {
          iosTargets()
          js().browser()
          jvm()
        }
        // Needed for lint in downstream Android projects to analyze this dependency.
        project.plugins.apply("com.android.lint")
      }
      ToolkitAndroid -> {
        project.plugins.apply("com.android.library")
        project.plugins.apply("org.jetbrains.kotlin.android")
      }
      ToolkitIos -> {
        project.applyKotlinMultiplatform {
          iosTargets()
        }
      }
      ToolkitHtml -> {
        project.applyKotlinMultiplatform {
          js().browser()
        }
      }
      ToolkitComposeUi -> {
        project.plugins.apply("com.android.library")
        project.applyKotlinMultiplatform {
          androidTarget().publishLibraryVariants("release")
          iosTargets()
          jvm()
        }
      }
      TreehouseCommon -> {
        project.plugins.apply("com.android.library")
        project.applyKotlinMultiplatform {
          androidTarget().publishLibraryVariants("release")
          iosTargets()
          js().nodejs()
          jvm()
        }
      }
      TreehouseGuest -> {
        project.applyKotlinMultiplatform {
          js().nodejs()
          jvm() // For easier testing.
        }
      }
      TreehouseHost -> {
        project.plugins.apply("com.android.library")
        project.applyKotlinMultiplatform {
          androidTarget().publishLibraryVariants("release")
          iosTargets()
          jvm()
        }
      }
    }
  }

  private fun Project.injectTestSourcesForParsing(kotlin: KotlinJvmProjectExtension) {
    // In order to simplify writing test schemas, inject the test sources and
    // test classpath as properties into the test runtime. This allows testing
    // the FIR-based parser on sources written inside the test case. Cool!
    tasks.named(TEST_TASK_NAME, Test::class.java).configure {
      val compilation = kotlin.target.compilations.getByName(TEST_COMPILATION_NAME)

      val sources = compilation.defaultSourceSet.kotlin.sourceDirectories.files
      it.systemProperty("redwood.internal.sources", sources.joinToString(File.pathSeparator))

      val classpath = project.configurations.getByName(compilation.compileDependencyConfigurationName).files
      it.systemProperty("redwood.internal.classpath", classpath.joinToString(File.pathSeparator))

      // FIR is heap hungry.
      it.maxHeapSize = "2g"
    }
  }

  override fun targets(group: TargetGroup) {
    targets(ModifiedTargetGroup(group, emptyMap()))
  }

  override fun publishing() {
    val isBom = project.path == ":redwood-bom"

    project.plugins.apply("com.vanniktech.maven.publish")

    val publishing = project.extensions.getByName("publishing") as PublishingExtension
    publishing.apply {
      repositories {
        it.maven {
          it.name = "LocalMaven"
          it.url = project.rootProject.layout.buildDirectory.asFile.get().resolve("localMaven").toURI()
        }

        // Want to push to an internal repository for testing?
        // Set the following properties in ~/.gradle/gradle.properties.
        //
        // internalUrl=YOUR_INTERNAL_URL
        // internalUsername=YOUR_USERNAME
        // internalPassword=YOUR_PASSWORD
        //
        // Then run the following command to publish a new internal release:
        //
        // ./gradlew publishAllPublicationsToInternalRepository -DRELEASE_SIGNING_ENABLED=false
        val internalUrl = project.providers.gradleProperty("internalUrl")
        val internalUsername = project.providers.gradleProperty("internalUsername")
        val internalPassword = project.providers.gradleProperty("internalPassword")
        if (internalUrl.isPresent && internalUsername.isPresent && internalPassword.isPresent) {
          it.maven {
            it.name = "internal"
            it.setUrl(internalUrl)
            it.credentials {
              it.username = internalUsername.get()
              it.password = internalPassword.get()
            }
          }
        }
      }
    }

    val mavenPublishing = project.extensions.getByName("mavenPublishing") as MavenPublishBaseExtension
    mavenPublishing.apply {
      publishToMavenCentral(SonatypeHost.DEFAULT, automaticRelease = true)
      if (project.providers.systemProperty("RELEASE_SIGNING_ENABLED").getOrElse("true").toBoolean()) {
        signAllPublications()
      }

      coordinates(REDWOOD_GROUP_ID, project.name, REDWOOD_VERSION)

      pom { pom ->
        pom.name.set(project.name)
        pom.description.set("Multiplatform reactive UI using Kotlin and Jetpack Compose")
        pom.inceptionYear.set("2020")
        pom.url.set("https://github.com/cashapp/redwood/")

        pom.licenses {
          it.license { license ->
            license.name.set("Apache-2.0")
            license.url.set("https://www.apache.org/licenses/LICENSE-2.0")
            license.distribution.set("repo")
          }
        }

        pom.developers {
          it.developer { developer ->
            developer.id.set("cashapp")
            developer.name.set("CashApp")
            developer.url.set("https://github.com/cashapp")
          }
        }

        pom.scm { scm ->
          scm.url.set("https://github.com/cashapp/redwood/")
          scm.connection.set("scm:git:git://github.com/cashapp/redwood.git")
          scm.developerConnection.set("scm:git:ssh://git@github.com/cashapp/redwood.git")
        }
      }
    }

    if (!isBom) {
      project.plugins.apply("org.jetbrains.dokka")

      // DokkaTaskPartial configures subprojects for multimodule docs
      // All options: https://kotlinlang.org/docs/dokka-gradle.html#configuration-options
      project.tasks.withType(DokkaTaskPartial::class.java) { task ->
        task.dokkaSourceSets.configureEach {
          it.suppressGeneratedFiles.set(false) // document generated code
        }
      }

      // Published modules should be explicit about their API visibility.
      var explicit = false
      project.withKotlinPlugins {
        explicitApi()
        explicit = true
      }
      project.afterEvaluate {
        check(explicit) {
          """Project "${project.path}" has unknown Kotlin plugin which needs explicit API tracking"""
        }
      }

      // Published modules should track their public API.
      project.plugins.apply("org.jetbrains.kotlinx.binary-compatibility-validator")
      val apiValidation = project.extensions.getByName("apiValidation") as ApiValidationExtension

      @OptIn(ExperimentalBCVApi::class)
      apiValidation.apply {
        klib.enabled = true

        // We run API checks on a Mac where all possible Kotlin targets are available.
        // Setting this to true will allow us to catch when targets are removed.
        klib.strictValidation = isCiEnvironment

        nonPublicMarkers += listOf(
          // Codegen-specific API only supports the exact same version as which did the generation.
          "app.cash.redwood.RedwoodCodegenApi",
          // The yoga module is an implementation detail of our layouts.
          "app.cash.redwood.yoga.RedwoodYogaApi",
        )
      }
    }
  }

  override fun cliApplication(name: String, mainClass: String) {
    project.plugins.apply("application")

    val application = project.extensions.getByName("application") as JavaApplication
    application.applicationName = name
    application.mainClass.set(mainClass)

    // A zip and tar are configured by default. We don't care about tar.
    project.tasks.named("distTar").configure { task ->
      task.enabled = false
    }

    // Build an exploded application directory by default for local testing.
    project.tasks.named("assemble").configure { task ->
      task.dependsOn(project.tasks.named("installDist"))
    }

    // If this module is also being published, attach the bundled application zip so that we get
    // free versioned hosting from Maven Central.
    project.plugins.withId("maven-publish") {
      val publishing = project.extensions.getByName("publishing") as PublishingExtension
      publishing.publications.withType(MavenPublication::class.java).configureEach { publication ->
        publication.artifact(project.tasks.named("distZip")) {
          // We must specify a classifier or else the pom's packaging value will change to
          // no longer reflect the primary build artifact.
          it.classifier = "cli"
        }
      }
    }
  }

  override fun ziplineApplication(name: String) {
    var hasZipline = false
    project.afterEvaluate {
      check(hasZipline) {
        "Project ${project.path} must have Zipline plugin to create Zipline application"
      }
    }
    project.plugins.withId("app.cash.zipline") {
      hasZipline = true

      val prepareTask = project.tasks.register("prepareEmbeddedZiplineApp", ZiplineAppEmbedTask::class.java) {
        // Note: This makes assumptions about our setup having a JS target with the default name.
        it.files.from(project.tasks.named("compileProductionExecutableKotlinJsZipline"))
        it.appName.set(name)
        it.outputDirectory.set(project.layout.buildDirectory.dir("treehouse"))
      }

      // Only a single file can be used as an artifact so zip up the compiled contents.
      val zipTask = project.tasks.register("zipEmbeddedZiplineApp", Zip::class.java) {
        it.from(prepareTask)
        it.destinationDirectory.set(project.layout.buildDirectory.dir("libs"))
        it.archiveClassifier.set("zipline")
      }

      val ziplineConfiguration = project.configurations.create("zipline") {
        it.isVisible = false
        it.isCanBeResolved = false
        it.isCanBeConsumed = true
        it.attributes {
          it.attribute(ziplineAttribute, ZIPLINE_ATTRIBUTE_VALUE)
        }
      }
      project.artifacts.add(ziplineConfiguration.name, zipTask)
    }
  }

  override fun embedZiplineApplication(dependencyNotation: Any) {
    var hasApplication = false
    project.afterEvaluate {
      check(hasApplication) {
        "Project ${project.path} must have Android Application plugin to embed Zipline application"
      }
    }
    project.plugins.withId("com.android.application") {
      hasApplication = true

      // Note: This will crash if you call it twice. We don't need this today, so it's not supported.
      val ziplineConfiguration = project.configurations.create("zipline") {
        it.isVisible = false
        it.isCanBeResolved = true
        it.isCanBeConsumed = false
        it.attributes {
          it.attribute(ziplineAttribute, ZIPLINE_ATTRIBUTE_VALUE)
        }
      }
      project.dependencies.add(ziplineConfiguration.name, dependencyNotation)

      val extractTask = project.tasks.register("extractEmbeddedZiplineApplication", ZiplineAppExtractTask::class.java) {
        it.inputFiles.from(ziplineConfiguration)
      }

      val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)
      androidComponents.onVariants {
        val assets = checkNotNull(it.sources.assets) {
          "Project ${project.path} assets must be enabled to embed Zipline application"
        }
        assets.addGeneratedSourceDirectory(extractTask, ZiplineAppExtractTask::outputDirectory)
      }
    }
  }

  override fun TaskContainer.generateComposeHelpers(packageName: String): TaskProvider<CopyPastaTask> {
    return create("composeHelpers", packageName)
  }

  override fun TaskContainer.generateFlexboxHelpers(packageName: String): TaskProvider<CopyPastaTask> {
    return create("flexboxHelpers", packageName)
  }

  private fun TaskContainer.create(fileName: String, packageName: String): TaskProvider<CopyPastaTask> {
    return register(fileName, CopyPastaTask::class.java) {
      it.fileName.set(fileName)
      it.packageName.set(packageName)
    }
  }

  override fun sharedSnapshotTests() {
    project.plugins.apply("com.google.devtools.ksp")
    project.dependencies.add("kspJvm", project.project(":build-support-ksp-processor"))
  }
}

private val ziplineAttribute = Attribute.of("zipline", String::class.java)
private const val ZIPLINE_ATTRIBUTE_VALUE = "yep"

private fun Project.withKotlinPlugins(block: KotlinProjectExtension.() -> Unit) {
  val handler = Action<AppliedPlugin> {
    val kotlin = extensions.getByName("kotlin") as KotlinProjectExtension
    kotlin.block()
  }
  pluginManager.withPlugin("org.jetbrains.kotlin.android", handler)
  pluginManager.withPlugin("org.jetbrains.kotlin.jvm", handler)
  pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform", handler)
}

private fun Project.applyKotlinJvm(block: KotlinJvmProjectExtension.() -> Unit) {
  pluginManager.apply("org.jetbrains.kotlin.jvm")
  val kotlin = extensions.getByType(KotlinJvmProjectExtension::class.java)
  kotlin.block()
}

private fun Project.applyKotlinMultiplatform(block: KotlinMultiplatformExtension.() -> Unit) {
  pluginManager.apply("org.jetbrains.kotlin.multiplatform")
  val kotlin = extensions.getByType(KotlinMultiplatformExtension::class.java)
  kotlin.block()
  kotlin.applyDefaultHierarchyTemplate()
}

private fun KotlinMultiplatformExtension.iosTargets() {
  iosArm64()
  iosSimulatorArm64()
  iosX64()
}
