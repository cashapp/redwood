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
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import org.gradle.testkit.runner.GradleRunner
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(TestParameterInjector::class)
class FixtureTest {
  @Test fun builds(
    @TestParameter(
      "schema-jvm",
      "schema-multiplatform",
    )
    fixtureName: String,
  ) {
    fixtureGradleRunner(fixtureName).build()
  }

  @Test fun schemaNoJvmFails() {
    val result = fixtureGradleRunner("schema-no-jvm").buildAndFail()
    assertThat(result.output).contains(
      "Redwood schema plugin requires a jvm() target when used with Kotlin multiplatform"
    )
  }

  @Test fun composeUiAppPackagingSucceeds() {
    // If our dependency substitution did not work the D8 step would fail with duplicate classes.
    fixtureGradleRunner("compose-ui", "assemble").build()
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
