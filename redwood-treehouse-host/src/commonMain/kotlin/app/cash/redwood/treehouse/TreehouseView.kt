/*
 * Copyright (C) 2022 Square, Inc.
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
import kotlinx.coroutines.flow.StateFlow

public interface TreehouseView {
  public val children: Widget.Children<*>
  public val hostConfiguration: StateFlow<HostConfiguration>
  public val codeListener: CodeListener
  public val readyForContent: Boolean
  public var readyForContentChangeListener: ReadyForContentChangeListener?

  /** Invoked when new code is loaded. This should at minimum clear all [children]. */
  public fun reset()

  public fun interface ReadyForContentChangeListener {
    /** Called when [TreehouseView.readyForContent] has changed. */
    public fun onReadyForContentChanged(view: TreehouseView)
  }

  public open class CodeListener {
    /**
     * Invoked when the initial code is still loading. This can be used to signal a loading state
     * in the UI before there is anything to display.
     */
    public open fun onInitialCodeLoading() {}

    /**
     * Invoked each time new code is loaded. This is called after the view's old children have
     * been cleared but before the children of the new code have been added.
     */
    public open fun onCodeLoaded(initial: Boolean) {}
  }
}
