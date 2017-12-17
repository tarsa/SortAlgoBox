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
package pl.tarsa.sortalgobox.sorts.tests

import org.scalatest.MustMatchers
import pl.tarsa.sortalgobox.core.common.items.agents.PlainItemsAgent
import pl.tarsa.sortalgobox.core.common.{
  ComparableItemsAgentSortAlgorithm,
  NumericItemsAgentSortAlgorithm,
  SelfMeasuredSortAlgorithm
}
import pl.tarsa.sortalgobox.random.Mwc64x

class SortCheckerInt(doSorting: Array[Int] => Unit) extends MustMatchers {
  def forEmptyArray(): Unit = {
    val array = Array.emptyIntArray
    doSorting(array)
  }

  def forSingleElementArray(): Unit = {
    val array = Array(5)
    doSorting(array)
    array mustBe Array(5)
  }

  def forFewElementsArray(): Unit = {
    val array = Array(5, 3, 2, 8)
    doSorting(array)
    array mustBe Array(2, 3, 5, 8)
  }

  def forArrayOfSize(size: Int, bits: Int = 32): Unit = {
    val generator = new Mwc64x
    val array = Array.fill(size)(generator.next(bits))
    val sortedArray = array.sorted
    doSorting(array)
    array mustBe sortedArray
  }
}

class SortCheckerLong(doSorting: Array[Long] => Unit) extends MustMatchers {

  def forEmptyArray(): Unit = {
    val array = Array.emptyLongArray
    doSorting(array)
  }

  def forSingleElementArray(): Unit = {
    val array = Array(5L)
    doSorting(array)
    array mustBe Array(5L)
  }

  def forFewElementsArray(): Unit = {
    val array = Array(5L, 3L, 2L, 8L)
    doSorting(array)
    array mustBe Array(2L, 3L, 5L, 8L)
  }

  def forArrayOfSize(size: Int): Unit = {
    val generator = new Mwc64x
    val array = Array.fill(size)(generator.nextLong())
    val sortedArray = array.sorted
    doSorting(array)
    array mustBe sortedArray
  }
}

object SortChecker {
  def apply(algorithm: SelfMeasuredSortAlgorithm[Int]): SortCheckerInt =
    new SortCheckerInt(array => algorithm.sort(array))

  def apply(algorithm: ComparableItemsAgentSortAlgorithm): SortCheckerInt = {
    new SortCheckerInt(intArray => {
      val sortSetup = algorithm.setupSort(intArray)
      algorithm.sortExplicit(sortSetup, PlainItemsAgent)
    })
  }

  def apply(algorithm: NumericItemsAgentSortAlgorithm): SortCheckerInt = {
    new SortCheckerInt(intArray => {
      val sortSetup = algorithm.setupSort(intArray)
      algorithm.sortExplicit(sortSetup, PlainItemsAgent)
    })
  }
}
