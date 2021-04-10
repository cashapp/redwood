package app.cash.treehouse.compose

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.promise

actual fun runTest(body: suspend CoroutineScope.() -> Unit): dynamic = GlobalScope.promise { body() }
