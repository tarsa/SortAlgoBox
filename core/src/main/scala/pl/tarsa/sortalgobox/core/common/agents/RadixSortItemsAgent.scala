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
package pl.tarsa.sortalgobox.core.common.agents

import pl.tarsa.sortalgobox.core.common.agents.ItemsAgent._

abstract class RadixSortItemsAgent[ItemType]
  extends SlicingItemsAgent[ItemType] {

  def storage1: Array[ItemType]
  def size1: Int = storage1.length
  def get1(i: Int): ItemType = storage1(i)
  def set1(i: Int, v: ItemType): Unit = storage1(i) = v

  def swap00(i: Int, j: Int): Unit = swap0(i, j)
  def swap01(i: Int, j: Int): Unit = swap(storage0, i, storage1, j)
  def swap10(i: Int, j: Int): Unit = swap(storage1, i, storage0, j)
  def swap11(i: Int, j: Int): Unit = swap(storage1, i, storage1, j)

  def copy00(i: Int, j: Int, n: Int): Unit = copy0(i, j, n)
  def copy01(i: Int, j: Int, n: Int): Unit = copy(storage0, i, storage1, j, n)
  def copy10(i: Int, j: Int, n: Int): Unit = copy(storage1, i, storage0, j, n)
  def copy11(i: Int, j: Int, n: Int): Unit = copy(storage1, i, storage1, j, n)
}
