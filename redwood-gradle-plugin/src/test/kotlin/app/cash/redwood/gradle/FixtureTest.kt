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
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEmpty
import assertk.assertions.prop
import java.io.File
import org.gradle.testkit.runner.BuildTask
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome.FAILED
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.gradle.testkit.runner.TaskOutcome.UP_TO_DATE
import org.junit.Test

class FixtureTest {
  @Test fun schemaProjectAccessor() {
    val fixtureDir = File("src/test/fixture/schema-project-accessor")
    fixtureGradleRunner(fixtureDir, "clean", "assemble").build()
    assertThat(fixtureDir.resolve("widget/build/generated/redwood").walk().toList()).isNotEmpty()
  }

  @Test fun schemaProjectReference() {
    val fixtureDir = File("src/test/fixture/schema-project-reference")
    fixtureGradleRunner(fixtureDir, "clean", "assemble").build()
    assertThat(fixtureDir.resolve("widget/build/generated/redwood").walk().toList()).isNotEmpty()
  }

  @Test fun schemaApiClean() {
    val fixtureDir = File("src/test/fixture/schema-api-clean")
    fixtureGradleRunner(fixtureDir, "clean", "check").build().let { result ->
      val redwoodApiCheck = result.task(":redwoodApiCheck")!!
      assertThat(redwoodApiCheck).prop(BuildTask::getOutcome).isEqualTo(SUCCESS)
    }
    // Ensure the task is properly incremental.
    fixtureGradleRunner(fixtureDir, "redwoodApiCheck").build().let { result ->
      val redwoodApiCheck = result.task(":redwoodApiCheck")!!
      assertThat(redwoodApiCheck).prop(BuildTask::getOutcome).isEqualTo(UP_TO_DATE)
    }
  }

  @Test fun schemaApiDirty() {
    val fixtureDir = File("src/test/fixture/schema-api-dirty")

    // Ensure we have the original dirty file as the test will overwrite it.
    fixtureDir.resolve("redwood-api.xml.original")
      .copyTo(fixtureDir.resolve("redwood-api.xml"), overwrite = true)

    fixtureGradleRunner(fixtureDir, "clean", "check").buildAndFail().let { result ->
      val redwoodApiCheck = result.task(":redwoodApiCheck")!!
      assertThat(redwoodApiCheck).prop(BuildTask::getOutcome).isEqualTo(FAILED)
      assertThat(result.output).contains(
        """
        |API file does not match!
        |
        |Differences:
        | - Widget(tag=2) is missing property(tag=2) which is part of the Kotlin schema (fixable)
        | - Widget(tag=3) is missing property(tag=2) which is part of the Kotlin schema (fixable)
        |
        |Run 'redwoodApiGenerate' to automatically update the file.
        |
        """.trimMargin(),
      )
    }
    fixtureGradleRunner(fixtureDir, "redwoodApiGenerate").build()
    fixtureGradleRunner(fixtureDir, "redwoodApiCheck").build()
  }

  @Test fun schemaApiMissing() {
    val fixtureDir = File("src/test/fixture/schema-api-missing")

    // Ensure we do not have this file. It may be left over from a previous run/failure.
    fixtureDir.resolve("redwood-api.xml").delete()

    fixtureGradleRunner(fixtureDir, "clean", "check").buildAndFail().let { result ->
      val redwoodApiCheck = result.task(":redwoodApiCheck")!!
      assertThat(redwoodApiCheck).prop(BuildTask::getOutcome).isEqualTo(FAILED)
      assertThat(result.output).contains(
        """
        |API file redwood-api.xml missing!
        |
        |Run 'redwoodApiGenerate' to generate this file.
        |
        """.trimMargin(),
      )
    }
    fixtureGradleRunner(fixtureDir, "redwoodApiGenerate").build()
    fixtureGradleRunner(fixtureDir, "redwoodApiCheck").build()
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
    vararg tasks: String = arrayOf("clean", "build"),
  ): GradleRunner {
    val gradleRoot = File(fixtureDir, "gradle").also { it.mkdir() }
    File("../gradle/wrapper").copyRecursively(File(gradleRoot, "wrapper"), true)

    return GradleRunner.create()
      .withProjectDir(fixtureDir)
      .withArguments(*tasks, "--no-build-cache", "--stacktrace", "-PredwoodVersion=$redwoodVersion")
      .withDebug(true) // Do not use a daemon.
  }
}
