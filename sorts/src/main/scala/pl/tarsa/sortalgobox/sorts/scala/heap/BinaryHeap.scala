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
package pl.tarsa.sortalgobox.sorts.scala.heap

import pl.tarsa.sortalgobox.core.common.agents.ComparingItemsAgent

import scala.annotation.tailrec

class BinaryHeap[ItemType](val itemsAgent: ComparingItemsAgent[ItemType]) {
  import itemsAgent._

  var size = 0

  def addElem(a: ItemType): Unit = {
    set0(size, a)
    size += 1
    siftUp(size - 1)
  }

  def extractTop: ItemType = {
    val result = get0(0)
    set0(0, get0(size - 1))
    size -= 1
    siftDown(0)
    result
  }

  @tailrec
  final def siftUp(currentIndex: Int): Unit = {
    if (currentIndex > 0) {
      val parentIndex = getParentIndex(currentIndex)
      if (compare0(parentIndex, currentIndex) < 0) {
        swap0(currentIndex, parentIndex)
        siftUp(parentIndex)
      }
    }
  }

  @tailrec
  final def siftDown(currentIndex: Int): Unit = {
    var maxIndex = currentIndex
    val firstChildIndex = currentIndex * 2 + 1
    if (firstChildIndex < size && compare0(firstChildIndex, maxIndex) > 0) {
      maxIndex = firstChildIndex
    }
    val secondChildIndex = firstChildIndex + 1
    if (secondChildIndex < size && compare0(secondChildIndex, maxIndex) > 0) {
      maxIndex = secondChildIndex
    }
    swap0(currentIndex, maxIndex)
    if (maxIndex != currentIndex) {
      siftDown(maxIndex)
    }
  }

  def getParentIndex(childIndex: Int) = (childIndex - 1) / 2
}

object BinaryHeap {
  def apply[ItemType](itemsAgent: ComparingItemsAgent[ItemType]):
  BinaryHeap[ItemType] = {
    val heap = new BinaryHeap[ItemType](itemsAgent)
    heap.size = itemsAgent.size0
    heapify(heap)
    heap
  }

  def heapify[ItemType](heap: BinaryHeap[ItemType]): Unit = {
    (0 until heap.size).foreach(heap.siftUp)
  }
}
