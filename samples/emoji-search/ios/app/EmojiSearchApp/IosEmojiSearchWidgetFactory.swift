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
import shared

class IosEmojiSearchWidgetFactory: WidgetEmojiSearchWidgetFactory {
    let imageLoader = RemoteImageLoader()

    func Row() -> WidgetRow {
        return RowBinding()
    }
    func Column() -> WidgetColumn {
        return ColumnBinding()
    }
    func ScrollableColumn() -> WidgetScrollableColumn {
        return ScrollableColumnBinding()
    }
    func TextInput() -> WidgetTextInput {
        return TextInputBinding()
    }
    func Text() -> WidgetText {
        return TextBinding()
    }
    func Image() -> WidgetImage {
        return ImageBinding(imageLoader: imageLoader)
    }
}
