/**
 * Copyright (C) 2015 Piotr Tarsa ( http://github.com/tarsa )
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
 *
 */
package pl.tarsa.sortalgobox.sorts.scala.merge

import pl.tarsa.sortalgobox.core.common.SortAlgorithm
import pl.tarsa.sortalgobox.core.common.agents.MergeSortStorageAgent

class MergeSort extends SortAlgorithm[MergeSortStorageAgent] {
  override def sort[ItemType](
    storageAgent: MergeSortStorageAgent[ItemType]): Unit = {
    import storageAgent._

    val n = size0
    for (width <- Stream.iterate(1L)(_ * 2).takeWhile(_ < n)) {
      for (i <- 0L until n by width * 2) {
        val start = Math.min(i, n)
        val med = Math.min(start + width, n)
        val next = Math.min(med + width, n)
        bottomUpMerge(storageAgent, start.toInt, med.toInt, next.toInt)
      }
      copy10(0, 0, n)
    }
  }

  private def bottomUpMerge[ItemType](
    storageAgent: MergeSortStorageAgent[ItemType],
    start: Int, med: Int, next: Int): Unit = {
    import storageAgent._

    var left = start
    var right = med
    var dest = start
    while (left < med && right < next) {
      if (compare00(left, right) <= 0) {
        set1(dest, get0(left))
        left += 1
      } else {
        set1(dest, get0(right))
        right += 1
      }
      dest += 1
    }
    copy01(left, dest, med - left)
    copy01(right, dest + med - left, next - right)
  }
}
