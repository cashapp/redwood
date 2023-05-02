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

import java.io.File
import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.workers.WorkerExecutor

@CacheableTask
internal abstract class RedwoodSchemaTask @Inject constructor(
  private val workerExecutor: WorkerExecutor,
) : DefaultTask() {
  @get:Classpath
  abstract val toolClasspath: ConfigurableFileCollection

  @get:Classpath
  abstract val classpath: ConfigurableFileCollection

  @get:Input
  abstract val schemaType: Property<String>

  @get:OutputDirectory
  abstract val outputDir: DirectoryProperty

  @TaskAction
  fun run() {
    val queue = workerExecutor.noIsolation()
    queue.submit(RedwoodSchemaWorker::class.java) {
      it.toolClasspath.from(toolClasspath)
      it.classpath.setFrom(classpath)
      it.schemaType.set(schemaType)
      it.outputDir.set(outputDir)
    }
  }
}

private interface RedwoodSchemaParameters : WorkParameters {
  val toolClasspath: ConfigurableFileCollection
  val classpath: ConfigurableFileCollection
  val schemaType: Property<String>
  val outputDir: DirectoryProperty
}

private abstract class RedwoodSchemaWorker @Inject constructor(
  private val execOperations: ExecOperations,
) : WorkAction<RedwoodSchemaParameters> {
  override fun execute() {
    parameters.outputDir.get().asFile.deleteRecursively()

    execOperations.javaexec { exec ->
      exec.classpath = parameters.toolClasspath
      exec.mainClass.set("app.cash.redwood.tooling.schema.Main")

      exec.args = listOf(
        "json",
        "--out",
        parameters.outputDir.get().asFile.absolutePath,
        "--class-path",
        parameters.classpath.files.joinToString(File.pathSeparator),
        parameters.schemaType.get(),
      )
    }
  }
}
