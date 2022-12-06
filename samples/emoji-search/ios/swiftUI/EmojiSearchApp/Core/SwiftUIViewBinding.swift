// Copyright © Square, Inc. All rights reserved.

import Foundation
import shared
import SwiftUI

protocol SwiftUIViewBinding: Redwood_widgetSwiftUIView, ObservableObject {
    
    associatedtype ViewType: View
    @ViewBuilder var view: ViewType { get }

    // This gets automatic conformance in BaseWidget
    var id: ObjectIdentifier { get }
    
}
