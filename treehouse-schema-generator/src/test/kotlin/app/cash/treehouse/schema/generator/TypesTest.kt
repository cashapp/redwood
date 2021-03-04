package app.cash.treehouse.schema.generator

import com.google.common.truth.Truth.assertThat
import org.junit.Assert.fail
import org.junit.Test

class TypesTest {
  @Test
  fun packageName() {
    assertThat(packageName(String::class.java)).isEqualTo("java.lang")
    assertThat(packageName(TypesTest::class.java)).isEqualTo("app.cash.treehouse.schema.generator")
    try {
      packageName(arrayOf<String>()::class.java)
      fail()
    } catch (_: IllegalArgumentException) {
    }
    try {
      packageName(Int::class.java)
      fail()
    } catch (_: IllegalArgumentException) {
    }
  }
}
