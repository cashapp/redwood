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

class RetainEverythingCodeListener(
  private val eventLog: EventLog,
) : CodeListener() {
  private var app: TreehouseApp<*>? = null
  private var view: TreehouseView<*>? = null

  override fun onInitialCodeLoading(app: TreehouseApp<*>, view: TreehouseView<*>) {
    this.app = app
    this.view = view
  }

  override fun onCodeLoaded(app: TreehouseApp<*>, view: TreehouseView<*>, initial: Boolean) {
    this.app = app
    this.view = view
    eventLog += "onCodeLoaded"
  }

  override fun onCodeDetached(app: TreehouseApp<*>, view: TreehouseView<*>, exception: Throwable?) {
    this.app = app
    this.view = view
    eventLog += "onCodeDetached"
  }
}
