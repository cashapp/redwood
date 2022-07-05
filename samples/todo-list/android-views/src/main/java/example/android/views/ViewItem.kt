/*
 * Copyright (C) 2021 Square, Inc.
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

package example.android.views

import android.view.View
import app.cash.redwood.LayoutModifier
import example.android.views.databinding.ItemBinding
import example.schema.widget.Item

class ViewItem(private val binding: ItemBinding) : Item<View> {
  override val value get() = binding.root

  override var layoutModifiers: LayoutModifier = LayoutModifier

  override fun content(content: String) {
    binding.text.text = content
  }

  override fun onComplete(onComplete: (() -> Unit)?) {
    binding.checkbox.setOnCheckedChangeListener { _, isChecked ->
      if (isChecked) {
        onComplete?.invoke()
      }
    }
  }
}
