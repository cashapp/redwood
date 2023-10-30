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

import app.cash.redwood.protocol.EventTag
import app.cash.redwood.protocol.Id
import app.cash.redwood.protocol.WidgetTag
import app.cash.zipline.ZiplineService
import kotlin.native.ObjCName

@ObjCName("AppLifecycle", exact = true)
public interface AppLifecycle : ZiplineService {
  public fun start(host: Host)

  public fun sendFrame(timeNanos: Long)

  public interface Host : ZiplineService {
    public fun requestFrame()

    /** Notify the host that an event was unrecognized and will be ignored. */
    public fun onUnknownEvent(widgetTag: WidgetTag, tag: EventTag)

    /**
     * Notify the host that an event was received for a node that either never existed or that no
     * longer exists. That event will be ignored.
     */
    public fun onUnknownEventNode(id: Id, tag: EventTag)

    /** Handle an uncaught exception. The app is now in an undefined state and must be stopped. */
    public fun handleUncaughtException(exception: Throwable)
  }
}
