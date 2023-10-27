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
import TestAppKt

class TestAppViewController : UIViewController {

    // MARK: - Private Properties

    private let urlSession: URLSession = .init(configuration: .default)

    // MARK: - UIViewController

    override func viewDidLoad() {
        super.viewDidLoad()

        view.backgroundColor = .white

        let testAppLauncher = TestAppLauncher(nsurlSession: urlSession, hostApi: IosHostApi())
        let treehouseApp = testAppLauncher.createTreehouseApp()
        let widgetSystem = TestSchemaWidgetSystem()
        let treehouseView = TreehouseUIView(widgetSystem: widgetSystem)
        let content = treehouseApp.createContent(
            source: TestAppContent(),
            codeListener: CodeListener()
        )
        ExposedKt.bindWhenReady(content: content, view: treehouseView)

        let tv = treehouseView.view
        tv.translatesAutoresizingMaskIntoConstraints = false

        self.view.addSubview(tv)
        let safeGuide = self.view.safeAreaLayoutGuide
        tv.heightAnchor.constraint(equalTo: safeGuide.heightAnchor).isActive = true
        tv.widthAnchor.constraint(equalTo: safeGuide.widthAnchor).isActive = true
        tv.topAnchor.constraint(equalTo: safeGuide.topAnchor).isActive = true
    }
}

class TestAppContent : TreehouseContentSource {
    func get(app: AppService) -> ZiplineTreehouseUi {
        let treehouesUi = (app as! TestAppPresenter)
        return treehouesUi.launch()
    }
}

class TestSchemaWidgetSystem : TreehouseViewWidgetSystem {
    func widgetFactory(
        json: Kotlinx_serialization_jsonJson,
        protocolMismatchHandler: ProtocolMismatchHandler
    ) -> ProtocolFactory {
        return TestSchemaProtocolFactory<UIView>(
            provider: TestSchemaWidgetFactories<UIView>(
                TestSchema: IosTestSchemaWidgetFactory(),
                RedwoodLayout: UIViewRedwoodLayoutWidgetFactory(),
                RedwoodLazyLayout: UIViewRedwoodLazyLayoutWidgetFactory()
            ),
            json: json,
            mismatchHandler: protocolMismatchHandler
        );
    }
}
