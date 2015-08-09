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
package pl.tarsa.sortalgobox.sorts.scala.radix

import pl.tarsa.sortalgobox.core.common._
import pl.tarsa.sortalgobox.core.common.agents.RadixSortStorageAgent

class RadixSort(radixBits: Int = 8)
  extends SortAlgorithm[RadixSortStorageAgent] {

  override def sort[ItemType](
    storageAgent: RadixSortStorageAgent[ItemType]): Unit = {
    import storageAgent._

    val n = size0
    val radix = 1 << radixBits
    val shiftsAndLengths = Stream.iterate(0)(_ + radixBits)
      .takeWhile(_ < keySizeInBits)
      .map(shift => (shift, Math.min(radixBits, keySizeInBits - shift)))
    for ((shift, length) <- shiftsAndLengths) {
      val counts = Array.ofDim[Int](radix)
      for (i <- 0 until size0) {
        val item = get0(i)
        counts(getItemSlice(item, shift, length)) += 1
      }
      val prefixSums = Array.ofDim[Int](radix)
      for (value <- 1 until radix) {
        prefixSums(value) = prefixSums(value - 1) + counts(value - 1)
      }
      for (i <- 0 until size0) {
        val item = get0(i)
        val keyPart = getItemSlice(item, shift, length)
        set1(prefixSums(keyPart), item)
        prefixSums(keyPart) += 1
      }
      copy10(0, 0, n)
    }
  }
}
