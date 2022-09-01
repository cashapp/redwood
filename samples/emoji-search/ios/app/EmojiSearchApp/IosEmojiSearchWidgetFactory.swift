//
//  IosEmojiSearchWidgetFactory.swift
//  EmojiSearchApp
//
//  Created by Jesse Wilson on 2022-08-31.
//  Copyright © 2022 Square Inc. All rights reserved.
//

import Foundation

import UIKit
import shared

class IosEmojiSearchWidgetFactory: WidgetEmojiSearchWidgetFactory {
    let imageLoader = RemoteImageLoader()
    
    func Column() -> WidgetColumn {
        return ColumnBinding()
    }
    func ScrollableColumn() -> WidgetScrollableColumn {
        return ScrollableColumnBinding()
    }
    func TextInput() -> WidgetTextInput {
        return TextInputBinding()
    }
    func Image() -> WidgetImage {
        print("MAKING AN IMAGE🦄 ")
        return ImageBinding(imageLoader: imageLoader)
    }
}
