/*
 * Copyright (C) 2024 Square, Inc.
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
package app.cash.redwood.treehouse.leaks

import java.util.IdentityHashMap

/** Returns the shortest cycle involving [start], or null if it participates in no cycle. */
internal fun Heap.findCycle(start: Any): List<String>? {
  val queue = ArrayDeque<Node>()
  val nodes = IdentityHashMap<Any, Node>()

  nodes[start] = Node(
    targetEdges = references(start),
    sourceEdge = null,
    sourceNode = null,
  ).also {
    queue += it
  }

  for (node in generateSequence { queue.removeFirstOrNull() }) {
    for (edge in node.targetEdges) {
      val instance = edge.instance ?: continue

      if (instance === start) {
        val result = ArrayDeque<String>()
        result.addFirst(edge.name)
        for (sourceNode in generateSequence(node) { it.sourceNode }) {
          result.addFirst(sourceNode.sourceEdge?.name ?: break)
        }
        return result
      }

      nodes.getOrPut(instance) {
        Node(
          targetEdges = references(instance),
          sourceEdge = edge,
          sourceNode = node,
        ).also {
          queue += it
        }
      }
    }
  }

  return null
}

internal interface Heap {
  fun references(instance: Any): List<Edge>
}

internal class Node(
  val targetEdges: List<Edge>,
  val sourceEdge: Edge?,
  val sourceNode: Node?,
)

internal data class Edge(
  val name: String,
  val instance: Any?,
)
