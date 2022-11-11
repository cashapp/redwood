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
package example.values

import app.cash.redwood.treehouse.ZiplineTreehouseUi
import app.cash.zipline.ZiplineService
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
class LazyListIntervalContent(
  @Contextual val count: Int,
  @Contextual val itemProvider: Item,
) {

  interface Item : ZiplineService {
    fun get(index: Int): ZiplineTreehouseUi
  }
}
