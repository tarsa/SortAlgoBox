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

abstract class ItemsAgent[ItemType] {
  import ItemsAgent._

  def storage0: Array[ItemType]
  def size0: Int = storage0.length
  def get0(i: Int): ItemType = storage0(i)
  def set0(i: Int, v: ItemType): Unit = storage0(i) = v

  def copy0(i: Int, j: Int, n: Int) = copy(storage0, i, storage0, j, n)

  def swap0(i: Int, j: Int): Unit =
    swap(storage0, i, storage0, j)
}

object ItemsAgent {
  def copy[ItemType](source: Array[ItemType], sourceStartIndex: Int,
    target: Array[ItemType], targetStartIndex: Int, items: Int): Unit = {
    System.arraycopy(source, sourceStartIndex, target, targetStartIndex, items)
  }

  def swap[ItemType](storageA: Array[ItemType], indexA: Int,
    storageB: Array[ItemType], indexB: Int): Unit = {
    val valueA = storageA(indexA)
    storageA(indexA) = storageB(indexB)
    storageB(indexB) = valueA
  }
}
