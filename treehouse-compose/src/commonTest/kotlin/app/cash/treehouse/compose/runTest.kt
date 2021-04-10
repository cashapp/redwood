package app.cash.treehouse.compose

import kotlinx.coroutines.CoroutineScope

expect fun runTest(body: suspend CoroutineScope.() -> Unit)
