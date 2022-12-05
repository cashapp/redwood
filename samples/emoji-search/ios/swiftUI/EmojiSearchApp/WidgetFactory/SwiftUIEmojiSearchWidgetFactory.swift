// Copyright © Square, Inc. All rights reserved.

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
    func Image() -> WidgetImage { ImageBinding() }
}
