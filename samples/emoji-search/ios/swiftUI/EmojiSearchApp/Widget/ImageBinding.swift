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
import shared
import SwiftUI

final class ImageBinding: BaseWidget, WidgetImage, SwiftUIViewBinding {
    
    @Published var lastURL: URL?
    @Published var image: UIImage? = nil
    
    let imageLoader: RemoteImageLoader
    
    func url(url: String) {
        image = nil
        guard let url = URL(string: url) else {
            return
        }

        lastURL = url
        imageLoader.loadImage(url: url) { [unowned self] url, image in
            guard self.lastURL == url else {
                return
            }

            self.image = image
        }
    }

    var view: some View { ImageView(binding: self) }
    
    init(imageLoader: RemoteImageLoader) {
        self.imageLoader = imageLoader
    }
}

struct ImageView: View {
    
    @ObservedObject var binding: ImageBinding
    
    var body: some View {
        content
    }
    
    @ViewBuilder
    private var content: some View {
        if let image = binding.image {
            Image(uiImage: image)
        } else {
            EmptyView()
        }
    }
}
