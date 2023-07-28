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
package app.cash.redwood.layout.widget

import app.cash.redwood.layout.api.CrossAxisAlignment
import app.cash.redwood.layout.api.MainAxisAlignment

public interface FlexContainer<W : Any> : Row<W>, Column<W> {
  override fun horizontalAlignment(horizontalAlignment: MainAxisAlignment) {
    mainAxisAlignment(horizontalAlignment)
  }

  override fun horizontalAlignment(horizontalAlignment: CrossAxisAlignment) {
    crossAxisAlignment(horizontalAlignment)
  }

  override fun verticalAlignment(verticalAlignment: MainAxisAlignment) {
    mainAxisAlignment(verticalAlignment)
  }

  override fun verticalAlignment(verticalAlignment: CrossAxisAlignment) {
    crossAxisAlignment(verticalAlignment)
  }

  public fun crossAxisAlignment(crossAxisAlignment: CrossAxisAlignment)

  public fun mainAxisAlignment(mainAxisAlignment: MainAxisAlignment)
}
