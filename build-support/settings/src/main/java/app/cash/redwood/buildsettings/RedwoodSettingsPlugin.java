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
package app.cash.redwood.buildsettings;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.initialization.Settings;

@SuppressWarnings("unused") // Invoked reflectively by Gradle.
public class RedwoodSettingsPlugin implements Plugin<Settings> {
  @Override
  public void apply(Settings target) {
    Action<Project> applyRedwoodPlugin =
        project -> project.getPlugins().apply("app.cash.redwood.build");

    target
        .getGradle()
        .allprojects(
            project -> {
              if (project.getPath().equals(":")) {
                // The root project needs to evaluate the buildscript classpath before applying.
                // Once we move to the plugins DSL in the main build we can remove this conditional.
                project.afterEvaluate(applyRedwoodPlugin);
              } else {
                project.beforeEvaluate(applyRedwoodPlugin);
              }
            });
  }
}
