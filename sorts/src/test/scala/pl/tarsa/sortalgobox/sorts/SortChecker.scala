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
package pl.tarsa.sortalgobox.sorts

import org.scalatest.Matchers
import pl.tarsa.sortalgobox.random.Mwc64x
import pl.tarsa.sortalgobox.sorts.common._

class SortChecker(measuredSortAlgorithm: MeasuredSortAlgorithm[Int])
  extends Matchers {
  
  def forEmptyArray(): Unit = {
    val array = Array.emptyIntArray
    measuredSortAlgorithm.sort(array)
  }

  def forSingleElementArray(): Unit = {
    val array = Array(5)
    measuredSortAlgorithm.sort(array)
    array shouldBe Array(5)
  }

  def forFewElementsArray(): Unit = {
    val array = Array(5, 3, 2, 8)
    measuredSortAlgorithm.sort(array)
    array shouldBe Array(2, 3, 5, 8)
  }

  def forArrayOfSize(size: Int): Unit = {
    val generator = new Mwc64x
    val array = Array.fill(size)(generator.nextInt())
    val sortedArray = array.sorted
    measuredSortAlgorithm.sort(array)
    array shouldBe sortedArray
  }
}

object SortChecker {
  def apply(measuredSortAlgorithm: MeasuredSortAlgorithm[Int]): SortChecker = {
    new SortChecker(measuredSortAlgorithm)
  }

  def apply(sortAlgorithm: SortAlgorithm[Int]): SortChecker = {
    new SortChecker(MeasuringSortAlgorithmWrapper(sortAlgorithm))
  }
}
