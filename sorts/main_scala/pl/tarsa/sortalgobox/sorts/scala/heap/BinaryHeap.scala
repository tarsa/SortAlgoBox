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
package pl.tarsa.sortalgobox.sorts.scala.heap

import pl.tarsa.sortalgobox.core.common.Specialization.Group
import pl.tarsa.sortalgobox.core.common.items.agents.ItemsAgent
import pl.tarsa.sortalgobox.core.common.items.buffers.ComparableItemsBuffer

import scala.annotation.tailrec

class BinaryHeap[@specialized(Group) Item] protected[heap] (
    val itemsAgent: ItemsAgent,
    val buf: ComparableItemsBuffer[Item]) {
  import itemsAgent._

  var size = 0

  def addElem(a: Item): Unit = {
    set(buf, size, a)
    size += 1
    siftUp(size - 1)
  }

  def extractTop: Item = {
    val result = get(buf, 0)
    set(buf, 0, get(buf, size - 1))
    size -= 1
    siftDown(0)
    result
  }

  @tailrec
  final def siftUp(currentIndex: Int): Unit = {
    if (currentIndex > 0) {
      val parentIndex = getParentIndex(currentIndex)
      if (compareLtI(buf, parentIndex, currentIndex)) {
        swap(buf, currentIndex, parentIndex)
        siftUp(parentIndex)
      }
    }
  }

  @tailrec
  final def siftDown(currentIndex: Int): Unit = {
    var maxIndex = currentIndex
    val firstChildIndex = currentIndex * 2 + 1
    if (firstChildIndex < size && compareGtI(buf, firstChildIndex, maxIndex)) {
      maxIndex = firstChildIndex
    }
    val secondChildIndex = firstChildIndex + 1
    if (secondChildIndex < size && compareGtI(buf, secondChildIndex, maxIndex)) {
      maxIndex = secondChildIndex
    }
    swap(buf, currentIndex, maxIndex)
    if (maxIndex != currentIndex) {
      siftDown(maxIndex)
    }
  }

  def getParentIndex(childIndex: Int): Int =
    (childIndex - 1) / 2
}

object BinaryHeap {
  def apply[@specialized(Group) Item](
      itemsAgent: ItemsAgent,
      buffer: ComparableItemsBuffer[Item]): BinaryHeap[Item] = {
    val heap = new BinaryHeap[Item](itemsAgent, buffer)
    heap.size = itemsAgent.size(buffer)
    heapify(heap)
    heap
  }

  def heapify[ItemType](heap: BinaryHeap[ItemType]): Unit = {
    (0 until heap.size).foreach(heap.siftUp)
  }
}
