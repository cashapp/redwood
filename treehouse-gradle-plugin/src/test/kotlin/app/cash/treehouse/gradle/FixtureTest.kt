package app.cash.treehouse.gradle

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
      "counter",
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
      "Treehouse schema plugin requires a jvm() target when used with Kotlin multiplatform"
    )
  }

  private fun fixtureGradleRunner(name: String): GradleRunner {
    val fixtureDir = File("src/test/fixture", name)
    val gradleRoot = File(fixtureDir, "gradle").also { it.mkdir() }
    File("../gradle/wrapper").copyRecursively(File(gradleRoot, "wrapper"), true)

    return GradleRunner.create()
      .withProjectDir(fixtureDir)
      .withArguments("clean", "build", "--stacktrace", "-PtreehouseVersion=$treehouseVersion")
  }
}
