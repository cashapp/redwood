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
import android.os.Parcel
import android.os.Parcelable
import android.view.View
import androidx.activity.OnBackPressedDispatcher as AndroidOnBackPressedDispatcher
import app.cash.redwood.treehouse.TreehouseView.ReadyForContentChangeListener
import app.cash.redwood.treehouse.TreehouseView.WidgetSystem
import app.cash.redwood.widget.RedwoodLayout
import app.cash.treehouse.host.R
import java.util.UUID

@SuppressLint("ViewConstructor")
public open class TreehouseLayout(
  context: Context,
  override val widgetSystem: WidgetSystem<View>,
  androidOnBackPressedDispatcher: AndroidOnBackPressedDispatcher,
  override val dynamicContentWidgetFactory: DynamicContentWidgetFactory<View> =
    EmptyDynamicContentWidgetFactory(context),
) : RedwoodLayout(context, androidOnBackPressedDispatcher),
  TreehouseView<View> {
  override var readyForContentChangeListener: ReadyForContentChangeListener<View>? = null
    set(value) {
      check(value == null || field == null) { "View already bound to a listener" }
      field = value
    }

  /**
   * Like [View.isAttachedToWindow]. We'd prefer that property but it's false until
   * [onAttachedToWindow] returns and true until [onDetachedFromWindow] returns.
   */
  final override var readyForContent: Boolean = false
    private set

  final override var stateSnapshotId: StateSnapshot.Id = StateSnapshot.Id(null)
    private set

  override var saveCallback: TreehouseView.SaveCallback? = null

  init {
    // The view needs to have an ID to participate in instance state saving.
    id = R.id.treehouse_layout
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
