// Copyright © Square, Inc. All rights reserved.

import Foundation
import shared
import SwiftUI

final class RowBinding: BaseWidget, WidgetRow, SwiftUIViewBinding {
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
    
    let children: Redwood_widgetWidgetChildren = Redwood_widgetSwiftUIChildren()
    var view: some View { RowView(binding: self) }

}

struct RowView: View {
    
    @ObservedObject var binding: RowBinding
    
    var body: some View {
        HStack(spacing: 0) {
            WidgetChildrenView(children: binding.children)
        }
    }
}
