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
package app.cash.redwood.layout

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.widget.TextView
import app.cash.paparazzi.DeviceConfig.Companion.PIXEL_6
import app.cash.paparazzi.Paparazzi
import app.cash.redwood.flexcontainer.AlignItems
import app.cash.redwood.flexcontainer.FlexDirection
import app.cash.redwood.flexcontainer.JustifyContent
import app.cash.redwood.layout.api.Padding
import app.cash.redwood.layout.view.ViewFlexContainer
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class ViewFlexContainerTest(
  private val parameters: Parameters,
) {
  companion object {
    @JvmStatic
    @Parameterized.Parameters(name = "{0}")
    fun parameters() = cartesianProduct(
      listOf(
        FlexDirection.Row,
        FlexDirection.Column,
      ),
      listOf(
        AlignItems.FlexStart,
        AlignItems.FlexEnd,
        AlignItems.Center,
        AlignItems.Stretch,
      ),
      listOf(
        JustifyContent.FlexStart,
        JustifyContent.FlexEnd,
        JustifyContent.Center,
        JustifyContent.SpaceBetween,
        JustifyContent.SpaceAround,
        JustifyContent.SpaceEvenly,
      ),
      listOf(
        Padding.Zero,
        Padding(100),
      ),
    ).map {
      // https://github.com/junit-team/junit5/issues/2703
      arrayOf(
        Parameters(
          flexDirection = it[0] as FlexDirection,
          alignItems = it[1] as AlignItems,
          justifyContent = it[2] as JustifyContent,
          padding = it[3] as Padding,
        ),
      )
    }

    class Parameters(
      val flexDirection: FlexDirection,
      val alignItems: AlignItems,
      val justifyContent: JustifyContent,
      val padding: Padding,
    ) {
      override fun toString() = "" +
        "FlexDirection.$flexDirection, " +
        "AlignItems.$alignItems, " +
        "JustifyContent.$justifyContent, " +
        "$padding"
    }
  }

  @get:Rule
  val paparazzi = Paparazzi(
    deviceConfig = PIXEL_6,
    theme = "android:Theme.Material.Light.NoActionBar",
  )

  @Test
  fun `render - `() {
    val items = listOf(
      "The Good, the Bad and the Ugly",
      "Forrest Gump",
      "Fight Club",
      "Inception",
      "The Lord of the Rings: The Two Towers",
    )
    val textViews = items.map { title ->
      TextView(paparazzi.context).apply {
        background = ColorDrawable(Color.GREEN)
        textSize = 18f
        setTextColor(Color.BLACK)
        text = title
      }
    }

    val container = ViewFlexContainer(paparazzi.context, parameters.flexDirection).apply {
      view.background = ColorDrawable(Color.LTGRAY)
      padding(parameters.padding)
      alignItems(parameters.alignItems)
      justifyContent(parameters.justifyContent)
    }

    textViews.forEachIndexed { index, textView ->
      container.children.insert(index, textView)
    }
    paparazzi.snapshot(container.view)
  }
}

private inline fun <reified T> cartesianProduct(vararg lists: List<T>): List<Array<T>> {
  return lists.fold(listOf(emptyArray())) { partials, list ->
    partials.flatMap { partial -> list.map { element -> partial + element } }
  }
}
