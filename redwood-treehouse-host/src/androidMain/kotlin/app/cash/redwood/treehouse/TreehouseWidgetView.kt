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

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.content.res.Configuration.UI_MODE_NIGHT_MASK
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.os.Parcel
import android.os.Parcelable
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.core.graphics.Insets
import app.cash.redwood.protocol.widget.RedwoodView
import app.cash.redwood.protocol.widget.RedwoodView.ReadyForContentChangeListener
import app.cash.redwood.protocol.widget.RedwoodView.WidgetSystem
import app.cash.redwood.ui.Density
import app.cash.redwood.ui.Size
import app.cash.redwood.ui.UiConfiguration
import app.cash.redwood.widget.ViewGroupChildren
import app.cash.redwood.widget.Widget
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@SuppressLint("ViewConstructor")
public class TreehouseWidgetView(
  context: Context,
  override val widgetSystem: WidgetSystem,
) : FrameLayout(context), RedwoodView, Saveable {
  override var readyForContentChangeListener: ReadyForContentChangeListener? = null
    set(value) {
      check(value == null || field == null) { "View already bound to a listener" }
      field = value
    }

  /**
   * Like [View.isAttachedToWindow]. We'd prefer that property but it's false until
   * [onAttachedToWindow] returns and true until [onDetachedFromWindow] returns.
   */
  override var readyForContent: Boolean = false
    private set

  override var stateSnapshotId: StateSnapshot.Id = StateSnapshot.Id(null)
    private set

  override var saveCallback: Saveable.SaveCallback? = null

  private val _children = ViewGroupChildren(this)
  override val children: Widget.Children<View> get() = _children

  private val mutableUiConfiguration = MutableStateFlow(computeUiConfiguration())

  override val uiConfiguration: StateFlow<UiConfiguration>
    get() = mutableUiConfiguration

  init {
    setOnWindowInsetsChangeListener { insets ->
      mutableUiConfiguration.value = computeUiConfiguration(insets = insets.safeDrawing)
    }
  }

  override fun reset() {
    _children.remove(0, _children.widgets.size)

    // Ensure any out-of-band views are also removed.
    removeAllViews()
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    readyForContent = true
    readyForContentChangeListener?.onReadyForContentChanged(this)
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    readyForContent = false
    readyForContentChangeListener?.onReadyForContentChanged(this)
  }

  @SuppressLint("DrawAllocation") // It's only on layout.
  override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
    mutableUiConfiguration.value = computeUiConfiguration()
    super.onLayout(changed, left, top, right, bottom)
  }

  override fun onConfigurationChanged(newConfig: Configuration) {
    super.onConfigurationChanged(newConfig)
    mutableUiConfiguration.value = computeUiConfiguration(config = newConfig)
  }

  override fun generateDefaultLayoutParams(): LayoutParams =
    LayoutParams(MATCH_PARENT, MATCH_PARENT)

  override fun onSaveInstanceState(): Parcelable? {
    val id = UUID.randomUUID().toString()
    val superState = super.onSaveInstanceState()
    saveCallback?.performSave(id)
    return SavedState(superState, id)
  }

  override fun onRestoreInstanceState(state: Parcelable?) {
    state as SavedState
    this.stateSnapshotId = StateSnapshot.Id(state.id)
    super.onRestoreInstanceState(state.superState)
  }

  private fun computeUiConfiguration(
    config: Configuration = context.resources.configuration,
    insets: Insets = rootWindowInsetsCompat.safeDrawing,
    viewportSize: Size = with(Density(resources)) { Size(width.toDp(), height.toDp()) },
  ): UiConfiguration {
    return UiConfiguration(
      darkMode = (config.uiMode and UI_MODE_NIGHT_MASK) == UI_MODE_NIGHT_YES,
      safeAreaInsets = insets.toMargin(Density(resources)),
      viewportSize = viewportSize,
    )
  }

  private class SavedState : BaseSavedState {
    val id: String

    constructor(superState: Parcelable?, id: String) : super(superState) {
      this.id = id
    }
    private constructor(parcel: Parcel) : super(parcel) {
      id = parcel.readString()!!
    }

    override fun writeToParcel(out: Parcel, flags: Int) {
      super.writeToParcel(out, flags)
      out.writeString(id)
    }

    companion object {

      // Android OS relies on CREATOR to restore SavedState
      @JvmField
      val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<SavedState> {
        override fun createFromParcel(parcel: Parcel): SavedState {
          return SavedState(parcel)
        }

        override fun newArray(size: Int): Array<SavedState?> {
          return arrayOfNulls(size)
        }
      }
    }
  }
}
