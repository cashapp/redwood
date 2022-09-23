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
import UIKit

final class RemoteImageLoader {

    private var cache: [URL: UIImage] = [:]

    func loadImage(url: URL, completion: @escaping (URL, UIImage) -> Void) {
        if let image = cache[url] {
            completion(url, image)
            return
        }

        // This is not a good image loader, but it's a good-enough image loader.
        DispatchQueue.global().async { [weak self] in
            guard let data = try? Data(contentsOf: url) else { return }
            guard let image = UIImage(data: data) else { return }
            DispatchQueue.main.async {
                self?.cache[url] = image
                completion(url, image)
            }
        }
    }
}
