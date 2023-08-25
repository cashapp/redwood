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
package app.cash.redwood.ui

import dev.drewhamilton.poko.Poko
import kotlinx.serialization.Serializable

@Serializable
@Poko
public class UiConfiguration(
  public val darkMode: Boolean = false,
  public val safeAreaInsets: Margin = Margin.Zero,
  /**
   * The size of the viewport into which the composition is rendering. This could be as lage as the
   * entire screen or as small as an individual view within a larger native screen.
   *
   * This does not offer any information on the size of the individual composables which are
   * rendering within the composition, but is the frame into which they will render. The root
   * composable if stretching to fill the viewport will match this size.
   */
  public val viewportSize: Size = Size.Zero,
  /**
   * The density of the display. This can be used to convert [Dp] within other properties back to
   * raw pixels, if needed.
   */
  public val density: Double = 1.0,
) {
  public companion object
}
