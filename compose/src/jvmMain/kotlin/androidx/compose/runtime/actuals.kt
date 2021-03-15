package androidx.compose.runtime

internal actual object Trace {
	actual fun beginSection(name: String): Any? {
		return null
	}
	actual fun endSection(token: Any?) {
	}
}

actual annotation class CheckResult(actual val suggest: String)
