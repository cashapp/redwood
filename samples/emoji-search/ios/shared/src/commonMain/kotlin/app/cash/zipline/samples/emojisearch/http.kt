package app.cash.zipline.samples.emojisearch

import app.cash.zipline.loader.ZiplineHttpClient
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.native.concurrent.freeze
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import okio.ByteString
import okio.IOException
import okio.toByteString
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSHTTPURLResponse
import platform.Foundation.NSURL
import platform.Foundation.NSURLResponse
import platform.Foundation.NSURLSession
import platform.Foundation.dataTaskWithURL

internal class URLSessionZiplineHttpClient(
  private val urlSession: NSURLSession,
) : ZiplineHttpClient {
  init {
    maybeFreeze()
  }

  override suspend fun download(url: String): ByteString {
    val nsUrl = NSURL(string = url)
    return suspendCancellableCoroutine { continuation: CancellableContinuation<ByteString> ->
      val completionHandler = CompletionHandler(url, continuation)

      val task = urlSession.dataTaskWithURL(
        url = nsUrl,
        completionHandler = completionHandler::invoke.maybeFreeze()
      )

      continuation.invokeOnCancellation {
        task.cancel()
      }

      task.resume()
    }
  }
}

private class CompletionHandler(
  private val url: String,
  private val continuation: CancellableContinuation<ByteString>,
) {
  fun invoke(data: NSData?, response: NSURLResponse?, error: NSError?) {
    if (error != null) {
      continuation.resumeWithException(IOException(error.description))
      return
    }

    if (response !is NSHTTPURLResponse || data == null) {
      continuation.resumeWithException(IOException("unexpected response: $response"))
      return
    }

    if (response.statusCode !in 200 until 300) {
      continuation.resumeWithException(
        IOException("failed to fetch $url: ${response.statusCode}")
      )
      return
    }

    continuation.resume(data.toByteString())
  }
}

/** Freeze this when executing on Kotlin/Native's strict memory model. */
private fun <T> T.maybeFreeze(): T {
  return if (Platform.memoryModel == MemoryModel.STRICT) {
    this.freeze()
  } else {
    this
  }
}
