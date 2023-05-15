/*
 * Copyright (C) 2020 Square, Inc.
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
package app.cash.redwood.lazylayout.uiview

import kotlinx.cinterop.ExportObjCClass
import kotlinx.cinterop.ObjCAction
import kotlinx.cinterop.cstr
import platform.Foundation.NSSelectorFromString
import platform.UIKit.UIControl
import platform.UIKit.UIControlEvents
import platform.darwin.NSObject
import platform.objc.OBJC_ASSOCIATION_RETAIN
import platform.objc.objc_setAssociatedObject

/**
 * Thanks to this magic incantation from:
 * https://github.com/icerockdev/moko-mvvm/blob/e8d66557499e568dc7bfb9bf6c8c4782a8790f09/mvvm-livedata/src/iosMain/kotlin/dev/icerock/moko/mvvm/utils/UIControlExt.kt
 */

internal fun <T : UIControl> T.setEventHandler(
  event: UIControlEvents,
  lambda: T.() -> Unit,
) {
  val lambdaTarget = ControlLambdaTarget(lambda)
  val action = NSSelectorFromString("action:")

  addTarget(
    target = lambdaTarget,
    action = action,
    forControlEvents = event,
  )

  objc_setAssociatedObject(
    `object` = this,
    key = "event$event".cstr,
    value = lambdaTarget,
    policy = OBJC_ASSOCIATION_RETAIN,
  )
}

@ExportObjCClass
private class ControlLambdaTarget<T : UIControl>(
  private val lambda: T.() -> Unit,
) : NSObject() {
  @ObjCAction
  fun action(sender: UIControl) {
    @Suppress("UNCHECKED_CAST")
    lambda(sender as T)
  }
}
