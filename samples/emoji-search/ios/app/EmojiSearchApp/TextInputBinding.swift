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

import shared
import UIKit

class TextInputBinding: WidgetTextInput {
    private let root: UITextField = {
        let view = UITextField()
        view.borderStyle = .roundedRect
        view.autocapitalizationType = .none
        return view
    }()

    func hint(hint: String) {
        root.placeholder = hint
    }

    func onTextChanged(onTextChanged: ((String) -> Void)? = nil) {
        let identifier = UIAction.Identifier("TextInputBinding.onTextChanged")

        root.removeAction(identifiedBy: identifier, for: .editingChanged)
        root.addAction(UIAction(identifier: identifier, handler: { [unowned self] _ in
            onTextChanged?(self.root.text ?? "")
        }), for: .editingChanged)
    }

    func text(text: String) {
        root.text = text
    }

    var layoutModifiers: Redwood_runtimeLayoutModifier = ExposedKt.layoutModifier()
    var value: Any { root }
}
