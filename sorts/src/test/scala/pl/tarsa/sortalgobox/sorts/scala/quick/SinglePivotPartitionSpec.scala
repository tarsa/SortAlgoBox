/*
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

import pl.tarsa.sortalgobox.core.common.agents.implementations.ComparingIntArrayItemsAgent
import pl.tarsa.sortalgobox.tests.CommonUnitSpecBase

import scala.language.implicitConversions

class SinglePivotPartitionSpec extends CommonUnitSpecBase {

  typeBehavior[SinglePivotPartition]

  implicit def intArrayToComparingItemsAgent(array: Array[Int]):
  ComparingIntArrayItemsAgent = new ComparingIntArrayItemsAgent(array)

  it should "handle empty arrays" in {
    val array = Array[Int]()
    val partition = new SinglePivotPartition
    val (leftAfter, rightStart) = partition.partitionAndComputeBounds(
      array, 0, array.length, 0)
    leftAfter shouldBe 0
    rightStart shouldBe 0
  }

  it should "handle single element arrays" in {
    val array = Array(5)
    val partition = new SinglePivotPartition
    val (leftAfter, rightStart) = partition.partitionAndComputeBounds(
      array, 0, array.length, 0)
    leftAfter should be <= rightStart
    array shouldBe Array(5)
  }

  it should "handle sorted arrays" in {
    def freshArray = Array(1, 2, 3, 5, 8, 13, 21)
    val array = freshArray
    val partition = new SinglePivotPartition
    val (leftAfter, rightStart) = partition.partitionAndComputeBounds(
      array, 0, array.length, 0)
    leftAfter shouldBe 0
    rightStart shouldBe 1
    array(0) shouldBe 1
    array.sorted shouldBe freshArray
  }

  it should "handle unsorted arrays" in {
    def freshArray = Array(1, 21, 5, 8, 3, 2, 13)
    val array = freshArray
    val partition = new SinglePivotPartition
    val pivotIndex = 3
    val pivot = array(pivotIndex)
    val (leftAfter, rightStart) = partition.partitionAndComputeBounds(
      array, 0, array.length, pivotIndex)
    all (array.slice(0, leftAfter)) should be <= pivot
    all (array.slice(rightStart, array.length)) should be >= pivot
    array.sorted shouldBe freshArray.sorted
  }
}
