//
//  ChildrenBinding.swift
//  EmojiSearchApp
//
//  Created by Kyle Bashour on 8/30/22.
//  Copyright © 2022 Square Inc. All rights reserved.
//

import UIKit
import shared

class ChildrenBinding: Redwood_widgetWidgetChildren {
    private var storage: [UIView] = []
    private var update: ([UIView]) -> Void

    init(update: @escaping ([UIView]) -> Void) {
        self.update = update
    }

    func clear() {
        storage.removeAll()
        update(storage)
    }

    func insert(index: Int32, widget: Any) {
        storage.insert(widget as! UIView, at: Int(index))
        update(storage)
    }

    func move(fromIndex: Int32, toIndex: Int32, count: Int32) {
        let childrenToMove = (0..<count).map { _ -> UIView in
            let child = storage[Int(fromIndex)]
            return child
        }

        let newIndex = Int((toIndex > fromIndex) ? toIndex - count : toIndex)

        childrenToMove.enumerated().forEach {
            storage.insert($0.element, at: newIndex + $0.offset)
        }

        update(storage)
    }

    func remove(index: Int32, count: Int32) {
        (0..<count).forEach { _ in storage.remove(at: Int(index)) }
        update(storage)
    }
}
