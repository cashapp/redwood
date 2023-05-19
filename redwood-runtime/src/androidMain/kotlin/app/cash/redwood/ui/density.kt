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
package app.cash.redwood.ui

import android.content.res.Resources

// Android uses 2.75 as a density scale for most recent Pixel devices and iOS
// uses 3. This aligns the two so the generic values used by Redwood layout are
// visually similar on both platforms.
internal actual const val DensityMultiplier = 1.1

/**
 * Create a [Density] from the device's [Resources].
 */
public fun Density(resources: Resources): Density {
  return Density(resources.displayMetrics.density.toDouble())
}
