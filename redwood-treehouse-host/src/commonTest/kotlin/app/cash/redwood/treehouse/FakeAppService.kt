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
package app.cash.redwood.treehouse

internal class FakeAppService private constructor(
  private val name: String,
  private val eventLog: EventLog,
  private val listeners: List<Listener>,
  private val mutableUis: MutableList<FakeZiplineTreehouseUi>,
) : AppService {

  val uis: List<FakeZiplineTreehouseUi>
    get() = mutableUis.toList()

  override val appLifecycle = object : AppLifecycle {
    override fun start(host: AppLifecycle.Host) {
      eventLog += "$name.appLifecycle.start()"
    }

    override fun sendFrame(timeNanos: Long) {
    }
  }

  constructor(
    name: String,
    eventLog: EventLog,
  ) : this(name, eventLog, listOf(), mutableListOf())

  /**
   * Return a FakeAppService that shares all state with this, but that notifies [listeners] of
   * events triggered through it.
   *
   * Note that this does not add [listener] to receive events triggered directly on this.
   *
   * This awkward pattern emulates `ZiplineScope`, which also only tracks objects on the scoped
   * wrapper.
   */
  fun withListener(listener: Listener) =
    FakeAppService(name, eventLog, listeners + listener, mutableUis)

  fun newUi(): ZiplineTreehouseUi {
    val result = FakeZiplineTreehouseUi("$name.uis[${mutableUis.size}]", eventLog)
    for (listener in listeners) {
      listener.onNewUi(result)
    }
    mutableUis += result
    return result
  }

  interface Listener {
    fun onNewUi(ui: ZiplineTreehouseUi)
  }
}
