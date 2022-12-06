// Copyright © Square, Inc. All rights reserved.

import Foundation
import shared

final class WidgetChildrenObserver: ObservableObject {
    
    let children: Redwood_widgetSwiftUIChildren
    init(children: Redwood_widgetWidgetChildren) {
        self.children = children as! Redwood_widgetSwiftUIChildren
        self.children.observer = { [weak self] in
            self?.objectWillChange.send()
        }
    }
    
    var widgets: [any Redwood_widgetWidget] {
        children.widgets
    }

    var swiftUIWidgets: [any SwiftUIViewBinding] {
        return children.widgets.map { widget in
            guard let swiftUIWidget = widget as? any SwiftUIViewBinding else {
                fatalError("Could not cast \(String(describing: widget)) as SwiftUIView")
            }
            return swiftUIWidget
        }
    }
}
