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

import pl.tarsa.sortalgobox.sorts.scala.heap.check.BinaryHeapChecker
import pl.tarsa.sortalgobox.tests.CommonUnitSpecBase

import scala.util.Random

class BinaryHeapSpec extends CommonUnitSpecBase {

  typeBehavior[BinaryHeap[_]]

  it should "build empty heap" in {
    val heap = BinaryHeap(Array.emptyIntArray)
    assert(BinaryHeapChecker.check(heap))
  }

  it should "build single element heap" in {
    val heap = BinaryHeap(Array(5))
    assert(BinaryHeapChecker.check(heap))
  }

  it should "build a few elements heap" in {
    val generator = new Random(seed = 0)
    val elems = Stream.continually(generator.nextInt(100)).take(10)
    val heap = BinaryHeap(Array(elems: _*))
    assert(BinaryHeapChecker.check(heap))
  }

  it should "build heap step by step" in {
    val generator = new Random(seed = 0)
    val size = 100
    val storage = Array.ofDim[Int](size)
    val heap = new BinaryHeap(storage)
    (0 until size).foreach { _ =>
      heap.addElem(generator.nextInt())
      assert(BinaryHeapChecker.check(heap))
    }
  }

  it should "fail on extraction from empty heap" in {
    val heap = BinaryHeap(Array.emptyIntArray)
    intercept[IndexOutOfBoundsException] {
      heap.extractTop
    }
  }

  it should "extract the only element from heap" in {
    val element = 5
    val heap = BinaryHeap(Array(element))
    assertResult(element)(heap.extractTop)
    assertResult(0)(heap.size)
  }

  it should "extract maximum element from heap" in {
    val heap = BinaryHeap(Array(5, 8, 3, 1))
    assertResult(8)(heap.extractTop)
    assertResult(3)(heap.size)
  }
}
