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
package pl.tarsa.sortalgobox.sorts.scala.heap.check

import pl.tarsa.sortalgobox.sorts.scala.heap.BinaryHeap
import pl.tarsa.sortalgobox.tests.CommonUnitSpecBase

class BinaryHeapCheckerSpec extends CommonUnitSpecBase {

  typeBehavior[BinaryHeapChecker.type]

  it should "handle empty heap" in {
    val array = Array.emptyIntArray
    val heap = new BinaryHeap(array) {
      size = array.length
    }
    assert(BinaryHeapChecker.check(heap))
  }

  it should "handle single element heap" in {
    val array = Array(5)
    val heap = new BinaryHeap(array) {
      size = array.length
    }
    assert(BinaryHeapChecker.check(heap))
  }

  it should "handle multiple element heap" in {
    val array = Array(5, 3, 4, 3, 2, 4)
    val heap = new BinaryHeap(array) {
      size = array.length
    }
    assert(BinaryHeapChecker.check(heap))
  }

  it should "detect violations of heap property" in {
    val array = Array(3, 3, 5, 2, 3)
    val heap = new BinaryHeap(array) {
      size = array.length
    }
    assert(!BinaryHeapChecker.check(heap))
  }
}
