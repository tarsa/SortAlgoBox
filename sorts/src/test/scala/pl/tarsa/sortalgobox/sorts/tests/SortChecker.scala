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
package pl.tarsa.sortalgobox.sorts.tests

import org.scalatest.Matchers
import pl.tarsa.sortalgobox.core.common._
import pl.tarsa.sortalgobox.core.common.agents._
import pl.tarsa.sortalgobox.random.Mwc64x
import pl.tarsa.sortalgobox.sorts.scala.merge.MergeSort
import pl.tarsa.sortalgobox.sorts.scala.radix.RadixSort

class SortChecker(doSorting: (Array[Int]) => Unit) extends Matchers {
  
  def forEmptyArray(): Unit = {
    val array = Array.emptyIntArray
    doSorting(array)
  }

  def forSingleElementArray(): Unit = {
    val array = Array(5)
    doSorting(array)
    array shouldBe Array(5)
  }

  def forFewElementsArray(): Unit = {
    val array = Array(5, 3, 2, 8)
    doSorting(array)
    array shouldBe Array(2, 3, 5, 8)
  }

  def forArrayOfSize(size: Int): Unit = {
    val generator = new Mwc64x
    val array = Array.fill(size)(generator.nextInt())
    val sortedArray = array.sorted
    doSorting(array)
    array shouldBe sortedArray
  }
}

object SortChecker {
  def apply(measuredSortAlgorithm: MeasuredSortAlgorithm[Int]): SortChecker = {
    new SortChecker(array => measuredSortAlgorithm.sort(array))
  }

  def apply(comparisonSortAlgorithm: ComparisonSortAlgorithm): SortChecker = {
    new SortChecker ({ intArray: Array[Int] =>
      val itemsAgent = new ComparingItemsAgent[Int] {
        override def storage0 = intArray

        override def compare(a: Int, b: Int): Int = Ordering.Int.compare(a, b)
      }
      comparisonSortAlgorithm.sort(itemsAgent)
    })
  }

  def apply(mergeSort: MergeSort): SortChecker = {
    new SortChecker ({ intArray: Array[Int] =>
      val buffer = Array.ofDim[Int](intArray.length)
      val itemsAgent = new MergeSortItemsAgent[Int] {
        override def storage0 = intArray

        override def storage1 = buffer

        override def compare(a: Int, b: Int): Int = Ordering.Int.compare(a, b)
      }
      mergeSort.sort(itemsAgent)
    })
  }

  def apply(radixSort: RadixSort): SortChecker = {
    new SortChecker ({ intArray: Array[Int] =>
      val buffer = Array.ofDim[Int](intArray.length)
      val itemsAgent = new RadixSortItemsAgent[Int] {
        override def storage0 = intArray

        override def storage1 = buffer

        override def keySizeInBits: Int = 32

        override def getItemSlice(v: Int, lowestBit: Int, length: Int): Int = {
          ((v ^ Int.MinValue) >>> lowestBit) & ((1 << length) - 1)
        }
      }
      radixSort.sort(itemsAgent)
    })
  }
}
