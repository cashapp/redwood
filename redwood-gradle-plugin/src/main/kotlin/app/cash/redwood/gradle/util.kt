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
package app.cash.redwood.gradle

import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import org.gradle.api.reporting.ReportingExtension

internal fun Project.redwoodDependency(artifactId: String): Any {
  // Indicates when the plugin is applied inside the Redwood repo to Redwood's own modules. This
  // changes dependencies from being external Maven coordinates to internal project references.
  val isInternalBuild = providers.gradleProperty("app.cash.redwood.internal").getOrElse("false").toBoolean()

  return if (isInternalBuild) {
    project(":$artifactId")
  } else {
    "app.cash.redwood:$artifactId:$redwoodVersion"
  }
}

internal fun Project.redwoodGeneratedDir(name: String): Provider<Directory> {
  return layout.buildDirectory.dir("generated/redwood/$name")
}

internal fun Project.redwoodReportDir(name: String): Provider<Directory> {
  return extensions.getByType(ReportingExtension::class.java).baseDirectory.dir("redwood/$name")
}
