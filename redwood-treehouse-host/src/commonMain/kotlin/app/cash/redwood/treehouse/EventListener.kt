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

import app.cash.redwood.protocol.ChildrenTag
import app.cash.redwood.protocol.EventTag
import app.cash.redwood.protocol.Id
import app.cash.redwood.protocol.LayoutModifierTag
import app.cash.redwood.protocol.PropertyTag
import app.cash.redwood.protocol.WidgetTag
import app.cash.zipline.Call
import app.cash.zipline.CallResult
import app.cash.zipline.Zipline
import app.cash.zipline.ZiplineService
import kotlin.native.ObjCName

@ObjCName("EventListener", exact = true)
public open class EventListener {
  /**
   * Invoked each time a [TreehouseApp] is created. When this is triggered the app may not yet have
   * any code loaded; but it will always attempt to load code.
   */
  public open fun appStart(
    app: TreehouseApp<*>,
  ) {
  }

  /**
   * Invoked with [TreehouseApp.cancel] when the application is shut down.
   *
   * This is different from [codeUnloaded] which occurs during hot reloads; this only occurs if the
   * app itself is explicitly closed.
   */
  public open fun appCanceled(
    app: TreehouseApp<*>,
  ) {
  }

  /**
   * Invoked for each attempt at loading code. This will be followed by a [codeLoadSuccess],
   * [codeLoadFailed], or [codeLoadSkipped] if the code is unchanged.
   *
   * @return any object. This value will be passed back to one of the above functions. The base
   *     function always returns null.
   */
  public open fun codeLoadStart(
    app: TreehouseApp<*>,
    manifestUrl: String?,
  ): Any? = null

  /**
   * Invoked when code is successfully downloaded and initialized.
   *
   * @param startValue the value returned by [codeLoadStart] for the start of this call. This
   *   is null unless [codeLoadStart] is overridden to return something else.
   */
  public open fun codeLoadSuccess(
    app: TreehouseApp<*>,
    manifestUrl: String?,
    zipline: Zipline,
    startValue: Any?,
  ) {
  }

  /**
   * Invoked when a code load is skipped because the code hasn't changed.
   *
   * @param startValue the value returned by [codeLoadStart] for the start of this call. This
   *   is null unless [codeLoadStart] is overridden to return something else.
   */
  public open fun codeLoadSkipped(
    app: TreehouseApp<*>,
    manifestUrl: String?,
    startValue: Any?,
  ) {
  }

  /**
   * Invoked when a code load fails.
   *
   * @param startValue the value returned by [codeLoadStart] for the start of this call. This
   *   is null unless [codeLoadStart] is overridden to return something else.
   */
  public open fun codeLoadFailed(
    app: TreehouseApp<*>,
    manifestUrl: String?,
    exception: Exception,
    startValue: Any?,
  ) {
  }

  /**
   * Invoked when code is unloaded because it is no longer needed. Typically this occurs when a hot
   * code update is applied.
   */
  public open fun codeUnloaded(
    app: TreehouseApp<*>,
    zipline: Zipline,
  ) {
  }

  /**
   * Invoked on a request to create an unknown widget [kind].
   */
  public open fun onUnknownWidget(
    app: TreehouseApp<*>,
    tag: WidgetTag,
  ) {
  }

  /**
   * Invoked on a request to create an unknown layout modifier [tag].
   */
  public open fun onUnknownLayoutModifier(
    app: TreehouseApp<*>,
    tag: LayoutModifierTag,
  ) {
  }

  /**
   * Invoked on a request to manipulate unknown children [tag] for the specified widget [kind].
   */
  public open fun onUnknownChildren(
    app: TreehouseApp<*>,
    widgetTag: WidgetTag,
    tag: ChildrenTag,
  ) {
  }

  /**
   * Invoked on a request to set an unknown property [tag] for the specified widget [kind].
   */
  public open fun onUnknownProperty(
    app: TreehouseApp<*>,
    widgetTag: WidgetTag,
    tag: PropertyTag,
  ) {
  }

  /** Invoked on a request to process an unknown event [tag] for the specified widget [widgetTag]. */
  public open fun onUnknownEvent(
    app: TreehouseApp<*>,
    widgetTag: WidgetTag,
    tag: EventTag,
  ) {
  }

  /** Invoked for an event whose node [id] is unknown. */
  public open fun onUnknownEventNode(
    app: TreehouseApp<*>,
    id: Id,
    tag: EventTag,
  ) {
  }

  /**
   * Invoked when a network download starts. This will be followed by [downloadSuccess] or
   * [downloadFailed].
   *
   * @return any object. This value will be passed back to one of the above functions. The base
   *     function always returns null.
   */
  public open fun downloadStart(
    app: TreehouseApp<*>,
    url: String,
  ): Any? = null

