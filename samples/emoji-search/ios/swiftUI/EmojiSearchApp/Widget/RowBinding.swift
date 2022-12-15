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
