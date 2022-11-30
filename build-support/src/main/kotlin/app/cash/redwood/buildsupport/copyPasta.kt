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

package app.cash.redwood.buildsupport

import java.nio.charset.StandardCharsets.UTF_8
import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider

object ComposeHelpers {
  @JvmStatic fun TaskContainer.get(packageName: String) = create("composeHelpers", packageName)
}

object FlexContainerHelpers {
  @JvmStatic fun TaskContainer.get(packageName: String) = create("flexContainerHelpers", packageName)
}

private fun TaskContainer.create(fileName: String, packageName: String): TaskProvider<CopyPastaTask> {
  return register(fileName, CopyPastaTask::class.java) {
    it.fileName.set(fileName)
    it.packageName.set(packageName)
  }
}

abstract class CopyPastaTask @Inject constructor(layout: ProjectLayout) : DefaultTask() {
  @get:Input
  abstract val fileName: Property<String>

  @get:Input
  abstract val packageName: Property<String>

  @get:OutputDirectory
  abstract val outputDirectory: DirectoryProperty

  init {
    @Suppress("LeakingThis")
    outputDirectory.convention(
      layout.buildDirectory.zip(fileName) { buildDir, fileName ->
        buildDir.dir("generated").dir("source").dir(fileName)
      }
    )
  }

  @TaskAction fun run() {
    val file = "${fileName.get()}.kt"
    val content = CopyPastaTask::class.java.getResourceAsStream(file)!!
      .use { it.readAllBytes().toString(UTF_8) }
      .replace("package com.example\n", "package ${packageName.get()}\n")

    outputDirectory.get().file(file).asFile.writeText(content)
  }
}
