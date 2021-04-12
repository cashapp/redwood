package app.cash.treehouse.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget

class TreehouseSchemaPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    var applied = false

    project.plugins.withId("org.jetbrains.kotlin.jvm") {
      applied = true

      val kotlin = project.extensions.getByType(KotlinJvmProjectExtension::class.java)
      kotlin.target.applySchemaAnnotationDependency()
    }
    project.plugins.withId("org.jetbrains.kotlin.multiplatform") {
      applied = true

      val kotlin = project.extensions.getByType(KotlinMultiplatformExtension::class.java)
      kotlin.targets.all { it.applySchemaAnnotationDependency() }

      // TODO check there is a JVM target in an afterEvaluate
    }

    project.afterEvaluate {
      check(applied) {
        "Treehouse schema plugin requires the Kotlin JVM or multiplatform plugin to be applied."
      }
    }
  }

  private fun KotlinTarget.applySchemaAnnotationDependency() {
    compilations.getByName("main") { compilation ->
      compilation.dependencies {
        api("app.cash.treehouse:treehouse-schema-annotations:$treehouseVersion")
      }
    }
  }
}
