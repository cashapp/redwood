//
//  EmojiSearchWidgetFactoryBinding.swift
//  EmojiSearchApp
//
//  Created by Kyle Bashour on 8/30/22.
//  Copyright © 2022 Square Inc. All rights reserved.
//

import shared

class EmojiSearchWidgetFactoryBinding: WidgetEmojiSearchWidgetFactory {
    func Column() -> WidgetColumn {
        ColumnBinding()
    }

    func Image() -> WidgetImage {
        ImageBinding(imageLoader: RemoteImageLoader())
    }

    func ScrollableColumn() -> WidgetScrollableColumn {
        ScrollableColumnBinding()
    }

    func TextInput() -> WidgetTextInput {
        TextInputBinding()
    }
}
