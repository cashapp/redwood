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
import EmojiSearchKt
import UIKit

class TextBinding: Text {
    private let root: UILabel = {
        let view = UILabel()
        return view
    }()

    var modifier: Modifier = ExposedKt.modifier()
    var value: Any { root }

    func text(text: String) {
        root.text = text

        // This very simple integration wraps the size of whatever text is entered. Calling
        // this function will update the bounds and trigger relayout in the parent.
        root.sizeToFit()
    }

    func detach() {
    }
}
