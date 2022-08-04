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

import com.google.common.truth.Truth.assertThat
import java.io.File
import org.gradle.testkit.runner.GradleRunner
import org.junit.Test

class FixtureTest {
  @Test fun schemaNoJvmFails() {
    val result = fixtureGradleRunner("schema-no-kotlin-jvm").buildAndFail()
    assertThat(result.output).contains(
      "Redwood schema plugin requires the Kotlin JVM plugin to be applied.",
    )
  }

  @Test fun composeUiAppPackagingSucceeds() {
    // If our dependency substitution did not work the D8 step would fail with duplicate classes.
    fixtureGradleRunner("compose-ui", "assemble").build()
  }

  @Test fun lintNoKotlinFails() {
    val result = fixtureGradleRunner("lint-no-kotlin").buildAndFail()
    assertThat(result.output).contains(
      "'app.cash.redwood.lint' requires a compatible Kotlin plugin to be applied (root project)",
    )
  }

  @Test fun lintAndroidNoKotlinFails() {
    val result = fixtureGradleRunner("lint-android-no-kotlin").buildAndFail()
    assertThat(result.output).contains(
      "'app.cash.redwood.lint' requires a compatible Kotlin plugin to be applied (root project)",
    )
  }

  @Test fun lintAndroid() {
    val result = fixtureGradleRunner("lint-android").build()
    val lintTasks = result.tasks.map { it.path }.filter { it.startsWith(":redwoodLint") }
    assertThat(lintTasks).containsExactly(
      ":redwoodLintDebug",
      ":redwoodLintRelease",
      ":redwoodLint",
    )
  }

  @Test fun lintJs() {
    val result = fixtureGradleRunner("lint-js").build()
    val lintTasks = result.tasks.map { it.path }.filter { it.startsWith(":redwoodLint") }
    assertThat(lintTasks).containsExactly(
      ":redwoodLint",
    )
  }

  @Test fun lintJvm() {
    val result = fixtureGradleRunner("lint-jvm").build()
    val lintTasks = result.tasks.map { it.path }.filter { it.startsWith(":redwoodLint") }
    assertThat(lintTasks).containsExactly(
      ":redwoodLint",
    )
  }

  @Test fun lintMppAndroid() {
    val result = fixtureGradleRunner("lint-mpp-android").build()
    val lintTasks = result.tasks.map { it.path }.filter { it.startsWith(":redwoodLint") }
    assertThat(lintTasks).containsExactly(
      ":redwoodLintAndroidDebug",
      ":redwoodLintAndroidRelease",
      ":redwoodLintJvm",
      ":redwoodLint",
    )
  }

  @Test fun lintMppNoAndroid() {
    val result = fixtureGradleRunner("lint-mpp-no-android").build()
    val lintTasks = result.tasks.map { it.path }.filter { it.startsWith(":redwoodLint") }
    assertThat(lintTasks).containsExactly(
      ":redwoodLintJs",
      ":redwoodLintJvm",
      ":redwoodLint",
    )
  }

  private fun fixtureGradleRunner(
    name: String,
    task: String = "build",
  ): GradleRunner {
    val fixtureDir = File("src/test/fixture", name)
    val gradleRoot = File(fixtureDir, "gradle").also { it.mkdir() }
    File("../gradle/wrapper").copyRecursively(File(gradleRoot, "wrapper"), true)

    return GradleRunner.create()
      .withProjectDir(fixtureDir)
      .withArguments("clean", task, "--stacktrace", "-PredwoodVersion=$redwoodVersion")
      .withDebug(true) // Do not use a daemon.
  }
}
