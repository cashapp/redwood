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

import app.cash.redwood.protocol.EventSink
import app.cash.redwood.ui.UiConfiguration
import app.cash.zipline.ZiplineService
import kotlin.native.ObjCName
import kotlinx.coroutines.flow.StateFlow

/**
 * Adapt TreehouseComposition to conform the limitations of Zipline interfaces.
 *
 * Most callers shouldn't use this directly; instead use `TreehouseUi`.
 */
@ObjCName("ZiplineTreehouseUi", exact = true)
public interface ZiplineTreehouseUi :
  ZiplineService,
  EventSink {
  public fun start(host: Host)

  public fun snapshotState(): StateSnapshot? = null

  /** Access to the host that presents this UI. */
  public interface Host :
    ZiplineService,
    ChangesSinkService {
    public val uiConfigurations: StateFlow<UiConfiguration>
    public val stateSnapshot: StateSnapshot?

    public fun addOnBackPressedCallback(onBackPressedCallbackService: OnBackPressedCallbackService): CancellableService
  }
}
