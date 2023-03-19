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

/**
 * A UI built as an interactive widget tree, that may or may not be actively running, or bound to an
 * on-screen display.
 *
 * This content may be bound and unbound multiple times. For example, if its target view is
 * obscured, or removed it may be unbound, only to be rebound later to the same view or another
 * view. Whether state of the content is retained between bind and unbind cycles is an
 * implementation detail. In practice this will work similarly to refreshing a webpage: changes
 * applied using the content are retained but widget state like scroll positions and selection
 * bounds are lost.
 *
 * Calling [bind] may not immediately yield a ready widget tree; the content source may require
 * work to prepare its UI such as downloading code or asynchronous calls to a worker thread.
 *
 * Content must be unbound after use.
 */
public interface Content {
  /**
   * It is an error to bind multiple views simultaneously.
   *
   * This function may only be invoked on [TreehouseDispatchers.ui].
   */
  public fun bind(view: TreehouseView)

  /**
   * Calling [unbind] without a bound view is safe.
   *
   * This function may only be invoked on [TreehouseDispatchers.ui].
   */
  public fun unbind()
}
