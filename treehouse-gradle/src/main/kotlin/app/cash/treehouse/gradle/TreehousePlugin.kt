package app.cash.treehouse.gradle

import app.cash.exhaustive.Exhaustive
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType.androidJvm
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType.common
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType.js
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType.jvm
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType.native
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

class TreehousePlugin : KotlinCompilerPluginSupportPlugin {
  override fun isApplicable(kotlinCompilation: KotlinCompilation<*>) = true

  override fun getCompilerPluginId() = "app.cash.treehouse"

  override fun getPluginArtifact() = SubpluginArtifact(
    "androidx.compose.compiler",
    "compiler",
    composeVersion,
  )

  override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
    kotlinCompilation.dependencies {
      implementation("app.cash.treehouse:treehouse-compose:$treehouseVersion")
    }
    kotlinCompilation.enableIr()

    return kotlinCompilation.target.project.provider { emptyList() }
  }
}

internal fun KotlinCompilation<*>.enableIr() {
  @Exhaustive when (platformType) {
    androidJvm, jvm -> {
      (kotlinOptions as KotlinJvmOptions).useIR = true
    }
    common, js, native -> {
      // Nothing to do!
    }
  }
}
