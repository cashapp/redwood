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
package com.example.redwood.emojisearch.android.tests

import android.content.Context
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.By.pkg
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import org.junit.Before
import org.junit.Test

abstract class AbstractEmojiSearchUiTest(private val appPackage: String) {
  private val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())!!
  private val search get() = device.findObject(By.clazz("android.widget.EditText"))!!

  @Before fun before() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val intent = context.packageManager.getLaunchIntentForPackage(appPackage)!!.apply {
      addFlags(FLAG_ACTIVITY_CLEAR_TASK)
    }
    context.startActivity(intent)
    device.wait(Until.hasObject(pkg(appPackage).depth(0)), 5_000)
  }

  @Test fun searchTrees() {
    awaitText("0. +1")
    search.text = "tree"
    awaitText("301. christmas_tree")
    search.clear()
    awaitText("0. +1")
  }

  private fun awaitText(value: String, duration: Duration = 4.seconds) {
    val text = device.findObject(UiSelector().text(value))
    if (!text.waitForExists(duration.inWholeMilliseconds)) {
      throw AssertionError("Waited $duration for \"$value\" but never appeared")
    }
  }
}
