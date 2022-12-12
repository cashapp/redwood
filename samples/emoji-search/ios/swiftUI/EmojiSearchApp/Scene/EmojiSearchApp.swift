// Copyright © Square, Inc. All rights reserved.

import SwiftUI
import shared

@main
struct EmojiSearchApp: App {

    init() {
        
    }

    var body: some Scene {
        WindowGroup {
            ContentView(viewModel: ContentViewModel())
        }
    }
    
}

class EmojiSearchContent : Redwood_treehouseTreehouseViewContent {
    func get(app: Redwood_treehouseAppService) -> Redwood_treehouseZiplineTreehouseUi {
        let treehouesUi = (app as! PresentersEmojiSearchPresenter)
        return treehouesUi.launch()
    }
}

class EmojiSearchWidgetSystem : Redwood_treehouseTreehouseViewWidgetSystem {

    func widgetFactory(app: Redwood_treehouseTreehouseApp<Redwood_treehouseAppService>,
                       json: Kotlinx_serialization_jsonJson,
                       protocolMismatchHandler: Redwood_protocol_widgetProtocolMismatchHandler
    ) -> Redwood_protocol_widgetDiffConsumingNodeFactory {
        return ProtocolEmojiSearchDiffConsumingNodeFactory<Redwood_widgetSwiftUIView>(
            widgets: SwiftUIEmojiSearchWidgetFactory(treehouseApp: app, widgetSystem: self),
            json: json,
            mismatchHandler: protocolMismatchHandler
        );
    }
}

