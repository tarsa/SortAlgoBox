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

import pl.tarsa.sortalgobox.core.common.ComparisonsSupport._
import pl.tarsa.sortalgobox.core.common.{ComparisonSortAlgorithm, ExSituAlgorithm}

class MergeSort[T: Conv](implicit val makeArray: Int => Array[T])
  extends ComparisonSortAlgorithm[T] with ExSituAlgorithm[T] {

  override def sort(array: Array[T]): Unit = {
    val n = array.length
    val buffer = makeArray(n)
    for (width <- Stream.iterate(1L)(_ * 2).takeWhile(_ < n)) {
      for (i <- 0L until n by width * 2) {
        val start = Math.min(i, n)
        val med = Math.min(start + width, n)
        val next = Math.min(med + width, n)
        bottomUpMerge(array, buffer, start.toInt, med.toInt, next.toInt)
      }
      Array.copy(buffer, 0, array, 0, n)
    }
  }

  private def bottomUpMerge(array: Array[T], buffer: Array[T],
    start: Int, med: Int, next: Int): Unit = {
    var left = start
    var right = med
    var dest = start
    while (left < med && right < next) {
      if (array(left) <= array(right)) {
        buffer(dest) = array(left)
        left += 1
      } else {
        buffer(dest) = array(right)
        right += 1
      }
      dest += 1
    }
    Array.copy(array, left, buffer, dest, med - left)
    Array.copy(array, right, buffer, dest + med - left, next - right)
  }
}
