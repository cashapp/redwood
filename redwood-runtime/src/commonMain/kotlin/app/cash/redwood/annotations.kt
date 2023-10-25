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
package app.cash.redwood

import androidx.compose.runtime.StableMarker

/**
 * Marks a layout scope as part of a DSL.
 */
@DslMarker
@StableMarker
public annotation class LayoutScopeMarker

/**
 * Denote an API which should only be used by Redwood's generated code and is not considered
 * stable across any version.
 *
 * @suppress
 */
@RequiresOptIn("This API is for use by Redwood's generated code only")
public annotation class RedwoodCodegenApi
