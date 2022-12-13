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
