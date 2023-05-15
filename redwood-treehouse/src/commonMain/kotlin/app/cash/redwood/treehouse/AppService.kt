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
package app.cash.redwood.treehouse

import app.cash.zipline.ZiplineService
import kotlin.native.ObjCName

/**
 * Base interface for Treehouse applications. Each application should extend this interface to
 * declare APIs that are declared by downloaded code and called from host code.
 *
 * Note that due to a Zipline limitation it's necessary for implementing classes to declare a direct
 * dependency on [ZiplineService]. https://github.com/cashapp/zipline/issues/765
 */
@ObjCName("AppService", exact = true)
public interface AppService : ZiplineService {
  public val appLifecycle: AppLifecycle
}
