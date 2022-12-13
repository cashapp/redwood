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