  /**
   * Invoked when a network download completes successfully.
   *
   * @param startValue the value returned by [downloadStart] for the start of this call. This
   *   is null unless [downloadStart] is overridden to return something else.
   */
  public open fun downloadSuccess(
    app: TreehouseApp<*>,
    url: String,
    startValue: Any?,
  ) {
  }

  /**
   * Invoked when a network download fails.
   *
   * @param startValue the value returned by [downloadStart] for the start of this call. This
   *   is null unless [downloadStart] is overridden to return something else.
   */
  public open fun downloadFailed(
    app: TreehouseApp<*>,
    url: String,
    exception: Exception,
    startValue: Any?,
  ) {
  }

  /**
   * Invoked when a module load starts. This is the process of loading code into QuickJS.
   *
   * @return any object. This value will be passed back to [moduleLoadEnd] when the call is
   *   completed. The base function always returns null.
   */
  public open fun moduleLoadStart(
    app: TreehouseApp<*>,
    zipline: Zipline,
    moduleId: String,
  ): Any? {
    return null
  }

  /**
   * Invoked when a module load completes.
   *
   * @param startValue the value returned by [moduleLoadStart] for the start of this call. This is
   *   null unless [moduleLoadStart] is overridden to return something else.
   */
  public open fun moduleLoadEnd(
    app: TreehouseApp<*>,
    zipline: Zipline,
    moduleId: String,
    startValue: Any?,
  ) {
  }

  /**
   * Invoked before Zipline runs the host-side initializer for an application.
   *
   * @return any object. This value will be passed back to [initializerEnd] when the call is
   *   completed. The base function always returns null.
   */
  public open fun initializerStart(
    app: TreehouseApp<*>,
    zipline: Zipline,
    applicationName: String,
  ): Any? {
    return null
  }

  /**
   * Invoked after the host-side initializer completes.
   *
   * @param startValue the value returned by [initializerStart] for the start of this call. This is
   *   null unless [initializerStart] is overridden to return something else.
   */
  public open fun initializerEnd(
    app: TreehouseApp<*>,
    zipline: Zipline,
    applicationName: String,
    startValue: Any?,
  ) {
  }

  /**
   * Invoked before Zipline runs the downloaded code's main function.
   *
   * @return any object. This value will be passed back to [mainFunctionStart] when the call is
   *   completed. The base function always returns null.
   */
  public open fun mainFunctionStart(
    app: TreehouseApp<*>,
    zipline: Zipline,
    applicationName: String,
  ): Any? {
    return null
  }

  /**
   * Invoked after Zipline runs the downloaded code's main function.
   *
   * @param startValue the value returned by [mainFunctionStart] for the start of this call. This is
   *   null unless [mainFunctionStart] is overridden to return something else.
   */
  public open fun mainFunctionEnd(
    app: TreehouseApp<*>,
    zipline: Zipline,
    applicationName: String,
    startValue: Any?,
  ) {
  }

  /**
   * Invoked when the manifest couldn't be decoded as JSON. For example, this might occur if there's
   * a captive portal on the network.
   */
  public open fun manifestParseFailed(
    app: TreehouseApp<*>,
    url: String?,
    exception: Exception,
  ) {
  }

  /**
   * Invoked when something calls [Zipline.bind], or a service is sent via an API.
   */
  public open fun bindService(
    app: TreehouseApp<*>,
    name: String,
    service: ZiplineService,
  ) {
  }

  /**
   * Invoked when something calls [Zipline.take], or a service is received via an API.
   */
  public open fun takeService(
    app: TreehouseApp<*>,
    name: String,
    service: ZiplineService,
  ) {
  }

  /**
   * Invoked when a service function is called. This may be invoked for either suspending or
   * non-suspending functions.
   *
   * @return any object. This value will be passed back to [callEnd] when the call is completed. The
   *   base function always returns null.
   */
  public open fun callStart(
    app: TreehouseApp<*>,
    call: Call,
  ): Any? = null

  /**
   * Invoked when a service function call completes.
   *
   * @param startValue the value returned by [callStart] for the start of this call. This is null
   *   unless [callStart] is overridden to return something else.
   */
  public open fun callEnd(
    app: TreehouseApp<*>,
    call: Call,
    result: CallResult,
    startValue: Any?,
  ) {
  }

  /**
   * Invoked when a service is garbage collected without being closed.
   *
   * Note that this method may be invoked after [codeUnloaded].
   */
  public open fun serviceLeaked(
    app: TreehouseApp<*>,
    name: String,
  ) {
  }
}
