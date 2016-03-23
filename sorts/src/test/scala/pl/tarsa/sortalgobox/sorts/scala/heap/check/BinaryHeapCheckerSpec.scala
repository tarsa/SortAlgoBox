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
package pl.tarsa.sortalgobox.sorts.scala.heap.check

import pl.tarsa.sortalgobox.core.common.agents.implementations.ComparingIntArrayItemsAgent
import pl.tarsa.sortalgobox.sorts.scala.heap.BinaryHeap
import pl.tarsa.sortalgobox.tests.CommonUnitSpecBase

import scala.language.implicitConversions

class BinaryHeapCheckerSpec extends CommonUnitSpecBase {

  typeBehavior[BinaryHeapChecker.type]

  implicit def intArrayToComparingItemsAgent(array: Array[Int]):
  ComparingIntArrayItemsAgent = new ComparingIntArrayItemsAgent(array)

  it must "handle empty heap" in {
    val array = Array.emptyIntArray
    val heap = new BinaryHeap(array) {
      size = array.length
    }
    assert(BinaryHeapChecker.check(heap))
  }

  it must "handle single element heap" in {
    val array = Array(5)
    val heap = new BinaryHeap(array) {
      size = array.length
    }
    assert(BinaryHeapChecker.check(heap))
  }

  it must "handle multiple element heap" in {
    val array = Array(5, 3, 4, 3, 2, 4)
    val heap = new BinaryHeap(array) {
      size = array.length
    }
    assert(BinaryHeapChecker.check(heap))
  }

  it must "detect violations of heap property" in {
    val array = Array(3, 3, 5, 2, 3)
    val heap = new BinaryHeap(array) {
      size = array.length
    }
    assert(!BinaryHeapChecker.check(heap))
  }
}
