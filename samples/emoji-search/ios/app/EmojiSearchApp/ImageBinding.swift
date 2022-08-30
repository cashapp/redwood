//
//  ImageBinding.swift
//  EmojiSearchApp
//
//  Created by Kyle Bashour on 8/30/22.
//  Copyright © 2022 Square Inc. All rights reserved.
//

import Foundation
import shared
import UIKit

class ImageBinding: WidgetImage {
    private let root = UIImageView()
    private let imageLoader: RemoteImageLoader
    private var lastURL: URL?

    init(imageLoader: RemoteImageLoader) {
        self.imageLoader = imageLoader
        self.layoutModifiers = NoopRuntimeLayoutModifier()
    }

    var layoutModifiers: Redwood_runtimeLayoutModifier
    var value: Any { root }

    func url(url: String) {
        root.image = nil

        guard let url = URL(string: url) else {
            return
        }

        lastURL = url
        imageLoader.loadImage(url: url) { [unowned self] url, image in
            guard self.lastURL == url else {
                return
            }

            self.root.image = image
        }
    }
}
