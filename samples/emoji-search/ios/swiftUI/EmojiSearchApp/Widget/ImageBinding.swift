// Copyright © Square, Inc. All rights reserved.

import Foundation
import shared
import SwiftUI

final class ImageBinding: WidgetImage, SwiftUIView {
    
    @Published var url: String = ""
    
    func url(url: String) {
        self.url = url
    }
    
    var layoutModifiers: Redwood_runtimeLayoutModifier = ExposedKt.layoutModifier()
    var view: some View { ImageView(binding: self) }
    var value: Any { NSNull() }
    
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
