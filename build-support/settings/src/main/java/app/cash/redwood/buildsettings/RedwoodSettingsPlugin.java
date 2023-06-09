package app.cash.redwood.buildsettings;

import org.gradle.api.Plugin;
import org.gradle.api.initialization.Settings;

@SuppressWarnings("unused") // Invoked reflectively by Gradle.
public class RedwoodSettingsPlugin implements Plugin<Settings> {
  @Override public void apply(Settings target) {
    target.getGradle().allprojects(project -> {
      if (!project.getPath().equals(":")) {
        project.beforeEvaluate(ignored -> {
          project.getPlugins().apply("app.cash.redwood.build");
        });
      }
    });
  }
}
