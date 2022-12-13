// Copyright © Square, Inc. All rights reserved.

import Foundation
import shared
import SwiftUI

public struct TreehouseAppKey: EnvironmentKey {
    public static var defaultValue: (Redwood_treehouseTreehouseApp<AnyObject>)? = nil
}

public extension EnvironmentValues {
    
    var treehouseApp: (Redwood_treehouseTreehouseApp<AnyObject>)? {
        get { self[TreehouseAppKey.self] }
        set { self[TreehouseAppKey.self] = newValue }
    }
}

struct WidgetSystemKey: EnvironmentKey {
    
    static var defaultValue: (Redwood_treehouseTreehouseViewWidgetSystem)? = nil
}

extension EnvironmentValues {
    
    var treehouseWidgetSystem: (Redwood_treehouseTreehouseViewWidgetSystem)? {
        get { self[WidgetSystemKey.self] }
        set { self[WidgetSystemKey.self] = newValue }
    }
}

