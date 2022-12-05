// Copyright © Square, Inc. All rights reserved.

import Foundation
import shared
import SwiftUI

public struct TreehouseAppKey: EnvironmentKey {
    // TODO: This type shouldn't be bound to PresentersEmojiSearchPresenter
    public static var defaultValue: (Redwood_treehouseTreehouseApp<PresentersEmojiSearchPresenter>)? = nil
}

public extension EnvironmentValues {
    
    var treehouseApp: (Redwood_treehouseTreehouseApp<PresentersEmojiSearchPresenter>)? {
        get { self[TreehouseAppKey.self] }
        set { self[TreehouseAppKey.self] = newValue }
    }
}

struct WidgetSystemKey: EnvironmentKey {
    
    static var defaultValue: (EmojiSearchWidgetSystem)? = nil
}

extension EnvironmentValues {
    
    var treehouseWidgetSystem: (EmojiSearchWidgetSystem)? {
        get { self[WidgetSystemKey.self] }
        set { self[WidgetSystemKey.self] = newValue }
    }
}

