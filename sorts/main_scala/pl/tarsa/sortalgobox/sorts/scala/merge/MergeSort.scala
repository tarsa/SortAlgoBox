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
package pl.tarsa.sortalgobox.sorts.scala.merge

import pl.tarsa.sortalgobox.core.common.ComparableItemsAgentSortAlgorithm
import pl.tarsa.sortalgobox.core.common.Specialization.Group
import pl.tarsa.sortalgobox.core.common.items.buffers.ComparableItemsBuffer

import scala.{specialized => spec}

object MergeSort extends ComparableItemsAgentSortAlgorithm {
  class Setup[@spec(Group) Item: Permit](
      val buffer1: ComparableItemsBuffer[Item],
      val buffer2: ComparableItemsBuffer[Item])

  override def setupSort[@spec(Group) Item: Ordering](
      items: Array[Item]): Setup[Item] = {
    new Setup(ComparableItemsBuffer(0, items, 0),
              ComparableItemsBuffer(0, items.clone(), 0))
  }

  override protected def sort[@spec(Group) Item: Setup, _: Agent](): Unit = {
    val buf1 = setup[Item].buffer1
    val buf2 = setup[Item].buffer2

    val n = a.size(buf1)
    for (width <- Iterator.iterate(1L)(_ * 2).takeWhile(_ < n)) {
      for (i <- 0L until n by width * 2) {
        val start = Math.min(i, n)
        val med = Math.min(start + width, n)
        val next = Math.min(med + width, n)
        bottomUpMerge(start.toInt, med.toInt, next.toInt)
      }
      for (i <- 0 until n) {
        a.set(buf1, i, a.get(buf2, i))
      }
    }
  }

  private def bottomUpMerge[@spec(Group) Item: Setup, _: Agent](
      start: Int,
      med: Int,
      next: Int): Unit = {
    val buf1 = setup[Item].buffer1
    val buf2 = setup[Item].buffer2

    var left = start
    var right = med
    var dest = start
    while (left < med && right < next) {
      if (a.compareLteI(buf1, left, right)) {
        a.set(buf2, dest, a.get(buf1, left))
        left += 1
      } else {
        a.set(buf2, dest, a.get(buf1, right))
        right += 1
      }
      dest += 1
    }
    while (left < med) {
      a.set(buf2, dest, a.get(buf1, left))
      left += 1
      dest += 1
    }
    while (right < next) {
      a.set(buf2, dest, a.get(buf1, right))
      right += 1
      dest += 1
    }
  }
}
