// Copyright © Square, Inc. All rights reserved.

import SwiftUI

struct ContentView: View {
    
    @StateObject var viewModel: ContentViewModel
    
    var body: some View {
        TreehouseViewRenderer(treehouseView: viewModel.treehouseView)
        .environment(\.treehouseApp, viewModel.treehouseApp)
        .environment(\.treehouseWidgetSystem, viewModel.widgetSystem)
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        let viewModel = ContentViewModel()
        ContentView(viewModel: viewModel)
    }
}
