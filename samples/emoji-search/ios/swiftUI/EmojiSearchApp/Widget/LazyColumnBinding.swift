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

final class LazyColumnBinding: BaseWidget, WidgetLazyColumn, SwiftUIViewBinding {
    
    @Published var intervals: [Redwood_treehouse_lazylayout_apiLazyListIntervalContent] = []
    @Published fileprivate var randomID: String = ""
    
    func intervals(intervals: [Redwood_treehouse_lazylayout_apiLazyListIntervalContent]) {
        self.intervals = intervals
        self.randomID = UUID().uuidString
    }
    
    var view: some View { LazyColumnView(binding: self) }
}

struct LazyColumnView: View {
    
    @ObservedObject var binding: LazyColumnBinding
    
    var body: some View {
        LazyVStack {
            ForEach(Array(binding.intervals.indices), id: \.self) { index in
                let interval = binding.intervals[index]
                ForEach(Array(0..<interval.count), id: \.self) { itemIndex in
                    LazyCell(interval: interval, index: itemIndex)
                        .frame(minHeight: 20)
                }
            }
        }
        .id(binding.randomID)
    }
}

struct LazyCell: View {
    let interval: Redwood_treehouse_lazylayout_apiLazyListIntervalContent
    let index: Int32
    
    var body: some View {
        return TreehouseUIRenderer(treehouseUI: interval.itemProvider.get(index_: index))
    }
}
