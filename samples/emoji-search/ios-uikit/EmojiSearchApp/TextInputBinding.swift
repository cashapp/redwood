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

class TextInputBinding: TextInput {
    private var state = ValuesTextFieldState(text: "", selectionStart: 0, selectionEnd: 0, userEditCount: 0)
    private var onChange: ((ValuesTextFieldState) -> Void)?
    private var updating = false

    private let root: UITextField = {
        let view = UITextField()
        view.borderStyle = .roundedRect
        view.autocapitalizationType = .none
        return view
    }()

    init() {
        let identifier = UIAction.Identifier("TextInputBinding.onTextChanged")
        root.addAction(UIAction(identifier: identifier, handler: {_ in
            self.stateChanged(text: self.root.text)
        }), for: .editingChanged)
    }

    func state(state: ValuesTextFieldState) {
        if (state.userEditCount < self.state.userEditCount) {
            return
        }

        precondition(!updating)
        updating = true
        self.state = state
        root.text = state.text
        updating = false
    }

    func hint(hint: String) {
        root.placeholder = hint
    }

    func onChange(onChange: ((ValuesTextFieldState) -> Void)? = nil) {
        self.onChange = onChange
    }

    func stateChanged(text: String?) {
        // Ignore this update if it isn't a user edit.
        if (updating) {
            return
        }

        let newState = state.userEdit(
            text: (text ?? ""),
            selectionStart: 0,
            selectionEnd: 0
        )

        if (!state.contentEquals(other: newState)) {
            state = newState
            onChange?(newState)
        }
    }

    var modifier: Modifier = ExposedKt.modifier()
    var value: Any { root }
}
