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
package app.cash.redwood.treehouse

import app.cash.redwood.treehouse.TreehouseView.ReadyForContentChangeListener
import okio.Closeable

/**
 * Binds this content to [view] whenever the view is
 * [ready for content][TreehouseView.readyForContent]. The content will bind and unbind as this view
 * is attached and detached from the UI.
 *
 * Returns a closeable that unbinds from the content and stops tracking the ready state.
 */
public fun <A : AppService> Content<A>.bindWhenReady(view: TreehouseView): Closeable {
  val listener = ReadyForContentChangeListener {
    if (view.readyForContent) {
      bind(view)
    } else {
      unbind()
    }
  }

  view.readyForContentChangeListener = listener
  listener.onReadyForContentChanged(view)

  return object : Closeable {
    override fun close() {
      unbind()
      view.readyForContentChangeListener = null
    }
  }
}

public fun <A : AppService> TreehouseContentSource<A>.bindWhenReady(
  view: TreehouseView,
  app: TreehouseApp<A>,
  codeListener: CodeListener = CodeListener(),
): Closeable {
  val content = app.createContent(this, codeListener)
  return content.bindWhenReady(view)
}
