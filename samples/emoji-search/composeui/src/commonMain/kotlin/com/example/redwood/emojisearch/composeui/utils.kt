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
package com.example.redwood.emojisearch.composeui

import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.fetch.NetworkFetcher

fun applySingletonImageLoader() {
  SingletonImageLoader.setSafe { context ->
    ImageLoader.Builder(context)
      .components {
        add(NetworkFetcher.Factory())
      }
      .build()
  }
}
