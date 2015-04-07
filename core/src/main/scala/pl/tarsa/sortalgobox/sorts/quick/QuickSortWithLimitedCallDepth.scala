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
package pl.tarsa.sortalgobox.sorts.quick

import pl.tarsa.sortalgobox.sorts.common.ComparisonsSupport
import ComparisonsSupport.Conv

class QuickSortWithLimitedCallDepth[T: Conv](
  val partition: SinglePivotPartition[T]) extends QuickSort[T] {

  override def sort(array: Array[T]): Unit = {
    sortManualTcoOuter(array, 0, array.length, partition)
  }

  def sortManualTcoOuter(array: Array[T], start: Int, after: Int,
    partition: SinglePivotPartition[T]): Unit = {
    var currentStart = start
    var currentAfter = after
    while (currentAfter - currentStart > 1) {
      val (newStart, newAfter) =
        sortManualTcoInner(array, currentAfter, currentStart, partition)
      currentStart = newStart
      currentAfter = newAfter
    }
  }

  def sortManualTcoInner(array: Array[T], after: Int, start: Int,
    partition: SinglePivotPartition[T]) = {
    val (leftAfter, rightStart) = selectPivotAndComputePartitionsBounds(array,
      start, after, partition)
    spawnSmallerAndReturnBiggerPartition(array, start, after,
      leftAfter, rightStart, partition)
  }

  def selectPivotAndComputePartitionsBounds(array: Array[T],
    start: Int, after: Int, partition: SinglePivotPartition[T]) = {
    val pivotIndex = (after - start) / 2 + start
    val (leftAfter, rightStart) = partition.partitionAndComputeBounds(
      array, start, after, pivotIndex)
    (leftAfter, rightStart)
  }

  def spawnSmallerAndReturnBiggerPartition(array: Array[T],
    start: Int, after: Int, leftAfter: Int, rightStart: Int,
    partition: SinglePivotPartition[T]) = {
    val leftPartitionSize = leftAfter - start
    val rightPartitionSize = after - rightStart
    if (leftPartitionSize < rightPartitionSize) {
      sortManualTcoOuter(array, start, leftAfter, partition)
      (rightStart, after)
    } else {
      sortManualTcoOuter(array, rightStart, after, partition)
      (start, leftAfter)
    }
  }
}
