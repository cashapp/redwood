/*
 * Copyright (C) 2023 Square, Inc.
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
@file:JvmName("CKlibUtils")
package app.cash.redwood.buildsupport

import app.cash.redwood.buildsupport.OS.linux
import app.cash.redwood.buildsupport.OS.osx
import app.cash.redwood.buildsupport.OS.windows
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.TargetSupportException

fun llvmHome(): String {
  return "${System.getProperty("user.home")}/.konan/dependencies/${llvmName()}"
}

// https://github.com/JetBrains/kotlin/blob/master/kotlin-native/konan/konan.properties
private fun llvmName(): String {
  return when (osName()) {
    osx -> if (HostManager.hostArch() == "aarch64") {
      "apple-llvm-20200714-macos-aarch64-1"
    } else {
      "apple-llvm-20200714-macos-x64-1"
    }
    linux -> "llvm-11.1.0-linux-x64-2"
    windows -> "llvm-11.1.0-windows-x64-2"
  }
}

private fun osName(): OS {
  val javaOsName = System.getProperty("os.name")
  return when {
    javaOsName == "Mac OS X" -> osx
    javaOsName == "Linux" -> linux
    javaOsName.startsWith("Windows") -> windows
    else -> throw TargetSupportException("Unknown operating system: $javaOsName")
  }
}

private enum class OS {
  osx, linux, windows
}
