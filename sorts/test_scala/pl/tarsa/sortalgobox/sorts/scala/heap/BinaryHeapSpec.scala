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

import pl.tarsa.sortalgobox.core.common.items.agents.PlainItemsAgent
import pl.tarsa.sortalgobox.core.common.items.buffers.ComparableItemsBuffer
import pl.tarsa.sortalgobox.sorts.scala.heap.check.BinaryHeapChecker
import pl.tarsa.sortalgobox.tests.CommonUnitSpecBase

import scala.language.implicitConversions
import scala.util.Random

class BinaryHeapSpec extends CommonUnitSpecBase {
  typeBehavior[BinaryHeap[Int]]

  private def agent = PlainItemsAgent

  implicit def intArrayToComparableItemsBuffer(
      array: Array[Int]): ComparableItemsBuffer[Int] =
    ComparableItemsBuffer(0, array, 0)

  it must "build empty heap" in {
    val heap = BinaryHeap(agent, Array.emptyIntArray)
    assert(BinaryHeapChecker.check(heap))
  }

  it must "build single element heap" in {
    val heap = BinaryHeap(agent, Array(5))
    assert(BinaryHeapChecker.check(heap))
  }

  it must "build a few elements heap" in {
    val generator = new Random(seed = 0)
    val elems = Stream.continually(generator.nextInt(100)).take(10)
    val heap = BinaryHeap(agent, Array(elems: _*))
    assert(BinaryHeapChecker.check(heap))
  }

  it must "build heap step by step" in {
    val generator = new Random(seed = 0)
    val size = 100
    val storage = Array.ofDim[Int](size)
    val heap = new BinaryHeap(agent, storage)
    forAll(0 until size) { _ =>
      heap.addElem(generator.nextInt())
      assert(BinaryHeapChecker.check(heap))
    }
  }

  it must "fail on extraction from empty heap" in {
    val heap = BinaryHeap(agent, Array.emptyIntArray)
    intercept[IndexOutOfBoundsException] {
      heap.extractTop
    }
  }

  it must "extract the only element from heap" in {
    val element = 5
    val heap = BinaryHeap(agent, Array(element))
    heap.extractTop mustBe element
    heap.size mustBe 0
  }

  it must "extract maximum element from heap" in {
    val heap = BinaryHeap(agent, Array(5, 8, 3, 1))
    heap.extractTop mustBe 8
    heap.size mustBe 3
  }
}
