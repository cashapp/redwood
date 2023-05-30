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

import kotlinx.cinterop.ExportObjCClass
import kotlinx.cinterop.ObjCAction
import platform.Foundation.NSRunLoop
import platform.Foundation.NSSelectorFromString
import platform.QuartzCore.CADisplayLink
import platform.darwin.NSObject

@ExportObjCClass
internal class DisplayLinkTarget(
  private val callback: DisplayLinkTarget.() -> Unit,
) : NSObject() {
  private val displayLink: CADisplayLink = CADisplayLink.displayLinkWithTarget(
    target = this,
    selector = NSSelectorFromString(this::onFrame.name),
  )

  /** This function must be public to be a valid candidate for [NSSelectorFromString]. */
  @ObjCAction
  fun onFrame() {
    callback(this)
  }

  fun subscribe() {
    displayLink.addToRunLoop(
      NSRunLoop.currentRunLoop,
      NSRunLoop.currentRunLoop.currentMode,
    )
  }

  fun unsubscribe() {
    displayLink.removeFromRunLoop(
      NSRunLoop.currentRunLoop,
      NSRunLoop.currentRunLoop.currentMode,
    )
  }
}
