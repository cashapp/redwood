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
package app.cash.redwood.dom.testing

/**
 * Configure the canvas that we will snapshot our element on.
 */
public class Frame(
  public val width: Int?,
  public val height: Int?,
  public val pixelRatio: Double,
) {
  public companion object {
    public val None: Frame = Frame(width = null, height = null, pixelRatio = 1.0)
    public val Iphone14: Frame = Frame(width = 390, height = 844, pixelRatio = 3.0)
  }
}
