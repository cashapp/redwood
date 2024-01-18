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
   * Invoked when a code load is skipped because the cached code isn't up-to-date.
   *
   * @param startValue the value returned by [codeLoadStart] for the start of this call. This
   *   is null unless [codeLoadStart] is overridden to return something else.
   */
  public open fun codeLoadSkippedNotFresh(
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
   * Invoked on a request to create an unknown widget with [tag]. This is a schema mismatch and the
   * widget is ignored.
   */
  public open fun unknownWidget(
    tag: WidgetTag,
  ) {
  }

  /**
   * Invoked on a request to create an unknown modifier with [tag]. This is a schema mismatch and
   * the modifier is ignored.
   */
  public open fun unknownModifier(
    tag: ModifierTag,
  ) {
  }

  /**
   * Invoked on a request to manipulate unknown children with [tag] for a widget with [widgetTag].
   * This is a schema mismatch and the child nodes are ignored.
   */
  public open fun unknownChildren(
    widgetTag: WidgetTag,
    tag: ChildrenTag,
  ) {
  }

  /**
   * Invoked on a request to set an unknown property with [tag] for a widget with [widgetTag]. This
   * is a schema mismatch and the property is ignored.
   */
  public open fun unknownProperty(
    widgetTag: WidgetTag,
    tag: PropertyTag,
  ) {
  }

  /**
   * Invoked on a request to process an unknown event with [tag] for a widget with [widgetTag]. This
   * is a schema mismatch and the event is dropped.
   */
  public open fun unknownEvent(
    widgetTag: WidgetTag,
    tag: EventTag,
  ) {
  }

  /**
   * Invoked when an event is received on a widget that no longer exists.
   *
   * This is a normal artifact of the asynchronous event processing used by Treehouse. For example,
   * it will occur if a user is still scrolling a `LazyColumn` when it is removed from a layout. The
   * scroll event is discarded and that's fine.
   */
  public open fun unknownEventNode(
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
   * Invoked when the loader has successfully fetched a manifest, verified it (if necessary), and
   * will proceed to download and load each of its modules.
   */
  public open fun manifestReady(
    manifest: ZiplineManifest,
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
    public val NONE: Factory = Factory { _, _ ->
      EventListener()
    }
  }
}
