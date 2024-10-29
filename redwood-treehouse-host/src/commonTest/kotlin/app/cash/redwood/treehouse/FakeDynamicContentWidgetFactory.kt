/*
 * Copyright (C) 2024 Square, Inc.
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

import app.cash.redwood.Modifier
import app.cash.redwood.testing.WidgetValue
import app.cash.redwood.widget.WidgetSystem

class FakeDynamicContentWidgetFactory : DynamicContentWidgetFactory<WidgetValue> {
  override fun Loading(): Loading<WidgetValue> = FakeLoading()
  override fun Crashed(): Crashed<WidgetValue> = FakeCrashed()

  private class FakeLoading : Loading<WidgetValue> {
    override val value: WidgetValue = LoadingValue
    override var modifier: Modifier = Modifier
  }

  private data object LoadingValue : WidgetValue {
    override val modifier: Modifier = Modifier
    override fun <W : Any> toWidget(widgetSystem: WidgetSystem<W>) = error("unexpected call")
    override fun toDebugString(): String = "Loading"
  }

  private class FakeCrashed : Crashed<WidgetValue> {
    private lateinit var uncaughtException: Throwable
    override var modifier: Modifier = Modifier
    override val value: WidgetValue
      get() = CrashedValue(uncaughtException)

    override fun uncaughtException(uncaughtException: Throwable) {
      this.uncaughtException = uncaughtException
    }

    override fun restart(restart: () -> Unit) {
    }
  }

  private data class CrashedValue(
    private val uncaughtException: Throwable,
  ) : WidgetValue {
    override val modifier: Modifier = Modifier
    override fun <W : Any> toWidget(widgetSystem: WidgetSystem<W>) = error("unexpected call")
    override fun toDebugString(): String {
      // Canonicalize "java.lang.Exception(boom!)" to "kotlin.Exception(boom!)".
      val exceptionString = uncaughtException.toString().replace("java.lang.", "kotlin.")
      return "Crashed($exceptionString)"
    }
  }
}
