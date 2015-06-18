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
package pl.tarsa.sortalgobox.sorts.scala.quick

import pl.tarsa.sortalgobox.core.common.ArrayHelpers
import pl.tarsa.sortalgobox.core.common.ComparisonsSupport.Conv

private [sortalgobox]
class SinglePivotPartition[T: Conv] {
  import ArrayHelpers._

  private var array: Array[T] = _
  private var notBiggerAfter: Int = _
  private var notSmallerStart: Int = _

  def partitionAndComputeBounds(array: Array[T], start: Int, after: Int,
                                pivotIndex: Int): (Int, Int) = {
    this.array = array
    notBiggerAfter = start
    notSmallerStart = after
    if (thereIsNotPartitionedPortion) {
      val pivot = array(pivotIndex)
      partitionNonEmptyArray(pivot)
    }
    computeOptimizedPartitionsBounds
  }

  def partitionNonEmptyArray(pivot: T): Unit = {
    while (thereIsNotPartitionedPortion) {
      growPartitionsWithoutSwapping(pivot)
      if (thereIsNotPartitionedPortion) {
        growBlockedPartitionsBySwap()
      }
    }
  }

  def thereIsNotPartitionedPortion = notBiggerAfter < notSmallerStart

  def growPartitionsWithoutSwapping(pivot: T): Unit = {
    growLeftPartitionUntilBlocked(pivot)
    growRightPartitionUntilBlocked(pivot)
  }

  def growLeftPartitionUntilBlocked(pivot: T): Unit = {
    while (thereIsNotPartitionedPortion && array(notBiggerAfter) < pivot) {
      notBiggerAfter += 1
    }
  }

  def growRightPartitionUntilBlocked(pivot: T): Unit = {
    while (thereIsNotPartitionedPortion && array(notSmallerStart - 1) > pivot) {
      notSmallerStart -= 1
    }
  }

  def growBlockedPartitionsBySwap(): Unit = {
    notSmallerStart -= 1
    swap(array, notBiggerAfter, notSmallerStart)
    notBiggerAfter += 1
  }

  def computeOptimizedPartitionsBounds = {
    if (partitionsOverlap) {
      assert(partitionsOverlapMinimally,
        s"Overlap is: ${notBiggerAfter - notSmallerStart}")
      (notBiggerAfter - 1, notSmallerStart + 1)
    } else {
      (notBiggerAfter, notSmallerStart)
    }
  }

  def partitionsOverlapMinimally = notBiggerAfter - notSmallerStart == 1

  def partitionsOverlap = notBiggerAfter > notSmallerStart
}
