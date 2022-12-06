// Copyright © Square, Inc. All rights reserved.

import Foundation
import shared

class BaseWidget: NSObject, Identifiable {

    @objc var value: Any { NSNull() }
    @objc var layoutModifiers: Redwood_runtimeLayoutModifier = ExposedKt.layoutModifier()

}
