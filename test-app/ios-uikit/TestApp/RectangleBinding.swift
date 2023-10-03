/*
 * Copyright (C) 2023 Square, Inc.
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
import TestAppKt
import UIKit

class RectangleBinding: Rectangle {

    private let root: UIView = {
        let view = UIView()
        return view
    }()

    var modifier: Modifier = ExposedKt.modifier()
    var value: Any { root }

    func backgroundColor(backgroundColor: UInt32) {
        root.backgroundColor = UIColor(argb: UInt(backgroundColor))
    }

    func cornerRadius(cornerRadius: Float) {
        root.layer.cornerRadius = CGFloat(cornerRadius)
    }
}

private extension UIColor {
    convenience init(argb: UInt) {
        let alpha = CGFloat((argb >> 24) & 0xFF) / 255.0
        let red = CGFloat((argb >> 16) & 0xFF) / 255.0
        let green = CGFloat((argb >> 8) & 0xFF) / 255.0
        let blue = CGFloat(argb & 0xFF) / 255.0

        self.init(red: red, green: green, blue: blue, alpha: alpha)
    }
}
