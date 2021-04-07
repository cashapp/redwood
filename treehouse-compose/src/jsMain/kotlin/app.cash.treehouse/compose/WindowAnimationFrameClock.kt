package app.cash.treehouse.compose

import androidx.compose.runtime.MonotonicFrameClock
import kotlinx.browser.window
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object WindowAnimationFrameClock : MonotonicFrameClock {
  override suspend fun <R> withFrameNanos(
    onFrame: (Long) -> R
  ): R = suspendCoroutine { continuation ->
    window.requestAnimationFrame {
      val durationMillis = it.toLong()
      val durationNanos = durationMillis * 1000000
      val result = onFrame(durationNanos)
      continuation.resume(result)
    }
  }
}
