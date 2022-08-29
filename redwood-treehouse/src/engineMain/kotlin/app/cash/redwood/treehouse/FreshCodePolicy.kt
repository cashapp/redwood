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

public enum class FreshCodePolicy {
  /** For development; potentially throws away user state */
  ALWAYS_REFRESH_IMMEDIATELY,

  /** When we know fresh code is on the way, show a loading UI until it's ready */
  WAIT_FOR_FRESH_CODE,

  /** Stale code now is better than fresh code soon. */
  FAST_PIXELS,
}
