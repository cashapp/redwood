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
import EmojiSearchKt
import UIKit

class ImageBinding: Image {
    private let root: UIImageView = {
        let view = ImageView()
        view.contentMode = .scaleAspectFit
        view.setContentHuggingPriority(.required, for: .horizontal)
        return view
    }()
    private let imageLoader: RemoteImageLoader
    private var lastURL: URL?

    init(imageLoader: RemoteImageLoader) {
        self.imageLoader = imageLoader
    }

    var layoutModifiers: LayoutModifier = ExposedKt.layoutModifier()
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

private class ImageView: UIImageView {

    override var intrinsicContentSize: CGSize {
        return CGSize(width: 48, height: 48)
    }

    override func sizeThatFits(_ size: CGSize) -> CGSize {
        return CGSize(width: 48, height: 48)
    }
}
