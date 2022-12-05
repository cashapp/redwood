// Copyright © Square, Inc. All rights reserved.

import Foundation
import shared

final class SwiftUILayoutWidgetFactory: WidgetRedwoodLayoutWidgetFactory {
    func Column() -> WidgetColumn { ColumnBinding() }
    func Row() -> WidgetRow { RowBinding() }


}
