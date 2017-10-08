/*
 * Copyright (C) 2015 - 2017 Piotr Tarsa ( http://github.com/tarsa )
 *
 * This software is provided 'as-is', without any express or implied
 * warranty.  In no event will the author be held liable for any damages
 * arising from the use of this software.
 *
 * Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 *
 * 1. The origin of this software must not be misrepresented; you must not
 * claim that you wrote the original software. If you use this software
 * in a product, an acknowledgment in the product documentation would be
 * appreciated but is not required.
 * 2. Altered source versions must be plainly marked as such, and must not be
 * misrepresented as being the original software.
 * 3. This notice may not be removed or altered from any source distribution.
 */
package pl.tarsa.sortalgobox.sorts.scala.sab

import pl.tarsa.sortalgobox.core.common.ComparisonSortAlgorithm
import pl.tarsa.sortalgobox.core.common.agents.ComparingItemsAgent

import scala.annotation.tailrec

class SabHeapBinaryOneBasedVariantA extends ComparisonSortAlgorithm {
  override def sort[ItemType](agent: ComparingItemsAgent[ItemType]): Unit = {
    heapsort(agent.withBase(1))
  }

  private def heapsort[ItemType](agent: ComparingItemsAgent[ItemType]): Unit = {
    heapify(agent)
    drainHeap(agent)
  }

  private def heapify[ItemType](agent: ComparingItemsAgent[ItemType]): Unit = {
    val count = agent.size0
    for (item <- (count / 2) to 1 by -1) {
      siftDown(agent, item, count)
    }
  }

  private def siftDown[ItemType](agent: ComparingItemsAgent[ItemType],
                                 start: Int,
                                 end: Int): Unit = {
    import agent._

    @tailrec
    def siftStep(root: Int): Unit = {
      val left = root * 2
      val right = left + 1

      if (right <= end) {
        if (compareLt0(root, left)) {
          if (compareLt0(left, right)) {
            swap0(root, right)
            siftStep(right)
          } else {
            swap0(root, left)
            siftStep(left)
          }
        } else {
          if (compareLt0(root, right)) {
            swap0(root, right)
            siftStep(right)
          }
        }
      } else {
        if (left == end && compareLt0(root, left)) {
          swap0(root, left)
        }
      }
    }

    siftStep(start)
  }

  private def drainHeap[ItemType](
      agent: ComparingItemsAgent[ItemType]): Unit = {
    val count = agent.size0
    for (next <- count until 1 by -1) {
      agent.swap0(next, 1)
      siftDown(agent, 1, next - 1)
    }
  }
}
