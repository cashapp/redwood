// Copyright © Square, Inc. All rights reserved.

import Foundation
import shared
import SwiftUI

final class RowBinding: WidgetRow, SwiftUIView, Identifiable {
    func height(height: Int32) {
        
    }
    
    func horizontalAlignment(horizontalAlignment_ horizontalAlignment: Int32) {
        
    }
    
    func overflow(overflow: Int32) {
        
    }
    
    func padding(padding: Redwood_layout_apiPadding) {
        
    }
    
    func verticalAlignment(verticalAlignment_ verticalAlignment: Int32) {
        
    }
    
    func width(width: Int32) {
        
    }
    
    var children: Redwood_widgetWidgetChildren!
    var value: Any { NSNull() }
    var view: some View { RowView(binding: self) }
    
    var layoutModifiers: Redwood_runtimeLayoutModifier = ExposedKt.layoutModifier()
    
    init() {
        self.children = Redwood_widgetSwiftUIChildren(parent: self)
    }
    
    
}

struct RowView: View {
    
    @ObservedObject var binding: RowBinding
    
    var body: some View {
        HStack(spacing: 0) {
            WidgetChildrenView(children: binding.children)
        }
    }
}
