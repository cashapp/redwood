// Copyright © Square, Inc. All rights reserved.

import Foundation
import shared
import SwiftUI

final class ColumnBinding: WidgetColumn, SwiftUIView {
    func height(height: Int32) {

    }

    func horizontalAlignment(horizontalAlignment: Int32) {

    }

    func overflow(overflow: Int32) {
        
    }

    func padding(padding: Redwood_layout_apiPadding) {

    }

    func verticalAlignment(verticalAlignment: Int32) {

    }

    func width(width: Int32) {

    }

    var children: Redwood_widgetWidgetChildren!

    var layoutModifiers: Redwood_runtimeLayoutModifier = ExposedKt.layoutModifier()

    var value: Any { NSNull() }
    
    var view: some View { ColumnView(binding: self) }
    
    init() {
        self.children = Redwood_widgetSwiftUIChildren(parent: self)
    }

}


struct ColumnView: View {
    
    @ObservedObject var binding: ColumnBinding
    
    var body: some View {
        ScrollView {
            VStack(spacing: 0) {
                WidgetChildrenView(children: binding.children)
            }
        }
    }
    
}
