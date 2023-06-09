package app.cash.redwood.buildsettings;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.initialization.Settings;

@SuppressWarnings("unused") // Invoked reflectively by Gradle.
public class RedwoodSettingsPlugin implements Plugin<Settings> {
  @Override public void apply(Settings target) {
    target.getGradle().allprojects(project -> {
      Action<Project> applyRedwoodPlugin = ignored -> {
        project.getPlugins().apply("app.cash.redwood.build");
      };
      if (project.getPath().equals(":")) {
        // The root project needs to evaluate the buildscript classpath before applying. Once we
        // move to using the plugins DSL in the main build this special case can be removed.
        project.afterEvaluate(applyRedwoodPlugin);
      } else {
        project.beforeEvaluate(applyRedwoodPlugin);
      }
    });
  }
}
