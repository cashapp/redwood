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
package app.cash.redwood.treehouse

class FakeCodeEventPublisher(
  private val eventLog: EventLog,
) : CodeEventPublisher {
  override fun onInitialCodeLoading(view: TreehouseView<*>) {
    eventLog += "codeListener.onInitialCodeLoading($view)"
  }

  override fun onCodeLoaded(view: TreehouseView<*>, initial: Boolean) {
    eventLog += "codeListener.onCodeLoaded($view, initial = $initial)"
  }

  override fun onCodeDetached(view: TreehouseView<*>, exception: Throwable?) {
    // Canonicalize "java.lang.Exception(boom!)" to "kotlin.Exception(boom!)".
    val exceptionString = exception?.toString()?.replace("java.lang.", "kotlin.")
    eventLog += "codeListener.onCodeDetached($view, $exceptionString)"
  }
}
