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
import app.cash.redwood.protocol.ModifierTag
import app.cash.redwood.protocol.PropertyTag
import app.cash.redwood.protocol.WidgetTag
import app.cash.redwood.treehouse.EventListener.Factory
import app.cash.zipline.Call
import app.cash.zipline.CallResult
import app.cash.zipline.Zipline
import app.cash.zipline.ZiplineManifest
import app.cash.zipline.ZiplineService
import kotlin.native.ObjCName

@ObjCName("EventListener", exact = true)
public open class EventListener {
  /**
   * Invoked for each attempt at loading code. This will be followed by a [codeLoadSuccess],
   * [codeLoadFailed], or [codeLoadSkipped] if the code is unchanged.
   *
   * @return any object. This value will be passed back to one of the above functions. The base
   *     function always returns null.
   */
  public open fun codeLoadStart(): Any? = null

  /**
   * Invoked when code is successfully downloaded and initialized.
   *
   * @param startValue the value returned by [codeLoadStart] for the start of this call. This
   *   is null unless [codeLoadStart] is overridden to return something else.
   */
  public open fun codeLoadSuccess(
    manifest: ZiplineManifest,
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
    exception: Exception,
    startValue: Any?,
  ) {
  }

  /**
   * Invoked when code is unloaded because it is no longer needed. Typically this occurs when a hot
   * code update is applied.
   */
  public open fun codeUnloaded() {
  }

  /**
   * Invoked on a request to create an unknown widget [kind].
   */
  public open fun onUnknownWidget(
    tag: WidgetTag,
  ) {
  }

  /**
   * Invoked on a request to create an unknown modifier [tag].
   */
  public open fun onUnknownModifier(
    tag: ModifierTag,
  ) {
  }

  /**
   * Invoked on a request to manipulate unknown children [tag] for the specified widget [kind].
   */
  public open fun onUnknownChildren(
    widgetTag: WidgetTag,
    tag: ChildrenTag,
  ) {
  }

  /**
   * Invoked on a request to set an unknown property [tag] for the specified widget [kind].
   */
  public open fun onUnknownProperty(
    widgetTag: WidgetTag,
    tag: PropertyTag,
  ) {
  }

  /** Invoked on a request to process an unknown event [tag] for the specified widget [widgetTag]. */
  public open fun onUnknownEvent(
    widgetTag: WidgetTag,
    tag: EventTag,
  ) {
  }

  /** Invoked for an event whose node [id] is unknown. */
  public open fun onUnknownEventNode(
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
    url: String,
  ): Any? = null

  /**
   * Invoked when a network download completes successfully.
   *
   * @param startValue the value returned by [downloadStart] for the start of this call. This
   *   is null unless [downloadStart] is overridden to return something else.
   */
  public open fun downloadSuccess(
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
    url: String,
    exception: Exception,
    startValue: Any?,
  ) {
  }

  /**
   * Invoked when a the manifest verifier successfully verifies a key. Manifest verification
   * failures are signaled with [codeLoadFailed].
   */
  public open fun manifestVerified(
    manifest: ZiplineManifest,
    verifiedKey: String,
  ) {
  }

  /**
   * Invoked when a module load starts. This is the process of loading code into QuickJS.
   *
   * @return any object. This value will be passed back to [moduleLoadEnd] when the call is
   *   completed. The base function always returns null.
   */
  public open fun moduleLoadStart(
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
    applicationName: String,
    startValue: Any?,
  ) {
  }

  /**
   * Invoked when the manifest couldn't be decoded as JSON. For example, this might occur if there's
   * a captive portal on the network.
   */
  public open fun manifestParseFailed(
    exception: Exception,
  ) {
  }

  /**
   * Invoked when something calls [Zipline.bind], or a service is sent via an API.
   */
  public open fun bindService(
    name: String,
    service: ZiplineService,
  ) {
  }

  /**
   * Invoked when something calls [Zipline.take], or a service is received via an API.
   */
  public open fun takeService(
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
    call: Call,
  ): Any? = null

  /**
   * Invoked when a service function call completes.
   *
   * @param startValue the value returned by [callStart] for the start of this call. This is null
   *   unless [callStart] is overridden to return something else.
   */
  public open fun callEnd(
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
    name: String,
  ) {
  }

  /**
   * Invoked when [app] has thrown an uncaught exception.
   *
   * This indicates an unrecoverable software bug. Development implementations should report the
   * exception to the developer. Production implementations should post the exception to a bug
   * tracking service.
   *
   * When a Treehouse app fails its current [Zipline] instance is canceled so no further code will
   * execute. A new [Zipline] will start when new code available, or when the app is restarted.
   */
  public open fun uncaughtException(
    exception: Throwable,
  ) {
  }

  public fun interface Factory {
    /**
     * Returns an event listener that receives the events of a specific code session. Each code
     * session includes a single [Zipline] instance, unless code loading fails, in which case there
     * will be no [Zipline] instance.
     */
    public fun create(
      app: TreehouseApp<*>,
      manifestUrl: String?,
    ): EventListener
  }

  public companion object {
    public val NONE: Factory = Factory { app, manifestUrl ->
      EventListener()
    }
  }
}
