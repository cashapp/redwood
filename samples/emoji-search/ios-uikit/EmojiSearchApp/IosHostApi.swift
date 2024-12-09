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

class IosHostApi : HostApi {
    private let client: URLSession = .init(configuration: .default)

    func httpCall(url: String, headers: [String : String], completionHandler: @escaping (String?, Error?) -> Void) {
        var request = URLRequest(url: URL(string: url)!)
        for (name, value) in headers {
            request.addValue(value, forHTTPHeaderField: name)
        }
        let task = client.dataTask(with: request) { data, response, error in
            // The KMM memory model doesn't do shared objects well, so Zipline expects the callback
            // on the same thread that the download was initiated from. This happens to be the main
            // thread, so we can bounce back to that thread for now.
            // Switching to the new KMM memory model may remove the need for this.
            DispatchQueue.main.async {
                completionHandler(data.map {
                    return String(decoding: $0, as: UTF8.self)
                }, error)
            }
        }

        task.resume()
    }

    func openUrl(url: String) {
        guard let url = URL(string: url) else { return }
        DispatchQueue.main.async {
            UIApplication.shared.open(url)
        }
    }

    func log(message: String) {
    }

    func close() {
    }
}
