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
package pl.tarsa.sortalgobox.sorts.scala.radix

import pl.tarsa.sortalgobox.core.common.Specialization.{Group => Grp}
import pl.tarsa.sortalgobox.core.common._
import pl.tarsa.sortalgobox.core.common.items.buffers.NumericItemsBuffer
import pl.tarsa.sortalgobox.core.common.items.buffers.NumericItemsBuffer.Evidence

import scala.{specialized => spec}

class RadixSort(radixBits: Int = 8) extends NumericItemsAgentSortAlgorithm {
  class Setup[@spec(Grp) Item: Permit](val buffer1: NumericItemsBuffer[Item],
                                       val buffer2: NumericItemsBuffer[Item])

  override def setupSort[@spec(Grp) Item: Evidence](
      items: Array[Item]): Setup[Item] = {
    new Setup(NumericItemsBuffer(0, items, 0),
              NumericItemsBuffer(0, items.clone(), 0))
  }

  override protected def sort[@spec(Grp) Item: Setup, _: Agent](): Unit = {
    val buf1 = setup[Item].buffer1
    val buf2 = setup[Item].buffer2
    val ops = buf1.itemsOps

    val n = a.size(buf1)
    val radix = 1 << radixBits
    val shifts = Stream
      .iterate(0)(_ + radixBits)
      .takeWhile(_ < a.itemBitsSize(buf1))
    for (shift <- shifts) {
      val length = Math.min(radixBits, a.itemBitsSize(buf1) - shift)
      val counts = Array.ofDim[Int](radix)
      for (i <- 0 until n) {
        val item = a.get(buf1, i)
        counts(a.getSlice(ops, item, shift, length)) += 1
      }
      val prefixSums = Array.ofDim[Int](radix)
      for (value <- 1 until radix) {
        prefixSums(value) = prefixSums(value - 1) + counts(value - 1)
      }
      for (i <- 0 until n) {
        val item = a.get(buf1, i)
        val keyPart = a.getSlice(ops, item, shift, length)
        a.set(buf2, prefixSums(keyPart), item)
        prefixSums(keyPart) += 1
      }
      for (i <- 0 until n) {
        a.set(buf1, i, a.get(buf2, i))
      }
    }
  }
}
