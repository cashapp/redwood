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

