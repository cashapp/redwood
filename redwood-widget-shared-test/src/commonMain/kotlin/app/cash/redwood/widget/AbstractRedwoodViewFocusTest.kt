/*
 * Copyright (C) 2025 Square, Inc.
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
@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE") // for HostFocusDirector.

package app.cash.redwood.widget

import app.cash.redwood.Modifier
import app.cash.redwood.compose.HostFocusDirector
import app.cash.redwood.snapshot.testing.TestWidgetFactory
import app.cash.redwood.snapshot.testing.text
import app.cash.redwood.ui.core.api.FocusRequester
import app.cash.redwood.ui.core.modifier.FocusRequester as FocusRequesterModifier
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlin.test.Test

abstract class AbstractRedwoodViewFocusTest<W : Any, R : RedwoodView<W>> {

  abstract val widgetFactory: TestWidgetFactory<W>

  abstract fun redwoodView(): R

  /** Returns the child element in [redwoodView] that is currently focused. */
  abstract fun getFocused(redwoodView: RedwoodView<W>): Widget<W>?

  @Test
  fun testRequestFocus() {
    val redwoodView = redwoodView()

    val focusDirector = HostFocusDirector(redwoodView)
    val nameFocusRequester = focusDirector.newFocusRequester()
    val colorFocusRequester = focusDirector.newFocusRequester()

    val nameText = widgetFactory.text("name")
    nameText.modifier = Modifier.then(FocusRequesterModifierImpl(nameFocusRequester))
    redwoodView.children.insert(0, nameText)

    val colorText = widgetFactory.text("color")
    colorText.modifier = Modifier.then(FocusRequesterModifierImpl(colorFocusRequester))
    redwoodView.children.insert(1, colorText)

    assertThat(getFocused(redwoodView)).isNull()

    nameFocusRequester.requestFocus()
    assertThat(getFocused(redwoodView)).isEqualTo(nameText)

    colorFocusRequester.requestFocus()
    assertThat(getFocused(redwoodView)).isEqualTo(colorText)
  }

  class FocusRequesterModifierImpl(
    override val value: FocusRequester,
  ) : FocusRequesterModifier
}
