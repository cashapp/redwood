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
import UIKit
import shared

class EmojiSearchViewController : UIViewController {

    // MARK: - Private Properties

    private let urlSession: URLSession = .init(configuration: .default)

    // MARK: - UIViewController

    override func viewDidLoad() {
        super.viewDidLoad()

        view.backgroundColor = .white
    }

    override func loadView() {
        let emojiSearchLauncher = EmojiSearchLauncher(nsurlSession: urlSession, hostApi: IosHostApi())
        let treehouseApp = emojiSearchLauncher.createTreehouseApp()
        emojiSearchLauncher.widgetFactory = IosEmojiSearchWidgetFactory(treehouseApp: treehouseApp)
        let treehouseView = Redwood_treehouseTreehouseUIKitView<PresentersEmojiSearchPresenter>(treehouseApp: treehouseApp)
        treehouseView.setContent(content: EmojiSearchContent())

        view = treehouseView.view
    }
}

class EmojiSearchContent : Redwood_treehouseTreehouseViewContent {
    func get(app: Any) -> Redwood_treehouseZiplineTreehouseUi {
        let treehouesUi = (app as! PresentersEmojiSearchPresenter)
        return treehouesUi.launch()
    }
}
