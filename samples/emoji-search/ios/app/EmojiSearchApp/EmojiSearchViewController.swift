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

        let emojiSearchLauncher = EmojiSearchLauncher(nsurlSession: urlSession, hostApi: IosHostApi(), widgetFactory: IosEmojiSearchWidgetFactory())
        let treehouseApp = emojiSearchLauncher.createTreehouseApp()
        let treehouseView = Redwood_treehouseTreehouseUIKitView<PresentersEmojiSearchPresenter>(treehouseApp: treehouseApp)
        treehouseView.setContent(content: EmojiSearchContent())

        let newView = treehouseView.view
        view.addSubview(newView)
        newView.translatesAutoresizingMaskIntoConstraints = false
        newView.leadingAnchor.constraint(equalTo: view.leadingAnchor).isActive = true
        newView.topAnchor.constraint(equalTo: view.topAnchor, constant: 100).isActive = true
        newView.widthAnchor.constraint(equalTo: view.widthAnchor).isActive = true
        newView.heightAnchor.constraint(equalTo: view.heightAnchor).isActive = true
    }
}

class EmojiSearchContent : Redwood_treehouseTreehouseViewContent {
    func get(app: Any) -> Redwood_treehouseZiplineTreehouseUi {
        let treehouesUi = (app as! PresentersEmojiSearchPresenter)
        return treehouesUi.launch()
    }
}
