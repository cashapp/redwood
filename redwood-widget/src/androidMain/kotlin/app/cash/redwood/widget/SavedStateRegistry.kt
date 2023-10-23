/*
 * Copyright 2021 The Android Open Source Project
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
package app.cash.redwood.widget

import androidx.savedstate.SavedStateRegistry as AndroidXSavedStateRegistry
import android.os.Binder
import android.os.Bundle
import android.os.Parcelable
import android.util.Size
import android.util.SizeF
import android.util.SparseArray
import java.io.Serializable

internal class AndroidSavedStateRegistry(
  private val key: String,
  private val delegate: AndroidXSavedStateRegistry,
) : SavedStateRegistry {
  override fun canBeSaved(value: Any): Boolean {
    // lambdas in Kotlin implement Serializable, but will crash if you really try to save them.
    // we check for both Function and Serializable (see kotlin.jvm.internal.Lambda) to support
    // custom user defined classes implementing Function interface.
    if (value is Function<*> && value is Serializable) {
      return false
    }
    return acceptableClasses.any { it.isInstance(value) }
  }

  /**
   * Contains Classes which can be stored inside [Bundle].
   *
   * Some of the classes are not added separately because:
   *
   * This classes implement Serializable:
   * - Arrays (DoubleArray, BooleanArray, IntArray, LongArray, ByteArray, FloatArray, ShortArray,
   * CharArray, Array<Parcelable, Array<String>)
   * - ArrayList
   * - Primitives (Boolean, Int, Long, Double, Float, Byte, Short, Char) will be boxed when casted
   * to Any, and all the boxed classes implements Serializable.
   * This class implements Parcelable:
   * - Bundle
   *
   * Note: it is simplified copy of the array from SavedStateHandle (lifecycle-viewmodel-savedstate).
   */
  private val acceptableClasses = arrayOf(
    Serializable::class.java,
    Parcelable::class.java,
    String::class.java,
    SparseArray::class.java,
    Binder::class.java,
    Size::class.java,
    SizeF::class.java,
  )

  override fun consumeRestoredState(): Map<String, List<Any?>>? {
    return delegate.consumeRestoredStateForKey(key)?.toMap()
  }

  override fun registerSavedStateProvider(provider: () -> Map<String, List<Any?>>) {
    delegate.registerSavedStateProvider(key) { provider().toBundle() }
  }

  override fun unregisterSavedStateProvider() {
    delegate.unregisterSavedStateProvider(key)
  }

  @Suppress("DEPRECATION")
  private fun Bundle.toMap(): Map<String, List<Any?>> {
    return keySet().associateWith { key ->
      getParcelableArrayList(key)!!
    }
  }

  @Suppress("UNCHECKED_CAST")
  private fun Map<String, List<Any?>>.toBundle(): Bundle {
    val bundle = Bundle()
    forEach { (key, list) ->
      val arrayList = if (list is ArrayList<Any?>) list else ArrayList(list)
      bundle.putParcelableArrayList(
        key,
        arrayList as ArrayList<Parcelable?>,
      )
    }
    return bundle
  }
}
