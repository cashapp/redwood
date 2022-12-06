// Copyright © Square, Inc. All rights reserved.

import Foundation
import shared
import SwiftUI

final class LazyColumnBinding: WidgetLazyColumn, SwiftUIView, Identifiable {
    
    @Published var intervals: [Redwood_treehouse_lazylayout_apiLazyListIntervalContent] = []
    @Published fileprivate var randomID: String = ""
    
    func intervals(intervals: [Redwood_treehouse_lazylayout_apiLazyListIntervalContent]) {
        self.intervals = intervals
        self.randomID = UUID().uuidString
    }
    
    var layoutModifiers: Redwood_runtimeLayoutModifier = ExposedKt.layoutModifier()
    
    var view: some View { LazyColumnView(binding: self) }
    var value: Any { NSNull() }
}

struct LazyColumnView: View {
    
    @ObservedObject var binding: LazyColumnBinding
    
    @Environment(\.treehouseApp) private var treehouseApp
    @Environment(\.treehouseWidgetSystem) private var treehouseWidgetSystem
    
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
