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
package pl.tarsa.sortalgobox.sorts.heap

import pl.tarsa.sortalgobox.sorts.common.ArrayHelpers.swap
import pl.tarsa.sortalgobox.sorts.common.ComparisonsSupport.Conv

import scala.annotation.tailrec

class BinaryHeap[T: Conv](var storage: Array[T]) {
  var size = 0

  def addElem(a: T): Unit = {
    storage(size) = a
    size += 1
    siftUp(size - 1)
  }

  def extractTop: T = {
    val result = storage(0)
    storage(0) = storage(size - 1)
    size -= 1
    siftDown(0)
    result
  }

  @tailrec
  final def siftUp(currentIndex: Int): Unit = {
    if (currentIndex > 0) {
      val parentIndex = getParentIndex(currentIndex)
      if (storage(parentIndex) < storage(currentIndex)) {
        swap(storage, currentIndex, parentIndex)
        siftUp(parentIndex)
      }
    }
  }

  @tailrec
  final def siftDown(currentIndex: Int): Unit = {
    var maxIndex = currentIndex
    val firstChildIndex = currentIndex * 2 + 1
    if (firstChildIndex < size &&
      storage(firstChildIndex) > storage(maxIndex)) {
      maxIndex = firstChildIndex
    }
    val secondChildIndex = firstChildIndex + 1
    if (secondChildIndex < size &&
      storage(secondChildIndex) > storage(maxIndex)) {
      maxIndex = secondChildIndex
    }
    swap(storage, currentIndex, maxIndex)
    if (maxIndex != currentIndex) {
      siftDown(maxIndex)
    }
  }

  def getParentIndex(childIndex: Int) = (childIndex - 1) / 2
}

object BinaryHeap {
  def apply[T: Conv](storage: Array[T]): BinaryHeap[T] = {
    val heap = new BinaryHeap[T](storage)
    heap.size = storage.length
    heapify(heap)
    heap
  }

  def heapify[T: Conv](heap: BinaryHeap[T]): Unit = {
    heap.storage.indices.foreach(heap.siftUp)
  }
}
