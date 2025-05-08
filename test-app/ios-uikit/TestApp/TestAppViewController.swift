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

        let testAppLauncher = TestAppLauncher(
          nsurlSession: urlSession,
          hostApi: IosHostApi(),
          hostProtocolFactory: TestSchemaHostProtocolFactory.shared
        )
        let treehouseApp = testAppLauncher.createTreehouseApp()
        let widgetSystem = TestSchemaWidgetSystem<UIView>(
          TestSchema: IosTestSchemaWidgetFactory(),
          RedwoodUiBasic: UIViewRedwoodUiBasicWidgetFactory(),
          RedwoodLayout: UIViewRedwoodLayoutWidgetFactory(),
          RedwoodLazyLayout: UIViewRedwoodLazyLayoutWidgetFactory()
        )
        let treehouseView = TreehouseUIView(widgetSystem: widgetSystem)
        let content = treehouseApp.createContent(
            source: TestAppContent()
        )
        ExposedKt.bindWhenReady(content: content, view: treehouseView)

        let tv = treehouseView.value
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
        let treehouseUi = (app as! TestAppPresenter)
        return treehouseUi.launchForApp()
    }
}
