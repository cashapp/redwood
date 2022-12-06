// Copyright © Square, Inc. All rights reserved.

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
