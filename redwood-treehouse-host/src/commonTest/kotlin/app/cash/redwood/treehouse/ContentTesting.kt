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

import kotlinx.coroutines.flow.first

/**
 * Suspends until content is available; either it is already in the view or it is preloaded and a
 * call to [bind] will immediately show this content.
 *
 * @param untilChangeCount the number of changes received to wait for. This is approximately the
 *     number of recompositions that had non-empty updates.
 */
suspend fun Content.awaitContent(untilChangeCount: Int = 1) {
  state.first { it.deliveredChangeCount >= untilChangeCount }
}

suspend fun Content.awaitInitialCodeLoading() {
  state.first { it.deliveredChangeCount == 0 }
}

suspend fun Content.awaitCodeLoaded(loadCount: Int = 1) {
  state.first { it.attached && it.loadCount == loadCount }
}

suspend fun Content.awaitCodeDetached(exceptionMessage: String? = null) {
  state.first { !it.attached && it.uncaughtException?.message == exceptionMessage }
}
