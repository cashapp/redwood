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
package app.cash.redwood.gradle

import app.cash.redwood.gradle.RedwoodComposeExtension.DangerZone
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

internal class RedwoodComposeExtensionImpl(
  private val project: Project,
) : RedwoodComposeExtension, DangerZone {
  private var metricsEnabled = false
  private var reportsEnabled = false

  // Explicit backing property avoids Gradle attempting to reflect on an implicit backing field.
  override val kotlinCompilerPlugin: Property<String> get() = _kotlinCompilerPlugin

  private val _kotlinCompilerPlugin = project.objects.property(String::class.java)
    .convention(composeCompilerVersion)

  override fun dangerZone(body: Action<DangerZone>) {
    body.execute(this)
  }

  override fun enableMetrics() {
    if (metricsEnabled) return
    metricsEnabled = true

    project.tasks.withType(KotlinCompile::class.java).configureEach {
      val dir = project.redwoodReportDir("compose-metrics/${it.name}").get().asFile.absolutePath
      it.compilerOptions.freeCompilerArgs.addAll(
        "-P",
        "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=$dir",
      )
    }
  }

  override fun enableReports() {
    if (reportsEnabled) return
    reportsEnabled = true

    project.tasks.withType(KotlinCompile::class.java).configureEach {
      val dir = project.redwoodReportDir("compose-reports/${it.name}").get().asFile.absolutePath
      it.compilerOptions.freeCompilerArgs.addAll(
        "-P",
        "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=$dir",
      )
    }
  }
}
