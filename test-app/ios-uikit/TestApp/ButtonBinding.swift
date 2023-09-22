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

class ButtonBinding: Button {
    private let root: UIButton = {
        let view = UIButton()
        view.backgroundColor = UIColor.gray
        return view
    }()

    var modifier: Modifier = ExposedKt.modifier()
    var value: Any { root }
    var onClick: (() -> Void)? = nil

    func text(text: String?) {
        root.setTitle(text, for: .normal)

        // This very simple integration wraps the size of whatever text is entered. Calling
        // this function will update the bounds and trigger relayout in the parent.
        root.sizeToFit()
    }
    
    func onClick(onClick: (() -> Void)? = nil) {
        self.onClick = onClick
        if (onClick != nil) {
            root.addTarget(self, action: #selector(clicked), for: .touchUpInside)
        } else {
            root.removeTarget(self, action: #selector(clicked), for: .touchUpInside)
        }
    }
    
    @objc func clicked() {
        if (self.onClick != nil) {
            self.onClick?()
        }
    }
}
