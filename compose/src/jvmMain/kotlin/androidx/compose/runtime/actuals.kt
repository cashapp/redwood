package androidx.compose.runtime

// TODO The notion of a global EmbeddingContext actual should not exist! It breaks concurrent
//  usage on multiple threads which target multiple threads. This should always be pulled from the
//  Recomposer. https://issuetracker.google.com/issues/168110493
var yoloGlobalEmbeddingContext: EmbeddingContext? = null

actual fun EmbeddingContext(): EmbeddingContext = yoloGlobalEmbeddingContext!!

internal actual fun recordSourceKeyInfo(key: Any) {}
actual fun keySourceInfoOf(key: Any): String? = null
actual fun resetSourceInfo() {}

internal actual object Trace {
	actual fun beginSection(name: String): Any? {
		return null
	}
	actual fun endSection(token: Any?) {
	}
}

actual annotation class MainThread
actual annotation class CheckResult(actual val suggest: String)
