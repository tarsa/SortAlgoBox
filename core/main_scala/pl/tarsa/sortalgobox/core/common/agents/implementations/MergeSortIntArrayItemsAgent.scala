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
package pl.tarsa.sortalgobox.core.common.agents.implementations

import pl.tarsa.sortalgobox.core.common.agents.MergeSortItemsAgent

class MergeSortIntArrayItemsAgent(items: Array[Int],
                                  buffer: Array[Int],
                                  base: Int = 0)
    extends MergeSortItemsAgent[Int] {
  override type SelfType = MergeSortIntArrayItemsAgent

  override def withBase(newIndexingBase: Int): SelfType = {
    if (base == newIndexingBase) this
    else new MergeSortIntArrayItemsAgent(items, buffer, newIndexingBase)
  }

  override def size0: Int =
    items.length

  override def get0(i: Int): Int =
    items(i - base)

  override def set0(i: Int, v: Int): Unit =
    items(i - base) = v

  override def copy0(i: Int, j: Int, n: Int): Unit =
    System.arraycopy(items, i - base, items, j - base, n)

  override def size1: Int =
    buffer.length

  override def get1(i: Int): Int =
    buffer(i - base)

  override def set1(i: Int, v: Int): Unit =
    buffer(i - base) = v

  override def compare(a: Int, b: Int): Int =
    Ordering.Int.compare(a, b)

  override def copy00(i: Int, j: Int, n: Int): Unit =
    System.arraycopy(items, i - base, items, j - base, n)

  override def copy01(i: Int, j: Int, n: Int): Unit =
    System.arraycopy(items, i - base, buffer, j - base, n)

  override def copy10(i: Int, j: Int, n: Int): Unit =
    System.arraycopy(buffer, i - base, items, j - base, n)

  override def copy11(i: Int, j: Int, n: Int): Unit =
    System.arraycopy(buffer, i - base, buffer, j - base, n)
}
