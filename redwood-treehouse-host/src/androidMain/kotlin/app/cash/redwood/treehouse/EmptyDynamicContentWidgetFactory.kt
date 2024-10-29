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

import android.content.Context
import android.view.View
import app.cash.redwood.Modifier

internal class EmptyDynamicContentWidgetFactory(
  private val context: Context,
) : DynamicContentWidgetFactory<View> {
  override fun Loading(): Loading<View> = EmptyLoading(context)

  override fun Crashed(): Crashed<View> = EmptyCrashed(context)

  class EmptyLoading(context: Context) : Loading<View> {
    override val value = View(context)
    override var modifier: Modifier = Modifier
  }

  class EmptyCrashed(context: Context) : Crashed<View> {
    override val value = View(context)
    override var modifier: Modifier = Modifier

    override fun uncaughtException(uncaughtException: Throwable) {
    }

    override fun restart(restart: () -> Unit) {
    }
  }
}
