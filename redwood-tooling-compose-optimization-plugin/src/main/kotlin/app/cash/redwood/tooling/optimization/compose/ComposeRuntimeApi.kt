/*
 * Copyright (C) 2025 Square, Inc.
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
package app.cash.redwood.tooling.optimization.compose

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

internal class ComposeRuntimeApi(pluginContext: IrPluginContext) {
  val isTraceInProgress = pluginContext.referenceFunctions(isTraceInProgressId).single()
  val sourceInformationMarkerStart = pluginContext.referenceFunctions(sourceInformationMarkerStartId).single()
  val sourceInformationMarkerEnd = pluginContext.referenceFunctions(sourceInformationMarkerEndId).single()

  companion object {
    fun maybeCreate(pluginContext: IrPluginContext): ComposeRuntimeApi? {
      if (pluginContext.referenceClass(composerId) == null) {
        // Compose runtime is not present on compilation classpath.
        return null
      }
      return ComposeRuntimeApi(pluginContext)
    }

    private val runtimePackageName = FqName("androidx.compose.runtime")
    private val composerId = ClassId(runtimePackageName, Name.identifier("Composer"))
    private val isTraceInProgressId = CallableId(runtimePackageName, Name.identifier("isTraceInProgress"))
    private val sourceInformationMarkerStartId = CallableId(runtimePackageName, Name.identifier("sourceInformationMarkerStart"))
    private val sourceInformationMarkerEndId = CallableId(runtimePackageName, Name.identifier("sourceInformationMarkerEnd"))
  }
}
