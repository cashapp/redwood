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

import UIKit
import shared

class LazyColumnBinding<A : AnyObject>: NSObject, WidgetLazyColumn, UITableViewDataSource {
    private let treehouseApp: Redwood_treehouseTreehouseApp<A>
    private let widgetSystem: Redwood_treehouseTreehouseViewWidgetSystem
    private let root = UITableView()
    private var intervals: [ValuesLazyListIntervalContent] = []

    init(treehouseApp: Redwood_treehouseTreehouseApp<A>, widgetSystem: Redwood_treehouseTreehouseViewWidgetSystem) {
        self.treehouseApp = treehouseApp
        self.widgetSystem = widgetSystem
        super.init()
        root.dataSource = self
    }

    func intervals(intervals: [ValuesLazyListIntervalContent]) {
        self.intervals = intervals
        root.reloadData()
    }

    var layoutModifiers: Redwood_runtimeLayoutModifier = ExposedKt.layoutModifier()
    var value: Any { root }

    func numberOfSections(in tableView: UITableView) -> Int {
        return intervals.count
    }

    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return Int(intervals[section].count)
    }

    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let treehouseView = Redwood_treehouseTreehouseUIKitView(treehouseApp: treehouseApp, widgetSystem: widgetSystem)
        treehouseView.setContent(content: CellContent(itemProvider: intervals[indexPath.section].itemProvider, index: indexPath.row))
        let cell = TableViewCell(view: treehouseView.view)
        return cell
    }
}

private class CellContent : Redwood_treehouseTreehouseViewContent {
    private let itemProvider: ValuesLazyListIntervalContentItem
    private let index: Int

    init(itemProvider: ValuesLazyListIntervalContentItem, index: Int) {
        self.itemProvider = itemProvider
        self.index = index
    }

    func get(app: Any) -> Redwood_treehouseZiplineTreehouseUi {
        return itemProvider.get(index_: Int32(index))
    }
}

private class TableViewCell: UITableViewCell {
    private let view: UIView

    init(view: UIView) {
        self.view = view
        super.init(style: .default, reuseIdentifier: nil)

        addSubview(view)
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    override func layoutSubviews() {
        super.layoutSubviews()

        view.frame = bounds
    }
}
