//
//  NoopRuntimeLayoutModifier.swift
//  EmojiSearchApp
//
//  Created by Kyle Bashour on 8/30/22.
//  Copyright © 2022 Square Inc. All rights reserved.
//

import shared

class NoopRuntimeLayoutModifier: Redwood_runtimeLayoutModifier {
    func all(predicate: @escaping (Redwood_runtimeLayoutModifierElement) -> KotlinBoolean) -> Bool {
        true
    }

    func any(predicate: @escaping (Redwood_runtimeLayoutModifierElement) -> KotlinBoolean) -> Bool {
        true
    }

    func foldIn(initial: Any?, operation: @escaping (Any?, Redwood_runtimeLayoutModifierElement) -> Any?) -> Any? {
        self
    }

    func foldOut(initial: Any?, operation: @escaping (Redwood_runtimeLayoutModifierElement, Any?) -> Any?) -> Any? {
        self
    }

    func then(other: Redwood_runtimeLayoutModifier) -> Redwood_runtimeLayoutModifier {
        self
    }
}
