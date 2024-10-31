/*
 * Copyright (C) 2023 Square, Inc.
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
import UIKit
import EmojiSearchKt

/// Renders an emoji, plus the first line of the exception message, centered and wrapped. The view
/// has a light-yellow background.
///
/// ```
///                         🦨
///          app.cash.zipline.ZiplineException
///                  RuntimeException
///                        boom!
/// ```
class CrashedUIView : Crashed {

    let emoji = UILabel()
    let message = UILabel()
    let centeredContent = UIView()
    let root = UIView()
    var value: Any { root }

    var modifier: Modifier = ExposedKt.modifier()

    required init() {
        emoji.textAlignment = .center
        emoji.font = emoji.font.withSize(40)
        emoji.text = "🦨"
        emoji.numberOfLines = 0
        emoji.translatesAutoresizingMaskIntoConstraints = false

        message.textAlignment = .center
        message.font = message.font.withSize(16)
        message.numberOfLines = 0
        message.translatesAutoresizingMaskIntoConstraints = false

        centeredContent.translatesAutoresizingMaskIntoConstraints = false
        centeredContent.addSubview(emoji)
        centeredContent.addSubview(message)

        NSLayoutConstraint.activate([
            emoji.topAnchor.constraint(equalTo: centeredContent.topAnchor),
            emoji.leftAnchor.constraint(equalTo: centeredContent.leftAnchor),
            emoji.rightAnchor.constraint(equalTo: centeredContent.rightAnchor),
        ])

        NSLayoutConstraint.activate([
            message.topAnchor.constraint(equalTo: emoji.bottomAnchor, constant: 10),
            message.leftAnchor.constraint(equalTo: centeredContent.leftAnchor, constant: 10),
            message.rightAnchor.constraint(equalTo: centeredContent.rightAnchor, constant: -10),
            message.bottomAnchor.constraint(equalTo: centeredContent.bottomAnchor),
        ])

        root.translatesAutoresizingMaskIntoConstraints = false
        root.addSubview(centeredContent)
        NSLayoutConstraint.activate([
            centeredContent.centerYAnchor.constraint(equalTo: root.centerYAnchor),
            centeredContent.leftAnchor.constraint(equalTo: root.leftAnchor),
            centeredContent.rightAnchor.constraint(equalTo: root.rightAnchor),
        ])
        root.backgroundColor = UIColor(red: 255/255.0, green: 250/255.0, blue: 225/255.0, alpha: 1.0)
        root.translatesAutoresizingMaskIntoConstraints = false
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    func restart(restart: @escaping () -> Void) {
    }

    func uncaughtException(uncaughtException: KotlinThrowable) {
        message.text = exceptionToLabel(uncaughtException)
    }

    private func exceptionToLabel(_ exception: KotlinThrowable) -> String {
        var result = exception.description()
        let endOfString = result.firstIndex(of: "\n")
        if (endOfString != nil) {
            result = String(result[..<endOfString!])
        }
        return result.replacingOccurrences(of: ": ", with: "\n")
    }
}
