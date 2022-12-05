// Copyright © Square, Inc. All rights reserved.

import Foundation
import shared
import SwiftUI

protocol SwiftUIView: Redwood_widgetSwiftUIView, ObservableObject {
    
    associatedtype ViewType: View
    @ViewBuilder var view: ViewType { get }
    
}
