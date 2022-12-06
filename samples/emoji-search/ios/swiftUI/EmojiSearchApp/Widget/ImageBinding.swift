// Copyright © Square, Inc. All rights reserved.

import Foundation
import shared
import SwiftUI

final class ImageBinding: BaseWidget, WidgetImage, SwiftUIViewBinding {
    
    @Published var url: String = ""
    
    func url(url: String) {
        self.url = url
    }

    var view: some View { ImageView(binding: self) }
    
}

struct ImageView: View {
    
    @ObservedObject var binding: ImageBinding
    
    var body: some View {
        content
            .id(binding.url)
    }
    
    @ViewBuilder
    private var content: some View {
        if let url = URL(string: binding.url) {
            AsyncImage(url: url)
        } else {
            EmptyView()
        }
    }
}
