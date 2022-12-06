// Copyright © Square, Inc. All rights reserved.

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

