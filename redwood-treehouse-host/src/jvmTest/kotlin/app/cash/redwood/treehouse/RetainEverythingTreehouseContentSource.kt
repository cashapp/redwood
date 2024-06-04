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
package app.cash.redwood.treehouse

import com.example.redwood.testapp.treehouse.TestAppPresenter

/** A content source that keeps a reference to everything it sees, for defensive leak testing. */
class RetainEverythingTreehouseContentSource : TreehouseContentSource<TestAppPresenter> {
  private var app: TestAppPresenter? = null

  override fun get(app: TestAppPresenter): ZiplineTreehouseUi {
    this.app = app
    return app.launchForTester()
  }
}
