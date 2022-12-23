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

import androidx.compose.runtime.Composable

/**
 * Drives a UI on the host platform.
 *
 * Implementing classes may also implement [app.cash.zipline.ZiplineScoped], in which case the scope
 * object will be closed immediately after this is closed.
 */
public interface TreehouseUi {
  @Composable
  public fun Show()

  public fun close() {
  }
}
