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
package com.example.redwood.reposearch.presenter

import app.cash.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface Event {

  data class SearchTerm(
    val searchTerm: String,
  ) : Event
}

sealed interface ViewModel {

  object Empty : ViewModel

  data class SearchResults(
    val searchTerm: String,
    val repositories: Flow<PagingData<Repository>>,
  ) : ViewModel
}

@Serializable
data class Repositories(
  @SerialName("total_count") val totalCount: Int,
  val items: List<Repository>,
)

@Serializable
data class Repository(
  @SerialName("full_name") val fullName: String,
  @SerialName("stargazers_count") val stargazersCount: Int,
)

object RateLimitExceeded : Exception()
