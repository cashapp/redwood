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

import app.cash.redwood.widget.Widget
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

@OptIn(ExperimentalObjCName::class)
@ObjCName("DynamicContentWidgetFactory", exact = true)
public interface DynamicContentWidgetFactory<W : Any> {
  public fun Loading(): Loading<W>
  public fun Crashed(): Crashed<W>
}

@OptIn(ExperimentalObjCName::class)
@ObjCName("Loading", exact = true)
public interface Loading<W : Any> : Widget<W>

@OptIn(ExperimentalObjCName::class)
@ObjCName("Crashed", exact = true)
public interface Crashed<W : Any> : Widget<W> {
  public fun uncaughtException(uncaughtException: Throwable)
  public fun restart(restart: () -> Unit)
}
