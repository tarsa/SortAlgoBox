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

import pl.tarsa.sortalgobox.core.common.agents.ComparingStorageAgent

class QuickSortWithLimitedCallDepth(
  val partition: SinglePivotPartition) extends QuickSort {

  override def sort[ItemType](
    storageAgent: ComparingStorageAgent[ItemType]): Unit = {
    import storageAgent._
    sortManualTcoOuter(storageAgent, 0, size0)
  }

  def sortManualTcoOuter[ItemType](
    storageAgent: ComparingStorageAgent[ItemType],
    start: Int, after: Int): Unit = {
    var currentStart = start
    var currentAfter = after
    while (currentAfter - currentStart > 1) {
      val (newStart, newAfter) =
        sortManualTcoInner(storageAgent, currentAfter, currentStart)
      currentStart = newStart
      currentAfter = newAfter
    }
  }

  def sortManualTcoInner[ItemType](
    storageAgent: ComparingStorageAgent[ItemType], after: Int,
    start: Int) = {
    val (leftAfter, rightStart) = selectPivotAndComputePartitionsBounds(
      storageAgent, start, after)
    spawnSmallerAndReturnBiggerPartition(storageAgent, start, after,
      leftAfter, rightStart)
  }

  def selectPivotAndComputePartitionsBounds[ItemType](
    storageAgent: ComparingStorageAgent[ItemType],
    start: Int, after: Int) = {
    val pivotIndex = (after - start) / 2 + start
    val (leftAfter, rightStart) = partition.partitionAndComputeBounds(
      storageAgent, start, after, pivotIndex)
    (leftAfter, rightStart)
  }

  def spawnSmallerAndReturnBiggerPartition[ItemType](
    storageAgent: ComparingStorageAgent[ItemType],
    start: Int, after: Int, leftAfter: Int, rightStart: Int) = {
    val leftPartitionSize = leftAfter - start
    val rightPartitionSize = after - rightStart
    if (leftPartitionSize < rightPartitionSize) {
      sortManualTcoOuter(storageAgent, start, leftAfter)
      (rightStart, after)
    } else {
      sortManualTcoOuter(storageAgent, rightStart, after)
      (start, leftAfter)
    }
  }
}
