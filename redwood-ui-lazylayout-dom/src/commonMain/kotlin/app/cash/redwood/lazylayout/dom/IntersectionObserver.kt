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
package app.cash.redwood.lazylayout.dom

import org.w3c.dom.DOMRect
import org.w3c.dom.Element
import org.w3c.dom.ParentNode

/**
 * https://developer.mozilla.org/en-US/docs/Web/API/Intersection_Observer_API
 */
internal external class IntersectionObserver(
  callback: (entries: Array<IntersectionObserverEntry>, observer: IntersectionObserver) -> Unit,
  options: IntersectionObserverOptions = definedExternally,
) {
  val root: ParentNode

  val rootMargin: String
  val thresholds: Array<Double>

  fun disconnect()

  fun observe(target: Element)

  fun unobserve(target: Element)
}

internal sealed external interface IntersectionObserverOptions {
  var root: ParentNode?
  var rootMargin: String?
  var threshold: Array<Double>?
}

internal external class IntersectionObserverEntry {
  val boundingClientRect: DOMRect

  val intersectionRatio: Double

  val intersectionRect: DOMRect

  val isIntersecting: Boolean

  val rootBounds: DOMRect?

  val target: Element

  val time: Double
}
