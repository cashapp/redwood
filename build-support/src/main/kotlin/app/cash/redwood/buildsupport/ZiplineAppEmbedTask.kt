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
package app.cash.redwood.buildsupport

import app.cash.zipline.ZiplineManifest
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

private const val ZIPLINE_MANIFEST_JSON = "manifest.zipline.json"

internal abstract class ZiplineAppEmbedTask : DefaultTask() {
  @get:InputFiles
  abstract val files: ConfigurableFileCollection

  @get:Input
  abstract val appName: Property<String>

  @get:OutputDirectory
  abstract val outputDirectory: DirectoryProperty

  @TaskAction
  fun execute() {
    val outputDirectory = outputDirectory.get()
    outputDirectory.asFile.apply {
      deleteRecursively()
      mkdirs()
    }

    val fileMap = files.asFileTree.files
      .associateByTo(mutableMapOf()) { it.name }

    val inputManifestFile = checkNotNull(fileMap.remove(ZIPLINE_MANIFEST_JSON)) {
      "No zipline.manifest.json file found in input files ${fileMap.keys}"
    }
    val inputManifest = ZiplineManifest.decodeJson(inputManifestFile.readText())

    // Add a timestamp to the manifest which is required for Zipline to load as an embedded app.
    val outputManifest = inputManifest.copy(
      unsigned = inputManifest.unsigned.copy(
        freshAtEpochMs = System.currentTimeMillis(),
      ),
    )
    val outputManifestFile = outputDirectory.file("${appName.get()}.$ZIPLINE_MANIFEST_JSON").asFile
    outputManifestFile.writeText(outputManifest.encodeJson())

    // Rewrite all .zipline files to their SHA-256 hashes which is how Zipline loads when embedded.
    for (module in outputManifest.modules.values) {
      val inputFile = checkNotNull(fileMap.remove(module.url)) {
        "No ${module.url} file found in input files as specified by the manifest"
      }
      val outputFile = outputDirectory.file(module.sha256.hex()).asFile
      inputFile.copyTo(outputFile)
    }
  }
}
