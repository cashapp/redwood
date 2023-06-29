/*
 * Copyright 2021 The Android Open Source Project
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
package app.cash.redwood.lazylayout.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.referentialEqualityPolicy
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import app.cash.redwood.lazylayout.compose.layout.LazyLayoutIntervalContent
import app.cash.redwood.lazylayout.compose.layout.LazyLayoutItemProvider

// Copied from https://github.com/androidx/androidx/blob/a733905d282ecdba574bc5e35d6b0ebf83c82dcd/compose/foundation/foundation/src/commonMain/kotlin/androidx/compose/foundation/lazy/LazyListItemProvider.kt
// Removed support for content types, header indices, item scope, and pinnable items.

internal interface LazyListItemProvider : LazyLayoutItemProvider {
  val itemScope: LazyItemScopeImpl
}

@Composable
internal fun rememberLazyListItemProvider(
  content: LazyListScope.() -> Unit,
): LazyListItemProvider {
  val latestContent = rememberUpdatedState(content)
  return remember(latestContent) {
    LazyListItemProviderImpl(
      latestContent = { latestContent.value },
      itemScope = LazyItemScopeImpl,
    )
  }
}

private class LazyListItemProviderImpl(
  private val latestContent: () -> (LazyListScope.() -> Unit),
  override val itemScope: LazyItemScopeImpl,
) : LazyListItemProvider {
  private val listContent by derivedStateOf(referentialEqualityPolicy()) {
    LazyListIntervalContent(latestContent())
  }

  override val itemCount: Int get() = listContent.itemCount

  @Composable
  override fun Item(index: Int) {
    listContent.withInterval(index) { localIndex, content ->
      content.item(itemScope, localIndex)
    }
  }
}
