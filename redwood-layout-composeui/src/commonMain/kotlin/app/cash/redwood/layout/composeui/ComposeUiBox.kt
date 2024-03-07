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
package app.cash.redwood.layout.composeui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import app.cash.redwood.Modifier as RedwoodModifier
import app.cash.redwood.layout.api.Constraint
import app.cash.redwood.layout.api.CrossAxisAlignment
import app.cash.redwood.layout.widget.Box
import app.cash.redwood.ui.Margin
import app.cash.redwood.widget.compose.ComposeWidgetChildren

internal class ComposeUiBox(
  private val backgroundColor: Int = 0,
) : Box<@Composable () -> Unit> {
  override val children = ComposeWidgetChildren()

  private var width by mutableStateOf(Constraint.Wrap)
  private var height by mutableStateOf(Constraint.Wrap)
  private var margin by mutableStateOf(Margin.Zero)
  private var alignment by mutableStateOf(BiasAlignment(-1f, -1f))

  override val value = @Composable {
    Box(computeModifier(), contentAlignment = alignment) {
      children.Render()
    }
  }

  @Composable
  private fun computeModifier(): Modifier {
    var modifier: Modifier = Modifier
    modifier = if (width == Constraint.Fill) {
      modifier.fillMaxWidth()
    } else {
      modifier.wrapContentWidth(Alignment.Start, unbounded = true)
    }
    modifier = if (height == Constraint.Fill) {
      modifier.fillMaxHeight()
    } else {
      modifier.wrapContentHeight(Alignment.Top, unbounded = true)
    }
    modifier = margin.let { margin ->
      modifier.padding(
        start = margin.start.toDp(),
        top = margin.top.toDp(),
        end = margin.end.toDp(),
        bottom = margin.bottom.toDp(),
      )
    }
    if (backgroundColor != 0) {
      modifier = modifier.background(Color(backgroundColor))
    }
    return modifier
  }

  override var modifier: RedwoodModifier = RedwoodModifier

  override fun width(width: Constraint) {
    this.width = width
  }

  override fun height(height: Constraint) {
    this.height = height
  }

  override fun margin(margin: Margin) {
    this.margin = margin
  }

  override fun horizontalAlignment(horizontalAlignment: CrossAxisAlignment) {
    alignment = BiasAlignment(
      horizontalBias = when (horizontalAlignment) {
        CrossAxisAlignment.Start -> -1f
        CrossAxisAlignment.Center -> 0f
        CrossAxisAlignment.End -> 1f
        // TODO Implement stretch with custom Layout.
        CrossAxisAlignment.Stretch -> -1f
        else -> throw AssertionError()
      },
      verticalBias = alignment.verticalBias,
    )
  }

  override fun verticalAlignment(verticalAlignment: CrossAxisAlignment) {
    alignment = BiasAlignment(
      horizontalBias = alignment.horizontalBias,
      verticalBias = when (verticalAlignment) {
        CrossAxisAlignment.Start -> -1f
        CrossAxisAlignment.Center -> 0f
        CrossAxisAlignment.End -> 1f
        // TODO Implement stretch with custom Layout.
        CrossAxisAlignment.Stretch -> -1f
        else -> throw AssertionError()
      },
    )
  }
}
