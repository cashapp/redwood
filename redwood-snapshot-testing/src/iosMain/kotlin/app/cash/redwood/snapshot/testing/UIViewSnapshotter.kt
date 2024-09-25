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
package app.cash.redwood.snapshot.testing

import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit
import platform.CoreGraphics.CGRectMake
import platform.UIKit.UIColor
import platform.UIKit.UIView

/** Snapshot the subject on a white background. */
class UIViewSnapshotter(
  private val callback: UIViewSnapshotCallback,
  private val subject: UIView,
) : Snapshotter {

  override fun snapshot(name: String?) {
    layoutSubject()

    // Unfortunately even with animations forced off, UITableView's animation system breaks
    // synchronous snapshots. The simplest workaround is to delay snapshots one frame.
    callback.verifySnapshot(subject, name, delay = 1.milliseconds.toDouble(DurationUnit.SECONDS))
  }

  /** Do layout without taking a snapshot. */
  fun layoutSubject() {
    subject.layoutIfNeeded()
  }

  companion object {
    fun framed(
      callback: UIViewSnapshotCallback,
      widget: UIView,
    ): UIViewSnapshotter {
      val frame = UIView()
        .apply {
          val screenSize = CGRectMake(0.0, 0.0, 390.0, 844.0) // iPhone 14.

          backgroundColor = UIColor.whiteColor
          setFrame(screenSize)

          widget.setFrame(screenSize)
          addSubview(widget)
        }
      return UIViewSnapshotter(callback, frame)
    }
  }
}
