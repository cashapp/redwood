//  Created by Alexander Skorulis on 4/12/2022.

import Foundation
import shared
import SwiftUI

final class TextBinding: WidgetText, SwiftUIView {
    
    @Published var text: String = ""
    
    func text(text: String) {
        self.text = text
    }
    
    var view: some View { TextView(binding: self) }
    var value: Any { NSNull() }
    var layoutModifiers: Redwood_runtimeLayoutModifier = ExposedKt.layoutModifier()
    
}

struct TextView: View {
    
    @ObservedObject var binding: TextBinding
    
    var body: some View {
        Text(binding.text)
    }
}
