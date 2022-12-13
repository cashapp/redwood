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

import Foundation
import shared
import SwiftUI

class SwiftUIEmojiSearchWidgetFactory<A : AnyObject>: WidgetEmojiSearchWidgetFactory {
    let treehouseApp: Redwood_treehouseTreehouseApp<A>
    let widgetSystem: Redwood_treehouseTreehouseViewWidgetSystem
    let imageLoader = RemoteImageLoader()

    var RedwoodLayout: WidgetRedwoodLayoutWidgetFactory = SwiftUILayoutWidgetFactory()
    var RedwoodTreehouseLazyLayout: WidgetRedwoodTreehouseLazyLayoutWidgetFactory

    init(treehouseApp: Redwood_treehouseTreehouseApp<A>, widgetSystem: Redwood_treehouseTreehouseViewWidgetSystem) {
        self.treehouseApp = treehouseApp
        self.widgetSystem = widgetSystem
        self.RedwoodTreehouseLazyLayout = SwiftUILazyLayoutWidgetFactory()
    }

    func TextInput() -> WidgetTextInput { TextInputBinding() }
    func Text() -> WidgetText { TextBinding() }
    func Image() -> WidgetImage { ImageBinding(imageLoader: imageLoader) }
}
