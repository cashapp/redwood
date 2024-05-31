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

interface RedwoodBuildExtension {
  fun targets(group: TargetGroup)

  /** Add the Compose compiler plugin. */
  fun composeCompiler()

  /**
   * Enable artifact publishing and Dokka documentation generation.
   *
   * The published `artifactId` will be set to the project name.
   */
  fun publishing()

  /** Bundle a zip with dependencies and startup scripts for a CLI. */
  fun cliApplication(name: String, mainClass: String)

  /**
   * Bundle a zip of a Zipline application's compiled `.zipline` files ready for embedding.
   *
   * @name Name of the Treehouse application. Will be used to prefix the Zipline manifest file.
   */
  fun ziplineApplication(name: String)

  /** Consume a Zipline application in an Android application and embed it within assets. */
  fun embedZiplineApplication(dependencyNotation: Any)
}

enum class TargetGroup {
  /** Common targets supported by core modules which are not specific to any platform. */
  Common,

  /** Same as [Common], but with an additional Android target rather than relying on JVM. */
  CommonWithAndroid,

  /** Tooling only runs on the JVM. */
  Tooling,

  /** All toolkit targets for common modules. Includes JVM but not Android. */
  ToolkitAllWithoutAndroid,
  ToolkitAndroid,
  ToolkitIos,
  ToolkitHtml,
  ToolkitComposeUi,

  /** Guest code which runs inside Treehouse. This also includes the JVM for easier testing. */
  TreehouseGuest,

  /** Host code which supports loading Treehouse guest code. */
  TreehouseHost,
}
