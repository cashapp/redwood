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
import SwiftUI

// MARK: - Memory footprint

final class TextInputBinding: BaseWidget, WidgetTextInput, SwiftUIViewBinding {

    @Published var hint: String = ""
    @Published var text: String = ""
    
    private var onChange: ((ValuesTextFieldState) -> Void)?
    private var userEditCount: Int64 = 0

    func hint(hint: String) {
        self.hint = hint
    }

    func onChange(onChange: ((ValuesTextFieldState) -> Void)? = nil) {
        self.onChange = onChange
    }

    func state(state: ValuesTextFieldState) {
        if self.text != state.text && state.userEditCount >= self.userEditCount {
            self.text = state.text
            self.userEditCount = state.userEditCount
        }
    }

    var view: some View { TextInputView(binding: self) }
    
    var textBinding: Binding<String> {
        return Binding<String> { [unowned self] in
            return self.text
        } set: { [unowned self] newValue in
            if newValue != self.text {
                self.text = newValue
                self.userEditCount += 1
                let state = ValuesTextFieldState(
                    text: newValue,
                    selectionStart: 0,
                    selectionEnd: 0,
                    userEditCount: self.userEditCount
                )
                self.onChange?(state)
            }
        }
    }
    
}

struct TextInputView: View {

    @ObservedObject var binding: TextInputBinding
    
    var body: some View {
        TextField(binding.hint, text: binding.textBinding)
    }
    
}

