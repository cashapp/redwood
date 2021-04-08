package app.cash.treehouse.schema.parser

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TypesTest {
  @Test
  fun packageName() {
    assertThat(packageName(String::class.java)).isEqualTo("java.lang")
    assertThat(packageName(TypesTest::class.java)).isEqualTo("app.cash.treehouse.schema.parser")
    assertThrows<IllegalArgumentException> {
      packageName(arrayOf<String>()::class.java)
    }
    assertThrows<IllegalArgumentException> {
      packageName(Int::class.java)
    }
  }
}
