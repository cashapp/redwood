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

import UIKit
import TestAppKt

class IosTestSchemaWidgetFactory: TestSchemaWidgetFactory {
    func TextInput() -> TextInput {
        fatalError()
    }

    func Text() -> Text {
        return TextBinding()
    }

    func Button() -> Button {
        return ButtonBinding()
    }
    
    func Button2() -> Button2 {
        fatalError()
    }
    
    func ScopedTestRow() -> ScopedTestRow {
        fatalError()
    }
    
    func TestRow() -> TestRow {
        fatalError()
    }
}
