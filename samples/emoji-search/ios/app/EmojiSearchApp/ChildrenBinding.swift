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
