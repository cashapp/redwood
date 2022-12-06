// Copyright © Square, Inc. All rights reserved.

import Foundation
import SwiftUI
import shared

// MARK: - Memory footprint

struct WidgetChildrenView {
    
    @StateObject private var observer: WidgetChildrenObserver
    
    init(children: Redwood_widgetWidgetChildren) {
        _observer = StateObject(wrappedValue: WidgetChildrenObserver(children: children))
    }
}

// MARK: - Rendering

extension WidgetChildrenView: View {
    
    var body: some View {
        return ForEach(observer.swiftUIWidgets, id: \.self.id) { widget in
            childView(widget: widget)
        }
    }
    
    private func childView(widget: any SwiftUIView) -> AnyView {
        return AnyView(widget.view)
    }
    
}
