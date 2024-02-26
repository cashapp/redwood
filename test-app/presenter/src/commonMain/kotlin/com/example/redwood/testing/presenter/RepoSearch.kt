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
package com.example.redwood.testing.presenter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import app.cash.paging.PagingSource
import app.cash.paging.PagingSourceLoadParams
import app.cash.paging.PagingSourceLoadResult
import app.cash.paging.PagingSourceLoadResultPage
import app.cash.paging.PagingState
import app.cash.paging.compose.collectAsLazyPagingItems
import app.cash.paging.createPager
import app.cash.paging.createPagingConfig
import app.cash.redwood.Modifier
import app.cash.redwood.layout.api.Constraint
import app.cash.redwood.layout.compose.Column
import app.cash.redwood.lazylayout.compose.LazyColumn
import app.cash.redwood.ui.Margin
import app.cash.redwood.ui.dp
import com.example.redwood.testing.compose.Text
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private val pagingConfig = createPagingConfig(pageSize = 20, initialLoadSize = 20).apply {
  check(pageSize == initialLoadSize) {
    "As GitHub uses offset based pagination, an elegant PagingSource implementation requires each page to be of equal size."
  }
}

@Composable
fun RepoSearch(
  httpClient: HttpClient,
  modifier: Modifier = Modifier,
) {
  // TODO Make term interactive with TextInput.
  val latestSearchTerm by remember { mutableStateOf("android") }

  val pager = remember(httpClient, latestSearchTerm) {
    createPager(pagingConfig) {
      RepositoryPagingSource(httpClient, latestSearchTerm)
    }
  }

  val lazyPagingItems = pager.flow.collectAsLazyPagingItems()
  LazyColumn(
    width = Constraint.Fill,
    height = Constraint.Fill,
    placeholder = { RepositoryItem(Repository(fullName = "Placeholder…", 0)) },
    modifier = modifier,
  ) {
    items(lazyPagingItems.itemCount) { index ->
      RepositoryItem(lazyPagingItems[index]!!)
    }
  }
}

@Composable
private fun RepositoryItem(
  repository: Repository,
) {
  Column(margin = Margin(16.dp)) {
    Text(text = repository.fullName)
  }
}

private class RepositoryPagingSource(
  private val httpClient: HttpClient,
  private val searchTerm: String,
) : PagingSource<Int, Repository>() {

  private val json = Json {
    ignoreUnknownKeys = true
  }

  init {
    require(searchTerm.isNotEmpty())
  }

  override suspend fun load(params: PagingSourceLoadParams<Int>): PagingSourceLoadResult<Int, Repository> {
    val page = params.key ?: FIRST_PAGE_INDEX
    val repositoriesJson = httpClient.call(
      "https://api.github.com/search/repositories?page=$page&per_page=${params.loadSize}&sort=stars&q=$searchTerm",
      mapOf("Accept" to "application/vnd.github.v3+json"),
    )
    val repositories = json.decodeFromString<Repositories>(repositoriesJson)
    return PagingSourceLoadResultPage(
      data = repositories.items,
      prevKey = (page - 1).takeIf { it >= FIRST_PAGE_INDEX },
      nextKey = if (repositories.items.isNotEmpty()) page + 1 else null,
    ) as PagingSourceLoadResult<Int, Repository>
  }

  override fun getRefreshKey(state: PagingState<Int, Repository>): Int = TODO()

  companion object {

    /**
     * The GitHub REST API uses [1-based page numbering](https://docs.github.com/en/rest/overview/resources-in-the-rest-api#pagination).
     */
    const val FIRST_PAGE_INDEX = 1
  }
}

@Serializable
private data class Repositories(
  @SerialName("total_count") val totalCount: Int,
  val items: List<Repository>,
)

@Serializable
private data class Repository(
  @SerialName("full_name") val fullName: String,
  @SerialName("stargazers_count") val stargazersCount: Int,
)
