// Copyright © Square, Inc. All rights reserved.

import Foundation
import shared

class SwiftUILazyLayoutWidgetFactory: WidgetRedwoodTreehouseLazyLayoutWidgetFactory {
    func LazyColumn() -> WidgetLazyColumn {
        LazyColumnBinding()
    }
    
}
