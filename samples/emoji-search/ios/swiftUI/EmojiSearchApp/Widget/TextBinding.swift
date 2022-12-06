//  Created by Alexander Skorulis on 4/12/2022.

import Foundation
import shared
import SwiftUI

final class TextBinding: BaseWidget, WidgetText, SwiftUIViewBinding {
    
    @Published var text: String = ""
    
    func text(text: String) {
        self.text = text
    }
    
    var view: some View { TextView(binding: self) }
    
}

struct TextView: View {
    
    @ObservedObject var binding: TextBinding
    
    var body: some View {
        Text(binding.text)
    }
}
