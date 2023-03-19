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
