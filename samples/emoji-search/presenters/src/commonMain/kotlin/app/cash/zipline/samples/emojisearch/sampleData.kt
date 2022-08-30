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
package app.cash.zipline.samples.emojisearch

val sampleImages = listOf(
  EmojiImage(
    "bolivia",
    "https://github.githubassets.com/images/icons/emoji/unicode/1f1e7-1f1f4.png?v8",
  ),
  EmojiImage(
    "bomb",
    "https://github.githubassets.com/images/icons/emoji/unicode/1f4a3.png?v8",
  ),
  EmojiImage(
    "bone",
    "https://github.githubassets.com/images/icons/emoji/unicode/1f9b4.png?v8",
  ),
  EmojiImage(
    "book",
    "https://github.githubassets.com/images/icons/emoji/unicode/1f4d6.png?v8",
  ),
  EmojiImage(
    "bookmark",
    "https://github.githubassets.com/images/icons/emoji/unicode/1f516.png?v8",
  ),
  EmojiImage(
    "bookmark_tabs",
    "https://github.githubassets.com/images/icons/emoji/unicode/1f4d1.png?v8",
  ),
  EmojiImage(
    "books",
    "https://github.githubassets.com/images/icons/emoji/unicode/1f4da.png?v8",
  ),
  EmojiImage(
    "boom",
    "https://github.githubassets.com/images/icons/emoji/unicode/1f4a5.png?v8",
  ),
  EmojiImage(
    "boomerang",
    "https://github.githubassets.com/images/icons/emoji/unicode/1fa83.png?v8",
  ),
  EmojiImage(
    "boot",
    "https://github.githubassets.com/images/icons/emoji/unicode/1f462.png?v8",
  ),
  EmojiImage(
    "bosnia_herzegovina",
    "https://github.githubassets.com/images/icons/emoji/unicode/1f1e7-1f1e6.png?v8",
  ),
  EmojiImage(
    "botswana",
    "https://github.githubassets.com/images/icons/emoji/unicode/1f1e7-1f1fc.png?v8",
  ),
  EmojiImage(
    "bouncing_ball_man",
    "https://github.githubassets.com/images/icons/emoji/unicode/26f9-2642.png?v8",
  ),
)

val initialViewModel = EmojiSearchViewModel("", listOf())
val sampleViewModel = EmojiSearchViewModel("donut", sampleImages)
