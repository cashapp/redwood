// Created by Alexander skorulis on 1/12/2022.
// Copyright © Square, Inc. All rights reserved. 

import SwiftUI
import shared

@main
struct EmojiSearchAppApp: App {

    private let urlSession: URLSession = .init(configuration: .default)

    init() {
        let emojiSearchLauncher = EmojiSearchLauncher(nsurlSession: urlSession, hostApi: IosHostApi())
        let treehouseApp = emojiSearchLauncher.createTreehouseApp()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
