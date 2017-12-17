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
package pl.tarsa.sortalgobox.sorts.scala.bitonic

import pl.tarsa.sortalgobox.core.common.Specialization.Group
import pl.tarsa.sortalgobox.sorts.scala.ComparisonSortBase

object BitonicSort extends ComparisonSortBase {
  override def sort[@specialized(Group) Item: Setup, _: Agent](): Unit = {
    val buf = buf1[Item]

    val size = a.size(buf)
    val phasesPerBlock = Iterator
      .iterate(1)(_ + 1)
      .takeWhile(phases => (1L << (phases - 1)) < size)
    for (phasesInBlock <- phasesPerBlock;
         phase <- phasesInBlock to 1 by -1) {
      val firstPhaseInBlock = phase == phasesInBlock
      val halfBlockSize = 1 << (phase - 1)
      for (i <- 0 until (size / 2)) {
        val (first, second) = if (firstPhaseInBlock) {
          val upper = (i + (i & -halfBlockSize)) ^ (halfBlockSize - 1)
          val lower = upper ^ ((halfBlockSize << 1) - 1)
          (upper, lower)
        } else {
          val upper = i + (i & -halfBlockSize)
          val lower = upper + halfBlockSize
          (upper, lower)
        }
        if (second < size && a.compareGtI(buf, first, second)) {
          a.swap(buf, first, second)
        }
      }
    }
  }
}
