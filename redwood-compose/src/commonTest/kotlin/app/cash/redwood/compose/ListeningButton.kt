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
package app.cash.redwood.compose

import app.cash.redwood.Modifier
import app.cash.redwood.testing.WidgetValue
import app.cash.redwood.widget.ChangeListener
import com.example.redwood.testing.testing.ButtonValue
import com.example.redwood.testing.widget.Button

class ListeningButton : Button<WidgetValue>, ChangeListener {
  private val changes = ArrayList<String>()
  fun changes(): List<String> {
    val snapshot = changes.toList()
    changes.clear()
    return snapshot
  }

  override fun text(text: String?) {
    changes += "text $text"
  }

  override fun onClick(onClick: (() -> Unit)?) {
    changes += "onClick ${onClick != null}"
  }

  override fun onEndChanges() {
    changes += "onEndChanges"
  }

  override var modifier: Modifier
    get() = throw AssertionError()
    set(value) {
      changes += "modifier $value"
    }

  override val value get() = ButtonValue(text = "", onClick = {})
}
