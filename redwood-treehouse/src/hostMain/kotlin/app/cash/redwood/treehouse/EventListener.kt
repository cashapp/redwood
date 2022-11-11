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

import app.cash.zipline.Call
import app.cash.zipline.CallResult
import app.cash.zipline.ZiplineService

public abstract class EventListener {
  public open fun appCreated(
    app: TreehouseApp<*>,
  ) {
  }

  public open fun appCanceled(
    app: TreehouseApp<*>,
  ) {
  }

  public open fun codeLoadStart(
    app: TreehouseApp<*>,
    manifestUrl: String?,
  ): Any? = null

  public open fun codeLoadSuccess(
    app: TreehouseApp<*>,
    manifestUrl: String?,
    startValue: Any?,
  ) {
  }

  public open fun codeLoadSkipped(
    app: TreehouseApp<*>,
    manifestUrl: String?,
    startValue: Any?,
  ) {
  }

  public open fun codeLoadFailed(
    app: TreehouseApp<*>,
    manifestUrl: String?,
    exception: Exception,
    startValue: Any?,
  ) {
  }

  public open fun onUnknownWidget(
    app: TreehouseApp<*>,
    kind: Int,
  ) {
  }

  public open fun onUnknownLayoutModifier(
    app: TreehouseApp<*>,
    tag: Int,
  ) {
  }

  public open fun onUnknownChildren(
    app: TreehouseApp<*>,
    kind: Int,
    tag: UInt,
  ) {
  }

  public open fun onUnknownProperty(
    app: TreehouseApp<*>,
    kind: Int,
    tag: UInt,
  ) {
  }

  public open fun downloadStart(
    app: TreehouseApp<*>,
    url: String,
  ): Any? = null

  public open fun downloadSuccess(
    app: TreehouseApp<*>,
    url: String,
    startValue: Any?,
  ) {
  }

  public open fun downloadFailed(
    app: TreehouseApp<*>,
    url: String,
    exception: Exception,
    startValue: Any?,
  ) {
  }

  public open fun manifestParseFailed(
    app: TreehouseApp<*>,
    url: String?,
    exception: Exception,
  ) {
  }

  public open fun bindService(
    name: String,
    service: ZiplineService,
  ) {
  }

  public open fun takeService(
    name: String,
    service: ZiplineService,
  ) {
  }

  public open fun callStart(
    call: Call,
  ): Any? = null

  public open fun callEnd(
    call: Call,
    result: CallResult,
    startValue: Any?,
  ) {
  }

  public open fun serviceLeaked(
    name: String,
  ) {
  }

  public companion object {
    public val NONE: EventListener = object : EventListener() {
    }
  }
}
