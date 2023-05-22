/*
 * Copyright (C) 2021 Square, Inc.
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
package app.cash.redwood.gradle

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.containsExactly
import assertk.assertions.isNotEmpty
import java.io.File
import org.gradle.testkit.runner.GradleRunner
import org.junit.Test

class FixtureTest {
  @Test fun schemaProjectAccessor() {
    val fixtureDir = File("src/test/fixture/schema-project-accessor")
    fixtureGradleRunner(fixtureDir, "assemble").build()
    assertThat(fixtureDir.resolve("widget/build/generated/redwood").walk().toList()).isNotEmpty()
  }

  @Test fun schemaProjectReference() {
    val fixtureDir = File("src/test/fixture/schema-project-reference")
    fixtureGradleRunner(fixtureDir, "assemble").build()
    assertThat(fixtureDir.resolve("widget/build/generated/redwood").walk().toList()).isNotEmpty()
  }

  @Test fun lintNoKotlinFails() {
    val fixtureDir = File("src/test/fixture/lint-no-kotlin")
    val result = fixtureGradleRunner(fixtureDir).buildAndFail()
    assertThat(result.output).contains(
      "'app.cash.redwood.lint' requires a compatible Kotlin plugin to be applied (root project)",
    )
  }

  @Test fun lintAndroidNoKotlinFails() {
    val fixtureDir = File("src/test/fixture/lint-android-no-kotlin")
    val result = fixtureGradleRunner(fixtureDir).buildAndFail()
    assertThat(result.output).contains(
      "'app.cash.redwood.lint' requires a compatible Kotlin plugin to be applied (root project)",
    )
  }

  @Test fun lintAndroid() {
    val fixtureDir = File("src/test/fixture/lint-android")
    val result = fixtureGradleRunner(fixtureDir).build()
    val lintTasks = result.tasks.map { it.path }.filter { it.startsWith(":redwoodLint") }
    assertThat(lintTasks).containsExactly(
      ":redwoodLintDebug",
      ":redwoodLintRelease",
      ":redwoodLint",
    )
  }

  @Test fun lintJs() {
    val fixtureDir = File("src/test/fixture/lint-js")
    val result = fixtureGradleRunner(fixtureDir).build()
    val lintTasks = result.tasks.map { it.path }.filter { it.startsWith(":redwoodLint") }
    assertThat(lintTasks).containsExactly(
      ":redwoodLint",
    )
  }

  @Test fun lintJvm() {
    val fixtureDir = File("src/test/fixture/lint-jvm")
    val result = fixtureGradleRunner(fixtureDir).build()
    val lintTasks = result.tasks.map { it.path }.filter { it.startsWith(":redwoodLint") }
    assertThat(lintTasks).containsExactly(
      ":redwoodLint",
    )
  }

  @Test fun lintMppAndroid() {
    val fixtureDir = File("src/test/fixture/lint-mpp-android")
    val result = fixtureGradleRunner(fixtureDir).build()
    val lintTasks = result.tasks.map { it.path }.filter { it.startsWith(":redwoodLint") }
    assertThat(lintTasks).containsExactly(
      ":redwoodLintAndroidDebug",
      ":redwoodLintAndroidRelease",
      ":redwoodLintJvm",
      ":redwoodLint",
    )
  }

  @Test fun lintMppNoAndroid() {
    val fixtureDir = File("src/test/fixture/lint-mpp-no-android")
    val result = fixtureGradleRunner(fixtureDir).build()
    val lintTasks = result.tasks.map { it.path }.filter { it.startsWith(":redwoodLint") }
    assertThat(lintTasks).containsExactly(
      ":redwoodLintJs",
      ":redwoodLintJvm",
      ":redwoodLint",
    )
  }

  @Test fun protocolWithoutModifiers() {
    // When no layout modifier are present, the serialization codegen contains unused things.
    // Ensure that it does not produce warnings (which are set to error in this build).
    val fixtureDir = File("src/test/fixture/protocol-no-modifiers")
    fixtureGradleRunner(fixtureDir).build()
  }

  private fun fixtureGradleRunner(
    fixtureDir: File,
    task: String = "build",
  ): GradleRunner {
    val gradleRoot = File(fixtureDir, "gradle").also { it.mkdir() }
    File("../gradle/wrapper").copyRecursively(File(gradleRoot, "wrapper"), true)

    return GradleRunner.create()
      .withProjectDir(fixtureDir)
      .withArguments("clean", task, "--stacktrace", "-PredwoodVersion=$redwoodVersion")
      .withDebug(true) // Do not use a daemon.
  }
}
