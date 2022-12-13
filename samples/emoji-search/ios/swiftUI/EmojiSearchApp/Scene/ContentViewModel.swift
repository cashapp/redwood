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

