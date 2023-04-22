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

import androidx.compose.runtime.Composable
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.compose.collectAsLazyPagingItems
import app.cash.redwood.treehouse.StandardAppLifecycle
import app.cash.redwood.treehouse.TreehouseUi
import app.cash.redwood.treehouse.lazylayout.compose.LazyColumn
import app.cash.redwood.treehouse.lazylayout.paging.items
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class RepoSearchTreehouseUi(
  private val httpClient: HttpClient,
  private val appLifecycle: StandardAppLifecycle,
) : TreehouseUi {
  private var latestSearchTerm = "android"

  private val pager: Pager<Int, Repository> = run {
    val pagingConfig = PagingConfig(pageSize = 20, initialLoadSize = 20)
    check(pagingConfig.pageSize == pagingConfig.initialLoadSize) {
      "As GitHub uses offset based pagination, an elegant PagingSource implementation requires each page to be of equal size."
    }
    Pager(pagingConfig) {
      RepositoryPagingSource(httpClient, latestSearchTerm)
    }
  }

  @Composable
  override fun Show() {
    val lazyPagingItems = pager.flow.collectAsLazyPagingItems()
    LazyColumn(appLifecycle) {
      items(
        items = lazyPagingItems,
        itemToKey = { it!!.fullName },
      ) {
        RepoSearch(it!!)
      }
    }
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

  override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Repository> {
    val page = params.key ?: FIRST_PAGE_INDEX
    val repositoriesJson = httpClient.call(
      "https://api.github.com/search/repositories?page=$page&per_page=${params.loadSize}&sort=stars&q=$searchTerm",
      mapOf("Accept" to "application/vnd.github.v3+json"),
    )
    val repositories = json.decodeFromString<Repositories>(repositoriesJson)
    return LoadResult.Page(
      data = repositories.items,
      prevKey = (page - 1).takeIf { it >= FIRST_PAGE_INDEX },
      nextKey = if (repositories.items.isNotEmpty()) page + 1 else null,
    )
  }

  override fun getRefreshKey(state: PagingState<Int, Repository>): Int = TODO()

  companion object {

    /**
     * The GitHub REST API uses [1-based page numbering](https://docs.github.com/en/rest/overview/resources-in-the-rest-api#pagination).
     */
    const val FIRST_PAGE_INDEX = 1
  }
}
