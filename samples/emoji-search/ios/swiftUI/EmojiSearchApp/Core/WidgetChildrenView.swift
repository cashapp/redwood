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
        return ForEach(Array(observer.widgets.indices), id: \.self) { index in
            childView(widget: observer.widgets[index])
        }
    }
    
    private func childView(widget: Any) -> AnyView {
        guard let child = widget as? any SwiftUIView else {
            fatalError("Could not cast \(String(describing: widget)) as SwiftUIView")
        }
        return AnyView(child.view)
    }
    
}
