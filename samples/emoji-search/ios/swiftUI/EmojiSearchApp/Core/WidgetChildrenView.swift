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
        ForEach(swiftUIWidgets, id: \.self.id) { widget in
            childView(widget: widget)
        }
    }
    
    private func childView(widget: any SwiftUIViewBinding) -> AnyView {
        return AnyView(widget.view)
    }

    // This causes compilation issues when inside the observer
    private var swiftUIWidgets: [any SwiftUIViewBinding] {
        return observer.widgets.map { widget in
            guard let swiftUIWidget = widget as? any SwiftUIViewBinding else {
                fatalError("Could not cast \(String(describing: widget)) as SwiftUIView")
            }
            return swiftUIWidget
        }
    }
    
}
