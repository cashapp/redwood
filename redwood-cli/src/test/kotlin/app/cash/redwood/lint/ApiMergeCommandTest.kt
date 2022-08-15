/*
 * Copyright (C) 2022 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package app.cash.redwood.lint

import com.google.common.jimfs.Configuration.unix
import com.google.common.jimfs.Jimfs
import com.google.common.truth.Truth.assertThat
import kotlin.io.path.readText
import kotlin.io.path.writeText
import org.junit.Test

class ApiMergeCommandTest {
  @Test fun mergesInputs() {
    val fs = Jimfs.newFileSystem(unix())
    val root = fs.rootDirectories.single()

    val input1 = root.resolve("input1.xml")
    input1.writeText(
      """
      |<api version="1">
      |  <widget tag="1" since="2">
      |    <trait tag="1" since="2"/>
      |  </widget>
      |</api>
      |
      """.trimMargin(),
    )

    val input2 = root.resolve("input2.xml")
    input2.writeText(
      """
      |<api version="1">
      |  <widget tag="1" since="2">
      |    <trait tag="1" since="3"/>
      |  </widget>
      |</api>
      |
      """.trimMargin(),
    )

    val output = root.resolve("output.xml")

    val command = ApiMergeCommand(fs)
    command.main(listOf("--out", output.toString(), input1.toString(), input2.toString()))

    assertThat(output.readText()).isEqualTo(
      """
      |<api version="1">
      |  <widget tag="1" since="2">
      |    <trait tag="1" since="3"/>
      |  </widget>
      |</api>
      """.trimMargin(),
    )
  }
}
