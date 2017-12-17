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

import pl.tarsa.sortalgobox.core.common.Specialization.{Group => Grp}
import pl.tarsa.sortalgobox.core.common.items.buffers.ComparableItemsBuffer
import pl.tarsa.sortalgobox.sorts.scala.ComparisonSortBase

import scala.annotation.tailrec
import scala.{specialized => spec}

object SabHeapBinaryOneBasedVariantA extends ComparisonSortBase {
  override def setupSort[@spec(Grp) Item: Ordering](
      items: Array[Item]): Setup[Item] =
    new Setup(ComparableItemsBuffer(0, items, 1))

  override def sort[@spec(Grp) Item: Setup, _: Agent](): Unit = {
    heapify()
    drainHeap()
  }

  private def heapify[@spec(Grp) Item: Setup, _: Agent](): Unit = {
    val count = a.size(buf1[Item])
    for (item <- (count / 2) to 1 by -1) {
      siftDown(item, count)
    }
  }

  private def siftDown[@spec(Grp) Item: Setup, _: Agent](start: Int,
                                                         end: Int): Unit = {
    val buf = buf1[Item]

    @tailrec
    def siftStep(root: Int): Unit = {
      val left = root * 2
      val right = left + 1

      if (right <= end) {
        if (a.compareLtI(buf, root, left)) {
          if (a.compareLtI(buf, left, right)) {
            a.swap(buf, root, right)
            siftStep(right)
          } else {
            a.swap(buf, root, left)
            siftStep(left)
          }
        } else {
          if (a.compareLtI(buf, root, right)) {
            a.swap(buf, root, right)
            siftStep(right)
          }
        }
      } else {
        if (left == end && a.compareLtI(buf, root, left)) {
          a.swap(buf, root, left)
        }
      }
    }

    siftStep(start)
  }

  private def drainHeap[@spec(Grp) Item: Setup, _: Agent](): Unit = {
    val buf = buf1[Item]
    val count = a.size(buf)
    for (next <- count until 1 by -1) {
      a.swap(buf, next, 1)
      siftDown(1, next - 1)
    }
  }
}
