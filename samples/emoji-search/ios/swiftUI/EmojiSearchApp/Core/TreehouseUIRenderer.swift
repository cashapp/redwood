// Copyright © Square, Inc. All rights reserved.

import Foundation
import shared
import SwiftUI

struct TreehouseUIRenderer: View {
    
    let treehouseUI: Redwood_treehouseZiplineTreehouseUi
    
    @Environment(\.treehouseApp) private var treehouseApp
    @Environment(\.treehouseWidgetSystem) private var treehouseWidgetSystem

    var body: some View {
        guard let app = treehouseApp, let widgetSystem = treehouseWidgetSystem else {
            fatalError("Missing expected environment variables")
        }
        let view = Redwood_treehouseTreehouseSwiftUIView(treehouseApp: app, widgetSystem: widgetSystem)
        app.renderTo(view: view)
        let content = ContentWrapper(treehouseUI: treehouseUI)
        view.setContent(content: content)
        return TreehouseViewRenderer(treehouseView: view)
    }
    
}

private class ContentWrapper: Redwood_treehouseTreehouseViewContent {
    
    let treehouseUI: Redwood_treehouseZiplineTreehouseUi
    
    init(treehouseUI: Redwood_treehouseZiplineTreehouseUi) {
        self.treehouseUI = treehouseUI
    }
    
    func get(app: Redwood_treehouseAppService) -> Redwood_treehouseZiplineTreehouseUi {
        return treehouseUI
    }
    
    
}
