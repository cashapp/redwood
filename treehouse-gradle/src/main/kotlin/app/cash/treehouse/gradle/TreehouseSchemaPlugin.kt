package app.cash.treehouse.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

class TreehouseSchemaPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    var applied = false

    project.plugins.withId("org.jetbrains.kotlin.jvm") {
      applied = true

      val kotlin = project.extensions.getByType(KotlinJvmProjectExtension::class.java)
      kotlin.target.apply {
        compilations.getByName("main") { compilation ->
          compilation.dependencies {
            api("app.cash.treehouse:treehouse-schema-runtime:$treehouseVersion")
          }
        }
      }
    }

    project.afterEvaluate {
      check(applied) {
        "Treehouse schema plugin requires the Kotlin JVM plugin to be applied."
      }
    }
  }
}
