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
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity.RELATIVE
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.workers.WorkerExecutor

@CacheableTask
internal abstract class RedwoodSchemaApiCheckTask @Inject constructor(
  private val workerExecutor: WorkerExecutor,
) : DefaultTask() {
  @get:Classpath
  abstract val toolClasspath: ConfigurableFileCollection

  @get:Input
  abstract val useFir: Property<Boolean>

  @get:Classpath
  abstract val sources: ConfigurableFileCollection

  @get:Classpath
  abstract val classpath: ConfigurableFileCollection

  @get:Input
  abstract val schemaType: Property<String>

  @get:InputFiles
  @get:PathSensitive(RELATIVE)
  abstract val apiFile: RegularFileProperty

  /**
   * A dummy file inside the project's build directory which will be created as empty when this
   * task runs. Gradle requires at least one output in order to skip a task as up-to-date if no
   * inputs have changed.
   */
  @get:OutputFile
  abstract val dummyOutputFile: RegularFileProperty

  @TaskAction
  fun run() {
    dummyOutputFile.get().asFile.apply {
      parentFile.mkdirs()
      writeBytes(byteArrayOf())
    }

    val queue = workerExecutor.noIsolation()
    queue.submit(RedwoodSchemaApiWorker::class.java) {
      it.toolClasspath.from(toolClasspath)
      it.useFir.set(useFir)
      it.sources.setFrom(sources)
      it.classpath.setFrom(classpath)
      it.schemaType.set(schemaType)
      it.apiFile.set(apiFile)
      it.mode.set("check")
    }
  }
}

internal abstract class RedwoodSchemaApiGenerateTask @Inject constructor(
  private val workerExecutor: WorkerExecutor,
) : DefaultTask() {
  @get:Classpath
  abstract val toolClasspath: ConfigurableFileCollection

  @get:Input
  abstract val useFir: Property<Boolean>

  @get:Classpath
  abstract val sources: ConfigurableFileCollection

  @get:Classpath
  abstract val classpath: ConfigurableFileCollection

  @get:Input
  abstract val schemaType: Property<String>

  @get:OutputFile
  abstract val apiFile: RegularFileProperty

  @TaskAction
  fun run() {
    val queue = workerExecutor.noIsolation()
    queue.submit(RedwoodSchemaApiWorker::class.java) {
      it.toolClasspath.from(toolClasspath)
      it.useFir.set(useFir)
      it.sources.setFrom(sources)
      it.classpath.setFrom(classpath)
      it.schemaType.set(schemaType)
      it.apiFile.set(apiFile)
      it.mode.set("generate")
    }
  }
}

private interface RedwoodSchemaApiParameters : WorkParameters {
  val toolClasspath: ConfigurableFileCollection
  val useFir: Property<Boolean>
  val sources: ConfigurableFileCollection
  val classpath: ConfigurableFileCollection
  val schemaType: Property<String>
  val mode: Property<String>
  val apiFile: RegularFileProperty
}

private abstract class RedwoodSchemaApiWorker @Inject constructor(
  private val execOperations: ExecOperations,
) : WorkAction<RedwoodSchemaApiParameters> {
  override fun execute() {
    execOperations.javaexec { exec ->
      exec.classpath = parameters.toolClasspath
      exec.mainClass.set("app.cash.redwood.tooling.schema.Main")

      exec.args = buildList {
        add("api")
        add("--mode")
        add(parameters.mode.get())
        add("--file")
        add(parameters.apiFile.get().asFile.absolutePath)
        add("--fix-with")
        add(REDWOOD_API_GENERATE_TASK_NAME)
        if (parameters.useFir.get()) {
          add("--use-fir")
        }
        for (source in parameters.sources.files) {
          add("--source")
          add(source.toString())
        }
        add("--class-path")
        add(parameters.classpath.files.joinToString(File.pathSeparator))
        add(parameters.schemaType.get())
      }
    }
  }
}
