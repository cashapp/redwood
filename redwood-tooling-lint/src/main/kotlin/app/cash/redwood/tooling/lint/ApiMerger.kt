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
package app.cash.redwood.tooling.lint

/**
 * Merges one or more API definitions into a single, merged API definition.
 *
 * Each API definition is an XML document in the following form:
 * ```xml
 * <api version="1">
 *   <widget tag="1" since="1">
 *     <trait tag="1" since="1"/>
 *     <trait tag="3" since="1"/>
 *   </widget>
 *   <layout-modifier tag="1" since="1">
 *     <property tag="1" since="1"/>
 *     <property tag="3" since="1"/>
 *   </layout-modifier>
 * </api>
 * ```
 */
public class ApiMerger {
  private val apis = mutableListOf<RedwoodApi>()

  /** Parse [xml] and add its API definition for merging. */
  public fun add(xml: String): ApiMerger = apply {
    val lintApi = RedwoodApi.deserialize(xml)
    require(lintApi.version == 1U) {
      "Only Redwood API XML version 1 is supported. Found: ${lintApi.version}"
    }
    apis += lintApi
  }

  @JvmSynthetic // Hide to Java callers.
  public operator fun plusAssign(xml: String) {
    add(xml)
  }

  /** Merge added API definitions into a single API definition XML. */
  public fun merge(): String {
    check(apis.isNotEmpty()) {
      "One or more API definitions must be added in order to merge"
    }
    return apis.merge().serialize()
  }

  private companion object {
    private fun List<RedwoodApi>.merge(): RedwoodApi {
      return RedwoodApi(
        version = 1U,
        widgets = mergeItems({ it.widgets }, { it.tag }, { it.merge() }),
        layoutModifiers = mergeItems({ it.layoutModifiers }, { it.tag }, { it.merge() }),
      )
    }

    private fun List<RedwoodWidget>.merge(): RedwoodWidget {
      return RedwoodWidget(
        tag = first().tag,
        since = maxOf { it.since },
        traits = mergeItems({ it.traits }, { it.tag }, { it.merge() }),
      )
    }

    private fun List<RedwoodWidgetTrait>.merge(): RedwoodWidgetTrait {
      return RedwoodWidgetTrait(
        tag = first().tag,
        since = maxOf { it.since },
      )
    }

    private fun List<RedwoodLayoutModifier>.merge(): RedwoodLayoutModifier {
      return RedwoodLayoutModifier(
        tag = first().tag,
        since = maxOf { it.since },
        properties = mergeItems({ it.properties }, { it.tag }, { it.merge() }),
      )
    }

    private fun List<RedwoodLayoutModifierProperty>.merge(): RedwoodLayoutModifierProperty {
      return RedwoodLayoutModifierProperty(
        tag = first().tag,
        since = maxOf { it.since },
      )
    }

    private fun <T, I, G : Comparable<G>> List<T>.mergeItems(
      itemSelector: (T) -> List<I>,
      groupSelector: (I) -> G,
      itemMerge: (List<I>) -> I,
    ): List<I> {
      return flatMap(itemSelector)
        .groupBy(groupSelector)
        .values
        .filter { it.size == size }
        .map(itemMerge)
        .sortedBy(groupSelector)
    }
  }
}
