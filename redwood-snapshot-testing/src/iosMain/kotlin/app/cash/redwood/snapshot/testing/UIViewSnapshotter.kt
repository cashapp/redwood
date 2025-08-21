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
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGPointMake
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.UIKit.UIColor
import platform.UIKit.UIScrollView
import platform.UIKit.UIView
import platform.UIKit.UIViewNoIntrinsicMetric

/**
 * Snapshot the subject on a white background.
 *
 * This either snapshots the view sized to be full-screen, or it uses [heightConstraint] to size the
 * view to whatever height it requires. Scrolling snapshots are always sized to the screen height.
 */
class UIViewSnapshotter(
  private val callback: UIViewSnapshotCallback,
  private val subject: UIView,
  private val widthConstraint: Constraint = Constraint.Fill,
  private val heightConstraint: Constraint = Constraint.Fill,
) : Snapshotter {

  override fun snapshot(name: String?, scrolling: Boolean) {
    layoutSubject(scrolling)

    // Unfortunately even with animations forced off, UITableView's animation system breaks
    // synchronous snapshots. The simplest workaround is to delay snapshots one frame.
    callback.verifySnapshot(subject, name, delay = 1.milliseconds.toDouble(DurationUnit.SECONDS))

    if (scrolling) {
      var scrollCount = 0
      val scrollView = findScrollView(subject) ?: return
      val contentHeight = scrollView.contentSize.useContents { height }
      val frameHeight = scrollView.frame.useContents { size.height }
      var offset = 0.0
      while (offset + frameHeight < contentHeight) {
        offset = minOf(offset + frameHeight, contentHeight - frameHeight)
        scrollView.setContentOffset(CGPointMake(0.0, offset), false)
        scrollCount++

        check(scrollCount < 15) {
          "This view has been scrolled 15 times! Bad input?"
        }

        callback.verifySnapshot(
          view = subject,
          name = "${name.orEmpty()}_$scrollCount",
          delay = 1.milliseconds.toDouble(DurationUnit.SECONDS),
        )
      }
      scrollView.setContentOffset(CGPointMake(0.0, 0.0), false)
    }
  }

  private fun findScrollView(view: UIView): UIScrollView? {
    if (view is UIScrollView) return view

    for (subview in view.subviews) {
      return findScrollView(subview as UIView) ?: continue
    }

    return null
  }

  /** Do layout without taking a snapshot. */
  fun layoutSubject(scrolling: Boolean = false) {
    require(widthConstraint == Constraint.Fill) {
      "width wrap not yet implemented"
    }

    if (heightConstraint == Constraint.Wrap && !scrolling) {
      val widget = subject.subviews[0] as UIView

      widget.setFrame(CGRectMake(0.0, 0.0, 0.0, 0.0))
      val wrapSize = widget.sizeThatFits(
        screenSize.useContents { CGSizeMake(width, UIViewNoIntrinsicMetric) },
      )

      val frame = wrapSize.useContents { CGRectMake(0.0, 0.0, width, height) }
      subject.setFrame(frame)
      widget.setFrame(frame)
    }

    subject.layoutIfNeeded()
  }

  companion object {
    private val screenSize = CGSizeMake(390.0, 844.0) // iPhone 14.
    private val screenRect = screenSize.useContents { CGRectMake(0.0, 0.0, width, height) }

    fun framed(
      callback: UIViewSnapshotCallback,
      widget: UIView,
      widthConstraint: Constraint = Constraint.Fill,
      heightConstraint: Constraint = Constraint.Fill,
    ): UIViewSnapshotter {
      val frame = UIView()
        .apply {
          backgroundColor = UIColor.whiteColor
          setFrame(screenRect)

          widget.setFrame(screenRect)
          addSubview(widget)
        }
      return UIViewSnapshotter(callback, frame, widthConstraint, heightConstraint)
    }
  }

  enum class Constraint {
    Fill,
    Wrap,
  }
}
