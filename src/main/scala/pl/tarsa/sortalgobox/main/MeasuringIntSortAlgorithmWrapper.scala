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
package pl.tarsa.sortalgobox.main

import pl.tarsa.sortalgobox.core.common.agents.ComparingStorageAgent
import pl.tarsa.sortalgobox.core.common._

object MeasuringIntSortAlgorithmWrapper {
  def apply(plainSortAlgorithm: AnyRef): MeasuredSortAlgorithm[Int] =
    plainSortAlgorithm match {
      case sortAlgorithm: IntSortAlgorithm =>
        wrap(sortAlgorithm)
      case sortAlgorithm: ComparisonSortAlgorithm =>
        wrap { intArray: Array[Int] =>
          val storageAgent = new ComparingStorageAgent[Int] {
            override def storage0 = intArray

            override def compare(a: Int, b: Int): Int =
              Ordering.Int.compare(a, b)
          }
          val startTime = System.nanoTime()
          sortAlgorithm.sort(storageAgent)
          System.nanoTime() - startTime
        }
    }

  def wrap(doSorting: (Array[Int]) => Unit): MeasuredSortAlgorithm[Int] =
    new MeasuredSortAlgorithm[Int] {
      override def sort(array: Array[Int]): Long = {
        val startTime = System.nanoTime()
        doSorting(array)
        System.nanoTime() - startTime
      }
    }
}
