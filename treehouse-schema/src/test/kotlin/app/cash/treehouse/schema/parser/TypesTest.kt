package app.cash.treehouse.schema.parser

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TypesTest {
  @Test
  fun packageName() {
    assertThat(String::class.packageName).isEqualTo("java.lang")
    assertThat(TypesTest::class.packageName).isEqualTo("app.cash.treehouse.schema.parser")
    assertThrows<IllegalArgumentException> {
      arrayOf<String>()::class.packageName
    }
    assertThrows<IllegalArgumentException> {
      Int::class.packageName
    }
  }
}
