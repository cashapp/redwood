// Copyright © Square, Inc. All rights reserved.

import Foundation
import shared

final class ContentViewModel: ObservableObject {
    
    private let urlSession: URLSession = .init(configuration: .default)
    let treehouseView: Redwood_treehouseTreehouseSwiftUIView<PresentersEmojiSearchPresenter>
    
    let treehouseApp: Redwood_treehouseTreehouseApp<PresentersEmojiSearchPresenter>
    let widgetSystem: EmojiSearchWidgetSystem

    var anyApp: Redwood_treehouseTreehouseApp<AnyObject> {
        treehouseApp as! Redwood_treehouseTreehouseApp<AnyObject>
    }
    
    init() {
        let emojiSearchLauncher = EmojiSearchLauncher(nsurlSession: urlSession, hostApi: IosHostApi())
        treehouseApp = emojiSearchLauncher.createTreehouseApp()
        widgetSystem = EmojiSearchWidgetSystem()

        treehouseView = Redwood_treehouseTreehouseSwiftUIView<PresentersEmojiSearchPresenter>(
            treehouseApp: treehouseApp,
            widgetSystem: widgetSystem
        )
        treehouseApp.renderTo(view: treehouseView)
        treehouseView.setContent(content: EmojiSearchContent())
    }
    
}
