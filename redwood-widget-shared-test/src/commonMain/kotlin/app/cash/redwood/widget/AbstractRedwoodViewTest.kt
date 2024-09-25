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
package app.cash.redwood.widget

import app.cash.redwood.snapshot.testing.Snapshotter
import app.cash.redwood.snapshot.testing.TestWidgetFactory
import app.cash.redwood.snapshot.testing.text
import kotlin.test.Test

abstract class AbstractRedwoodViewTest<W : Any, R : RedwoodView<W>> {

  abstract val widgetFactory: TestWidgetFactory<W>

  abstract fun redwoodView(): R

  abstract fun snapshotter(redwoodView: R): Snapshotter

  @Test
  fun testSingleChildElement() {
    val redwoodView = redwoodView()
    redwoodView.children.insert(0, widgetFactory.text("Hello"))
    snapshotter(redwoodView).snapshot()
  }
}
