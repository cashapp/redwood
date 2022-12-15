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

import EmojiSearchKt
import UIKit

class TextInputBinding: WidgetTextInput {
    private let root: UITextField = {
        let view = UITextField()
        view.borderStyle = .roundedRect
        view.autocapitalizationType = .none
        return view
    }()

    func state(state: ValuesTextFieldState) {
        root.text = state.text
    }

    func hint(hint: String) {
        root.placeholder = hint
    }

    func onChange(onChange: ((ValuesTextFieldState) -> Void)? = nil) {
        let identifier = UIAction.Identifier("TextInputBinding.onTextChanged")

        root.removeAction(identifiedBy: identifier, for: .editingChanged)
        root.addAction(UIAction(identifier: identifier, handler: { [unowned self] _ in
            onChange?(ValuesTextFieldState(text: self.root.text ?? "", selectionStart: 0, selectionEnd: 0, userEditCount: 0))
        }), for: .editingChanged)
    }

    var layoutModifiers: Redwood_runtimeLayoutModifier = ExposedKt.layoutModifier()
    var value: Any { root }
}
