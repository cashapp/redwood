//
//  TextInputBinding.swift
//  EmojiSearchApp
//
//  Created by Kyle Bashour on 8/30/22.
//  Copyright © 2022 Square Inc. All rights reserved.
//

import shared
import UIKit

class TextInputBinding: WidgetTextInput {
    private let root = UITextField()

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

    var layoutModifiers: Redwood_runtimeLayoutModifier = NoopRuntimeLayoutModifier()
    var value: Any { root }
}
