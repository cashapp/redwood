package app.cash.treehouse.schema.parser

import com.google.common.truth.ThrowableSubject
import com.google.common.truth.Truth.assertThat

inline fun <reified T : Throwable> assertThrows(body: () -> Unit): ThrowableSubject {
  try {
    body()
  } catch (t: Throwable) {
    if (t is T) {
      return assertThat(t)
    }
    throw t
  }
  throw AssertionError(
    "Expect body to throw ${T::class.java.simpleName} but it completed successfully"
  )
}
