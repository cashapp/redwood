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
package app.cash.redwood.ui.basic.uiview

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.Foundation.create
import platform.UIKit.UIImage

internal class RemoteImageLoader {
  private val scope = MainScope()
  private val cache = mutableMapOf<NSURL, UIImage>()

  fun loadImage(url: NSURL, callback: (NSURL, UIImage) -> Unit) {
    cache[url]?.let {
      callback(url, it)
      return
    }

    // This is not a good image loader, but it's a good-enough image loader.
    scope.launch {
      val image = withContext(Dispatchers.IO) {
        NSData.create(url)?.let { data ->
          UIImage.imageWithData(data)
        }
      }
      if (image != null) {
        cache[url] = image
        callback(url, image)
      }
    }
  }
}
