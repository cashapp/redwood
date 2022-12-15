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

final class WidgetChildrenObserver: ObservableObject {
    
    let children: Redwood_widgetSwiftUIChildren
    
    init(children: Redwood_widgetWidgetChildren) {
        self.children = children as! Redwood_widgetSwiftUIChildren
        self.children.observer = { [weak self] in
            self?.objectWillChange.send()
        }
    }
    
    var widgets: [any Redwood_widgetWidget] {
        children.widgets
    }

}
