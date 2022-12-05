// Copyright © Square, Inc. All rights reserved.

import Foundation
import shared
import SwiftUI

struct TreehouseViewRenderer<AppType: AnyObject>: View {
    
    let treehouseView: Redwood_treehouseTreehouseSwiftUIView<AppType>
    
    var body: some View {
        ZStack {
            WidgetChildrenView(children: treehouseView.children)
        }
    }
}
