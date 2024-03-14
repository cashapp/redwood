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
import EmojiSearchKt
import SnackBar

class EmojiSearchViewController : UIViewController, EmojiSearchEventListener {
    // MARK: - Private Properties

    private let urlSession: URLSession = .init(configuration: .default)
    private var success = true
    private var snackBar: SnackBarPresentable? = nil

    // MARK: - UIViewController

    override func viewDidLoad() {
        super.viewDidLoad()

        view.backgroundColor = .white
    }

    override func loadView() {
        let emojiSearchLauncher = EmojiSearchLauncher(nsurlSession: urlSession, hostApi: IosHostApi())
        let treehouseApp = emojiSearchLauncher.createTreehouseApp(listener: self)
        let widgetSystem = EmojiSearchTreehouseWidgetSystem(treehouseApp: treehouseApp)
        let treehouseView = TreehouseUIView(widgetSystem: widgetSystem)
        let content = treehouseApp.createContent(
            source: EmojiSearchContent(),
            codeListener: EmojiSearchCodeListener(treehouseView)
        )
        ExposedKt.bindWhenReady(content: content, view: treehouseView)
        view = treehouseView.view
    }

    func codeLoadFailed() {
        if (success) {
            // Only show the Snackbar on the first transition from success.
            success = false
            let snackBar = SnackBar.make(in: view, message: "Unable to load guest code from server", duration: SnackBar.Duration.infinite)
                .setAction(with: "Dismiss", action: { self.maybeDismissSnackBar() })
            snackBar.show()
            self.snackBar = snackBar
        }
    }

    func codeLoadSuccess() {
        success = true
        maybeDismissSnackBar()
    }

    private func maybeDismissSnackBar() {
        if let snackBar = snackBar {
            snackBar.dismiss()
            self.snackBar = nil
        }
    }
}

class EmojiSearchCodeListener : CodeListener {
    let treehouseView: TreehouseUIView

    init(_ treehouseView: TreehouseUIView) {
        self.treehouseView = treehouseView
    }

    override func onUncaughtException(app: TreehouseApp<AnyObject>, view: TreehouseView, exception: KotlinThrowable) {
        treehouseView.reset()

        let exceptionView = ExceptionView(exception)
        exceptionView.translatesAutoresizingMaskIntoConstraints = false
        treehouseView.view.addSubview(exceptionView)
        NSLayoutConstraint.activate([
            exceptionView.topAnchor.constraint(equalTo: treehouseView.view.topAnchor),
            exceptionView.leftAnchor.constraint(equalTo: treehouseView.view.leftAnchor),
            exceptionView.rightAnchor.constraint(equalTo: treehouseView.view.rightAnchor),
            exceptionView.bottomAnchor.constraint(equalTo: treehouseView.view.bottomAnchor),
        ])
    }
}

class EmojiSearchContent : TreehouseContentSource {
    func get(app: AppService) -> ZiplineTreehouseUi {
        let treehouesUi = (app as! EmojiSearchPresenter)
        return treehouesUi.launch()
    }
}

class EmojiSearchTreehouseWidgetSystem : TreehouseViewWidgetSystem {
    let treehouseApp: TreehouseApp<EmojiSearchPresenter>

    init(treehouseApp: TreehouseApp<EmojiSearchPresenter>) {
        self.treehouseApp = treehouseApp
    }

    func widgetFactory(
        json: Kotlinx_serialization_jsonJson,
        protocolMismatchHandler: ProtocolMismatchHandler
    ) -> ProtocolFactory {
        return EmojiSearchProtocolFactory<UIView>(
            widgetSystem: EmojiSearchWidgetSystem<UIView>(
                EmojiSearch: IosEmojiSearchWidgetFactory(treehouseApp: treehouseApp, widgetSystem: self),
                RedwoodLayout: UIViewRedwoodLayoutWidgetFactory(),
                RedwoodLazyLayout: UIViewRedwoodLazyLayoutWidgetFactory()
            ),
            json: json,
            mismatchHandler: protocolMismatchHandler
        );
    }
}
